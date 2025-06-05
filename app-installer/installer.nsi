; BuildTask Installer Script - Embedded Files Version
; Requires NSIS 3.0 or later

!define APP_NAME "BuildTask"
!define APP_VERSION "1.0.0"
!define APP_PUBLISHER "INF_ŻÓŁCI"
!define MARIADB_SERVICE "MariaDB"
!define API_PORT "8080"
!define DB_PORT "3306"
!define MARIADB_VERSION "11.7"

; Include required plugins
!include "MUI2.nsh"
!include "LogicLib.nsh"
!include "FileFunc.nsh"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "..\frontend\src\assets\buildtask_logo.ico"
!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_TEXT "Uruchom BuildTask"
!define MUI_FINISHPAGE_RUN_FUNCTION "LaunchApplication"

; Pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "..\LICENSE"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

; Languages
!insertmacro MUI_LANGUAGE "Polish"

; Variable declarations
Var MariaDBStatus
Var ServiceName
Var MySQLExePath

; Installer settings
Name "${APP_NAME}"
OutFile "BuildTask-Installer.exe"
InstallDir "$PROGRAMFILES\${APP_NAME}"
InstallDirRegKey HKLM "Software\${APP_NAME}" "InstallDir"
RequestExecutionLevel admin

; Reserve files (for better compression)
ReserveFile "tools\init.sql"
ReserveFile "tools\mariadb-installer.msi"
ReserveFile "app\BuildTask-Setup.exe"

Section "Main Installation" SecMain
    ; Set output path
    SetOutPath $INSTDIR

    ; Extract embedded files to temp directory
    DetailPrint "Extracting installation files..."
    CreateDirectory "$TEMP\BuildTask-Install"

    ; Extract init.sql
    File "/oname=$TEMP\BuildTask-Install\init.sql" "tools\init.sql"

    ; Extract MariaDB installer
    File "/oname=$TEMP\BuildTask-Install\mariadb-installer.msi" "tools\mariadb-installer.msi"

    ; Extract main application installer
    File "/oname=$TEMP\BuildTask-Install\BuildTask-Setup.exe" "app\BuildTask-Setup.exe"

    DetailPrint "Files extracted successfully"

    ; Step 0: Check administrator privileges
    DetailPrint "=== STEP 0: Checking administrator privileges ==="
    Call CheckAdminRights
    DetailPrint "Administrator privileges confirmed"

    ; Step 0.5: Pre-installation checks
    DetailPrint "=== STEP 0.5: Pre-installation checks ==="
    Call PreInstallationChecks
    DetailPrint "Pre-installation checks completed"

    ; Step 1: Check if API port is available
    DetailPrint "=== STEP 1: Checking API port availability ==="
    Call CheckAPIPort
    Pop $R0
    ${If} $R0 == "1"
        MessageBox MB_ICONSTOP "ERROR: Port ${API_PORT} is busy!$\n$\nPort ${API_PORT} is already being used by another application.$\nTo continue with installation:$\n$\n1. Close all applications that may be using port ${API_PORT}$\n2. Check Task Manager for hidden processes$\n3. Restart your computer if the problem persists$\n4. Run the installer again$\n$\nIf the problem continues, contact your system administrator."
        Abort
    ${EndIf}
    DetailPrint "API port ${API_PORT} is available"

    ; Step 2: Check MariaDB installation and status
    DetailPrint "=== STEP 2: Checking MariaDB installation ==="
    Call CheckMariaDB
    DetailPrint "MariaDB check completed - status: $MariaDBStatus"

    ; Step 3: Install/Configure MariaDB if needed
    DetailPrint "=== STEP 3: Installing/Configuring MariaDB ==="
    Call InstallConfigureMariaDB
    DetailPrint "MariaDB installation/configuration completed"

    ; Step 4: Initialize database
    DetailPrint "=== STEP 4: Initializing database ==="
    Call InitializeDatabase
    DetailPrint "Database initialization completed"

    ; Step 5: Install main application
    DetailPrint "=== STEP 5: Installing main application ==="
    Call InstallMainApp
    DetailPrint "Main application installation completed"

    ; Cleanup temp files
    DetailPrint "Cleaning up temporary files..."
    Delete "$TEMP\BuildTask-Install\init.sql"
    Delete "$TEMP\BuildTask-Install\mariadb-installer.msi"
    Delete "$TEMP\BuildTask-Install\BuildTask-Setup.exe"
    RMDir "$TEMP\BuildTask-Install"

    ; Create uninstaller
    WriteUninstaller "$INSTDIR\Uninstall.exe"

    ; Registry entries
    WriteRegStr HKLM "Software\${APP_NAME}" "InstallDir" "$INSTDIR"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "DisplayName" "${APP_NAME}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "UninstallString" "$INSTDIR\Uninstall.exe"

SectionEnd

; Function to check administrator rights
Function CheckAdminRights
    UserInfo::GetAccountType
    Pop $R0
    ${If} $R0 != "Admin"
        MessageBox MB_ICONSTOP "ERROR: No administrator privileges!$\n$\nThe installer requires administrator privileges to:$\n• Install system services (MariaDB)$\n• Write to system folders$\n• Modify system registry$\n$\nSolution:$\n1. Right-click on the installer$\n2. Select 'Run as administrator'$\n3. Confirm in the UAC (User Account Control) window$\n$\nIf you still have problems, contact your computer administrator."
        Abort
    ${EndIf}
FunctionEnd

; Function for pre-installation checks
Function PreInstallationChecks
    ; Check if extracted files exist
    ${IfNot} ${FileExists} "$TEMP\BuildTask-Install\init.sql"
        MessageBox MB_ICONSTOP "ERROR: Database initialization file missing!$\n$\nFile not found: init.sql$\n$\nThis file is required to create the database.$\nThe installer may be corrupted.$\n$\nSolution:$\n1. Download the installer again$\n2. Check if the file wasn't blocked by antivirus$\n3. Contact the software provider"
        Abort
    ${EndIf}

    ${IfNot} ${FileExists} "$TEMP\BuildTask-Install\BuildTask-Setup.exe"
        MessageBox MB_ICONSTOP "ERROR: Main application installer missing!$\n$\nFile not found: BuildTask-Setup.exe$\n$\nThis file contains the main BuildTask application.$\nThe installer may be corrupted.$\n$\nSolution:$\n1. Check if you downloaded the complete installation package$\n2. Download the full installer again$\n3. Check if antivirus didn't block extraction"
        Abort
    ${EndIf}
FunctionEnd

; Function to check if API port is available
Function CheckAPIPort
    nsExec::ExecToStack 'cmd /c "netstat -an | findstr :${API_PORT}"'
    Pop $R0
    Pop $R1

    ${If} $R0 == 0
        StrLen $R2 $R1
        ${If} $R2 > 0
            Push "1"
        ${Else}
            Push "0"
        ${EndIf}
    ${Else}
        Push "0"
    ${EndIf}
FunctionEnd

; Function to check MariaDB installation
Function CheckMariaDB
    DetailPrint "Checking for MariaDB services..."

    SimpleSC::ExistsService "MariaDB"
    Pop $R0
    ${If} $R0 == 0
        StrCpy $ServiceName "MariaDB"
        Goto service_found
    ${EndIf}

    SimpleSC::ExistsService "MySQL"
    Pop $R0
    ${If} $R0 == 0
        StrCpy $ServiceName "MySQL"
        Goto service_found
    ${EndIf}

    DetailPrint "No MariaDB service found - will be installed automatically"
    StrCpy $MariaDBStatus "not_installed"
    Return

    service_found:
        DetailPrint "Found MariaDB service: $ServiceName"
        SimpleSC::ServiceIsRunning "$ServiceName"
        Pop $R0
        ${If} $R0 == 0
            DetailPrint "MariaDB service ($ServiceName) is already running"
            StrCpy $MariaDBStatus "running"
        ${Else}
            DetailPrint "MariaDB service ($ServiceName) exists but is stopped"
            StrCpy $MariaDBStatus "stopped"
        ${EndIf}
FunctionEnd

; Function to install/configure MariaDB
Function InstallConfigureMariaDB
    ${If} $MariaDBStatus == "not_installed"
        DetailPrint "MariaDB not installed - starting installation process"
        Call InstallMariaDB
    ${ElseIf} $MariaDBStatus == "stopped"
        DetailPrint "MariaDB installed but stopped - starting service"
        Call StartMariaDB
    ${Else}
        DetailPrint "MariaDB is already running - skipping start"
    ${EndIf}

    DetailPrint "Waiting for MariaDB service to be fully ready..."
    Sleep 5000

    DetailPrint "Performing final MariaDB port check..."
    Call CheckMariaDBPort
    DetailPrint "MariaDB configuration completed successfully"
FunctionEnd

; Function to install MariaDB
Function InstallMariaDB
    DetailPrint "Installing MariaDB..."

    ${IfNot} ${FileExists} "$TEMP\BuildTask-Install\mariadb-installer.msi"
        MessageBox MB_ICONSTOP "ERROR: MariaDB installer missing!$\n$\nThe MariaDB installer was not properly extracted.$\nThe main installer may be corrupted.$\n$\nSolution:$\n1. Download the complete BuildTask installer again$\n2. Check if antivirus didn't interfere with extraction$\n3. Try running from a different location"
        Abort
    ${EndIf}

    ; Check if port 3306 is free
    DetailPrint "Checking port ${DB_PORT} availability..."
    nsExec::ExecToStack 'cmd /c "netstat -an | findstr :${DB_PORT}"'
    Pop $R0
    Pop $R1
    ${If} $R0 == 0
        StrLen $R2 $R1
        ${If} $R2 > 0
            MessageBox MB_ICONSTOP "ERROR: Port ${DB_PORT} is already busy!$\n$\nPort ${DB_PORT} (standard MySQL/MariaDB port) is being used by another application.$\n$\nPossible causes:$\n• You already have MySQL or MariaDB installed$\n• Another application is using this port$\n• Previous installation wasn't properly removed$\n$\nSolution:$\n1. Check Windows 'Services' if MySQL/MariaDB isn't already installed$\n2. Stop the service if it exists$\n3. Uninstall previous version of MySQL/MariaDB$\n4. Restart your computer$\n5. Try again"
            Abort
        ${EndIf}
    ${EndIf}

    ; Run MariaDB installer silently
    DetailPrint "Running MariaDB installer - this may take several minutes..."
    ExecWait 'msiexec /i "$TEMP\BuildTask-Install\mariadb-installer.msi" /quiet /qn SERVICENAME="${MARIADB_SERVICE}" PASSWORD="root"' $R0

    ${If} $R0 != 0
        MessageBox MB_ICONSTOP "ERROR: MariaDB installation failed! (Code: $R0)$\n$\nPlease check Windows Event Viewer for detailed error information.$\n$\nCommon solutions:$\n1. Run installer as administrator$\n2. Check available disk space$\n3. Temporarily disable antivirus$\n4. Restart computer and try again"
        Abort
    ${EndIf}

    DetailPrint "MariaDB has been successfully installed"
    StrCpy $MariaDBStatus "stopped"
    Call StartMariaDB
FunctionEnd

; Function to start MariaDB
Function StartMariaDB
    DetailPrint "Starting MariaDB service..."

    StrCpy $ServiceName "MariaDB"
    Call TryStartService
    Pop $R0
    ${If} $R0 == 0
        DetailPrint "Successfully started service: $ServiceName"
        Goto service_started
    ${EndIf}

    StrCpy $ServiceName "MySQL"
    Call TryStartService
    Pop $R0
    ${If} $R0 == 0
        DetailPrint "Successfully started service: $ServiceName"
        Goto service_started
    ${EndIf}

    DetailPrint "Failed to start any MariaDB service"
    MessageBox MB_ICONSTOP "ERROR: Cannot start MariaDB service!$\n$\nTried service names: MariaDB, MySQL$\n$\nPlease:$\n1. Check Windows Services (services.msc)$\n2. Look for MariaDB or MySQL service$\n3. Start it manually$\n4. Continue installation"
    Abort

    service_started:
        DetailPrint "MariaDB service started successfully"
FunctionEnd

; Helper function to try starting a service
Function TryStartService
    DetailPrint "Trying to start service: $ServiceName"

    SimpleSC::ExistsService "$ServiceName"
    Pop $R0
    ${If} $R0 != 0
        DetailPrint "Service $ServiceName does not exist (code: $R0)"
        Push 1
        Return
    ${EndIf}

    SimpleSC::ServiceIsRunning "$ServiceName"
    Pop $R0
    ${If} $R0 == 0
        DetailPrint "Service $ServiceName is already running"
        Push 0
        Return
    ${EndIf}

    DetailPrint "Starting service $ServiceName..."
    SimpleSC::StartService "$ServiceName" ""
    Pop $R0

    ${If} $R0 == 0
        DetailPrint "Service $ServiceName started successfully"
        Sleep 2000
        Push 0
    ${Else}
        DetailPrint "Failed to start service $ServiceName (code: $R0)"
        Push $R0
    ${EndIf}
FunctionEnd

; Function to check MariaDB port
Function CheckMariaDBPort
    DetailPrint "Checking if MariaDB is listening on port ${DB_PORT}..."
    Sleep 3000

    nsExec::ExecToStack 'cmd /c "netstat -an | findstr :${DB_PORT}"'
    Pop $R0
    Pop $R1

    ${If} $R0 != 0
        DetailPrint "WARNING: MariaDB is not yet listening on port ${DB_PORT}"
        DetailPrint "Waiting additional 10 seconds for MariaDB to start..."
        Sleep 10000

        nsExec::ExecToStack 'cmd /c "netstat -an | findstr :${DB_PORT}"'
        Pop $R0
        Pop $R1

        ${If} $R0 != 0
            DetailPrint "MariaDB still not listening on port ${DB_PORT}"
            MessageBox MB_ICONEXCLAMATION "WARNING: MariaDB port check failed!$\n$\nMariaDB service appears to be running but is not listening on port ${DB_PORT}.$\n$\nContinue anyway?" IDYES port_continue
            Abort

            port_continue:
                DetailPrint "User chose to continue despite port issues"
        ${Else}
            DetailPrint "MariaDB is now listening on port ${DB_PORT} (after retry)"
        ${EndIf}
    ${Else}
        DetailPrint "MariaDB is listening correctly on port ${DB_PORT}"
    ${EndIf}
FunctionEnd

; Function to initialize database
Function InitializeDatabase
    ${IfNot} ${FileExists} "$TEMP\BuildTask-Install\init.sql"
        MessageBox MB_ICONSTOP "ERROR: Database initialization script missing!$\n$\nThe init.sql file was not properly extracted.$\nThe installer may be corrupted."
        Abort
    ${EndIf}

    DetailPrint "Waiting for MariaDB to be fully ready..."
    Sleep 5000

    DetailPrint "Locating MariaDB installation..."
    Call FindMariaDBPath
    Pop $MySQLExePath

    ${If} $MySQLExePath == "NOT_FOUND"
        DetailPrint "ERROR: Could not locate MariaDB installation"
        Abort
    ${EndIf}

    DetailPrint "Found MariaDB at: $MySQLExePath"
    DetailPrint "Creating database and user..."

    StrCpy $0 '"$MySQLExePath" -h localhost -P 3306 -u root -proot --execute="source $TEMP\BuildTask-Install\init.sql"'
    DetailPrint "Executing: $0"

    nsExec::ExecToLog '$0'
    Pop $R0

    ${If} $R0 == 0
        DetailPrint "Database initialization successful"
    ${Else}
        DetailPrint "Database initialization failed (code: $R0)"
        Abort
    ${EndIf}
FunctionEnd

; Function to find MariaDB installation path
Function FindMariaDBPath
    DetailPrint "Searching for MariaDB mysql.exe..."

    ${If} ${FileExists} "C:\Program Files\MariaDB ${MARIADB_VERSION}\bin\mysql.exe"
        Push "C:\Program Files\MariaDB ${MARIADB_VERSION}\bin\mysql.exe"
        Return
    ${EndIf}

    ${If} ${FileExists} "C:\Program Files (x86)\MariaDB ${MARIADB_VERSION}\bin\mysql.exe"
        Push "C:\Program Files (x86)\MariaDB ${MARIADB_VERSION}\bin\mysql.exe"
        Return
    ${EndIf}

    nsExec::ExecToStack 'where mysql 2>NUL'
    Pop $R0
    Pop $R1
    ${If} $R0 == 0
        Push "mysql"
        Return
    ${EndIf}

    Push "NOT_FOUND"
FunctionEnd

; Function to install main application
Function InstallMainApp
    ${IfNot} ${FileExists} "$TEMP\BuildTask-Install\BuildTask-Setup.exe"
        MessageBox MB_ICONSTOP "ERROR: Main application installer missing!$\n$\nThe BuildTask-Setup.exe was not properly extracted."
        Abort
    ${EndIf}

    DetailPrint "Installing BuildTask application - this may take several minutes..."
    ExecWait '"$TEMP\BuildTask-Install\BuildTask-Setup.exe" /S' $R0

    ${If} $R0 != 0
        MessageBox MB_ICONSTOP "ERROR: Main application installation failed!$\nError code: $R0$\n$\nPlease check Windows Event Viewer for details."
        Abort
    ${EndIf}

    DetailPrint "BuildTask application has been successfully installed"
FunctionEnd

; Function to launch application after installation
Function LaunchApplication
    ${If} ${FileExists} "$DESKTOP\BuildTask.lnk"
        ExecShell "" "$DESKTOP\BuildTask.lnk"
    ${ElseIf} ${FileExists} "$SMPROGRAMS\BuildTask\BuildTask.lnk"
        ExecShell "" "$SMPROGRAMS\BuildTask\BuildTask.lnk"
    ${ElseIf} ${FileExists} "$LOCALAPPDATA\Programs\BuildTask\BuildTask.exe"
        ExecShell "" "$LOCALAPPDATA\Programs\BuildTask\BuildTask.exe"
    ${ElseIf} ${FileExists} "$PROGRAMFILES\BuildTask\BuildTask.exe"
        ExecShell "" "$PROGRAMFILES\BuildTask\BuildTask.exe"
    ${Else}
        MessageBox MB_ICONINFORMATION "BuildTask has been installed successfully!$\n$\nYou can find the application in Start Menu or on Desktop."
    ${EndIf}
FunctionEnd

; Uninstaller section
Section "Uninstall"
    DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}"
    DeleteRegKey HKLM "Software\${APP_NAME}"
    Delete "$INSTDIR\Uninstall.exe"
    RMDir "$INSTDIR"

    MessageBox MB_ICONINFORMATION "${APP_NAME} application has been successfully uninstalled.$\n$\nMariaDB was not removed because it may be used by other applications."
SectionEnd