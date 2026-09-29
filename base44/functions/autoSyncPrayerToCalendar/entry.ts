import { createClientFromRequest } from "npm:@base44/sdk@0.8.40";

const CONNECTOR_ID = "6a89b87623c194840fcc4eb1";

// Auto-sync prayer times to builder's Google Calendar (BYO_SHARED connection)
// Runs daily via scheduled automation — creates events with native Android reminders
// This ensures Adhan notifications work on APK via Google Calendar's native push

const LAT = 33.5731;
const LNG = -7.5898;
const METHOD = 4; // Muslim World League

const pad = (n) => String(n).padStart(2, "0");

export default async function(req) {
  try {
    const base44 = createClientFromRequest(req);

    // Get builder's Google Calendar connection (BYO_SHARED)
    let accessToken;
    try {
      ({ accessToken } = await base44.asServiceRole.connectors.getConnection("googlecalendar"));
    } catch {
      return Response.json({ error: "Google Calendar not connected", connected: false }, { status: 401 });
    }

    // Get today's prayer times
    const today = new Date();
    const day = today.getDate();
    const month = today.getMonth() + 1;
    const year = today.getFullYear();
    const apiUrl = `https://api.aladhan.com/v1/calendar/${year}/${month}?latitude=${LAT}&longitude=${LNG}&method=${METHOD}`;

    const prayerRes = await fetch(apiUrl);
    const prayerData = await prayerRes.json();

    if (!prayerData?.data?.[day - 1]?.timings) {
      return Response.json({ error: "Failed to get prayer times" }, { status: 500 });
    }

    const dayData = prayerData.data[day - 1];
    const timings = dayData.timings;
    const timezone = dayData?.meta?.timezone || "Africa/Casablanca";

    const prayers = [
      { key: "Fajr", name: "صلاة الفجر" },
      { key: "Dhuhr", name: "صلاة الظهر" },
      { key: "Asr", name: "صلاة العصر" },
      { key: "Maghrib", name: "صلاة المغرب" },
      { key: "Isha", name: "صلاة العشاء" },
    ];

    // Deduplication: check for existing prayer events today
    const timeMin = `${year}-${pad(month)}-${pad(day)}T00:00:00Z`;
    const timeMax = `${year}-${pad(month)}-${pad(day)}T23:59:59Z`;
    const listUrl = `https://www.googleapis.com/calendar/v3/calendars/primary/events?timeMin=${timeMin}&timeMax=${timeMax}&q=${encodeURIComponent("صلاة")}`;
    const listRes = await fetch(listUrl, { headers: { Authorization: `Bearer ${accessToken}` } });
    const listData = await listRes.json().catch(() => ({}));
    const existingSummaries = (listData.items || []).map((e) => e.summary);

    const createdEvents = [];

    for (const prayer of prayers) {
      if (existingSummaries.includes(prayer.name)) {
        createdEvents.push({ prayer: prayer.name, skipped: true });
        continue;
      }

      const time = timings[prayer.key];
      if (!time) continue;

      const [h, m] = time.split(" ")[0].split(":").map(Number);
      const startStr = `${year}-${pad(month)}-${pad(day)}T${pad(h)}:${pad(m)}:00`;
      const endH = h + Math.floor((m + 30) / 60);
      const endM = (m + 30) % 60;
      const endStr = `${year}-${pad(month)}-${pad(day)}T${pad(endH)}:${pad(endM)}:00`;

      const event = {
        summary: prayer.name,
        description: `حان وقت ${prayer.name} — تطبيق نور`,
        start: { dateTime: startStr, timeZone: timezone },
        end: { dateTime: endStr, timeZone: timezone },
        reminders: {
          useDefault: false,
          overrides: [
            { method: "popup", minutes: 10 },
            { method: "popup", minutes: 0 },
          ],
        },
        colorId: "11", // Green
      };

      const response = await fetch("https://www.googleapis.com/calendar/v3/calendars/primary/events", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${accessToken}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(event),
      });

      if (response.ok) {
        const data = await response.json();
        createdEvents.push({ prayer: prayer.name, eventId: data.id });
      }
    }

    return Response.json({
      success: true,
      createdEvents,
      count: createdEvents.filter((e) => !e.skipped).length,
      skipped: createdEvents.filter((e) => e.skipped).length,
    });
  } catch (error) {
    return Response.json({ error: error.message }, { status: 500 });
  }
}
