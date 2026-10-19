#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/env.sh"
cd "$LIBRARY_PROJECT_DIR"
mkdir -p .runtime
# Old protobuf/abseil versions retained by Homebrew are needed by this installed MySQL.
LIBRARY_MYSQL_LIBS='/opt/homebrew/Cellar/protobuf/33.4_1/lib:/opt/homebrew/Cellar/abseil/20260107.1/lib'
if ! mysqladmin --protocol=TCP -h127.0.0.1 -P3307 -uroot ping >/dev/null 2>&1; then
  if [[ ! -d .runtime/mysql/mysql ]]; then
    env DYLD_LIBRARY_PATH="$LIBRARY_MYSQL_LIBS" /opt/homebrew/opt/mysql/bin/mysqld --no-defaults --initialize-insecure --basedir=/opt/homebrew/opt/mysql --datadir="$PWD/.runtime/mysql"
  fi
  env DYLD_LIBRARY_PATH="$LIBRARY_MYSQL_LIBS" /opt/homebrew/opt/mysql/bin/mysqld --no-defaults --basedir=/opt/homebrew/opt/mysql --datadir="$PWD/.runtime/mysql" --port=3307 --bind-address=127.0.0.1 --mysqlx=0 --socket="$PWD/.runtime/mysql.sock" --pid-file="$PWD/.runtime/mysql.pid" --log-error="$PWD/.runtime/mysql.log" --daemonize
fi
if [[ ! -f "$CATALINA_HOME/bin/catalina.sh" ]]; then
  curl -fL https://repo.maven.apache.org/maven2/org/apache/tomcat/tomcat/10.1.61/tomcat-10.1.61.tar.gz -o .runtime/tomcat.tar.gz
  tar -xzf .runtime/tomcat.tar.gz -C .runtime
fi
mvn -q package
cp target/LibraryWeb2026.war "$CATALINA_HOME/webapps/"
if ! curl -fsS http://127.0.0.1:8080/LibraryWeb2026/ >/dev/null; then
  python3 - <<'PYLAUNCH'
import os,subprocess,pathlib
root=pathlib.Path(os.environ['CATALINA_HOME'])
with open(root/'logs/catalina.out','ab') as output:
    subprocess.Popen([str(root/'bin/catalina.sh'),'run'],stdin=subprocess.DEVNULL,stdout=output,stderr=output,start_new_session=True)
PYLAUNCH
fi
printf '\nLocal: http://localhost:8080/LibraryWeb2026/\n'
if command -v ipconfig >/dev/null; then
  LIBRARY_LAN_IP="$(ipconfig getifaddr en0 || true)"
  printf 'LAN:   http://%s:8080/LibraryWeb2026/\n' "$LIBRARY_LAN_IP"
fi
