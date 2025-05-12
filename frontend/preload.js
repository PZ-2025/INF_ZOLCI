// frontend/preload.js
const { contextBridge, ipcRenderer } = require('electron');

// API URL is set to localhost by default
const API_URL = process.env.API_URL || 'http://localhost:8080';

contextBridge.exposeInMainWorld('api', {
    getApiUrl: () => API_URL
});

window.addEventListener('DOMContentLoaded', () => {
    const replaceText = (selector, text) => {
        const element = document.getElementById(selector);
        if (element) element.innerText = text;
    };

    for (const dependency of ['chrome', 'node', 'electron']) {
        replaceText(`${dependency}-version`, process.versions[dependency]);
    }
});