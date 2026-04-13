/*
 * MIT License
 *
 * Copyright (c) 2026 Allen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/*
 * VULNERABLE CODE
 * File: src/main/java/com/demo/UploadAction.java
 * Purpose: Struts2 Action class that handles file uploads at /upload.action.
 *          This is the VULNERABLE SURFACE for CVE-2017-5638. The vulnerability is NOT
 *          in this Action class itself — it is in Struts2's multipart request parser
 *          (JakartaMultiPartRequest), which evaluates OGNL expressions embedded in the
 *          Content-Type header before this action's execute() is ever called.
 * Time in attack timeline: Active from server startup through May 13, 2017 and beyond —
 *                           any multipart POST to /upload.action with a malicious
 *                           Content-Type header triggers the vulnerability.
 */
package com.demo;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Struts2 file-upload action.
 *
 * The Struts2 multipart parser reads the Content-Type header BEFORE dispatching to
 * this action. When CVE-2017-5638 is exploited, OGNL in that header is evaluated by
 * the parser and arbitrary code executes — this execute() method is never reached.
 */
@Namespace("/")
@Action(value = "/upload", results = {
    @Result(name = "success", location = "/success.jsp"),
    @Result(name = "input",   location = "/upload.jsp")
})
public class UploadAction extends ActionSupport {

    // --- Struts2 file-upload convention fields ---
    private File   upload;
    private String uploadFileName;
    private String uploadContentType;

    @Override
    public String execute() throws Exception {
        if (upload != null) {
            // Save uploaded file to system temp directory
            File dest = new File(System.getProperty("java.io.tmpdir"), uploadFileName);
            copyFile(upload, dest);
        }
        return SUCCESS;
    }

    // --- Getters and setters required by Struts2 file-upload interceptor ---

    public File getUpload() { return upload; }
    public void setUpload(File upload) { this.upload = upload; }

    public String getUploadFileName() { return uploadFileName; }
    public void setUploadFileName(String uploadFileName) { this.uploadFileName = uploadFileName; }

    public String getUploadContentType() { return uploadContentType; }
    public void setUploadContentType(String uploadContentType) { this.uploadContentType = uploadContentType; }

    // Simple file copy (Java 7+ NIO)
    private void copyFile(File src, File dest) throws IOException {
        Files.copy(src.toPath(), dest.toPath(),
                   java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
}
