// main.js
import { app, BrowserWindow } from 'electron';
import path from 'path';
import { fileURLToPath } from 'url';
import dotenv from "dotenv";
dotenv.config();

// Get __dirname equivalent in ES modules
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const createWindow = () => {
    const win = new BrowserWindow({
        width: 1200,
        height: 800,
        resizable: true,
        icon: app.isPackaged
            ? path.join(process.resourcesPath, 'dist/assets/buildtask_logo.ico')
            : path.join(__dirname, 'src/assets/buildtask_logo.ico'),
        webPreferences: {
            preload: path.join(__dirname, 'preload.js'),
            contextIsolation: true,
            nodeIntegration: false
        }
    });

    // Ładuj strony w zależności od trybu (produkcyjny/deweloperski)
    if (app.isPackaged) {
        // W produkcji, ładuj z zasobów
        win.loadFile(path.join(process.resourcesPath, 'dist/index.html'));
    } else {
        // W trybie dev, ładuj z lokalnego katalogu dist
        win.loadFile(path.join(__dirname, 'dist/index.html'));
    }
};

app.whenReady().then(() => {
    createWindow();

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) createWindow();
    });
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') app.quit();
});