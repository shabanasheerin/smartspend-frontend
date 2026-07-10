import { createSlice } from '@reduxjs/toolkit';

const storedTheme = localStorage.getItem('theme') || 'light';

const uiSlice = createSlice({
  name: 'ui',
  initialState: {
    theme: storedTheme,
    sidebarOpen: true,
  },
  reducers: {
    toggleTheme(state) {
      state.theme = state.theme === 'light' ? 'dark' : 'light';
      localStorage.setItem('theme', state.theme);
    },
    toggleSidebar(state) {
      state.sidebarOpen = !state.sidebarOpen;
    },
  },
});

export const { toggleTheme, toggleSidebar } = uiSlice.actions;
export default uiSlice.reducer;
