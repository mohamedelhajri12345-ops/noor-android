import React from 'react'
import ReactDOM from 'react-dom/client'
import App from '@/App.jsx'
import '@/index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <App />
)

// Register Service Worker for offline support (production only)
if ('serviceWorker' in navigator) {
  if (!import.meta.env.DEV) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('/sw.js').catch(() => {});
    });
  } else {
    // Unregister stale service workers in dev to prevent cache issues
    window.addEventListener('load', () => {
      navigator.serviceWorker.getRegistrations()
        .then(regs => regs.forEach(r => r.unregister()))
        .catch(() => {});
    });
  }
}
