# SC3010-Computer-Security — CVE-2017-5638 Attack Recreation

This repository is part of the [SC3010 Computer Security course project](https://github.com/Shuhui95/SC3010-Case-Study).  
It recreates **CVE-2017-5638**, a critical Remote Code Execution vulnerability in Apache Struts 2 (versions 2.3.5–2.3.31 and 2.5–2.5.10), famously exploited in the **2017 Equifax data breach** to exfiltrate personal records of roughly 147 million people.

The vulnerability arises from a single design flaw: when Apache Struts fails to parse a `multipart/form-data` upload request, it builds an error message by embedding the raw `Content-Type` header — without sanitizing it — into a string that is then passed through the OGNL expression evaluator. An attacker who puts an OGNL expression inside the `Content-Type` header therefore gets it executed server-side as the Tomcat process user.

This repo contains:
- A working **vulnerable Java/Maven server** (Struts2 2.3.28) with a file-upload endpoint.
- A **PowerShell exploit script** that demonstrates the full injection → RCE chain.
- Annotated **reference source files** from Apache Struts 2.3.28 mapping each step of the call chain.

---

## Attack State Machine

The diagram below shows each decision branch in the Struts2 request pipeline. The red path is the one the attacker forces; grey `→ ...` branches are safe paths that don't apply during the exploit.

```mermaid
flowchart TD
    Start([Attacker sends HTTP POST /upload.action<br/>Content-Type: &#37;&#123;OGNL_PAYLOAD&#125;.multipart/form-data])
    Start --> F1

    F1[/"<b>STEP 1</b><br/>StrutsPrepareAndExecuteFilter.doFilter()"/]
    F1 --> D1{URL in excludedPatterns?}
    D1 -- "Yes → chain.doFilter() ..." --> OUT1([normal servlet chain ...])
    D1 -- No --> F2

    F2[/"<b>STEP 2</b><br/>PrepareOperations.wrapRequest()"/]
    F2 --> F3

    F3[/"<b>STEP 3</b><br/>Dispatcher.wrapRequest()<br/>reads request.getContentType()"/]
    F3 --> D2{"Content-Type contains<br/>&quot;multipart/form-data&quot;?"}
    D2 -- "No → StrutsRequestWrapper ..." --> OUT2([normal non-upload request ...])
    D2 -- "Yes (attacker's payload still<br/>contains the keyword)" --> F4

    F4[/"<b>STEP 4</b><br/>new MultiPartRequestWrapper(mpr, request, saveDir, ...)<br/>constructor calls multi.parse()"/]
    F4 --> F5

    F5[/"<b>STEP 5</b><br/>JakartaMultiPartRequest.parse()<br/>→ Commons FileUpload.parseRequest()"/]
    F5 --> D3{"FileUpload can parse<br/>Content-Type?"}
    D3 -- "Yes → normal file upload ..." --> OUT3([files extracted, action executes ...])
    D3 -- "No — Content-Type is malformed<br/>throws InvalidContentTypeException<br/>(message = raw Content-Type string)" --> CATCH

    CATCH["parse() catch block<br/>calls buildErrorMessage(e, &#123;&#125;)"]
    CATCH --> F6

    F6[/"<b>STEP 6</b><br/>JakartaMultiPartRequest.buildErrorMessage()<br/>→ LocalizedTextUtil.findText(class, errorKey, locale, e.getMessage(), args)"/]
    F6 --> D4{"Resource bundle has key<br/>struts.messages.upload.error.*?"}
    D4 -- "Yes → safe localised message ..." --> OUT4([error displayed safely ...])
    D4 -- "No — falls back to<br/>e.getMessage() as the default<br/>(attacker's raw Content-Type)" --> TRANSLATE

    TRANSLATE["TextParseUtil.translateVariables()<br/>scans string for &#37;&#123;...&#125; expressions"]
    TRANSLATE --> D5{"String contains<br/>&#37;&#123;...&#125; OGNL expression?"}
    D5 -- "No → plain error string ..." --> OUT5([safe error message ...])
    D5 -- "Yes — evaluates OGNL<br/>inside attacker content" --> OGNL

    OGNL[/"<b>STEP 7a — OGNL sandbox bypass</b><br/>#ognlUtil.getExcludedClasses().clear()<br/>#ognlUtil.getExcludedPackageNames().clear()<br/><i>OgnlUtil.java</i>"/]
    OGNL --> ACCESS

    ACCESS[/"<b>STEP 7b — Unrestricted reflection</b><br/>#context.setMemberAccess(DEFAULT_MEMBER_ACCESS)<br/><i>OgnlContext.java</i>"/]
    ACCESS --> RCE

    RCE[/"<b>STEP 8 — RCE</b><br/>Runtime.getRuntime().exec(cmd)"/]
    RCE --> Done([Command executed as Tomcat process user])

    style Start fill:#d32f2f,color:#fff
    style Done fill:#d32f2f,color:#fff
    style OUT1 fill:#9e9e9e,color:#fff
    style OUT2 fill:#9e9e9e,color:#fff
    style OUT3 fill:#9e9e9e,color:#fff
    style OUT4 fill:#9e9e9e,color:#fff
    style OUT5 fill:#9e9e9e,color:#fff
    style OGNL fill:#b71c1c,color:#fff
    style ACCESS fill:#b71c1c,color:#fff
    style RCE fill:#b71c1c,color:#fff
    style CATCH fill:#e65100,color:#fff
    style TRANSLATE fill:#e65100,color:#fff
```

For the full state-machine with branch explanations, see [diagrams/cve-2017-5638-state-machine.md](diagrams/cve-2017-5638-state-machine.md).  
For a sequence diagram view and annotated OGNL payload breakdown, see [diagrams/cve-2017-5638-attack-chain.md](diagrams/cve-2017-5638-attack-chain.md).

---

## Pre-knowledge
- [How does OGNL injection work?](_note/OGNL-injection-introduction.md)
  - Summary of my 3-hour inquiry with **Gemini** on OGNL injection.

---

## Repository Structure

```
SC3010-Computer-Security/
├── attack-recreate/
│   ├── backend/          # Vulnerable Apache Struts2 2.3.28 server (Java/Maven)
│   └── attack-script/    # Exploit script for CVE-2017-5638
│       └── exploit_cve_2017_5638.ps1   # PowerShell (cross-platform)
├── struts-src-code/          # Apache Struts2 reference source + legal notices
│   ├── licenses/             # LICENSE, NOTICE, and component licenses
│   └── src/
│       ├── struts2-core/     # Request pipeline classes + vulnerable JakartaMultiPartRequest
│       ├── xwork2/           # ActionContext.java, OgnlUtil.java
│       └── ognl/             # OgnlContext.java
├── diagrams/                 # State-machine + sequence diagrams, annotated OGNL payload
└── _notes/                   # Background reading
```

See [attack-recreate/attack-script/README.md](attack-recreate/attack-script/README.md) for setup and usage instructions.

---

## References
- Gemini: https://gemini.com/
- Struts2 repo on GitHub, branch 2.3.28: https://github.com/apache/struts/tree/STRUTS_2_3_28

---

## Third-Party Software Notices

This repository reproduces portions of **Apache Struts 2.3.28** source code for academic security research purposes.

> Apache Struts is Copyright © 2000–2016 The Apache Software Foundation.  
> Licensed under the **Apache License, Version 2.0**.  
> A copy of the license is available at [`struts-src-code/licenses/LICENSE.txt`](struts-src-code/licenses/LICENSE.txt).  
> The full attribution notice required by the Apache License is in [`struts-src-code/licenses/NOTICE.txt`](struts-src-code/licenses/NOTICE.txt).

Apache Struts 2 bundles additional third-party components, each governed by their own license:

| Component | License file |
|-----------|--------------|
| OGNL (Object-Graph Navigation Library) | [`struts-src-code/licenses/OGNL-LICENSE.txt`](struts-src-code/licenses/OGNL-LICENSE.txt) |
| XWork | [`struts-src-code/licenses/XWORK-LICENSE.txt`](struts-src-code/licenses/XWORK-LICENSE.txt) |
| FreeMarker | [`struts-src-code/licenses/FREEMARKER-LICENSE.txt`](struts-src-code/licenses/FREEMARKER-LICENSE.txt) |

Source references:
- [apache/struts @ STRUTS\_2\_3\_28](https://github.com/apache/struts/tree/STRUTS_2_3_28) — Struts2 core and XWork
- [jkuhnert/ognl](https://github.com/jkuhnert/ognl) — OGNL 3.0.x
