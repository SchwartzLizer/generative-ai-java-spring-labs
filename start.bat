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

set "COMMAND=%~1"

if "%COMMAND%"=="" (
  call :run_app
  exit /b %errorlevel%
)
if "%COMMAND%"=="test" (
  call :run_test
  exit /b %errorlevel%
)
if "%COMMAND%"=="help" (
  call :print_usage
  exit /b 0
)

echo Unknown command: %COMMAND% 1>&2
echo. 1>&2
call :print_usage 1>&2
exit /b 1

REM run_app and run_test are called (not jumped to) so that this script
REM always exits from the single line above, rather than relying on each
REM branch to fall through past the other branch's code.
:run_app
call :check_repo_root
if errorlevel 1 exit /b 1
call :check_docker
if errorlevel 1 exit /b 1
call :ensure_env_file
if errorlevel 1 exit /b 1

echo.
echo Once the application is ready, open http://localhost:8080/dashboard
echo and sign in with the credentials from your local .env file
echo (APP_AGENT_USERNAME / APP_AGENT_PASSWORD, or APP_ADMIN_USERNAME / APP_ADMIN_PASSWORD).
echo Press Ctrl+C to stop, then run "docker compose down" to remove the containers.
echo.

REM Invoked as docker.exe (not the bare "docker") because Docker Desktop's
REM bin directory also ships an extension-less "docker" POSIX script for
REM Git Bash/WSL; leaving the extension off here is ambiguous for cmd.exe.
docker.exe compose up --build
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
echo   (no argument)  Start the application: check prerequisites, then run
echo                  'docker compose up --build'.
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
set "DOCKER_INFO_LOG=%TEMP%\start-bat-docker-info.txt"
docker.exe info >"%DOCKER_INFO_LOG%" 2>&1
if errorlevel 1 (
  echo Error: Docker Desktop does not appear to be running. 1>&2
  echo Start Docker Desktop, wait for it to finish starting, and run this script again. 1>&2
  echo. 1>&2
  echo Underlying error from 'docker info': 1>&2
  type "%DOCKER_INFO_LOG%" 1>&2
  del "%DOCKER_INFO_LOG%" >nul 2>&1
  exit /b 1
)
del "%DOCKER_INFO_LOG%" >nul 2>&1
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
