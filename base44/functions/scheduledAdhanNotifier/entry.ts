import { sendOneSignalNotification } from "../../shared/oneSignal.js";

// Scheduled adhan notifier — runs every 5 minutes via automation
// Sends OneSignal push notifications at prayer times (works even when app is closed)
// Uses Casablanca coordinates (user timezone: Africa/Casablanca)

const LAT = 33.5731;
const LNG = -7.5898;
const METHOD = 4; // Muslim World League

const PRAYERS = [
  { key: "Fajr", name: "الفجر" },
  { key: "Dhuhr", name: "الظهر" },
  { key: "Asr", name: "العصر" },
  { key: "Maghrib", name: "المغرب" },
  { key: "Isha", name: "العشاء" },
];

// Convert "HH:MM (TZ)" to minutes since midnight
const timeToMinutes = (timeStr) => {
  const clean = String(timeStr).split(" ")[0];
  const [h, m] = clean.split(":").map(Number);
  return h * 60 + m;
};

// Get current time in Africa/Casablanca timezone in minutes since midnight
const getCurrentMinutesInCasablanca = () => {
  const now = new Date();
  const timeStr = new Intl.DateTimeFormat("en-GB", {
    timeZone: "Africa/Casablanca",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).format(now);
  return timeToMinutes(timeStr);
};

export default async function (req) {
  try {
    const today = new Date();
    const day = today.getDate();
    const month = today.getMonth() + 1;
    const year = today.getFullYear();

    // Fetch this month's prayer times from aladhan API
    const apiUrl = `https://api.aladhan.com/v1/calendar/${year}/${month}?latitude=${LAT}&longitude=${LNG}&method=${METHOD}`;
    const res = await fetch(apiUrl);
    const data = await res.json();

    if (!data?.data?.[day - 1]?.timings) {
      return Response.json({ error: "Failed to get prayer times" }, { status: 500 });
    }

    const timings = data.data[day - 1].timings;
    const currentMinutes = getCurrentMinutesInCasablanca();
    const notificationsSent = [];

    // Check each prayer — notify if within 0-4 minutes after prayer time
    for (const prayer of PRAYERS) {
      const prayerTime = timings[prayer.key];
      if (!prayerTime) continue;
      const prayerMinutes = timeToMinutes(prayerTime);
      const diff = currentMinutes - prayerMinutes;

      if (diff >= 0 && diff < 5) {
        await sendOneSignalNotification({
          headings: { en: `Prayer Time: ${prayer.name}`, ar: `حان وقت صلاة ${prayer.name}` },
          contents: { en: "Allah is the Greatest", ar: "الله أكبر الله أكبر" },
          androidSound: "adhan",
        });
        notificationsSent.push(prayer.name);
      }
    }

    // Morning athkar reminder (15 min before Fajr)
    if (timings.Fajr) {
      const fajrMin = timeToMinutes(timings.Fajr);
      const athkarMin = fajrMin - 15;
      const diff = currentMinutes - athkarMin;
      if (diff >= 0 && diff < 5) {
        await sendOneSignalNotification({
          headings: { en: "Morning Athkar", ar: "أذكار الصباح" },
          contents: { en: "Don't forget your morning athkar", ar: "لا تنسَ أذكار الصباح 🌅" },
        });
        notificationsSent.push("morning_athkar");
      }
    }

    // Evening athkar reminder (15 min before Maghrib)
    if (timings.Maghrib) {
      const maghribMin = timeToMinutes(timings.Maghrib);
      const athkarMin = maghribMin - 15;
      const diff = currentMinutes - athkarMin;
      if (diff >= 0 && diff < 5) {
        await sendOneSignalNotification({
          headings: { en: "Evening Athkar", ar: "أذكار المساء" },
          contents: { en: "Don't forget your evening athkar", ar: "لا تنسَ أذكار المساء 🌙" },
        });
        notificationsSent.push("evening_athkar");
      }
    }

    // Daily app usage reminder at 10:00 AM
    if (currentMinutes >= 600 && currentMinutes < 605) {
      await sendOneSignalNotification({
        headings: { en: "Noor App", ar: "نور" },
        contents: { en: "Don't forget your daily athkar", ar: "لا تنسَ أذكارك اليوم 🌙" },
      });
      notificationsSent.push("daily_reminder");
    }

    return Response.json({ success: true, currentMinutes, notificationsSent });
  } catch (error) {
    return Response.json({ error: error.message }, { status: 500 });
  }
}
