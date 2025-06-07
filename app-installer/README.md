# BuildTask Installer - Installation Instructions

## Required Tools

### 1. NSIS (Nullsoft Scriptable Install System)

- Download from: https://nsis.sourceforge.io/Download
- Install in the default location
- Add to system PATH: `C:\Program Files (x86)\NSIS`

### 2. Launch4j

- Download from: http://launch4j.sourceforge.net/
- Install in: `C:\Program Files (x86)\Launch4j\`

### 3. NSIS Plugins (required)

- Simple Service Plugin: https://nsis.sourceforge.io/NSIS_Simple_Service_Plugin
- Download and place in: `C:\Program Files (x86)\NSIS\Plugins\x86-unicode\`

## File Preparation

### 1. Folder Structure

```
app-installer/
├── installer.nsi              # Main NSIS script
├── tools/
│   ├── mariadb-installer.msi  # MariaDB installer (download separately)
│   ├── jre/                   # JRE (download separately - 21 recommended, Adoptium OpenJDK)
│   └── init.sql               # Already exists
└── app/
    └── BuildTask-Setup.exe    # Needs to be created from frontend build (frontend/dist)

```

### 2. Downloading MariaDB Installer

1. Go to: https://mariadb.org/download/
2. Choose the latest stable version
3. Select "MSI Package" for Windows
4. Download and place it in `tools/mariadb-installer.msi`

### 3. Preparing the JRE

1. Go to https://adoptium.net/
2. Download the version on which You compiled BuildTask jar
3. Select the "JRE" option
4. Extract the JRE to a directory
5. Place the JRE in `app-installer/tools/jre/`

## Building the Installer

### Manual Build

```bash
# 1. Build the backend
cd backend
gradlew clean build -x test

# 2. Create backend.exe
launch4j config.xml # Ensure config.xml is correctly set up with paths to the JAR and JRE

# 3. Build the frontend
cd ../frontend
npm run electron:build:win

# 4. Copy installer to app/
cp "dist/BuildTask Setup *.exe" "../app-installer/app/BuildTask-Setup.exe"

# 5. Compile NSIS installer
cd ../app-installer
makensis installer.nsi
```

## Testing the Installer

### 1. Basic Test

1. Run `BuildTask-Installer.exe` as administrator
2. Ensure all steps complete successfully
3. Verify that the application runs after installation

### 2. Edge Case Testing

**Port 8080 already in use:**

```bash
# Simulate a busy port
docker run --name app_clone -d -p 8080:8080 busybox sleep 500000
# Run the installer - it should display an error
```

**Port 3306 already in use:**

```bash
# Simulate a busy port
docker run --name maria_clone -d -p 3306:3306 busybox sleep 500000
# Run the installer - it should display an error
```

**MariaDB not installed:**

- Uninstall MariaDB
- Run the installer - it should install MariaDB automatically

**MariaDB installed but stopped:**

```bash
net stop MariaDB
# Run the installer - it should start the service
```

## Troubleshooting

### NSIS Compilation Error

```
Error: Can't load plugin ServiceLib
```

**Solution:** Download and install the NSIS Simple Service plugin and add it to the NSIS plugins directory

### Launch4j Error

```
Cannot find JRE
```

**Solution:** Check the paths in config.xml

### Frontend Build Error

```
electron-builder failed
```

**Solution:**

```bash
cd frontend
npm install
npm run build
npm run electron:build:win
```

## Support

If you encounter issues, check:
- NSIS logs in the `%TEMP%` folder
- Windows Event Viewer for installation errors
- Whether all required files are in the correct locations