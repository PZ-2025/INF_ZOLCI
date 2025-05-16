import { warn } from 'vue'

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
        accent: "#7986CB",         // podświetlenia, ikony, liczby
        secondary: "#5C6BC0",      // nagłówki, menu, alternatywne przyciski
        success: "#4CAF50",        // status OK
        danger: "#F44336",         // błędy, ostrzeżenia
        text: "#1F2937",           // główny kolor tekstu
        muted: "#6B7280",          // pomocniczy tekst
        warning: "#3949AB",        // ostrzeżenia
        warningHover: "#303F9F",   // kolor ostrzeżenia po najechaniu
        navbar: "#212A5C",         // kolor tła paska nawigacyjnego
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-out both',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0', transform: 'translateY(-10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
      },
    },
  },
  plugins: [],
}
