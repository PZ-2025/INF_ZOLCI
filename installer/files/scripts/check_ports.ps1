$ErrorActionPreference = 'Stop'
$portsToCheck = @(3306, 8081, 8080)
$portsInUse = @()

foreach ($port in $portsToCheck) {
    $portInUse = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($portInUse) {
        $portsInUse += $port
    }
}

if ($portsInUse.Count -gt 0) {
    Write-Host "Nastepujace porty sa juz uzywane: $($portsInUse -join ', ')"
    exit 1
} else {
    Write-Host "Wszystkie wymagane porty sa dostepne."
    exit 0
}

if ($portInUse) {
    $process = Get-Process -Id (Get-NetTCPConnection -LocalPort $port).OwningProcess -ErrorAction SilentlyContinue
    $portsInUse += "$port (uzywany przez: $($process.Name))"
}