// index.js - using ES module imports
import { app, BrowserWindow } from 'electron';
import path from 'path';
import { fileURLToPath } from 'url';
import { spawn } from 'child_process';
import dotenv from "dotenv";
dotenv.config();

// Get __dirname equivalent in ES modules
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const createWindow = () => {
    const win = new BrowserWindow({
        width: 1200,
        height: 800,
        resizable: false, // Block resizing
        autoHideMenuBar: true,
        icon: path.join(__dirname, "src/assets/buildtask_logo.ico"),
        webPreferences: {
            preload: path.join(__dirname, "preload.js"),
            contextIsolation: true,
            nodeIntegration: false
        }
    });

    win.loadFile(path.join(__dirname, 'dist/index.html'));

    // Optional: open DevTools for debugging
    // win.webContents.openDevTools();
};

// START backend/backend.exe on app start
const startBackend = () => {
    const backendPath = path.join(__dirname,'..', '..', 'backend', 'backend.exe');

    const child = spawn(backendPath, {
        detached: true,
        stdio: 'ignore', // Ignore stdio to fully detach
        windowsHide: true // Hide console window
    });

    child.unref(); // Allow Electron to exit independently of backend
};

app.whenReady().then(() => {
    startBackend();
    createWindow();

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) createWindow();
    });
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') app.quit();
});
