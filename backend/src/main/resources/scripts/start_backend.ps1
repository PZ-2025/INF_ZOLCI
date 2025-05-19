# Plik: setup-env.ps1
# Instrukcja użycia: .\setup-env.ps1

# Ustawienie zmiennych środowiskowych
$env:MYSQL_ROOT_PASSWORD = "root"
$env:MYSQL_DATABASE = "buildtask_db"
$env:MYSQL_USER = "buildtask_user"
$env:MYSQL_PASSWORD = "buildtask_password"
$env:MYSQL_CONNECTION_STRING = "jdbc:mariadb://127.0.0.1:3306"
$env:SPRING_PROFILES_ACTIVE = "development"
$env:REPORTS_STORAGE_PATH = ".\reports"

# Informacja o załadowaniu zmiennych
Write-Host "Zmienne środowiskowe zostały ustawione:"
Write-Host "MYSQL_ROOT_PASSWORD=$env:MYSQL_ROOT_PASSWORD"
Write-Host "MYSQL_DATABASE=$env:MYSQL_DATABASE"
Write-Host "MYSQL_USER=$env:MYSQL_USER"
Write-Host "MYSQL_PASSWORD=$env:MYSQL_PASSWORD"
Write-Host "MYSQL_CONNECTION_STRING=$env:MYSQL_CONNECTION_STRING"
Write-Host "SPRING_PROFILES_ACTIVE=$env:SPRING_PROFILES_ACTIVE"
Write-Host "REPORTS_STORAGE_PATH=$env:REPORTS_STORAGE_PATH"

# Uruchomienie aplikacji
Start-Process "java" "-jar backend-0.0.1-SNAPSHOT.jar" -NoNewWindow -Wait
