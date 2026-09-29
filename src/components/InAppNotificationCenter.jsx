import React, { useState, useEffect } from 'react';
import { X, Bell, Clock, Sparkles, BookOpen } from 'lucide-react';

// In-app notification center — works in Android WebView (no browser Notification API needed)
// Listens for 'nur-in-app-notification' custom events and shows banners

export default function InAppNotificationCenter() {
  const [notifications, setNotifications] = useState([]);

  useEffect(() => {
    const handler = (e) => {
      if (!e?.detail) return;
      const { title, body, type } = e.detail;
      const id = Date.now() + Math.random();
      setNotifications(prev => [...prev.slice(-2), { id, title, body, type }]);
      // Auto-dismiss after 10 seconds
      setTimeout(() => {
        setNotifications(prev => prev.filter(n => n.id !== id));
      }, 10000);
    };
    window.addEventListener('nur-in-app-notification', handler);
    return () => window.removeEventListener('nur-in-app-notification', handler);
  }, []);

  const dismiss = (id) => {
    setNotifications(prev => prev.filter(n => n.id !== id));
  };

  if (notifications.length === 0) return null;

  const getIcon = (type) => {
    if (type?.startsWith('adhan')) return Clock;
    if (type?.startsWith('athkar')) return Sparkles;
    if (type?.startsWith('daily')) return BookOpen;
    return Bell;
  };

  return (
    <div className="fixed top-0 left-0 right-0 z-[200] safe-top px-4 pt-4 space-y-2 pointer-events-none">
      {notifications.map(n => {
        const Icon = getIcon(n.type);
        return (
          <div
            key={n.id}
            className="glass-card rounded-2xl p-3 flex items-start gap-3 pointer-events-auto animate-slide-up shadow-2xl"
            style={{ borderColor: 'hsl(45 73% 63% / 0.35)' }}
          >
            <div className="w-10 h-10 rounded-xl bg-gold/15 flex items-center justify-center shrink-0">
              <Icon className="w-5 h-5 text-gold" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-bold text-gold">{n.title}</p>
              <p className="text-xs text-foreground mt-0.5">{n.body}</p>
            </div>
            <button
              onClick={() => dismiss(n.id)}
              className="shrink-0 w-7 h-7 rounded-lg flex items-center justify-center text-muted-foreground hover:text-foreground active:scale-90 transition-transform"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        );
      })}
    </div>
  );
}
