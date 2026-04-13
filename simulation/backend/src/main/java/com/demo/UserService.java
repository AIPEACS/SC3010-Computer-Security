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
 * File: src/main/java/com/demo/UserService.java
 * Purpose: Database layer — reads user credentials from data/users.yaml using SnakeYAML.
 *          Represents the sensitive data store (PII / credential database) that an attacker
 *          can access after gaining RCE through CVE-2017-5638.
 * Time in attack timeline: Data at rest pre-attack; becomes exfiltration target after
 *                           the attacker achieves remote code execution on May 13, 2017.
 */
package com.demo;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Reads user records from {@code data/users.yaml} on the classpath.
 *
 * Each user record has three fields:
 * <ul>
 *   <li>{@code id}       — UUID string</li>
 *   <li>{@code username} — display name</li>
 *   <li>{@code password} — plaintext password (intentionally insecure for demo)</li>
 * </ul>
 */
public class UserService {

    private static final String USERS_YAML = "/data/users.yaml";

    /**
     * Returns the list of user maps loaded from users.yaml.
     * Each map contains keys: id, username, password.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getUsers() {
        Yaml yaml = new Yaml();
        InputStream in = getClass().getResourceAsStream(USERS_YAML);
        if (in == null) {
            return Collections.emptyList();
        }
        Map<String, Object> root = yaml.load(in);
        Object users = root.get("users");
        if (users instanceof List) {
            return (List<Map<String, String>>) users;
        }
        return Collections.emptyList();
    }
}
