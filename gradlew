#!/bin/sh
#
# Standard Gradle 8.7 Wrapper Script for POSIX (Linux/macOS)
# Uses gradle/wrapper/gradle-wrapper.jar directly without requiring system Gradle.
#

# Resolve links: $0 may be a link
app_path=$0
while [ -h "$app_path" ]; do
    ls=$( ls -ld "$app_path" )
    link=$( expr "$ls" : '.*-> \(.*\)$' )
    if expr "$link" : '/.*' > /dev/null; then
        app_path=$link
    else
        app_path=$( dirname "$app_path" )"/$link"
    fi
done

APP_HOME=$( cd "${app_path%/*}" && pwd -P ) || exit
APP_NAME="Gradle"
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
    if [ ! -x "$JAVACMD" ] ; then
        echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
        exit 1
    fi
else
    JAVACMD=java
    command -v java >/dev/null 2>&1 || { echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2; exit 1; }
fi

if [ ! -f "$CLASSPATH" ]; then
    echo "ERROR: gradle/wrapper/gradle-wrapper.jar not found at $CLASSPATH." >&2
    echo "Run 'gradle wrapper --gradle-version 8.7' once on a machine with Gradle or run the GitHub Actions workflow." >&2
    exit 1
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
    "-Dorg.gradle.appname=$APP_NAME" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
