#!/usr/bin/env bash
# start.sh - run or verify the generative-ai-java-spring-labs application.
#
# Usage:
#   ./start.sh          Start the application with Docker Compose.
#   ./start.sh test      Run the full Maven verification (mvnw verify).
#   ./start.sh help      Show this help message and exit.
set -e

print_usage() {
  cat <<'EOF'
Usage: start.sh [help|test]

  (no argument)  Start the application: check prerequisites, build and
                 start the containers, wait until the app is ready, then
                 open the dashboard in your browser.
  test           Run the full verification suite ('./mvnw verify'),
                 including the Testcontainers PostgreSQL integration tests.
  help           Show this help message and exit.
EOF
}

DASHBOARD_URL="http://localhost:8080/dashboard"
HEALTH_URL="http://localhost:8080/actuator/health"
HEALTH_TIMEOUT_SECONDS=300
HEALTH_POLL_INTERVAL_SECONDS=3

# Always operate from the repository root, regardless of where the script
# was invoked from.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

check_repo_root() {
  if [ ! -f compose.yaml ] || [ ! -f mvnw ]; then
    echo "Error: this does not look like the repository root (compose.yaml and mvnw were not found here)." >&2
    echo "Run this script from the root of the generative-ai-java-spring-labs repository." >&2
    exit 1
  fi
}

# Checking 'docker info' rather than 'docker --version' matters: the CLI
# responds fine even when the Docker Desktop daemon is not running, so
# '--version' would report success right before 'docker compose' fails.
check_docker() {
  if ! docker info >/dev/null 2>&1; then
    echo "Error: Docker Desktop does not appear to be running." >&2
    echo "Start Docker Desktop, wait for it to finish starting, and run this script again." >&2
    echo "" >&2
    echo "For details, run 'docker info' yourself." >&2
    exit 1
  fi
}

ensure_env_file() {
  if [ -f .env ]; then
    return 0
  fi
  if [ ! -f .env.example ]; then
    echo "Error: .env.example not found; cannot create .env." >&2
    exit 1
  fi
  cp .env.example .env
  echo ".env not found. Created it by copying .env.example."
}

check_curl() {
  if ! command -v curl >/dev/null 2>&1; then
    echo "Error: 'curl' is required to detect when the application is ready, but it was not found." >&2
    exit 1
  fi
}

# The 'app' service has no Docker healthcheck (only 'postgres' does), so
# 'docker compose ps' can only tell us the container is running, which
# happens minutes before Spring finishes starting. '/actuator/health' is
# permitted without authentication and reflects the app's own readiness,
# so we poll it directly instead.
wait_for_health() {
  echo -n "Waiting for the application to start"
  local waited=0
  while [ "$waited" -lt "$HEALTH_TIMEOUT_SECONDS" ]; do
    if curl --fail --silent --output /dev/null "$HEALTH_URL"; then
      echo ""
      echo "The application is up."
      return 0
    fi
    echo -n "."
    sleep "$HEALTH_POLL_INTERVAL_SECONDS"
    waited=$((waited + HEALTH_POLL_INTERVAL_SECONDS))
  done
  echo ""
  echo "Error: timed out after ${HEALTH_TIMEOUT_SECONDS}s waiting for $HEALTH_URL to respond." >&2
  echo "The containers are still running; inspect what is happening with 'docker compose logs'." >&2
  return 1
}

# Best-effort browser launch: detected by platform rather than assumed, and
# never treated as fatal. Headless environments (CI, WSL, a container) just
# get the printed URL instead.
open_browser() {
  local url="$1"
  local opener=""
  case "$(uname -s 2>/dev/null)" in
    Darwin) opener="open" ;;
    Linux) opener="xdg-open" ;;
    MINGW*|MSYS*|CYGWIN*) opener="start" ;;
  esac
  if [ -n "$opener" ] && command -v "$opener" >/dev/null 2>&1 && "$opener" "$url" >/dev/null 2>&1; then
    return 0
  fi
  echo "Could not open a browser automatically. Open $url manually."
  return 1
}

run_app() {
  check_repo_root
  check_docker
  check_curl
  ensure_env_file

  echo ""
  echo "Starting the application. The first run can take several minutes"
  echo "while Docker images are built and dependencies are downloaded."
  echo ""
  echo "Sign in with the credentials from your local .env file"
  echo "(APP_AGENT_USERNAME / APP_AGENT_PASSWORD, or APP_ADMIN_USERNAME / APP_ADMIN_PASSWORD)."
  echo "The application keeps running in the background after this script exits."
  echo "Press Ctrl+C to stop following the logs; run 'docker compose down' when you are done."
  echo ""

  if ! docker compose up -d --build; then
    echo "" >&2
    echo "Error: 'docker compose up --build' failed; see the output above for the reason." >&2
    exit 1
  fi

  if ! wait_for_health; then
    exit 1
  fi

  echo "Opening $DASHBOARD_URL"
  open_browser "$DASHBOARD_URL" || true

  echo ""
  echo "Following logs (Ctrl+C stops watching, the application keeps running)..."
  docker compose logs -f
}

run_test() {
  check_repo_root
  check_docker
  ensure_env_file

  echo "Running ./mvnw verify (includes Testcontainers PostgreSQL integration tests)..."
  ./mvnw verify
}

case "${1:-}" in
  "")
    run_app
    ;;
  test)
    run_test
    ;;
  help)
    print_usage
    exit 0
    ;;
  *)
    echo "Unknown command: $1" >&2
    echo "" >&2
    print_usage >&2
    exit 1
    ;;
esac
