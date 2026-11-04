@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET ___MVNW_CALLER_PREFERENCES_HASH=@(set MVNW_USERNAME=%USERNAME%&set MVNW_REPOURL=%MVNW_REPOURL%&call echo %MVNW_REPOURL%)

@SETLOCAL
@SET JAVA_HOME_DEFAULT=%JAVA_HOME%

@REM --- Download Maven if needed ---
@SET MAVEN_VERSION=3.9.6
@SET MAVEN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%-bin\apache-maven-%MAVEN_VERSION%
@IF EXIST "%MAVEN_DIR%\bin\mvn.cmd" GOTO RUN_MVN

@echo Downloading Apache Maven %MAVEN_VERSION%...
@powershell -Command "& { $url = 'https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip'; $dir = '%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%-bin'; New-Item -ItemType Directory -Force -Path $dir | Out-Null; Invoke-WebRequest -Uri $url -OutFile \"$dir\apache-maven-%MAVEN_VERSION%-bin.zip\"; Expand-Archive -Path \"$dir\apache-maven-%MAVEN_VERSION%-bin.zip\" -DestinationPath $dir -Force; Remove-Item \"$dir\apache-maven-%MAVEN_VERSION%-bin.zip\" }"

:RUN_MVN
@"%MAVEN_DIR%\bin\mvn.cmd" %*
@ENDLOCAL
