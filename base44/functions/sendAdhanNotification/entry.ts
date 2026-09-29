import { sendOneSignalNotification } from "../../shared/oneSignal.js";

export default async function (req) {
  try {
    const body = await req.json().catch(() => ({}));
    const { prayerName } = body;
    if (!prayerName) return Response.json({ error: "prayerName required" }, { status: 400 });

    const result = await sendOneSignalNotification({
      headings: { en: `Prayer Time: ${prayerName}`, ar: `حان وقت صلاة ${prayerName}` },
      contents: { en: "Allah is the Greatest", ar: "الله أكبر الله أكبر" },
      androidSound: "adhan",
    });

    if (result.error) return Response.json({ error: result.error }, { status: 500 });
    return Response.json({ success: true, data: result });
  } catch (error) {
    return Response.json({ error: error.message }, { status: 500 });
  }
}
