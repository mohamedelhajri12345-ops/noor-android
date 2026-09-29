// Notification service — Adhan audio + in-app notifications (Android WebView compatible)
// Browser Notification API doesn't work in Android WebView, so we use in-app notifications
// that render as banners inside the app. OneSignal backend push handles closed-app notifications.

import { audioManager } from '@/lib/audioManager';

// Adhan audio sources — Islamic Network CDN
const ADHAN_SOURCES = {
  default: 'https://cdn.islamic.network/cdn/azan/audio/adhan_makkah.mp3',
  makkah: 'https://cdn.islamic.network/cdn/azan/audio/adhan_makkah.mp3',
  madinah: 'https://cdn.islamic.network/cdn/azan/audio/adhan_madinah.mp3',
  egypt: 'https://cdn.islamic.network/cdn/azan/audio/adhan_egypt.mp3',
  palestine: 'https://cdn.islamic.network/cdn/azan/audio/adhan_palestine.mp3',
};

const ADHAN_FALLBACK = 'https://www.islamcan.com/audio/adhan/azan1.mp3';

let prayerWatcherInterval = null;
let reminderInterval = null;
let lastNotifiedPrayer = null;
let lastNotifiedAthkar = null;
let lastNotifiedDaily = null;

// Detect Android WebView — browser Notification API doesn't work here
const isAndroidWebView = () => {
  const ua = navigator.userAgent || '';
  return /Android/i.test(ua) && (/wv/i.test(ua) || /WebView/i.test(ua) || /Version\/\d+\.\d+/i.test(ua));
};

// Check if browser notifications are usable (not in WebView)
const canUseBrowserNotifications = () => {
  try {
    return 'Notification' in window && typeof Notification === 'function' && !isAndroidWebView();
  } catch {
    return false;
  }
};

const subtractMinutes = (timeStr, mins) => {
  const [h, m] = timeStr.split(':').map(Number);
  const total = h * 60 + m - mins;
  if (total < 0) return null;
  return `${String(Math.floor(total / 60)).padStart(2, '0')}:${String(total % 60).padStart(2, '0')}`;
};

// Request notification permission
export const requestNotificationPermission = async () => {
  if (canUseBrowserNotifications()) {
    if (Notification.permission === 'granted') return true;
    if (Notification.permission === 'denied') return false;
    try {
      const result = await Notification.requestPermission();
      return result === 'granted';
    } catch {
      return false;
    }
  }
  // In Android WebView, in-app notifications always work without permission
  return true;
};

export const hasNotificationPermission = () => {
  if (canUseBrowserNotifications()) {
    return Notification.permission === 'granted';
  }
  // In-app notifications work without browser permission
  return true;
};

// Show in-app notification (works everywhere including Android WebView)
const showInAppNotification = (title, body, type) => {
  window.dispatchEvent(new CustomEvent('nur-in-app-notification', {
    detail: { title, body, type }
  }));
};

// Show notification — always shows in-app banner; also tries browser Notification in PWA mode
export const showNotification = (title, body, tag) => {
  // Always show in-app notification (works in Android WebView)
  showInAppNotification(title, body, tag);

  // Vibrate on Android for haptic feedback
  if (navigator.vibrate) {
    const pattern = tag?.startsWith('adhan') ? [200, 100, 200, 100, 200] : 200;
    navigator.vibrate(pattern);
  }

  // Try browser notification if available (PWA mode, not WebView)
  if (canUseBrowserNotifications() && Notification.permission === 'granted') {
    try {
      new Notification(title, {
        body,
        tag: tag || 'nur-app',
        icon: 'https://media.base44.com/images/public/6a7e4939cdecab29c6f44cf5/1415dfccc_generated_image.png',
      });
    } catch { /* */ }
  }
};

// Play Adhan sound
export const playAdhan = () => {
  const adhanUrl = 'https://cdn.islamic.network/cdn/azan/audio/adhan_makkah.mp3';
  audioManager.play('adhan', adhanUrl, {
    title: 'الأذان',
    artist: 'مكة',
  }, ['https://www.islamcan.com/audio/adhan/azan1.mp3']);
};

// Start watching prayer times — shows in-app notification + plays Adhan when prayer time arrives
export const startPrayerWatcher = (prayerTimes) => {
  stopPrayerWatcher();
  if (!prayerTimes) return;

  const prayers = [
    { key: 'Fajr', name: 'الفجر', time: prayerTimes.Fajr },
    { key: 'Dhuhr', name: 'الظهر', time: prayerTimes.Dhuhr },
    { key: 'Asr', name: 'العصر', time: prayerTimes.Asr },
    { key: 'Maghrib', name: 'المغرب', time: prayerTimes.Maghrib },
    { key: 'Isha', name: 'العشاء', time: prayerTimes.Isha },
  ];

  const check = () => {
    const now = new Date();
    const nowStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    const todayKey = `${now.toDateString()}`;

    // Prayer adhan notifications
    for (const p of prayers) {
      if (!p.time) continue;
      const pTime = p.time.slice(0, 5);
      if (nowStr === pTime && lastNotifiedPrayer !== `${todayKey}-${p.key}`) {
        lastNotifiedPrayer = `${todayKey}-${p.key}`;
        const adhanEnabled = localStorage.getItem('nur_adhan_notifications') !== 'false';
        if (adhanEnabled) {
          playAdhan();
          showNotification(`حان وقت صلاة ${p.name}`, `الله أكبر، حان وقت صلاة ${p.name}`, `adhan-${p.key}`);
        }
      }
    }

    // Athkar reminders (15 min before Fajr and Maghrib)
    const reminderEnabled = localStorage.getItem('nur_reminder_notifications') !== 'false';
    if (reminderEnabled) {
      const morningAthkarTime = prayers[0].time ? subtractMinutes(prayers[0].time.slice(0, 5), 15) : null;
      if (morningAthkarTime && nowStr === morningAthkarTime && lastNotifiedAthkar !== `${todayKey}-morning`) {
        lastNotifiedAthkar = `${todayKey}-morning`;
        showNotification('أذكار الصباح', 'لا تنسَ أذكار الصباح 🌅', 'athkar-morning');
      }
      const eveningAthkarTime = prayers[3].time ? subtractMinutes(prayers[3].time.slice(0, 5), 15) : null;
      if (eveningAthkarTime && nowStr === eveningAthkarTime && lastNotifiedAthkar !== `${todayKey}-evening`) {
        lastNotifiedAthkar = `${todayKey}-evening`;
        showNotification('أذكار المساء', 'لا تنسَ أذكار المساء 🌙', 'athkar-evening');
      }

      // Daily content reminders
      if (nowStr === '08:00' && lastNotifiedDaily !== `${todayKey}-verse`) {
        lastNotifiedDaily = `${todayKey}-verse`;
        showNotification('آية اليوم', 'اقرأ آية اليوم وتدبرها 📖', 'daily-verse');
      }
      if (nowStr === '09:00' && lastNotifiedDaily !== `${todayKey}-hadith`) {
        lastNotifiedDaily = `${todayKey}-hadith`;
        showNotification('حديث اليوم', 'اقرأ حديث اليوم وطبقه ✨', 'daily-hadith');
      }
    }
  };

  check();
  prayerWatcherInterval = setInterval(check, 30000);
};

export const stopPrayerWatcher = () => {
  if (prayerWatcherInterval) {
    clearInterval(prayerWatcherInterval);
    prayerWatcherInterval = null;
  }
};

// Start periodic reminder notifications
export const startReminders = () => {
  stopReminders();
  const reminderEnabled = localStorage.getItem('nur_reminder_notifications') !== 'false';
  if (!reminderEnabled) return;

  const showReminder = () => {
    const reminders = [
      { title: 'نور', body: 'لا تنسَ أذكارك اليوم 🌙' },
      { title: 'نور', body: 'وقت لقراءة القرآن 📖' },
      { title: 'نور', body: 'تذكير: صلّ على النبي ﷺ' },
      { title: 'نور', body: 'لا تنسَ ذكر الله تعالى 🤲' },
      { title: 'نور', body: 'كيف حالك مع الله اليوم؟ ✨' },
    ];
    const r = reminders[Math.floor(Math.random() * reminders.length)];
    showNotification(r.title, r.body, 'reminder');
  };

  reminderInterval = setInterval(showReminder, 4 * 60 * 60 * 1000);
};

export const stopReminders = () => {
  if (reminderInterval) {
    clearInterval(reminderInterval);
    reminderInterval = null;
  }
};

// Initialize notifications on app start
export const initNotifications = async () => {
  const adhanEnabled = localStorage.getItem('nur_adhan_notifications') !== 'false';
  const reminderEnabled = localStorage.getItem('nur_reminder_notifications') !== 'false';

  if (adhanEnabled || reminderEnabled) {
    await requestNotificationPermission();
  }
  if (reminderEnabled) {
    startReminders();
  }
};

// Open app in external system browser for proper notification support (fallback for WebView)
export const openInBrowserForNotifications = () => {
  const url = window.location.origin + '/?enable_notifications=1';
  try {
    const host = url.replace(/^https?:\/\//, '');
    window.location.href = `intent://${host}#Intent;scheme=https;end`;
  } catch {
    window.open(url, '_blank');
  }
};
