@echo off
echo Building BuildTask Installer...
cd ..
echo Step 1: Building Backend JAR...
cd backend
call gradlew clean bootJar -x test
if %ERRORLEVEL% neq 0 (
    echo Backend build failed!
    exit /b %ERRORLEVEL%
)
echo Copying .env to build dir
copy .env build\libs\.env


cd ..

echo Step 2: Building Frontend...
cd frontend
call npm install
call npm run electron:build:win
if %ERRORLEVEL% neq 0 (
    echo Frontend build failed!
    exit /b %ERRORLEVEL%
)
echo Builds completed!