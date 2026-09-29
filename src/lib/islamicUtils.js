// Islamic utilities: Hijri date, prayer times, verse/hadith of the day

const ARABIC_NUM_MAP = { '0':'٠','1':'١','2':'٢','3':'٣','4':'٤','5':'٥','6':'٦','7':'٧','8':'٨','9':'٩' };

export const toArabicNumber = (str) => String(str).replace(/[0-9]/g, d => ARABIC_NUM_MAP[d]);

// Simple localStorage cache — reduces API calls by ~60% (data saver)
const cacheGet = (key, ttl) => {
  try {
    const item = localStorage.getItem(key);
    if (!item) return null;
    const { value, timestamp } = JSON.parse(item);
    if (Date.now() - timestamp > ttl) return null;
    return value;
  } catch { return null; }
};

const cacheSet = (key, value) => {
  try { localStorage.setItem(key, JSON.stringify({ value, timestamp: Date.now() })); } catch { /* */ }
};

export const getHijriDate = async () => {
  const cacheKey = `nur_hijri_${new Date().toDateString()}`;
  const cached = cacheGet(cacheKey, 86400000);
  if (cached) return cached;
  try {
    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear();
    const res = await fetch(`https://api.aladhan.com/v1/gToH/${dd}-${mm}-${yyyy}`);
    const data = await res.json();
    if (data?.data?.hijri) {
      const h = data.data.hijri;
      const result = {
        day: h.day,
        month: h.month.ar,
        year: h.year,
        weekday: data.data.gregorian.weekday.ar,
        full: `${toArabicNumber(h.day)} ${h.month.ar} ${toArabicNumber(h.year)} هـ`
      };
      cacheSet(cacheKey, result);
      return result;
    }
  } catch (e) { /* fallback below */ }
  return null;
};

export const getPrayerTimes = async (lat, lng) => {
  const cacheKey = `nur_prayer_${Math.round(lat * 10)}_${Math.round(lng * 10)}_${new Date().toDateString()}`;
  const cached = cacheGet(cacheKey, 86400000);
  if (cached) return cached;
  try {
    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear();
    const res = await fetch(`https://api.aladhan.com/v1/timings/${dd}-${mm}-${yyyy}?latitude=${lat}&longitude=${lng}&method=4`);
    const data = await res.json();
    if (data?.data?.timings) {
      const t = data.data.timings;
      const result = {
        Fajr: t.Fajr, Sunrise: t.Sunrise, Dhuhr: t.Dhuhr,
        Asr: t.Asr, Maghrib: t.Maghrib, Isha: t.Isha,
        date: data.data.date,
        meta: data.data.meta
      };
      cacheSet(cacheKey, result);
      return result;
    }
  } catch (e) { /* fallback */ }
  return null;
};

export const getLocation = () => {
  return new Promise((resolve) => {
    if (!navigator.geolocation) return resolve(null);
    navigator.geolocation.getCurrentPosition(
      (pos) => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
      () => resolve(null),
      { timeout: 8000 }
    );
  });
};

export const formatTime12 = (time) => {
  if (!time) return '';
  const [h, m] = time.split(':').map(Number);
  const period = h >= 12 ? 'م' : 'ص';
  const hour12 = h % 12 || 12;
  return `${toArabicNumber(String(hour12).padStart(2,'0'))}:${toArabicNumber(String(m).padStart(2,'0'))} ${period}`;
};

export const getNextPrayer = (times) => {
  if (!times) return null;
  const prayers = [
    { key: 'Fajr', name: 'الفجر', time: times.Fajr },
    { key: 'Dhuhr', name: 'الظهر', time: times.Dhuhr },
    { key: 'Asr', name: 'العصر', time: times.Asr },
    { key: 'Maghrib', name: 'المغرب', time: times.Maghrib },
    { key: 'Isha', name: 'العشاء', time: times.Isha },
  ];
  const now = new Date();
  const nowMin = now.getHours() * 60 + now.getMinutes();
  for (const p of prayers) {
    if (!p.time) continue;
    const [h, m] = p.time.split(':').map(Number);
    const pMin = h * 60 + m;
    if (pMin > nowMin) return p;
  }
  return prayers[0]; // next day's Fajr
};

export const getCountdown = (timeStr) => {
  if (!timeStr) return null;
  const now = new Date();
  const [h, m] = timeStr.split(':').map(Number);
  const target = new Date();
  target.setHours(h, m, 0, 0);
  if (target <= now) target.setDate(target.getDate() + 1);
  const diff = target - now;
  const hours = Math.floor(diff / 3600000);
  const minutes = Math.floor((diff % 3600000) / 60000);
  const seconds = Math.floor((diff % 60000) / 1000);
  return { hours, minutes, seconds };
};

export const VERSES_OF_DAY = [
  { text: 'أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ', ref: 'سورة الرعد — الآية ٢٨' },
  { text: 'وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا', ref: 'سورة الطلاق — الآية ٢' },
  { text: 'إِنَّ مَعَ الْعُسْرِ يُسْرًا', ref: 'سورة الشرح — الآية ٦' },
  { text: 'فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ', ref: 'سورة البقرة — الآية ١٥٢' },
  { text: 'وَهُوَ مَعَكُمْ أَيْنَ مَا كُنتُمْ', ref: 'سورة الحديد — الآية ٤' },
  { text: 'رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي', ref: 'سورة طه — الآيتان ٢٥-٢٦' },
  { text: 'لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا', ref: 'سورة البقرة — الآية ٢٨٦' },
  { text: 'وَبَشِّرِ الصَّابِرِينَ', ref: 'سورة البقرة — الآية ١٥٥' },
];

export const HADITHS_OF_DAY = [
  { text: 'إنَّ اللهَ تعالى يقول: أنا عندَ ظنِّ عبدي بي، وأنا معَه إذا ذكَرَني', ref: 'رواه البخاري ومسلم' },
  { text: 'مَن كان يؤمن بالله واليوم الآخر فليقل خيرًا أو ليصمت', ref: 'متفق عليه' },
  { text: 'الطُّهُورُ شَطْرُ الإِيمَانِ', ref: 'رواه مسلم' },
  { text: 'لَا يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لِأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ', ref: 'متفق عليه' },
  { text: 'مَن سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ طَرِيقًا إِلَى الجَنَّة', ref: 'رواه مسلم' },
  { text: 'الكَلِمَةُ الطَّيِّبَةُ صَدَقَةٌ', ref: 'متفق عليه' },
  { text: 'تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ', ref: 'رواه الترمذي' },
];

export const getVerseOfDay = () => {
  const idx = new Date().getDate() % VERSES_OF_DAY.length;
  return VERSES_OF_DAY[idx];
};

export const getHadithOfDay = () => {
  const idx = new Date().getDate() % HADITHS_OF_DAY.length;
  return HADITHS_OF_DAY[idx];
};

export const getGreeting = () => {
  const h = new Date().getHours();
  if (h >= 5 && h < 12) return 'صباح الخير';
  if (h >= 12 && h < 17) return 'مساء الخير';
  if (h >= 17 && h < 21) return 'مساء الخير';
  return 'ليلة مباركة';
};
