@echo off
REM Plik: setup-env.bat
REM Instrukcja użycia: call setup-env.bat

REM Ustawienie zmiennych środowiskowych
set MYSQL_ROOT_PASSWORD=root
set MYSQL_DATABASE=buildtask_db
set MYSQL_USER=buildtask_user
set MYSQL_PASSWORD=buildtask_password
set MYSQL_CONNECTION_STRING=jdbc:mariadb://127.0.0.1:3306
set SPRING_PROFILES_ACTIVE=development
set REPORTS_STORAGE_PATH=.\reports

REM Utworzenie katalogu raportów, jeśli nie istnieje
if not exist %REPORTS_STORAGE_PATH% mkdir %REPORTS_STORAGE_PATH%

REM Informacja o załadowaniu zmiennych
echo Zmienne środowiskowe zostały ustawione:
echo MYSQL_ROOT_PASSWORD=%MYSQL_ROOT_PASSWORD%
echo MYSQL_DATABASE=%MYSQL_DATABASE%
echo MYSQL_USER=%MYSQL_USER%
echo MYSQL_PASSWORD=%MYSQL_PASSWORD%
echo MYSQL_CONNECTION_STRING=%MYSQL_CONNECTION_STRING%
echo SPRING_PROFILES_ACTIVE=%SPRING_PROFILES_ACTIVE%
echo REPORTS_STORAGE_PATH=%REPORTS_STORAGE_PATH%
echo.
echo Katalog raportów: %REPORTS_STORAGE_PATH%

@REM cd /d %~dp0
java -jar backend-0.0.1-SNAPSHOT.jar