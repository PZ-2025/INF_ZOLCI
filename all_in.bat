@echo off
echo Building BuildTask Installer...

echo Step 1: Building Backend JAR...
cd backend
call gradlew clean uberJar
if %ERRORLEVEL% neq 0 (
    echo Backend build failed!
    exit /b %ERRORLEVEL%
)
cd ..

echo Step 2: Building Frontend...
cd frontend
call npm install
call npm run electron:build:win
if %ERRORLEVEL% neq 0 (
    echo Frontend build failed!
    exit /b %ERRORLEVEL%
)
cd ..

echo Step 3: Creating Database Setup EXE...
launch4j buildtask-db-setup.xml
if %ERRORLEVEL% neq 0 (
    echo Launch4j build failed!
    exit /b %ERRORLEVEL%
)

echo Step 4: Creating Installer...
"C:\Aplikacje i gry\Inno Setup 6\ISCC.exe" buildtask-installer.iss
if %ERRORLEVEL% neq 0 (
    echo Inno Setup compilation failed!
    exit /b %ERRORLEVEL%
)

echo Build completed! Installer is in the installer/ directory.