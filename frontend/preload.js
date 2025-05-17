// preload.js
const { contextBridge, ipcRenderer } = require('electron');
const fs = require('fs');
const path = require('path');

// Funkcja do pobierania adresu API z konfiguracji
function getApiUrlFromConfig() {
    try {
        // Próbuj odczytać adres API z pliku konfiguracyjnego backend-a
        const configPath = path.join(process.cwd(), '../config/application.properties');

        if (fs.existsSync(configPath)) {
            const configContent = fs.readFileSync(configPath, 'utf8');
            const serverPortMatch = configContent.match(/server\.port\s*=\s*(\d+)/);
            const port = serverPortMatch ? serverPortMatch[1] : '8080';
            return `http://localhost:${port}`;
        }
    } catch (err) {
        console.error('Nie można odczytać konfiguracji:', err);
    }

    // Zwróć domyślny adres, jeśli nie udało się odczytać konfiguracji
    return process.env.API_URL || 'http://localhost:8080';
}

// Eksponuj API dla renderera
contextBridge.exposeInMainWorld('api', {
    getApiUrl: getApiUrlFromConfig
});

// Debugowanie wersji
window.addEventListener('DOMContentLoaded', () => {
    const replaceText = (selector, text) => {
        const element = document.getElementById(selector)
        if (element) element.innerText = text
    }

    for (const dependency of ['chrome', 'node', 'electron']) {
        replaceText(`${dependency}-version`, process.versions[dependency])
    }
});