@echo off
setlocal
set "MVN_VERSION=3.9.11"
set "MVN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MVN_VERSION%"
if not exist "%MVN_DIR%\bin\mvn.cmd" (
  echo Downloading Apache Maven %MVN_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; $dir=Join-Path $env:USERPROFILE '.m2\wrapper\dists'; New-Item -ItemType Directory -Force -Path $dir ^| Out-Null; $zip=Join-Path $dir 'apache-maven-%MVN_VERSION%-bin.zip'; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/%MVN_VERSION%/binaries/apache-maven-%MVN_VERSION%-bin.zip' -OutFile $zip; Expand-Archive -LiteralPath $zip -DestinationPath $dir -Force; Remove-Item -LiteralPath $zip -Force"
  if errorlevel 1 exit /b 1
)
call "%MVN_DIR%\bin\mvn.cmd" %*
