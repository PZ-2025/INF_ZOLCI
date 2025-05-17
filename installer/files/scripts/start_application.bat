@echo off
echo Uruchamianie aplikacji BuildTask...
cd %~dp0\..
start "" "%~dp0\..\backend\buildtask_backend.exe"
start "" "%~dp0\..\frontend\BuildTask.exe"