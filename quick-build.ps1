$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.10.7-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14\bin;$env:PATH"
$env:MAVEN_HOME = "C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14"

Write-Host "Building Simplicity Model Binder..." -ForegroundColor Green
$mvn = "$env:MAVEN_HOME\bin\mvn.cmd"
& $mvn clean compile -pl simplicity-core,simplicity-model-binder 2>&1
