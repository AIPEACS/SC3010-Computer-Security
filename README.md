# SC3010-Computer-Security
- This is the second part of [SC3010 course project](https://github.com/Shuhui95/SC3010-Case-Study).
- Recreating vulnerability CVE-2017-5638, involving OGNL Expression Injection.

---

## Third-Party Software Notices

This repository reproduces portions of **Apache Struts 2.3.28** source code for academic security research purposes.

> Apache Struts is Copyright © 2000–2016 The Apache Software Foundation.  
> Licensed under the **Apache License, Version 2.0**.  
> A copy of the license is available at [`struts-src-code/LICENSE.txt`](struts-src-code/LICENSE.txt).  
> The full attribution notice required by the Apache License is in [`struts-src-code/NOTICE.txt`](struts-src-code/NOTICE.txt).

Apache Struts 2 bundles additional third-party components, each governed by their own license:

| Component | License file |
|-----------|--------------|
| OGNL (Object-Graph Navigation Library) | [`struts-src-code/OGNL-LICENSE.txt`](struts-src-code/OGNL-LICENSE.txt) |
| XWork | [`struts-src-code/XWORK-LICENSE.txt`](struts-src-code/XWORK-LICENSE.txt) |
| FreeMarker | [`struts-src-code/FREEMARKER-LICENSE.txt`](struts-src-code/FREEMARKER-LICENSE.txt) |

Source reference: [apache/struts @ STRUTS\_2\_3\_28](https://github.com/apache/struts/tree/STRUTS_2_3_28)

---
## Pre-knowledge
- [How does OGNL injection work?](_note/OGNL-injection-introduction.md)
  - Summary of my 3-hour inquiry with **Gemini** on OGNL injection.
---
## Structure

```
SC3010-Computer-Security/
├── simulation/
│   ├── backend/          # Vulnerable Apache Struts2 2.3.28 server (Java/Maven)
│   └── attack-script/    # Exploit script for CVE-2017-5638
│       └── exploit_cve_2017_5638.ps1   # PowerShell (cross-platform)
├── struts-src-code/     # License & notice files for bundled Apache Struts2 source
└── _notes/               # Background reading
```

See [simulation/README.md](simulation/README.md) for full setup and usage instructions.

---

## References
- Gemini: https://gemini.com/
- Struts2 repo on GitHub, branch 2.3.28: https://github.com/apache/struts/tree/STRUTS_2_3_28
