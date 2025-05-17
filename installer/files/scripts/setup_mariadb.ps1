param (
    [string]$DbName,
    [string]$DbUser,
    [string]$DbPassword,
    [string]$DbRootPassword,
    [string]$InstallDir,
    [string]$MariaDBPort = "3306"
)
$ErrorActionPreference = 'Stop'

try {
    $DbPasswordPlain = $DbPassword
    $DbRootPasswordPlain = $DbRootPassword

    $mariadbInstallerPath = "$InstallDir\mariadb\mariadb-11.4.5-winx64.msi"
    $phpMyAdminZipPath = "$InstallDir\phpMyAdmin\phpMyAdmin-5.2.2-all-languages.zip"
    $phpMyAdminDir = "$InstallDir\phpMyAdmin"

    # Instalacja MariaDB
    Write-Host "Instalowanie MariaDB..."
    $installArgs = "/i `"$mariadbInstallerPath`" /qn SERVICENAME=MariaDB PORT=$MariaDBPort PASSWORD=$DbRootPasswordPlain"
    Start-Process -FilePath "msiexec.exe" -ArgumentList $installArgs -Wait

    # Czekaj, az usluga MariaDB sie uruchomi
    Write-Host "Oczekiwanie na uruchomienie uslugi MariaDB..."
    $retryCount = 0
    $maxRetries = 10
    $serviceRunning = $false

    while (-not $serviceRunning -and $retryCount -lt $maxRetries) {
        $service = Get-Service -Name "MariaDB" -ErrorAction SilentlyContinue
        if ($service -and $service.Status -eq "Running") {
            $serviceRunning = $true
        } else {
            Start-Sleep -Seconds 5
            $retryCount++
        }
    }

    if (-not $serviceRunning) {
        throw "Nie udalo sie uruchomic uslugi MariaDB w okreslonym czasie."
    }

    # Konfiguracja bazy danych
    Write-Host "Konfigurowanie bazy danych..."
    # Sprawdz, czy sciezka istnieje, w przeciwnym razie uzyj wersji 11.4
    $mariaDBBinPath = "C:\Program Files\MariaDB 11.4\bin"
    if (-not (Test-Path $mariaDBBinPath)) {
        $mariaDBBinPath = "C:\Program Files\MariaDB 10.11\bin"
    }

    # Utworz plik SQL z komendami
    $sqlFile = "$env:TEMP\buildtask_setup.sql"
    @"
CREATE DATABASE IF NOT EXISTS $DbName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$DbUser'@'localhost' IDENTIFIED BY '$DbPasswordPlain';
GRANT ALL PRIVILEGES ON $DbName.* TO '$DbUser'@'localhost';
FLUSH PRIVILEGES;
"@ | Out-File -FilePath $sqlFile -Encoding ASCII

    # Wykonaj skrypt SQL (poprawione przekierowanie)
    $sqlContent = Get-Content -Path $sqlFile -Raw
    $sqlContent | & "$mariaDBBinPath\mysql.exe" -u root --password="$DbRootPasswordPlain"

    # Usun tymczasowy plik
    Remove-Item $sqlFile -Force

    # Rozpakuj i skonfiguruj phpMyAdmin
    Write-Host "Konfigurowanie phpMyAdmin..."
    Expand-Archive -Path $phpMyAdminZipPath -DestinationPath $phpMyAdminDir -Force

    # Utworz konfiguracje dla phpMyAdmin z lepszym generatorem blowfish_secret
    $randomBytes = New-Object byte[] 32
    [Security.Cryptography.RNGCryptoServiceProvider]::Create().GetBytes($randomBytes)
    $blowfishSecret = [Convert]::ToBase64String($randomBytes)

    $phpMyAdminConfigFile = "$phpMyAdminDir\config.inc.php"
    $phpMyAdminConfig = @"
<?php
`$cfg['blowfish_secret'] = '$blowfishSecret'; /* Bezpieczny klucz */
`$i = 0;
`$i++;
`$cfg['Servers'][`$i]['auth_type'] = 'cookie';
`$cfg['Servers'][`$i]['host'] = 'localhost';
`$cfg['Servers'][`$i]['port'] = '$MariaDBPort';
`$cfg['Servers'][`$i]['connect_type'] = 'tcp';
`$cfg['Servers'][`$i]['compress'] = false;
`$cfg['Servers'][`$i]['AllowNoPassword'] = false;
"@

    Set-Content -Path $phpMyAdminConfigFile -Value $phpMyAdminConfig

    # Utworz plik konfiguracyjny dla aplikacji
    $configTemplate = Get-Content -Path "$InstallDir\config\application-template.properties" -Raw
    $config = $configTemplate -replace '\$\{MYSQL_DATABASE\}', $DbName `
                             -replace '\$\{MYSQL_USER\}', $DbUser `
                             -replace '\$\{MYSQL_PASSWORD\}', $DbPasswordPlain `
                             -replace '\$\{MYSQL_PORT\}', $MariaDBPort

    # Zapisz konfiguracje
    Set-Content -Path "$InstallDir\config\application.properties" -Value $config

    # Wyczysz zmienne z haslami
    $DbPasswordPlain = $null
    $DbRootPasswordPlain = $null
    [System.GC]::Collect()

    Write-Host "Konfiguracja bazy danych zakonczona pomyslnie."
    exit 0
} catch {
    # Wyczysz zmienne z haslami w przypadku bledu
    if ($DbPasswordPlain) { $DbPasswordPlain = $null }
    if ($DbRootPasswordPlain) { $DbRootPasswordPlain = $null }
    [System.GC]::Collect()

    Write-Host "Blad: $_"
    exit 1
}