$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.10.7-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Set-Location "C:\Users\nrupa\simplicity"
& "C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14\bin\mvn.cmd" exec:java -pl simplicity-model-binder "-Dexec.mainClass=com.simplicity.model.binder.ModelBinderDemo" "-Dexec.classpathScope=compile"
