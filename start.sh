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

  (no argument)  Start the application: check prerequisites, then run
                 'docker compose up --build'.
  test           Run the full verification suite ('./mvnw verify'),
                 including the Testcontainers PostgreSQL integration tests.
  help           Show this help message and exit.
EOF
}

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

run_app() {
  check_repo_root
  check_docker
  ensure_env_file

  echo ""
  echo "Once the application is ready, open http://localhost:8080/dashboard"
  echo "and sign in with the credentials from your local .env file"
  echo "(APP_AGENT_USERNAME / APP_AGENT_PASSWORD, or APP_ADMIN_USERNAME / APP_ADMIN_PASSWORD)."
  echo "Press Ctrl+C to stop, then run 'docker compose down' to remove the containers."
  echo ""

  docker compose up --build
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
