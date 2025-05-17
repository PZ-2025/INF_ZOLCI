#define MyAppName "BuildTask"
#define MyAppVersion "1.0"
#define MyAppPublisher "BuildTask Inc."
#define MyAppURL "https://www.buildtask.com/"
#define MyAppExeName "scripts\start_application.bat"

[Setup]
AppId={{68504BF3-3AB2-45C1-B98D-1DF848CC099A}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
PrivilegesRequired=admin
OutputDir=.
OutputBaseFilename=buildtask_setup
SetupIconFile=installer\files\frontend\dist_electron\win-unpacked\resources\dist\assets\buildtask_logo-C-qCMMvT.ico
Compression=lzma
SolidCompression=yes
WizardStyle=modern
WizardImageFile=installer\graphics\installer_logo.bmp
WizardSmallImageFile=installer\graphics\installer_small.bmp
DisableWelcomePage=no

[Languages]
Name: "polish"; MessagesFile: "compiler:Languages\Polish.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "installer\files\backend\buildtask_backend.exe"; DestDir: "{app}\backend"; Flags: ignoreversion
Source: "installer\files\frontend\dist_electron\win-unpacked\BuildTask.exe"; DestDir: "{app}\frontend"; Flags: ignoreversion
Source: "installer\files\frontend\dist_electron\win-unpacked\resources\*"; DestDir: "{app}\frontend\resources"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "installer\files\config\*"; DestDir: "{app}\config"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "installer\files\scripts\*"; DestDir: "{app}\scripts"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "installer\files\mariadb\mariadb-11.4.5-winx64.msi"; DestDir: "{app}\mariadb"; Flags: ignoreversion
Source: "installer\phpMyAdmin\phpMyAdmin-5.2.2-all-languages.zip"; DestDir: "{app}\phpMyAdmin"; Flags: ignoreversion
Source: "frontend\node_modules\electron\dist\ffmpeg.dll"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{group}\{cm:ProgramOnTheWeb,{#MyAppName}}"; Filename: "{#MyAppURL}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "powershell.exe"; Parameters: "-ExecutionPolicy Bypass -File ""{app}\scripts\check_requirements.ps1"""; Description: "Sprawdzanie wymagań systemowych"; Flags: shellexec waituntilterminated; BeforeInstall: CheckRequirements
Filename: "powershell.exe"; Parameters: "-ExecutionPolicy Bypass -File ""{app}\scripts\check_ports.ps1"""; Description: "Sprawdzanie dostępności portów"; Flags: shellexec waituntilterminated; BeforeInstall: CheckPorts
Filename: "powershell.exe"; Parameters: "-ExecutionPolicy Bypass -File ""{app}\scripts\setup_mariadb.ps1"" -DbName ""{code:GetDbName}"" -DbUser ""{code:GetDbUser}"" -DbPassword ""{code:GetDbPassword}"" -DbRootPassword ""{code:GetDbRootPassword}"" -InstallDir ""{app}"" -MariaDBPort ""{code:GetDbPort}"""; Description: "Instalacja i konfiguracja bazy danych"; StatusMsg: "Instalowanie i konfigurowanie MariaDB..."; Flags: shellexec waituntilterminated
Filename: "{app}\{#MyAppExeName}"; Description: "Uruchom {#MyAppName}"; Flags: nowait postinstall skipifsilent

[Code]
var
  DatabasePage: TInputQueryWizardPage;
  RequirementsErrorPage: TOutputMsgWizardPage;
  PortsErrorPage: TOutputMsgWizardPage;
  RequirementsMet: Boolean;
  PortsAvailable: Boolean;

procedure InitializeWizard;
begin
  { Strona konfiguracji bazy danych }
  DatabasePage := CreateInputQueryPage(wpWelcome,
    'Konfiguracja bazy danych',
    'Wprowadź dane do konfiguracji bazy danych MariaDB.',
    'Proszę wprowadzić dane dostępowe, które zostaną użyte do konfiguracji bazy danych:');
  DatabasePage.Add('Nazwa bazy danych:', False);
  DatabasePage.Add('Nazwa użytkownika:', False);
  DatabasePage.Add('Hasło użytkownika:', True);
  DatabasePage.Add('Hasło administratora (root):', True);
  DatabasePage.Add('Port (domyślnie 3306):', False);

  { Domyślne wartości }
  DatabasePage.Values[0] := 'buildtask_db';
  DatabasePage.Values[1] := 'buildtask_user';
  DatabasePage.Values[2] := 'buildtask_password';
  DatabasePage.Values[3] := 'root';
  DatabasePage.Values[4] := '3306';

  { Strony błędów }
  RequirementsErrorPage := CreateOutputMsgPage(wpWelcome,
    'Błąd: Niespełnione wymagania systemowe',
    'Instalacja wymaga spełnienia określonych wymagań systemowych.',
    'Instalator wykrył, że Twój system nie spełnia następujących wymagań: ' +
    '- Java 17 lub nowsza ' +
    '- Minimum 4 GB pamięci RAM ' +
    '- Uprawnienia administratora ' +
    'Zainstaluj brakujące komponenty i uruchom instalator ponownie.');

  PortsErrorPage := CreateOutputMsgPage(wpWelcome,
    'Błąd: Wymagane porty są zajęte',
    'Instalacja wymaga wolnych portów.',
    'Instalator wykrył, że jeden lub więcej z następujących portów jest już zajęty: ' +
    '3306 (MariaDB), 8081 (phpMyAdmin), 8080 (API aplikacji). ' +
    'Zamknij aplikacje, które mogą używać tych portów, i spróbuj ponownie.');

  RequirementsMet := True;
  PortsAvailable := True;
end;

procedure CheckRequirements;
var
  ResultCode: Integer;
begin
  if not Exec('powershell.exe', '-ExecutionPolicy Bypass -File "' +
    ExpandConstant('{app}\scripts\check_requirements.ps1') + '"', '', SW_HIDE,
    ewWaitUntilTerminated, ResultCode) or (ResultCode <> 0) then
  begin
    RequirementsMet := False;
  end;
end;

procedure CheckPorts;
var
  ResultCode: Integer;
begin
  if not Exec('powershell.exe', '-ExecutionPolicy Bypass -File "' +
    ExpandConstant('{app}\scripts\check_ports.ps1') + '"', '', SW_HIDE,
    ewWaitUntilTerminated, ResultCode) or (ResultCode <> 0) then
  begin
    PortsAvailable := False;
  end;
end;

function ShouldSkipPage(PageID: Integer): Boolean;
begin
  Result := False;

  { Pokaż stronę błędu wymagań, jeśli wymagania nie są spełnione }
  if (PageID = RequirementsErrorPage.ID) and RequirementsMet then
    Result := True;

  { Pokaż stronę błędu portów, jeśli porty są zajęte }
  if (PageID = PortsErrorPage.ID) and PortsAvailable then
    Result := True;
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
  Result := True;

  { Nie pozwól kontynuować, jeśli wymagania nie są spełnione }
  if (CurPageID = RequirementsErrorPage.ID) and not RequirementsMet then
    Result := False;

  { Nie pozwól kontynuować, jeśli porty są zajęte }
  if (CurPageID = PortsErrorPage.ID) and not PortsAvailable then
    Result := False;

  { Walidacja danych bazy danych }
  if (CurPageID = DatabasePage.ID) then begin
    if (Trim(DatabasePage.Values[0]) = '') or
       (Trim(DatabasePage.Values[1]) = '') or
       (Trim(DatabasePage.Values[2]) = '') or
       (Trim(DatabasePage.Values[3]) = '') or
       (Trim(DatabasePage.Values[4]) = '') then
    begin
      MsgBox('Wszystkie pola muszą być wypełnione.', mbError, MB_OK);
      Result := False;
    end;
  end;
end;

function GetDbName(Param: String): String;
begin
  Result := DatabasePage.Values[0];
end;

function GetDbUser(Param: String): String;
begin
  Result := DatabasePage.Values[1];
end;

function GetDbPassword(Param: String): String;
begin
  Result := DatabasePage.Values[2];
end;

function GetDbRootPassword(Param: String): String;
begin
  Result := DatabasePage.Values[3];
end;

function GetDbPort(Param: String): String;
begin
  Result := DatabasePage.Values[4];
end;