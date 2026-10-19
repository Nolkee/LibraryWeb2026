#!/usr/bin/env bash
# Source this file: source scripts/env.sh
LIBRARY_PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
export CATALINA_HOME="${CATALINA_HOME:-$LIBRARY_PROJECT_DIR/.runtime/apache-tomcat-10.1.61}"
export CATALINA_BASE="$CATALINA_HOME"
export LIBRARY_DB_URL="${LIBRARY_DB_URL:-jdbc:mysql://127.0.0.1:3307/LibraryWeb2026?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai}"
export LIBRARY_DB_USER="${LIBRARY_DB_USER:-root}"
export LIBRARY_DB_PASSWORD="${LIBRARY_DB_PASSWORD:-}"
export PATH="$JAVA_HOME/bin:$CATALINA_HOME/bin:$PATH"
