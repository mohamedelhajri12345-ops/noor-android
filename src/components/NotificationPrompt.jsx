import React, { useState, useEffect } from 'react';
import { Bell, X } from 'lucide-react';
import { requestNotificationPermission, openInBrowserForNotifications, initNotifications, startReminders, startPrayerWatcher } from '@/lib/notificationService';

const PROMPT_KEY = 'nur_notif_prompt_shown';

export default function NotificationPrompt() {
  const [show, setShow] = useState(false);

  useEffect(() => {
    const shown = localStorage.getItem(PROMPT_KEY);
    if (!shown && 'Notification' in window && Notification.permission === 'default') {
      const timer = setTimeout(() => setShow(true), 3000);
      return () => clearTimeout(timer);
    }
    // Auto-start reminders if permission already granted
    if ('Notification' in window && Notification.permission === 'granted') {
      initNotifications();
      // Restore prayer watcher if cached prayer times exist
      try {
        const cached = localStorage.getItem('nur_prayer_times');
        if (cached) {
          const times = JSON.parse(cached);
          if (times && times.Fajr) startPrayerWatcher(times);
        }
      } catch { /* */ }
    }
  }, []);

  const enable = async () => {
    const granted = await requestNotificationPermission();
    if (granted) {
      // Start daily reminders immediately
      startReminders();
      // Restore prayer watcher if cached prayer times exist
      try {
        const cached = localStorage.getItem('nur_prayer_times');
        if (cached) {
          const times = JSON.parse(cached);
          if (times && times.Fajr) startPrayerWatcher(times);
        }
      } catch { /* */ }
    }
    // Also subscribe via OneSignal if available
    if (window.OneSignalDeferred) {
      window.OneSignalDeferred.push(async function () {
        try { await window.OneSignal.Notifications.requestPermission(); } catch { /* */ }
      });
    }
    localStorage.setItem(PROMPT_KEY, 'true');
    setShow(false);
  };

  const dismiss = () => {
    localStorage.setItem(PROMPT_KEY, 'true');
    setShow(false);
  };

  if (!show) return null;

  return (
    <div className="fixed bottom-20 left-4 right-4 z-50 animate-slide-up">
      <div className="glass-card rounded-2xl p-4 border-gold/30">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gold/10 flex items-center justify-center shrink-0">
            <Bell className="w-5 h-5 text-gold" />
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-bold text-foreground">تفعيل الإشعارات</p>
            <p className="text-xs text-muted-foreground">لتلقي تذكيرات الصلاة والأذكار</p>
          </div>
          <button onClick={enable}
            className="bg-gold-gradient text-primary-foreground rounded-xl px-4 py-2 text-sm font-bold active:scale-95 transition-transform shrink-0">
            تفعيل
          </button>
          <button onClick={dismiss} className="text-muted-foreground p-1 shrink-0">
            <X className="w-4 h-4" />
          </button>
        </div>
        <button onClick={() => { openInBrowserForNotifications(); dismiss(); }}
          className="w-full mt-2.5 text-xs text-gold/80 hover:text-gold transition-colors flex items-center justify-center gap-1.5">
          🌐 تفعيل عبر المتصفح الخارجي
        </button>
      </div>
    </div>
  );
}
