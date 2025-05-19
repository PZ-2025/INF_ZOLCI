#define MyAppName "BuildTask"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "INF_ŻÓŁCI"
#define MyAppURL "https://github.com/JustFiesta/BuildTask"
#define MyAppExeName "BuildTask.exe"

[Setup]
AppId={{3F7B3A9E-5D8B-421F-9D5F-E7F8E6C7B8A9}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
LicenseFile=LICENSE
OutputDir=installer
OutputBaseFilename=buildtask-setup
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "polish"; MessagesFile: "compiler:Languages\Polish.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked; OnlyBelowVersion: 6.1; Check: not IsAdminInstallMode

[Files]
; Aplikacja
Source: "..\frontend\dist\win-unpacked\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\frontend\dist\win-unpacked\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

; Instalator MariaDB i skrypt SQL
Source: "tools\mariadb-installer.msi"; DestDir: "{tmp}"; Flags: ignoreversion
Source: "tools\init.sql"; DestDir: "{tmp}"; Flags: ignoreversion

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
; Instalacja MariaDB
Filename: "msiexec.exe"; Parameters: "/i ""{tmp}\mariadb-installer.msi"" /passive /norestart SERVICENAME=MariaDB PASSWORD="""" PORT=3306 ENABLETCP=1 ALLOWREMOTEROOT=1 AUTOSTART=1"; StatusMsg: "Instalowanie MariaDB..."; Check: NeedsMariaDB

; Uruchom aplikację po instalacji
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[Code]
function NeedsMariaDB(): Boolean;
begin
  Result := not DirExists(ExpandConstant('{pf64}\MariaDB 11.7\bin'));
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  mysqlExe, initSQL, mariaDBService: string;
  resultCode: Integer;
  retryCount, maxRetries: Integer;
begin
  if CurStep = ssPostInstall then
  begin
    mysqlExe := ExpandConstant('{pf64}\MariaDB 11.7\bin\mysql.exe');
    initSQL := ExpandConstant('{tmp}\init.sql');

    if FileExists(mysqlExe) then
    begin
      mariaDBService := 'MariaDB';
      maxRetries := 10;
      retryCount := 0;

      while (retryCount < maxRetries) and
            (not Exec('sc', 'query ' + mariaDBService + ' | find "RUNNING"', '', SW_HIDE, ewWaitUntilTerminated, resultCode) or (resultCode <> 0)) do
      begin
        Sleep(2000);
        retryCount := retryCount + 1;
      end;

      Sleep(3000);

      if Exec(mysqlExe,
  '-u root --execute="source ' + initSQL + '"',
  '', SW_HIDE, ewWaitUntilTerminated, resultCode) then
      begin
        if resultCode <> 0 then
          MsgBox('Wykonanie skryptu SQL nie powiodło się. Kod błędu: ' + IntToStr(resultCode), mbError, MB_OK);
      end
      else
      begin
        if not Exec(mysqlExe, '-u root --protocol=TCP --port=3306 -e "source ' + initSQL + '"', '', SW_HIDE, ewWaitUntilTerminated, resultCode) then
          MsgBox('Nie udało się wykonać skryptu SQL. Błąd: ' + SysErrorMessage(resultCode), mbError, MB_OK);
      end;
    end
    else
    begin
      MsgBox('Nie znaleziono mysql.exe – konfiguracja bazy danych nie powiodła się.', mbError, MB_OK);
    end;
  end;
end;