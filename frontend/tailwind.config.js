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
  plugins: [
    function({ addBase }) {
      addBase({
        // Style dla Vue3 Datepicker
        '.datepicker-wrapper': {
          'width': '100% !important',
          'max-width': '100% !important',
          'overflow': 'hidden !important',
        },
        '.datepicker-wrapper input[type="text"]': {
          'background-color': 'white !important',
          'color': 'black !important',
          'border': '1px solid #d1d5db !important',
          'border-right': '1px solid #d1d5db !important', 
          'border-radius': '0.375rem !important',
          'width': '100% !important',
          'max-width': '100% !important',
          'box-sizing': 'border-box !important',
        },
        '.datepicker-wrapper input:focus': {
          'outline': 'none !important',
          'box-shadow': '0 0 0 2px #3F51B5 !important',
          'border-color': '#3F51B5 !important',
          'border-right-color': '#3F51B5 !important', 
        },
      })
    }
  ],
}