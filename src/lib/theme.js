// Theme management — night (dark) mode ONLY. Light mode removed entirely.

const THEME_KEY = 'nur_theme';

export const getTheme = () => 'dark';

export const applyTheme = () => {
  const root = document.documentElement;
  root.classList.add('dark');
  root.classList.remove('light');
  const meta = document.querySelector('meta[name="theme-color"]');
  if (meta) meta.setAttribute('content', '#0d160f');
};

export const setTheme = () => {
  try { localStorage.setItem(THEME_KEY, 'dark'); } catch { /* */ }
  applyTheme();
};

export const toggleTheme = () => 'dark';

export const initTheme = () => applyTheme();

// Data saving mode
const DATA_SAVER_KEY = 'nur_data_saver';

export const getDataSaver = () => {
  try { return localStorage.getItem(DATA_SAVER_KEY) === 'true'; } catch { return false; }
};

export const setDataSaver = (on) => {
  try { localStorage.setItem(DATA_SAVER_KEY, on ? 'true' : 'false'); } catch { /* */ }
};
