@echo off
REM start.bat - run or verify the generative-ai-java-spring-labs application.
REM
REM Usage:
REM   start.bat          Start the application with Docker Compose.
REM   start.bat test      Run the full Maven verification (mvnw.cmd verify).
REM   start.bat help      Show this help message and exit.

REM Always operate from the repository root, regardless of where the
REM script was invoked from (double-click, cmd.exe, or a shortcut).
cd /d "%~dp0"

set "DASHBOARD_URL=http://localhost:8080/dashboard"
set "HEALTH_URL=http://localhost:8080/actuator/health"
set /a HEALTH_TIMEOUT_SECONDS=300
set /a HEALTH_POLL_INTERVAL_SECONDS=3
set /a HEALTH_POLL_PING_COUNT=HEALTH_POLL_INTERVAL_SECONDS+1

set "COMMAND=%~1"

if "%COMMAND%"=="" goto :dispatch_run
if "%COMMAND%"=="test" goto :dispatch_test
if "%COMMAND%"=="help" (
  call :print_usage
  exit /b 0
)

echo Unknown command: %COMMAND% 1>&2
echo. 1>&2
call :print_usage 1>&2
exit /b 1

REM The no-argument and "test" branches above jump here with goto instead of
REM calling :run_app / :run_test from inside an if (...) block, because
REM %errorlevel% inside a parenthesised block is substituted once when the
REM block is parsed, before anything inside it runs. "exit /b %errorlevel%"
REM written directly inside such a block would therefore always report the
REM value from before the CALL, not the CALL's actual result -- the script
REM would print the right error text but always exit 0. Outside a block,
REM cmd.exe parses and executes one line at a time, so %errorlevel% on its
REM own line below correctly reflects the CALL that just ran.
:dispatch_run
call :run_app
exit /b %errorlevel%

:dispatch_test
call :run_test
exit /b %errorlevel%

REM run_app and run_test are called (not jumped to) so that this script
REM always exits from the single line above, rather than relying on each
REM branch to fall through past the other branch's code.
:run_app
call :check_repo_root
if errorlevel 1 exit /b 1
call :check_docker
if errorlevel 1 exit /b 1
call :check_curl
if errorlevel 1 exit /b 1
call :ensure_env_file
if errorlevel 1 exit /b 1

echo.
echo Starting the application. The first run can take several minutes
echo while Docker images are built and dependencies are downloaded.
echo.
echo Sign in with the credentials from your local .env file
echo (APP_AGENT_USERNAME / APP_AGENT_PASSWORD, or APP_ADMIN_USERNAME / APP_ADMIN_PASSWORD).
echo The application keeps running in the background after this script exits.
echo Press Ctrl+C to stop following the logs; run "docker compose down" when you are done.
echo.

REM Invoked as docker.exe (not the bare "docker") because Docker Desktop's
REM bin directory also ships an extension-less "docker" POSIX script for
REM Git Bash/WSL; leaving the extension off here is ambiguous for cmd.exe.
REM Started detached (-d) so this script can poll readiness below instead
REM of blocking on the build/app log stream.
docker.exe compose up -d --build
if errorlevel 1 (
  echo. 1>&2
  echo Error: "docker compose up --build" failed; see the output above for the reason. 1>&2
  exit /b 1
)

call :wait_for_health
if errorlevel 1 exit /b 1

echo Opening %DASHBOARD_URL%
call :open_browser "%DASHBOARD_URL%"

echo.
echo Following logs (Ctrl+C stops watching, the application keeps running)...
docker.exe compose logs -f
exit /b %errorlevel%

:run_test
call :check_repo_root
if errorlevel 1 exit /b 1
call :check_docker
if errorlevel 1 exit /b 1
call :ensure_env_file
if errorlevel 1 exit /b 1

echo Running mvnw.cmd verify (includes Testcontainers PostgreSQL integration tests)...
call .\mvnw.cmd verify
exit /b %errorlevel%

:print_usage
echo Usage: start.bat [help^|test]
echo.
echo   (no argument)  Start the application: check prerequisites, build and
echo                  start the containers, wait until the app is ready, then
echo                  open the dashboard in your browser.
echo   test           Run the full verification suite ('mvnw.cmd verify'),
echo                  including the Testcontainers PostgreSQL integration tests.
echo   help           Show this help message and exit.
goto :eof

:check_repo_root
if not exist "compose.yaml" goto :repo_root_error
if not exist "mvnw.cmd" goto :repo_root_error
exit /b 0
:repo_root_error
echo Error: this does not look like the repository root (compose.yaml and mvnw.cmd were not found here). 1>&2
echo Run this script from the root of the generative-ai-java-spring-labs repository. 1>&2
exit /b 1

REM Checking 'docker info' rather than 'docker --version' matters: the CLI
REM responds fine even when the Docker Desktop daemon is not running, so
REM '--version' would report success right before 'docker compose' fails.
REM Invoked as docker.exe for the same reason as the compose call below.
:check_docker
docker.exe info >nul 2>&1
if errorlevel 1 (
  echo Error: Docker Desktop does not appear to be running. 1>&2
  echo Start Docker Desktop, wait for it to finish starting, and run this script again. 1>&2
  echo. 1>&2
  echo For details, run 'docker info' yourself. 1>&2
  exit /b 1
)
exit /b 0

:ensure_env_file
if exist ".env" exit /b 0
if not exist ".env.example" (
  echo Error: .env.example not found; cannot create .env. 1>&2
  exit /b 1
)
copy /y ".env.example" ".env" >nul
echo .env not found. Created it by copying .env.example.
exit /b 0

:check_curl
curl.exe --version >nul 2>&1
if errorlevel 1 (
  echo Error: 'curl' is required to detect when the application is ready, but it was not found. 1>&2
  exit /b 1
)
exit /b 0

REM The 'app' service has no Docker healthcheck (only 'postgres' does), so
REM 'docker compose ps' can only tell us the container is running, which
REM happens minutes before Spring finishes starting. '/actuator/health' is
REM permitted without authentication and reflects the app's own readiness,
REM so we poll it directly instead.
:wait_for_health
set /a waited=0
<nul set /p "=Waiting for the application to start"
:wait_for_health_loop
curl.exe --fail --silent --output nul "%HEALTH_URL%" >nul 2>&1
if not errorlevel 1 goto :wait_for_health_ready
if %waited% GEQ %HEALTH_TIMEOUT_SECONDS% goto :wait_for_health_timeout
<nul set /p "=."
ping -n %HEALTH_POLL_PING_COUNT% 127.0.0.1 >nul
set /a waited+=HEALTH_POLL_INTERVAL_SECONDS
goto :wait_for_health_loop

:wait_for_health_ready
echo.
echo The application is up.
exit /b 0

:wait_for_health_timeout
echo.
echo Error: timed out after %HEALTH_TIMEOUT_SECONDS%s waiting for %HEALTH_URL% to respond. 1>&2
echo The containers are still running; inspect what is happening with "docker compose logs". 1>&2
exit /b 1

REM Best-effort browser launch. If it fails (headless box, no default
REM handler configured) that is not an error: the URL was already printed,
REM so the reader can open it by hand.
:open_browser
start "" "%~1" >nul 2>&1
if errorlevel 1 (
  echo Could not open a browser automatically. Open %~1 manually.
  exit /b 1
)
exit /b 0
