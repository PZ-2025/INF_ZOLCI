import { app, BrowserWindow, Menu } from 'electron';
import path from 'path';
import { fileURLToPath } from 'url';
import { spawn } from 'child_process';
import dotenv from "dotenv";
import { exec } from 'child_process';

// Get __dirname equivalent in ES modules
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Load environment variables
dotenv.config();

let mainWindow;
let backendProcess = null;

const isWindows = process.platform === 'win32';
const isDev = process.env.NODE_ENV === 'development';

function createWindow() {
    console.log('Creating main window');
    mainWindow = new BrowserWindow({
        width: 1200,
        height: 800,
        resizable: false,
        useContentSize: true,
        autoHideMenuBar: false,
        icon: path.join(__dirname, "src/assets/buildtask_logo.ico"),
        webPreferences: {
            preload: path.join(__dirname, "preload.js"),
            contextIsolation: true,
            nodeIntegration: false
        }
    });

    mainWindow.loadFile(path.join(__dirname, 'dist/index.html'));

    const menu = Menu.buildFromTemplate([
        {
            label: 'File',
            submenu: [
                { label: 'Start Backend', click: startBackend },
                { label: 'Stop Backend', click: stopBackend },
                { type: 'separator' },
                { label: 'Exit', click: () => { stopBackend(); app.quit(); } }
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
    ]);
    Menu.setApplicationMenu(menu);
    console.log('Menu created');
}

const startBackend = () => {
    if (isWindows) {
        const backendPath = path.join('./','backend', 'start-backend.bat');
        console.log('Starting backend with batch file:', backendPath);
        exec(`start "" "${backendPath}"`, (error) => {
            if (error) {
                console.error('Failed to start backend:', error);
            } else {
                console.log('Backend started successfully.');
            }
        });
    }
};

function stopBackend() {
    if (backendProcess) {
        try {
            if (isWindows) {
                spawn('taskkill', ['/pid', backendProcess.pid, '/f', '/t']);
            } else {
                backendProcess.kill();
            }
            console.log('Backend stopped');
        } catch (err) {
            console.error('Error stopping backend:', err);
        }
        backendProcess = null;
    }
}

app.whenReady().then(() => {
    console.log('App is ready');
    createWindow();
    console.log('Starting backend...');
    startBackend();

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) createWindow();
    });
});

app.on('will-quit', () => {
    stopBackend();
});
