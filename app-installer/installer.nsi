; BuildTask Installer Script
; Requires NSIS 3.0 or later

!define APP_NAME "BuildTask"
!define APP_VERSION "1.0.0"
!define APP_PUBLISHER "INF_ŻÓŁCI"
!define MARIADB_SERVICE "MariaDB"
!define API_PORT "8080"
!define DB_PORT "3306"

; MariaDB Version Configuration - UPDATE THIS FOR DIFFERENT MARIADB VERSIONS
; Examples: "11.7", "11.0", "10.11", "10.6"
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

Section "Main Installation" SecMain
    ; Set output path
    SetOutPath $INSTDIR

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
    ; Check if all required files exist
    ${IfNot} ${FileExists} "$EXEDIR\tools\init.sql"
        MessageBox MB_ICONSTOP "ERROR: Database initialization file missing!$\n$\nFile not found: tools\init.sql$\n$\nThis file is required to create the database.$\nCheck if the installer was downloaded correctly.$\n$\nIf the problem persists:$\n1. Download the installer again$\n2. Check if the file wasn't blocked by antivirus$\n3. Contact the software provider"
        Abort
    ${EndIf}

    ${IfNot} ${FileExists} "$EXEDIR\app\BuildTask-Setup.exe"
        MessageBox MB_ICONSTOP "ERROR: Main application installer missing!$\n$\nFile not found: app\BuildTask-Setup.exe$\n$\nThis file contains the main BuildTask application.$\n$\nSolution:$\n1. Check if you downloaded the complete installation package$\n2. Check if antivirus didn't delete the file$\n3. Download the full installer again$\n4. Temporarily disable antivirus and try again"
        Abort
    ${EndIf}

    ; Note: Disk space checking removed due to compatibility issues
    ; Windows and MSI installers will handle insufficient disk space automatically
FunctionEnd

; Function to check if API port is available
Function CheckAPIPort
    ; Use netstat to check if port is in use
    nsExec::ExecToStack 'cmd /c "netstat -an | findstr :${API_PORT}"'
    Pop $R0  ; Exit code
    Pop $R1  ; Output

    ${If} $R0 == 0  ; If netstat found something
        StrLen $R2 $R1
        ${If} $R2 > 0  ; If output is not empty
            Push "1"  ; Port is in use
        ${Else}
            Push "0"  ; Port is free
        ${EndIf}
    ${Else}
        Push "0"  ; Port is free
    ${EndIf}
FunctionEnd

; Function to check MariaDB installation
Function CheckMariaDB
    DetailPrint "Checking for MariaDB services..."

    ; Try different possible service names
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

    SimpleSC::ExistsService "MariaDB 10.11"
    Pop $R0
    ${If} $R0 == 0
        StrCpy $ServiceName "MariaDB 10.11"
        Goto service_found
    ${EndIf}

    SimpleSC::ExistsService "MariaDB 11.0"
    Pop $R0
    ${If} $R0 == 0
        StrCpy $ServiceName "MariaDB 11.0"
        Goto service_found
    ${EndIf}

    ; No MariaDB service found
    DetailPrint "No MariaDB service found - will be installed automatically"
    StrCpy $MariaDBStatus "not_installed"
    Return

    service_found:
        DetailPrint "Found MariaDB service: $ServiceName"

        ; Check if service is running
        SimpleSC::ServiceIsRunning "$ServiceName"
        Pop $R0
        ${If} $R0 == 0  ; Service is running
            DetailPrint "MariaDB service ($ServiceName) is already running"
            StrCpy $MariaDBStatus "running"
        ${Else}
            DetailPrint "MariaDB service ($ServiceName) exists but is stopped"
            StrCpy $MariaDBStatus "stopped"
        ${EndIf}
FunctionEnd

; Function to install/configure MariaDB
Function InstallConfigureMariaDB
    DetailPrint "Configuring MariaDB based on status: $MariaDBStatus"

    ${If} $MariaDBStatus == "not_installed"
        DetailPrint "MariaDB not installed - starting installation process"
        Call InstallMariaDB
    ${ElseIf} $MariaDBStatus == "stopped"
        DetailPrint "MariaDB installed but stopped - starting service"
        Call StartMariaDB
    ${Else}
        DetailPrint "MariaDB is already running - skipping start"
    ${EndIf}

    ; Final check if MariaDB is running
    DetailPrint "Waiting for MariaDB service to be fully ready..."
    Sleep 5000  ; Wait 5 seconds for service to fully start

    DetailPrint "Performing final MariaDB port check..."
    Call CheckMariaDBPort
    DetailPrint "MariaDB configuration completed successfully"
FunctionEnd

; Function to install MariaDB
Function InstallMariaDB
    DetailPrint "Installing MariaDB..."

    ; Check if installer exists
    ${IfNot} ${FileExists} "$EXEDIR\tools\mariadb-installer.msi"
        MessageBox MB_ICONSTOP "ERROR: MariaDB installer missing!$\n$\nFile not found: tools\mariadb-installer.msi$\n$\nMariaDB is a required database for BuildTask application.$\n$\nSolution:$\n1. Check if you downloaded the complete installation package$\n2. Check if antivirus didn't delete the file$\n3. Download the full installer again$\n$\nIf you already have MariaDB installed:$\n1. Start MariaDB service manually$\n2. Run the installer again"
        Abort
    ${EndIf}

    ; Check if port 3306 is free before installing
    DetailPrint "Checking port ${DB_PORT} availability..."
    nsExec::ExecToStack 'cmd /c "netstat -an | findstr :${DB_PORT}"'
    Pop $R0
    Pop $R1
    ${If} $R0 == 0  ; Port is in use
        StrLen $R2 $R1
        ${If} $R2 > 0
            MessageBox MB_ICONSTOP "ERROR: Port ${DB_PORT} is already busy!$\n$\nPort ${DB_PORT} (standard MySQL/MariaDB port) is being used by another application.$\n$\nPossible causes:$\n• You already have MySQL or MariaDB installed$\n• Another application is using this port$\n• Previous installation wasn't properly removed$\n$\nSolution:$\n1. Check Windows 'Services' if MySQL/MariaDB isn't already installed$\n2. Stop the service if it exists$\n3. Uninstall previous version of MySQL/MariaDB$\n4. Restart your computer$\n5. Try again$\n$\nFor advanced users:$\nYou can change the port in configuration or remove the conflicting application."
            Abort
        ${EndIf}
    ${EndIf}

    ; Run MariaDB installer silently
    DetailPrint "Running MariaDB installer - this may take several minutes..."
    ExecWait 'msiexec /i "$EXEDIR\tools\mariadb-installer.msi" /quiet /qn SERVICENAME="${MARIADB_SERVICE}" PASSWORD="root"' $R0

    ${If} $R0 != 0
        ${If} $R0 == 1603
            MessageBox MB_ICONSTOP "ERROR: MariaDB installation failed! (Code: 1603)$\n$\nThis is the most common MSI installation error.$\n$\nPossible causes:$\n• No administrator privileges$\n• Conflict with another MySQL/MariaDB version$\n• Corrupted installer$\n• Windows Installer error$\n$\nSolution:$\n1. Run installer as administrator$\n2. Check Windows Event Viewer for error details$\n3. Uninstall all MySQL/MariaDB versions$\n4. Run Windows Installer cleanup$\n5. Restart computer and try again$\n$\nAlternatively: install MariaDB manually from official website"
        ${ElseIf} $R0 == 1618
            MessageBox MB_ICONSTOP "ERROR: Another installation in progress! (Code: 1618)$\n$\nIf another installation is running in the system:$\n1. Wait for the other installation to complete$\n2. Run the installer again$\n$\nIf no installation is visible:$\n1. Open Task Manager$\n2. End 'msiexec.exe' processes$\n3. Restart your computer$\n4. Try again"
        ${ElseIf} $R0 == 1619
            MessageBox MB_ICONSTOP "ERROR: Installation package cannot be opened! (Code: 1619)$\n$\nThe mariadb-installer.msi file may be corrupted.$\n$\nSolution:$\n1. Download BuildTask installer again$\n2. Check if antivirus didn't damage the file$\n3. Try running from a different location$\n4. Check if you have enough disk space"
        ${Else}
            MessageBox MB_ICONSTOP "ERROR: MariaDB installation failed! (Code: $R0)$\n$\nAn unexpected error occurred during installation.$\n$\nSolution:$\n1. Check Windows Event Viewer (Windows Logs > Application)$\n2. Look for errors related to 'MsiInstaller'$\n3. Check if you have enough disk space$\n4. Restart your computer$\n5. Try installing MariaDB manually$\n$\nIf the problem persists, contact technical support.$\nInclude error code: $R0"
        ${EndIf}
        Abort
    ${EndIf}

    DetailPrint "MariaDB has been successfully installed"
    StrCpy $MariaDBStatus "stopped"
    Call StartMariaDB
FunctionEnd

; Function to start MariaDB
Function StartMariaDB
    DetailPrint "Starting MariaDB service..."

    ; First, let's check what MariaDB services actually exist
    DetailPrint "Checking available MariaDB services..."
    nsExec::ExecToStack 'sc query type= service | findstr /i maria'
    Pop $R1
    Pop $R2
    ${If} $R1 == 0
        DetailPrint "Found MariaDB services: $R2"
    ${Else}
        DetailPrint "No MariaDB services found via sc query"
    ${EndIf}

    ; Try different possible service names
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

    StrCpy $ServiceName "MariaDB 10.11"
    Call TryStartService
    Pop $R0
    ${If} $R0 == 0
        DetailPrint "Successfully started service: $ServiceName"
        Goto service_started
    ${EndIf}

    StrCpy $ServiceName "MariaDB 11.0"
    Call TryStartService
    Pop $R0
    ${If} $R0 == 0
        DetailPrint "Successfully started service: $ServiceName"
        Goto service_started
    ${EndIf}

    ; If none worked, show error
    DetailPrint "Failed to start any MariaDB service"
    MessageBox MB_ICONSTOP "ERROR: Cannot start MariaDB service!$\n$\nTried service names: MariaDB, MySQL, MariaDB 10.11, MariaDB 11.0$\n$\nPlease:$\n1. Check Windows Services (services.msc)$\n2. Look for MariaDB or MySQL service$\n3. Start it manually$\n4. Continue installation$\n$\nIf no MariaDB service exists, the installation may have failed."
    Abort

    service_started:
        DetailPrint "MariaDB service started successfully"
FunctionEnd

; Helper function to try starting a service
Function TryStartService
    DetailPrint "Trying to start service: $ServiceName"

    ; First check if service exists
    SimpleSC::ExistsService "$ServiceName"
    Pop $R0
    ${If} $R0 != 0
        DetailPrint "Service $ServiceName does not exist (code: $R0)"
        Push 1  ; Return failure
        Return
    ${EndIf}

    DetailPrint "Service $ServiceName exists, checking if running..."

    ; Check if already running
    SimpleSC::ServiceIsRunning "$ServiceName"
    Pop $R0
    ${If} $R0 == 0
        DetailPrint "Service $ServiceName is already running"
        Push 0  ; Return success
        Return
    ${EndIf}

    DetailPrint "Starting service $ServiceName..."
    SimpleSC::StartService "$ServiceName" ""
    Pop $R0

    ${If} $R0 == 0
        DetailPrint "Service $ServiceName started successfully"
        ; Wait a bit for service to fully start
        Sleep 2000
        Push 0  ; Return success
    ${Else}
        DetailPrint "Failed to start service $ServiceName (code: $R0)"
        Push $R0  ; Return error code
    ${EndIf}
FunctionEnd

; Function to check MariaDB port
Function CheckMariaDBPort
    ; Check if MariaDB port is accessible
    DetailPrint "Checking if MariaDB is listening on port ${DB_PORT}..."

    ; Give MariaDB some time to start listening
    Sleep 3000

    nsExec::ExecToStack 'cmd /c "netstat -an | findstr :${DB_PORT}"'
    Pop $R0
    Pop $R1

    ${If} $R0 != 0
        DetailPrint "WARNING: MariaDB is not yet listening on port ${DB_PORT}"
        DetailPrint "Waiting additional 10 seconds for MariaDB to start..."
        Sleep 10000

        ; Try again
        nsExec::ExecToStack 'cmd /c "netstat -an | findstr :${DB_PORT}"'
        Pop $R0
        Pop $R1

        ${If} $R0 != 0
            DetailPrint "MariaDB still not listening on port ${DB_PORT}"
            MessageBox MB_ICONEXCLAMATION "WARNING: MariaDB port check failed!$\n$\nMariaDB service appears to be running but is not listening on port ${DB_PORT}.$\n$\nThis may cause database initialization to fail.$\n$\nPossible causes:$\n• MariaDB is still starting up$\n• MariaDB is configured to use a different port$\n• Firewall is blocking the port$\n• MariaDB configuration error$\n$\nThe installation will continue, but you may need to configure the database manually.$\n$\nContinue anyway?" IDYES port_continue
            DetailPrint "User chose to abort due to port issues"
            Abort

            port_continue:
                DetailPrint "User chose to continue despite port issues"
        ${Else}
            DetailPrint "MariaDB is now listening on port ${DB_PORT} (after retry)"
        ${EndIf}
    ${Else}
        DetailPrint "MariaDB is listening correctly on port ${DB_PORT}"
    ${EndIf}

    ; Additional check: try to connect to MariaDB if port is listening
    ${If} $R0 == 0
        DetailPrint "Testing MariaDB connection..."
        nsExec::ExecToStack 'mysql -h localhost -P ${DB_PORT} -u root -proot -e "SELECT 1;" 2>NUL'
        Pop $R0
        ${If} $R0 != 0
            ; Try with mariadb client
            nsExec::ExecToStack 'mariadb -h localhost -P ${DB_PORT} -u root -proot -e "SELECT 1;" 2>NUL'
            Pop $R0
            ${If} $R0 != 0
                DetailPrint "Warning: Could not establish test connection to MariaDB"
                DetailPrint "This may indicate authentication or configuration issues"
            ${Else}
                DetailPrint "MariaDB connection test successful (mariadb client)"
            ${EndIf}
        ${Else}
            DetailPrint "MariaDB connection test successful (mysql client)"
        ${EndIf}
    ${EndIf}
FunctionEnd

; Function to initialize database
Function InitializeDatabase
    ; Check if init.sql exists
    ${IfNot} ${FileExists} "$EXEDIR\tools\init.sql"
        MessageBox MB_ICONSTOP "ERROR: Database initialization script missing!$\n$\nFile not found: tools\init.sql$\n$\nThis file is needed to create the application database.$\n$\nSolution:$\n1. Check if the installer was downloaded correctly$\n2. Download the complete installation package again$\n3. Check if antivirus didn't delete the file$\n$\nIf the problem persists, contact the software provider"
        Abort
    ${EndIf}

    ; Copy init.sql to temp directory
    CreateDirectory "$TEMP\BuildTask"
    CopyFiles "$EXEDIR\tools\init.sql" "$TEMP\BuildTask\init.sql"

    ; Wait for MariaDB to be fully ready (important!)
    DetailPrint "Waiting for MariaDB to be fully ready..."
    Sleep 5000  ; Wait 5 seconds for MariaDB to fully start

    ; Find MariaDB installation path
    DetailPrint "Locating MariaDB installation..."
    Call FindMariaDBPath
    Pop $MySQLExePath

    ${If} $MySQLExePath == "NOT_FOUND"
        DetailPrint "ERROR: Could not locate MariaDB installation"
        Abort
    ${EndIf}

    DetailPrint "Found MariaDB at: $MySQLExePath"

    ; Execute SQL script with full path
    DetailPrint "Creating database and user using: $MySQLExePath"

    ; Method 1: Try with init.sql file
    DetailPrint "Executing init.sql file..."
    StrCpy $0 '"$MySQLExePath" -h localhost -P 3306 -u root -proot --execute="source $TEMP\BuildTask\init.sql"'

    ; Pokaż pełną komendę w logu (np. dla debugowania)
    DetailPrint "Final command: $0"

    ; Teraz wykonaj
    nsExec::ExecToLog '$0'
    Pop $R0

    ${If} $R0 == 0
        DetailPrint "Database initialization successful ('$TEMP\BuildTask\init.sql')"
        Goto db_success
    ${EndIf}

    DetailPrint "Failed to initilize database with init.sql: (code: $R0), ..."
    Abort

    db_success:
        ; Verify database was created
        DetailPrint "Verifying database creation..."
        nsExec::ExecToStack '"$MySQLExePath" -h localhost -P 3306 -u root -proot -e "SHOW DATABASES LIKE 'buildtask_db';"' $R0
        ${If} $R0 == 0
            DetailPrint "Database verification successful - buildtask_db exists"
        ${Else}
            DetailPrint "Warning: Could not verify database creation"
        ${EndIf}
FunctionEnd

; Function to find MariaDB installation path
Function FindMariaDBPath
    DetailPrint "Searching for MariaDB mysql.exe..."

    ; Method 1: Try configured version first
    DetailPrint "Trying configured MariaDB version ${MARIADB_VERSION}..."
    ${If} ${FileExists} "C:\Program Files\MariaDB ${MARIADB_VERSION}\bin\mysql.exe"
        DetailPrint "Found configured version: C:\Program Files\MariaDB ${MARIADB_VERSION}\bin\mysql.exe"
        Push "C:\Program Files\MariaDB ${MARIADB_VERSION}\bin\mysql.exe"
        Return
    ${EndIf}

    ${If} ${FileExists} "C:\Program Files (x86)\MariaDB ${MARIADB_VERSION}\bin\mysql.exe"
        DetailPrint "Found configured version (x86): C:\Program Files (x86)\MariaDB ${MARIADB_VERSION}\bin\mysql.exe"
        Push "C:\Program Files (x86)\MariaDB ${MARIADB_VERSION}\bin\mysql.exe"
        Return
    ${EndIf}

    ; Method 2: Dynamic search in Program Files
    DetailPrint "Dynamically searching for MariaDB installations in Program Files..."
    nsExec::ExecToStack 'cmd /c "dir /B /AD "C:\Program Files\MariaDB*" 2>NUL"'
    Pop $R0
    Pop $R1
    ${If} $R0 == 0
        DetailPrint "Found MariaDB directories: $R1"
        Call ParseMariaDBDirectories
        Pop $R2
        ${If} $R2 != "NOT_FOUND"
            DetailPrint "Found via dynamic search: $R2"
            Push "$R2"
            Return
        ${EndIf}
    ${EndIf}

    ; Method 3: Dynamic search in Program Files (x86)
    DetailPrint "Dynamically searching for MariaDB installations in Program Files (x86)..."
    nsExec::ExecToStack 'cmd /c "dir /B /AD "C:\Program Files (x86)\MariaDB*" 2>NUL"'
    Pop $R0
    Pop $R1
    ${If} $R0 == 0
        DetailPrint "Found MariaDB directories (x86): $R1"
        StrCpy $9 "C:\Program Files (x86)"
        Call ParseMariaDBDirectories
        Pop $R2
        ${If} $R2 != "NOT_FOUND"
            DetailPrint "Found via dynamic search (x86): $R2"
            Push "$R2"
            Return
        ${EndIf}
    ${EndIf}

    ; Method 4: Try alternative executable names
    DetailPrint "Searching for mariadb.exe as alternative..."
    ${If} ${FileExists} "C:\Program Files\MariaDB ${MARIADB_VERSION}\bin\mariadb.exe"
        DetailPrint "Found mariadb.exe: C:\Program Files\MariaDB ${MARIADB_VERSION}\bin\mariadb.exe"
        Push "C:\Program Files\MariaDB ${MARIADB_VERSION}\bin\mariadb.exe"
        Return
    ${EndIf}

    ; Method 5: Try to find any MariaDB installation with registry
    DetailPrint "Searching Windows registry for MariaDB installation..."
    ReadRegStr $R0 HKLM "SOFTWARE\MariaDB Corporation AB\MariaDB" "INSTALLDIR"
    ${If} $R0 != ""
        DetailPrint "Found MariaDB in registry: $R0"
        ${If} ${FileExists} "$R0\bin\mysql.exe"
            DetailPrint "Found mysql.exe via registry: $R0\bin\mysql.exe"
            Push "$R0\bin\mysql.exe"
            Return
        ${EndIf}
        ${If} ${FileExists} "$R0\bin\mariadb.exe"
            DetailPrint "Found mariadb.exe via registry: $R0\bin\mariadb.exe"
            Push "$R0\bin\mariadb.exe"
            Return
        ${EndIf}
    ${EndIf}

    ; Method 6: Last resort - check PATH
    DetailPrint "Checking if mysql or mariadb is available in PATH..."
    nsExec::ExecToStack 'where mysql 2>NUL'
    Pop $R0
    Pop $R1
    ${If} $R0 == 0
        DetailPrint "Found mysql in PATH: $R1"
        Push "mysql"
        Return
    ${EndIf}

    nsExec::ExecToStack 'where mariadb 2>NUL'
    Pop $R0
    Pop $R1
    ${If} $R0 == 0
        DetailPrint "Found mariadb in PATH: $R1"
        Push "mariadb"
        Return
    ${EndIf}

    DetailPrint "ERROR: No MariaDB mysql.exe found anywhere!"
    DetailPrint "Searched locations:"
    DetailPrint "- C:\Program Files\MariaDB ${MARIADB_VERSION}\bin\"
    DetailPrint "- C:\Program Files (x86)\MariaDB ${MARIADB_VERSION}\bin\"
    DetailPrint "- All MariaDB* directories in Program Files"
    DetailPrint "- Windows Registry"
    DetailPrint "- System PATH"
    Push "NOT_FOUND"
FunctionEnd

; Helper function to parse MariaDB directories and find mysql.exe
Function ParseMariaDBDirectories
    ; Input: $R1 contains directory listing, $9 contains base path (if set)
    ; Output: Push path to mysql.exe or "NOT_FOUND"

    ${If} $9 == ""
        StrCpy $9 "C:\Program Files"
    ${EndIf}

    ; Simple approach: try to find mysql.exe using dir command
    DetailPrint "Searching for mysql.exe in all MariaDB directories under $9..."
    nsExec::ExecToStack 'cmd /c "dir /S /B "$9\MariaDB*\bin\mysql.exe" 2>NUL"'
    Pop $R0
    Pop $R1
    ${If} $R0 == 0
        ; Found mysql.exe, extract the first path
        StrCpy $R2 0
        find_first_line:
            StrCpy $R3 $R1 1 $R2
            ${If} $R3 == ""
                ; End of string without newline
                StrCpy $R4 $R1
                Goto found_path
            ${EndIf}
            ${If} $R3 == "$\r"
            ${OrIf} $R3 == "$\n"
                ; Found newline, extract the path
                StrCpy $R4 $R1 $R2
                Goto found_path
            ${EndIf}
            IntOp $R2 $R2 + 1
            Goto find_first_line

        found_path:
            ${If} $R4 != ""
                DetailPrint "Found mysql.exe at: $R4"
                Push "$R4"
                Return
            ${EndIf}
    ${EndIf}

    ; Try mariadb.exe if mysql.exe not found
    DetailPrint "Searching for mariadb.exe in all MariaDB directories under $9..."
    nsExec::ExecToStack 'cmd /c "dir /S /B "$9\MariaDB*\bin\mariadb.exe" 2>NUL"'
    Pop $R0
    Pop $R1
    ${If} $R0 == 0
        StrCpy $R2 0
        find_first_line2:
            StrCpy $R3 $R1 1 $R2
            ${If} $R3 == ""
                StrCpy $R4 $R1
                Goto found_path2
            ${EndIf}
            ${If} $R3 == "$\r"
            ${OrIf} $R3 == "$\n"
                StrCpy $R4 $R1 $R2
                Goto found_path2
            ${EndIf}
            IntOp $R2 $R2 + 1
            Goto find_first_line2

        found_path2:
            ${If} $R4 != ""
                DetailPrint "Found mariadb.exe at: $R4"
                Push "$R4"
                Return
            ${EndIf}
    ${EndIf}

    Push "NOT_FOUND"
FunctionEnd

; Function to install main application
Function InstallMainApp
    ; Check if main app installer exists
    ${IfNot} ${FileExists} "$EXEDIR\app\BuildTask-Setup.exe"
        MessageBox MB_ICONSTOP "ERROR: Main application installer missing!$\n$\nFile not found: app\BuildTask-Setup.exe$\n$\nThis file contains the main BuildTask application.$\n$\nSolution:$\n1. Check if you downloaded the complete installation package$\n2. Check if antivirus didn't delete the file$\n3. Download the full BuildTask installer again$\n4. Check if the file wasn't moved to Antivirus Quarantine$\n$\nIf the problem persists, contact the software provider"
        Abort
    ${EndIf}

    ; Run main application installer
    DetailPrint "Installing BuildTask application - this may take several minutes..."
    ExecWait '"$EXEDIR\app\BuildTask-Setup.exe" /S' $R0

    ${If} $R0 != 0
        ${If} $R0 == 1223  ; User cancelled
            MessageBox MB_ICONQUESTION "Application installation was cancelled by user.$\n$\nDo you want to try again?" IDYES TryAgain
            MessageBox MB_ICONINFORMATION "Installation will be aborted.$\n$\nTo install the application later:$\n1. Go to installer folder$\n2. Run file: app\BuildTask-Setup.exe$\n3. Follow the instructions"
            Abort
            TryAgain:
                ExecWait '"$EXEDIR\app\BuildTask-Setup.exe" /S' $R0
                ${If} $R0 != 0
                    MessageBox MB_ICONSTOP "Application installation failed again.$\nError code: $R0"
                    Abort
                ${EndIf}
        ${ElseIf} $R0 == 1619  ; Package couldn't be opened
            MessageBox MB_ICONSTOP "ERROR: Cannot open installation package! (Code: 1619)$\n$\nBuildTask-Setup.exe file may be corrupted.$\n$\nSolution:$\n1. Download BuildTask installer again$\n2. Check if the file wasn't damaged by antivirus$\n3. Try running from a different location$\n4. Check if you have enough disk space$\n5. Run as administrator"
            Abort
        ${ElseIf} $R0 == 1618  ; Another installation in progress
            MessageBox MB_ICONSTOP "ERROR: Another installation in progress! (Code: 1618)$\n$\nWindows system may be running another installation.$\n$\nSolution:$\n1. Wait for the other installation to complete$\n2. Check Task Manager for 'msiexec.exe' processes$\n3. Restart your computer if the problem persists$\n4. Try running the installer again"
            Abort
        ${ElseIf} $R0 == 1603  ; Fatal error during installation
            MessageBox MB_ICONSTOP "ERROR: Critical error during installation! (Code: 1603)$\n$\nThis is the most common Windows installation error.$\n$\nSolution:$\n1. Check if you have administrator privileges$\n2. Check Windows Event Viewer (Windows Logs > Application)$\n3. Check if you have enough disk space$\n4. Temporarily disable antivirus$\n5. Restart your computer$\n6. Try installing in Safe Mode$\n$\nIf the problem persists, try manual installation:$\nRun directly: app\BuildTask-Setup.exe"
            Abort
        ${Else}
            MessageBox MB_ICONSTOP "ERROR: Main application installation failed!$\nError code: $R0$\n$\nSolution:$\n1. Check Windows Event Viewer for details$\n2. Check if you have enough disk space$\n3. Check if antivirus is not blocking installation$\n4. Try running installer as administrator$\n5. Check folder structure and clean any BuildTask related folders\n6. Try manual installation: app\BuildTask-Setup.exe$\n$\nIf the problem persists, contact technical support.$\nProvide error code: $R0"
            Abort
        ${EndIf}
    ${EndIf}

    DetailPrint "BuildTask application has been successfully installed"
FunctionEnd

; Function to launch application after installation
Function LaunchApplication
    ; Try to find BuildTask executable in common locations
    ${If} ${FileExists} "$DESKTOP\BuildTask.lnk"
        ExecShell "" "$DESKTOP\BuildTask.lnk"
    ${ElseIf} ${FileExists} "$SMPROGRAMS\BuildTask\BuildTask.lnk"
        ExecShell "" "$SMPROGRAMS\BuildTask\BuildTask.lnk"
    ${ElseIf} ${FileExists} "$LOCALAPPDATA\Programs\BuildTask\BuildTask.exe"
        ExecShell "" "$LOCALAPPDATA\Programs\BuildTask\BuildTask.exe"
    ${ElseIf} ${FileExists} "$PROGRAMFILES\BuildTask\BuildTask.exe"
        ExecShell "" "$PROGRAMFILES\BuildTask\BuildTask.exe"
    ${Else}
        MessageBox MB_ICONINFORMATION "BuildTask has been installed successfully!$\n$\nYou can find the application in Start Menu or on Desktop.$\nIf you can't find it, look for 'BuildTask' in your applications."
    ${EndIf}
FunctionEnd

; Uninstaller section
Section "Uninstall"
    ; Remove registry keys
    DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}"
    DeleteRegKey HKLM "Software\${APP_NAME}"

    ; Remove files and directories
    Delete "$INSTDIR\Uninstall.exe"
    RMDir "$INSTDIR"

    ; Note: We don't automatically remove MariaDB as it might be used by other applications
    MessageBox MB_ICONINFORMATION "${APP_NAME} application has been successfully uninstalled.$\n$\nIMPORTANT NOTE:$\nMariaDB (database) was not removed because it may be used by other applications.$\n$\nIf you want to completely remove MariaDB:$\n1. Go to Control Panel > Programs and Features$\n2. Find 'MariaDB' in the list$\n3. Click 'Uninstall'$\n4. Delete MariaDB data folder (usually C:\ProgramData\MariaDB)$\n$\nIf you plan to reinstall BuildTask - leave MariaDB!"
SectionEnd