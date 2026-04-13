Push-Location $PSScriptRoot
try {
    mvn tomcat7:run
} finally {
    Pop-Location
}
Read-Host "Enter to exit"