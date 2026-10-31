#!/bin/sh
# Maven Wrapper script

SCRIPT_PATH="$(cd "$(dirname "$0")" && pwd)"

"$SCRIPT_PATH/.mvn/apache-maven-3.9.9/bin/mvn" "$@"
