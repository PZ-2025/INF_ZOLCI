$ErrorActionPreference = 'Stop'
$requirements = @{
    "Java" = $false
    "Memory" = $false
    "AdminRights" = $false
}

# Sprawdz Jave
try {
    $javaVersion = & java -version 2>&1
    if ($javaVersion -match "(java|openjdk) version") {
        $requirements["Java"] = $true
    }
} catch { }

# Sprawdz pamiec
$computerSystem = Get-CimInstance CIM_ComputerSystem
$totalMemory = [Math]::Round($computerSystem.TotalPhysicalMemory / 1GB, 2)
if ($totalMemory -ge 4) {
    $requirements["Memory"] = $true
}

# Sprawdz uprawnienia administratora
$identity = [Security.Principal.WindowsIdentity]::GetCurrent()
$principal = New-Object Security.Principal.WindowsPrincipal $identity
$requirements["AdminRights"] = $principal.IsInRole([Security.Principal.WindowsBuiltinRole]::Administrator)

# Zwroc wynik
$missingRequirements = $requirements.Keys | Where-Object { $requirements[$_] -eq $false }
if ($missingRequirements.Count -gt 0) {
    foreach ($req in $missingRequirements) {
        switch ($req) {
            "Java" { Write-Host "Java nie zostala znaleziona w systemie." }
            "Memory" { Write-Host "Za malo pamieci RAM: $totalMemory GB (wymagane minimum 4 GB)." }
            "AdminRights" { Write-Host "Brak uprawnien administratora." }
        }
    }
    Write-Host "Brakujace wymagania: $($missingRequirements -join ', ')"
    exit 1
}
else {
    Write-Host "Wszystkie wymagania spelnione."
    exit 0
}