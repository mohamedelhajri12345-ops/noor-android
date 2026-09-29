// الأناشيد الإسلامية — محتوى حلال بدون موسيقى (بصوت فقط)
// Sources: archive.org verified collections (free, public)
// Collections: arabia-nasheeds-karisik, mixvocalonlynasheeds, NASHEEDNEW_201803, nasheeds2021

const arc = (id, file) => `https://archive.org/download/${id}/${encodeURIComponent(file)}`;
const AR = 'arabia-nasheeds-karisik';
const MIX = 'mixvocalonlynasheeds';
const NEW = 'NASHEEDNEW_201803';
const N2021 = 'nasheeds2021';

export const nasheedCategories = [
  { id: 'all', name: 'الكل' },
  { id: 'prophet', name: 'عن النبي ﷺ' },
  { id: 'religious', name: 'دينية' },
  { id: 'arabic', name: 'عربية' },
  { id: 'english', name: 'إنجليزية' },
  { id: 'occasions', name: 'مناسبات' },
];

export const nasheeds = [
  // ═══════════ أناشيد عن النبي ﷺ ═══════════
  { id: 'p1', title: 'يا نبي سلام عليك', artist: 'بصوت فقط', duration: '4:00', category: 'prophet', url: arc(NEW, 'Ya Nabi Salam Alayka (Arabic - Vocals Only Versio).mp3') },
  { id: 'p2', title: 'يا نبي سلام عليك', artist: 'بصوت فقط', duration: '3:30', category: 'prophet', url: arc(MIX, 'Ya Nabi Salam Alayka (Arabic - Vocals Only Versio).mp3') },
  { id: 'p3', title: 'المصطفى', artist: 'Maher Zain', duration: '3:45', category: 'prophet', url: arc(NEW, 'The Chosen One (Vocals Only Versio).mp3') },
  { id: 'p4', title: 'محمد ﷺ', artist: 'بصوت فقط', duration: '4:20', category: 'prophet', url: arc(NEW, 'Muhammad(pbuh) - Vocal.mp3') },
  { id: 'p5', title: 'الحبيب', artist: 'بصوت فقط', duration: '7:24', category: 'prophet', url: arc(MIX, 'Al-Habib.mp3') },
  { id: 'p6', title: 'يا نبي سلام عليك', artist: 'منشد', duration: '3:00', category: 'prophet', url: arc(AR, '10__Ya_nabi_salam_Alayka.mp3') },

  // ═══════════ أناشيد دينية ═══════════
  { id: 'r1', title: 'يا الله', artist: 'منشد', duration: '4:10', category: 'religious', url: arc(AR, '04__Arabic__01__Ya_Allah.mp3') },
  { id: 'r2', title: 'سبحان الله', artist: 'Maher Zain', duration: '3:20', category: 'religious', url: arc(NEW, 'Subhana Allah (Vocals Only Versio).mp3') },
  { id: 'r3', title: 'اشكر الله', artist: 'Maher Zain', duration: '3:50', category: 'religious', url: arc(NEW, 'Thank You Allah (Vocals Only Versio).mp3') },
  { id: 'r4', title: 'إن شاء الله', artist: 'Maher Zain', duration: '4:00', category: 'religious', url: arc(NEW, 'Insha Allah (Arabic - Vocals Only Versio).mp3') },
  { id: 'r5', title: 'الله الله', artist: 'بصوت فقط', duration: '3:30', category: 'religious', url: arc(NEW, 'Allahi Allah Kiya Karo (Vocals Only Versio).mp3') },
  { id: 'r6', title: 'اللهم', artist: 'منشد', duration: '1:10', category: 'religious', url: arc(AR, 'Allahumma.mp3') },
  { id: 'r7', title: 'افتح عينيك', artist: 'Maher Zain', duration: '3:40', category: 'religious', url: arc(NEW, 'Open Your Eyes (Vocals Only Versio).mp3') },
  { id: 'r8', title: 'كن دائماً هناك', artist: 'Maher Zain', duration: '3:25', category: 'religious', url: arc(NEW, 'Always Be There (Vocals Only Versio).mp3') },
  { id: 'r9', title: 'ادعُ الله', artist: 'Muhabbat', duration: '3:15', category: 'religious', url: arc(N2021, 'Call on Allah by Muhabbat (Vocal Only Nasheed).mp3') },
  { id: 'r10', title: 'يا إلهي', artist: 'Muad', duration: '3:45', category: 'religious', url: arc(N2021, 'MUAD - YA ILAHI (VOCALS ONLY)(1).mp3') },
  { id: 'r11', title: 'نبض القلب', artist: 'Muad', duration: '3:00', category: 'religious', url: arc(N2021, 'MUAD - HEARTBEAT (VOCALS ONLY).mp3') },
  { id: 'r12', title: 'الله أعنّي', artist: 'Omar Esa', duration: '3:30', category: 'religious', url: arc(N2021, 'Omar Esa - Allah Help Me (Official Nasheed Video) - Vocals Only.mp3') },
  { id: 'r13', title: 'سبحان الله', artist: 'منشد', duration: '2:45', category: 'religious', url: arc(AR, 'Ad3ook__01__Sob7aan_Allah.mp3') },
  { id: 'r14', title: 'اشكر الله', artist: 'Muhabbat', duration: '3:20', category: 'religious', url: arc(N2021, 'Thank you Allah by Muhabbat (Vocal Only Nasheed).mp3') },

  // ═══════════ أناشيد عربية ═══════════
  { id: 'a1', title: 'القدس', artist: 'منشد', duration: '1:45', category: 'arabic', url: arc(AR, 'Alquods.mp3') },
  { id: 'a2', title: 'الأنصار', artist: 'منشد', duration: '1:30', category: 'arabic', url: arc(AR, 'Ansaar.MP3') },
  { id: 'a3', title: 'أأثروني', artist: 'منشد', duration: '5:00', category: 'arabic', url: arc(AR, 'Aetheroeny.mp3') },
  { id: 'a4', title: 'النصر الطوالي', artist: 'منشد', duration: '6:20', category: 'arabic', url: arc(AR, 'Alnasar Ul Tuwali.mp3') },
  { id: 'a5', title: 'أسد الأقصى', artist: 'منشد', duration: '3:40', category: 'arabic', url: arc(AR, 'Asad Ul Aqsa.mp3') },
  { id: 'a6', title: 'زيد الوفاء', artist: 'منشد', duration: '2:15', category: 'arabic', url: arc(AR, '1zayed_al_wafaa2.mp3') },
  { id: 'a7', title: 'الله أكبر', artist: 'منشد', duration: '4:20', category: 'arabic', url: arc(AR, 'Allah Allahu Akbar.mp3') },

  // ═══════════ أناشيد إنجليزية ═══════════
  { id: 'e1', title: 'أمتي', artist: 'Maher Zain', duration: '4:10', category: 'english', url: arc(NEW, 'Maher Zain - Ummati (English) - ماهر زين - (Vocals Only - بدون موسيقى) - Official Lyric Video.mp3') },
  { id: 'e2', title: 'المدينة', artist: 'Maher Zain', duration: '3:50', category: 'english', url: arc(NEW, 'Maher Zain - Medina - ماهر زين - (Vocals Only - بدون موسيقى) - Official Lyric Video.mp3') },
  { id: 'e3', title: 'أنا حي', artist: 'Maher Zain & Atif Aslam', duration: '3:30', category: 'english', url: arc(NEW, "Maher Zain & Atif Aslam - I'm Alive - (Vocals Only - بدون موسيقى) - Official Music Video.mp3") },
  { id: 'e4', title: 'تجرؤ على الإيمان', artist: 'Zain Bhikha', duration: '4:00', category: 'english', url: arc(NEW, 'Dare To Believe (Voice-Only) - Zain Bhikha ft. Taariq Uwais Malinga, Aaliyah Kara, Islamic Relif.mp3') },
  { id: 'e5', title: 'استيقظ', artist: 'Maher Zain', duration: '3:20', category: 'english', url: arc(NEW, 'Awaken (Vocals Only Version).mp3') },
  { id: 'e6', title: 'مبارك', artist: 'Ilyas Mao', duration: '3:15', category: 'english', url: arc(NEW, 'Ilyas Mao - Blessed (Vocals-Only Nasheed).mp3') },
  { id: 'e7', title: 'أنت الجديد', artist: 'Ilyas Mao', duration: '3:00', category: 'english', url: arc(NEW, 'Ilyas Mao - New You (Vocals-Only Nasheed).mp3') },
  { id: 'e8', title: 'العالم مشتعل', artist: 'Siedd', duration: '3:25', category: 'english', url: arc(NEW, 'Siedd - World On Fire [Official Nasheed Video] - Vocals Only.mp3') },
  { id: 'e9', title: 'شخصٌ مثلك', artist: 'Siedd', duration: '3:10', category: 'english', url: arc(NEW, 'Siedd - Someone Just Like This (Official Nasheed Video) Vocals Only (2).mp3') },
  { id: 'e10', title: 'حين تَصعُب الأوقات', artist: 'Native Deen', duration: '3:40', category: 'english', url: arc(MIX, 'When Times Are Rough – Voice Only – Native Deen (A. Malik).mp3') },
  { id: 'e11', title: 'لنصلِّ', artist: 'Omar Esa', duration: '3:36', category: 'english', url: arc(MIX, "'Lets Pray' by Omar Esa (@1omaresa).mp3") },
  { id: 'e12', title: 'أغنية لأخي', artist: 'بصوت فقط', duration: '3:06', category: 'english', url: arc(MIX, 'A Song For My Brother Fouseytube.mp3') },
  { id: 'e13', title: 'إن شاء الله', artist: 'Maher Zain', duration: '4:00', category: 'english', url: arc(NEW, 'Insha Allah (English - Vocals Only Versio).mp3') },

  // ═══════════ أناشيد مناسبات ═══════════
  { id: 'o1', title: 'بارك الله لكما', artist: 'Maher Zain', duration: '3:30', category: 'occasions', url: arc(NEW, 'Baraka Allahu Lakuma (Vocals Only Versio).mp3') },
  { id: 'o2', title: 'نشيد الزفاف', artist: 'Omar Esa', duration: '3:45', category: 'occasions', url: arc(MIX, "'The Wedding Nasheed' Official Video - Omar Esa (@1omaresa).mp3") },
  { id: 'o3', title: 'لِبقية حياتي', artist: 'Maher Zain', duration: '4:00', category: 'occasions', url: arc(NEW, 'For the Rest of My Life (Vocals Only Versio).mp3') },
  { id: 'o4', title: 'أمسك يدي', artist: 'Maher Zain', duration: '3:20', category: 'occasions', url: arc(NEW, 'Hold My Hand (Vocals Only Version).mp3') },
];
