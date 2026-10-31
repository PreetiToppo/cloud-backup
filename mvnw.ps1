# Maven Wrapper for Windows
$SCRIPT_DIR = Split-Path $MyInvocation.MyCommand.Path
$MAVEN_HOME = Join-Path $SCRIPT_DIR ".mvn\apache-maven-3.9.9"
$MAVEN_CMD = Join-Path $MAVEN_HOME "bin\mvn.cmd"

& $MAVEN_CMD @args
