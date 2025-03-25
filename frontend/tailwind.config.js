/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],  
  theme: {
    extend: {
      colors: {
        navbar: "#780116",
        accent: "#F7B538",
        secondary: "#DB7C26",
        warning: "#D8572A",
        danger: "#C32F27",
        primary: "#cc7204",
      },
    },
  },
  plugins: [],
}
