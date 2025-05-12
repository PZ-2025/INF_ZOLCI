// frontend/main.js
import { app, BrowserWindow, Menu } from 'electron';
import path from 'path';
import { fileURLToPath } from 'url';
import { spawn } from 'child_process';
import dotenv from "dotenv";
import fs from 'fs';

// Get __dirname equivalent in ES modules
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Load environment variables
dotenv.config();

let mainWindow;
let backendProcess = null;

// Check if in development mode
const isDev = process.env.NODE_ENV === 'development';

function createWindow() {
    mainWindow = new BrowserWindow({
        width: 1200,
        height: 800,
        resizable: false, // Blokuje możliwość zmiany rozmiaru okna
        useContentSize: true, // Sprawia, że podane wymiary odnoszą się do zawartości (bez ramek)
        autoHideMenuBar: true,      // menu jest schowane
        icon: path.join(__dirname, "src/assets/buildtask_logo.ico"),
        webPreferences: {
            preload: path.join(__dirname, "preload.js"),
            contextIsolation: true,
            nodeIntegration: false
        }
    });

    // Load the index.html file
    if (isDev) {
        mainWindow.loadFile(path.join(__dirname, 'dist/index.html'));
    } else {
        mainWindow.loadFile(path.join(__dirname, 'dist/index.html'));
    }

    // Create menu
    const template = [
        {
            label: 'File',
            submenu: [
                {
                    label: 'Start Backend',
                    click: startBackend
                },
                {
                    label: 'Stop Backend',
                    click: stopBackend
                },
                { type: 'separator' },
                {
                    label: 'Exit',
                    click: () => {
                        stopBackend();
                        app.quit();
                    }
                }
            ]
        },
        {
            label: 'View',
            submenu: [
                { role: 'reload' },
                { role: 'forceReload' },
                { role: 'toggleDevTools' },
                { type: 'separator' },
                { role: 'resetZoom' },
                { role: 'zoomIn' },
                { role: 'zoomOut' },
                { type: 'separator' },
                { role: 'togglefullscreen' }
            ]
        }
    ];

    const menu = Menu.buildFromTemplate(template);
    Menu.setApplicationMenu(menu);
}

function startBackend() {
    if (backendProcess) {
        console.log('Backend already running');
        return;
    }

    try {
        const backendDir = path.join(__dirname, 'backend');
        const isWindows = process.platform === 'win32';

        // Find the JAR file
        const files = fs.readdirSync(backendDir);
        const jarFile = files.find(file => file.endsWith('.jar'));

        if (!jarFile) {
            console.error('No JAR file found in the backend directory');
            return;
        }

        const jarPath = path.join(backendDir, jarFile);

        // Start the backend process
        if (isWindows) {
            backendProcess = spawn('java', ['-jar', jarPath], {
                cwd: backendDir,
                detached: false
            });
        } else {
            backendProcess = spawn('java', ['-jar', jarPath], {
                cwd: backendDir,
                detached: false
            });
        }

        console.log('Backend started');

        backendProcess.stdout.on('data', (data) => {
            console.log(`Backend stdout: ${data}`);
        });

        backendProcess.stderr.on('data', (data) => {
            console.error(`Backend stderr: ${data}`);
        });

        backendProcess.on('close', (code) => {
            console.log(`Backend process exited with code ${code}`);
            backendProcess = null;
        });
    } catch (error) {
        console.error('Failed to start backend:', error);
    }
}

function stopBackend() {
    if (backendProcess) {
        if (process.platform === 'win32') {
            spawn('taskkill', ['/pid', backendProcess.pid, '/f', '/t']);
        } else {
            backendProcess.kill();
        }
        backendProcess = null;
        console.log('Backend stopped');
    }
}

app.whenReady().then(() => {
    createWindow();

    // Automatically start the backend when the app starts
    startBackend();

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) createWindow();
    });
});

// app.on('window-all-closed', () => {
//     if (process.platform !== 'darwin') {
//         stopBackend();
//         app.quit();
//     }
// });

app.on('will-quit', () => {
    stopBackend();
});