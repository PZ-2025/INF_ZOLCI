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
        DetailPrint "Found MariaDB service: $ServiceName"
        Goto check_service_status
    ${EndIf}

    SimpleSC::ExistsService "MySQL"
    Pop $R0
    ${If} $R0 == 0
        StrCpy $ServiceName "MySQL"
        DetailPrint "Found MySQL service: $ServiceName"
        Goto check_service_status
    ${EndIf}

    DetailPrint "No MariaDB service found - will be installed automatically"
    StrCpy $MariaDBStatus "not_installed"
    Return

    check_service_status:
        DetailPrint "Checking service status for: $ServiceName"
        SimpleSC::ServiceIsRunning "$ServiceName"
        Pop $R0  ; error code (0 = success, <>0 = error)
        Pop $R1  ; service status (1 = running, 0 = not running)
        DetailPrint "ServiceIsRunning returned - Error code: $R0, Status: $R1"

        ${If} $R0 != 0
            DetailPrint "Error checking service status for $ServiceName (code: $R0)"
            StrCpy $MariaDBStatus "unknown"
        ${ElseIf} $R1 == 1
            DetailPrint "MariaDB service ($ServiceName) is running"
            StrCpy $MariaDBStatus "running"
        ${Else}
            DetailPrint "MariaDB service ($ServiceName) is stopped"
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
    ${ElseIf} $MariaDBStatus == "unknown"
        DetailPrint "MariaDB status unknown - attempting to start"
        Call StartMariaDB
    ${Else}
        DetailPrint "MariaDB is already running - skipping start"
    ${EndIf}

    DetailPrint "Waiting for MariaDB service to be fully ready..."
    Call WaitForMariaDBPort
    Pop $R0
    ${If} $R0 != 0
        MessageBox MB_ICONSTOP "ERROR: MariaDB service was started, but the server did not begin listening on port ${DB_PORT} within 30 seconds.$\n$\nPossible reasons:$\n• Configuration issue$\n• Port conflict$\n• Slow system startup$\n\nPlease check MariaDB logs or try to start the service manually."
        Abort
    ${EndIf}
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

    ; Check if service exists
    SimpleSC::ExistsService "$ServiceName"
    Pop $R0
    ${If} $R0 != 0
        DetailPrint "Service $ServiceName does not exist (code: $R0)"
        Push 1
        Return
    ${EndIf}

    ; Check if service is already running
    SimpleSC::ServiceIsRunning "$ServiceName"
    Pop $R0  ; error code
    Pop $R1  ; service status (1 = running, 0 = not running)

    ${If} $R0 != 0
        DetailPrint "Error checking service status for $ServiceName (code: $R0)"
        ; Continue with start attempt anyway
    ${ElseIf} $R1 == 1
        DetailPrint "Service $ServiceName is already running"
        Push 0
        Return
    ${Else}
        DetailPrint "Service $ServiceName is stopped, will start it"
    ${EndIf}

    ; Start the service
    DetailPrint "Starting service $ServiceName..."
    SimpleSC::StartService "$ServiceName" "" 30
    Pop $R0

    ${If} $R0 == 0
        DetailPrint "Service $ServiceName started successfully"
        Sleep 2000

        ; Verify service is actually running
        SimpleSC::ServiceIsRunning "$ServiceName"
        Pop $R2  ; error code
        Pop $R3  ; service status

        ${If} $R2 == 0
        ${AndIf} $R3 == 1
            DetailPrint "Service $ServiceName verified as running"
            Push 0
        ${Else}
            DetailPrint "Service $ServiceName started but verification failed (error: $R2, status: $R3)"
            Push 1
        ${EndIf}
    ${Else}
        DetailPrint "Failed to start service $ServiceName (code: $R0)"
        Push $R0
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

    ; Method 1: Try using stdin redirection with cmd /c (most reliable)
    DetailPrint "Attempting database initialization using stdin redirection..."
    nsExec::ExecToLog 'cmd /c ""$MySQLExePath" -h localhost -P 3306 -u root -proot < "$TEMP\BuildTask-Install\init.sql""'
    Pop $R0

    ${If} $R0 == 0
        DetailPrint "Database initialization successful (stdin method)"
        Goto db_success
    ${EndIf}

    ; Method 2: Try executing SQL commands directly (fallback)
    DetailPrint "Stdin method failed (code: $R0), trying direct SQL execution..."

    ; Execute each SQL command separately
    DetailPrint "Creating database..."
    nsExec::ExecToLog '"$MySQLExePath" -h localhost -P 3306 -u root -proot --execute="CREATE DATABASE IF NOT EXISTS buildtask_db;"'
    Pop $R0

    DetailPrint "Creating user..."
    nsExec::ExecToLog '"$MySQLExePath" -h localhost -P 3306 -u root -proot --execute="CREATE USER IF NOT EXISTS $\'buildtask_user$\'@$\'localhost$\' IDENTIFIED BY $\'buildtask_password$\';"'
    Pop $R0

    DetailPrint "Granting privileges..."
    nsExec::ExecToLog '"$MySQLExePath" -h localhost -P 3306 -u root -proot --execute="GRANT ALL PRIVILEGES ON buildtask_db.* TO $\'buildtask_user$\'@$\'localhost$\';"'
    Pop $R0

    DetailPrint "Flushing privileges..."
    nsExec::ExecToLog '"$MySQLExePath" -h localhost -P 3306 -u root -proot --execute="FLUSH PRIVILEGES;"'
    Pop $R0

    ${If} $R0 == 0
        DetailPrint "Database initialization successful (direct SQL method)"
        Goto db_success
    ${EndIf}

    ; Method 3: Try copying init.sql to a path without spaces (last resort)
    DetailPrint "Direct SQL failed (code: $R0), trying simpler path..."

    ; Copy to C:\init.sql temporarily
    CopyFiles "$TEMP\BuildTask-Install\init.sql" "C:\init.sql"

    nsExec::ExecToLog 'cmd /c ""$MySQLExePath" -h localhost -P 3306 -u root -proot < C:\init.sql"'
    Pop $R0

    ; Clean up
    Delete "C:\init.sql"

    ${If} $R0 == 0
        DetailPrint "Database initialization successful (simple path method)"
        Goto db_success
    ${EndIf}

    ; If all methods failed
    DetailPrint "All database initialization methods failed"
    MessageBox MB_ICONSTOP "ERROR: Database initialization failed!$\n$\nAll attempted methods failed:$\n1. stdin redirection$\n2. Direct SQL execution$\n3. Simple path method$\n$\nLast error code: $R0$\n$\nPlease:$\n1. Check if MariaDB is running properly$\n2. Verify root password is 'root'$\n3. Try running the installer as administrator$\n4. Check Windows Event Viewer for details"
    Abort

    db_success:
        ; Verify database was created
        DetailPrint "Verifying database creation..."
        nsExec::ExecToStack '"$MySQLExePath" -h localhost -P 3306 -u root -proot --execute="SHOW DATABASES LIKE $\'buildtask_db$\';"'
        Pop $R0
        Pop $R1
        ${If} $R0 == 0
            StrLen $R2 $R1
            ${If} $R2 > 10  ; Should contain "buildtask_db"
                DetailPrint "Database verification successful - buildtask_db exists"
            ${Else}
                DetailPrint "Warning: Database verification unclear - output: $R1"
            ${EndIf}
        ${Else}
            DetailPrint "Warning: Could not verify database creation (code: $R0)"
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

; Function to wait for MariaDB port
Function WaitForMariaDBPort
    DetailPrint "Waiting for MariaDB to listen on port ${DB_PORT}..."
    ; Max 30 seconds (30 attempts every 1s)
    StrCpy $0 0

    loop_start:
        IntCmp $0 30 loop_timeout

        nsExec::ExecToStack 'cmd /c "netstat -an | findstr :${DB_PORT}"'
        Pop $1
        Pop $2

        ${If} $1 == 0
            StrLen $3 $2
            ${If} $3 > 0
                DetailPrint "MariaDB is listening on port ${DB_PORT}"
                Push 0
                Return
            ${EndIf}
        ${EndIf}

        Sleep 1000
        IntOp $0 $0 + 1
        DetailPrint "Waiting for port ${DB_PORT}... attempt $0/30"
        Goto loop_start

    loop_timeout:
        DetailPrint "Timeout while waiting for MariaDB to listen on port ${DB_PORT}"
        Push 1
FunctionEnd

; Uninstaller section
Section "Uninstall"
    DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}"
    DeleteRegKey HKLM "Software\${APP_NAME}"
    Delete "$INSTDIR\Uninstall.exe"
    RMDir "$INSTDIR"

    MessageBox MB_ICONINFORMATION "${APP_NAME} application has been successfully uninstalled.$\n$\nMariaDB was not removed because it may be used by other applications."
SectionEnd
