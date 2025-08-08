@echo off
REM ==============================================
REM SimpleCommerce MDM Backend - Windows Script  
REM ==============================================

setlocal enabledelayedexpansion

REM Colors for Windows
set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"  
set "BLUE=[94m"
set "NC=[0m"

if "%1"=="" goto :help

REM Load .env file if exists
if exist .env (
    echo %BLUE%📄 Loading environment from .env file...%NC%
    for /f "tokens=1,2 delims==" %%a in (.env) do (
        if not "%%b"=="" set "%%a=%%b"
    )
)

goto :%1 2>nul || goto :help

:help
echo %BLUE%SimpleCommerce MDM Backend - Windows Commands%NC%
echo ==================================================
echo.
echo %GREEN%Development Commands:%NC%
echo   services-only    Start only DB + Redis + pgAdmin (recommended for local dev)
echo   full             Start full stack including API container  
echo   stop             Stop all services
echo   clean            Stop and remove all data
echo   logs             Show container logs
echo   status           Show container status
echo   fix-env          Fix .env file line endings for Windows
echo.
echo %GREEN%Examples:%NC%
echo   run-windows.bat services-only   ^(Start DB services, run API in IntelliJ^)
echo   run-windows.bat full           ^(Start everything in Docker^)
echo   run-windows.bat fix-env        ^(Fix line ending issues^)
echo.
goto :end

:services-only
echo %YELLOW%🚀 Starting services for local development...%NC%
echo %BLUE%This will start: PostgreSQL + Redis + pgAdmin%NC%
echo %BLUE%You can then run the API in IntelliJ IDEA%NC%
echo.

REM Create data directories if they don't exist
if not exist "data\postgres" mkdir data\postgres
if not exist "data\redis" mkdir data\redis  
if not exist "data\pgadmin" mkdir data\pgadmin
if not exist "data\logs" mkdir data\logs

docker-compose -f docker-compose.windows.yml up -d db redis pgadmin

echo.
echo %GREEN%✅ Services started!%NC%
echo %BLUE%📋 Connection Info:%NC%
echo   PostgreSQL: localhost:5432 
echo   Redis: localhost:6379
echo   pgAdmin: http://localhost:5050
echo.
echo %YELLOW%🔥 Now run your Spring Boot app in IntelliJ:%NC%
echo   Profile: dev
echo   Environment variables will be loaded from .env
echo.
goto :end

:full
echo %YELLOW%🚀 Starting full stack with Docker...%NC%

REM Create data directories
if not exist "data\postgres" mkdir data\postgres
if not exist "data\redis" mkdir data\redis
if not exist "data\pgadmin" mkdir data\pgadmin  
if not exist "data\logs" mkdir data\logs

docker-compose -f docker-compose.windows.yml --profile with-api up --build -d

echo.
echo %GREEN%✅ Full stack started!%NC%
echo %BLUE%📋 Services:%NC%
echo   API: http://localhost:8080
echo   Health: http://localhost:8080/api/v1/health
echo   pgAdmin: http://localhost:5050
echo.
goto :end

:stop
echo %YELLOW%🛑 Stopping all services...%NC%
docker-compose -f docker-compose.windows.yml down
echo %GREEN%✅ All services stopped!%NC%
goto :end

:clean
echo %RED%🧹 Cleaning up (this will remove all data)...%NC%
set /p confirm="Are you sure? This will delete all database data! (y/N): "
if /i "%confirm%"=="y" (
    docker-compose -f docker-compose.windows.yml down -v
    docker system prune -f
    echo %GREEN%✅ Cleanup completed!%NC%
) else (
    echo %YELLOW%❌ Cleanup cancelled.%NC%
)
goto :end

:logs
echo %BLUE%📋 Container logs:%NC%
docker-compose -f docker-compose.windows.yml logs -f
goto :end

:status
echo %BLUE%📊 Container status:%NC%
docker-compose -f docker-compose.windows.yml ps
goto :end

:fix-env
echo %YELLOW%🔧 Fixing .env file line endings for Windows...%NC%
if exist .env (
    powershell -Command "(Get-Content .env -Raw) -replace '`r`n','`n' -replace '`r','`n' | Set-Content .env -NoNewline"
    echo %GREEN%✅ Fixed .env file line endings!%NC%
) else (
    echo %RED%❌ .env file not found. Please copy from env.example first.%NC%
    echo %BLUE%Run: copy env.example .env%NC%
)
goto :end

:end
endlocal 