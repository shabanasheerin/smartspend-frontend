/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eef4ff',
          100: '#dae6ff',
          200: '#bcd0ff',
          300: '#8db0ff',
          400: '#5786ff',
          500: '#2f5dff',
          600: '#1a3ff2',
          700: '#152fd6',
          800: '#1727ac',
          900: '#182786',
        },
      },
    },
  },
  plugins: [],
}
