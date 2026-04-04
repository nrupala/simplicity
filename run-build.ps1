$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptRoot
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.10.7-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14\bin;$env:PATH"
$env:MAVEN_HOME = "C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14"

if (-not (Test-Path "$scriptRoot\pom.xml")) {
    Write-Host "ERROR: pom.xml not found in $scriptRoot" -ForegroundColor Red
    exit 1
}

$missingModules = @('simplicity-core', 'simplicity-rag', 'simplicity-knowledge-graph', 'simplicity-model-registry', 'simplicity-intelligence', 'simplicity-sovereignty', 'simplicity-model-binder', 'simplicity-api') | Where-Object { -not (Test-Path "$scriptRoot\$_") }
if ($missingModules) {
    Write-Host "ERROR: Maven child modules are missing from this checkout:" -ForegroundColor Red
    $missingModules | ForEach-Object { Write-Host " - $_" -ForegroundColor Yellow }
    Write-Host "Please verify that the repository includes the full set of module directories before building." -ForegroundColor Red
    exit 1
}

Write-Host "Building Simplicity..." -ForegroundColor Green
$mvn = "$env:MAVEN_HOME\bin\mvn.cmd"
& $mvn clean compile -pl simplicity-model-binder -am 2>&1 | Tee-Object -FilePath "$scriptRoot\build-output.txt"
