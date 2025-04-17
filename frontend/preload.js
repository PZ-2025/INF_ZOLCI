// preload.js
const { contextBridge } = require('electron');

// Expose API URL to renderer process
contextBridge.exposeInMainWorld('api', {
    // Provide default API URL
    getApiUrl: () => {
        // Try to get from process.env if available
        return process.env.API_URL || 'http://localhost:8080';
    }
});

// DOMContentLoaded listener for electron version information
window.addEventListener('DOMContentLoaded', () => {
    const replaceText = (selector, text) => {
        const element = document.getElementById(selector)
        if (element) element.innerText = text
    }

    for (const dependency of ['chrome', 'node', 'electron']) {
        replaceText(`${dependency}-version`, process.versions[dependency])
    }
});