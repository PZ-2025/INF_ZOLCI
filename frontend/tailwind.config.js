/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],  
  theme: {
    extend: {
      colors: {
        background: "#F9FAFB",     // jasne tło aplikacji
        surface: "#FFFFFF",        // karty, panele
        primary: "#3F51B5",        // główny kolor akcji (np. przyciski)
        accent: "#FFC107",         // podświetlenia, ikony, liczby
        secondary: "#5C6BC0",      // nagłówki, menu, alternatywne przyciski
        success: "#4CAF50",        // status OK
        danger: "#F44336",         // błędy, ostrzeżenia
        text: "#1F2937",           // główny kolor tekstu
        muted: "#6B7280",          // pomocniczy tekst
      },
    },
  },
  plugins: [],
}
