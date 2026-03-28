@REM ----------------------------------------------------------------------------
@REM Maven2 Start Up Batch script
@REM
@REM Required ENV vars:
@REM JAVA_HOME must be set
@REM
@REM Optional ENV vars
@REM M2_HOME - location of maven2's installed home dir
@REM MAVEN_BATCH_ECHO - set to 'on' to enable the echoing of the batch commands
@REM MAVEN_BATCH_PAUSE - set to 'on' to wait for a key stroke before ending
@REM MAVEN_OPTS - parameters passed to the Java VM

@echo off

@REM set %HOME% to equivalent of $HOME
if "%HOME%"=="" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

@REM Execute a user defined script before this script
if exist "%HOME%\mavenrc_pre.bat" call "%HOME%\mavenrc_pre.bat"

set ERROR_CODE=0

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%"=="" goto OkJHome

echo.
echo Error: JAVA_HOME not found in your environment. >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto init

echo.
echo Error: JAVA_HOME is set to an invalid directory. >&2
echo JAVA_HOME = "%JAVA_HOME%" >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.
goto error

@REM ==== END VALIDATION ====

:init

@REM Find the project base dir
set MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
IF NOT "%MAVEN_PROJECTBASEDIR%"=="" (
    set MAVEN_PROJECTBASEDIR=%~dp0
)

set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\mvnw"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

"%JAVA_HOME%\bin\java.exe" ^
    %MAVEN_OPTS% ^
    -classpath %WRAPPER_JAR% ^
    "-Dmaven.home=%MAVEN_PROJECTBASEDIR%" ^
    "-Dproject.home=%MAVEN_PROJECTBASEDIR%" ^
    %WRAPPER_LAUNCHER% %CMD_LINE_ARGS%

if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%

if not "%MAVEN_BATCH_ECHO%" == "on" (
    exit /b %ERROR_CODE%
)

if "%MAVEN_BATCH_PAUSE%" == "on" (
    pause
)

exit /b %ERROR_CODE%
