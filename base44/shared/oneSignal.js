import { secrets } from "base44:runtime";

// Shared OneSignal notification sender — used by adhan notification functions
export async function sendOneSignalNotification({ headings, contents, androidSound, data }) {
  const appId = secrets.get("ONESIGNAL_APP_ID");
  const apiKey = secrets.get("ONESIGNAL_REST_API_KEY");
  if (!appId || !apiKey) return { error: "OneSignal not configured" };

  const body = {
    app_id: appId,
    included_segments: ["All"],
    headings,
    contents,
  };

  if (androidSound) {
    body.android_sound = androidSound;
    body.android_vibrate = [200, 100, 200];
    body.android_led_color = "FFD700";
    body.android_accent_color = "FFD700";
  }
  if (data) body.data = data;

  const res = await fetch("https://onesignal.com/api/v1/notifications", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Basic ${apiKey}`,
    },
    body: JSON.stringify(body),
  });

  return await res.json().catch(() => ({}));
}
