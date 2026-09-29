import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Gamepad2, Music, ClipboardCheck, BookOpen, ChevronLeft, Sparkles, Heart, Share2 } from 'lucide-react';
import { getHijriDate, getVerseOfDay, getHadithOfDay, toArabicNumber } from '@/lib/islamicUtils';
import { occasions } from '@/data/occasions';
import { useLanguage } from '@/lib/LanguageContext';
import PullToRefresh from '@/components/PullToRefresh';
import { MosqueIcon, TasbihIcon, AthkarIcon, QuranIcon } from '@/components/IslamicIcons';
import PrayerWidget from '@/components/PrayerWidget';

export default function Home() {
  const [hijriDate, setHijriDate] = useState(null);
  const verse = getVerseOfDay();
  const hadith = getHadithOfDay();
  const { t } = useLanguage();

  useEffect(() => {
    getHijriDate().then(setHijriDate);
  }, []);

  useEffect(() => {
    const removeBranding = () => {
      const selector = 'a[href*="base44"], [class*="base44"], [id*="base44"], [class*="powered"], [class*="made-with"], [class*="badge"]';
      document.querySelectorAll(selector).forEach(el => el.remove());
    };
    removeBranding();
    const interval = setInterval(removeBranding, 1000);
    return () => clearInterval(interval);
  }, []);

  const handleRefresh = useCallback(async () => {
    localStorage.removeItem(`nur_hijri_${new Date().toDateString()}`);
    const date = await getHijriDate();
    setHijriDate(date);
  }, []);

  // Extract ayah number from ref for the circular indicator
  const ayahNumber = verse.ref.match(/الآية\s+(\S+)/)?.[1] || '';

  // Quick access — matches reference image exactly
  const quickAccess = [
    { path: '/prayer', label: 'الصلاة', icon: MosqueIcon },
    { path: '/tasbih', label: 'السبحة', icon: TasbihIcon },
    { path: '/athkar', label: 'الأذكار', icon: AthkarIcon },
    { path: '/quran', label: 'القرآن', icon: QuranIcon },
    { path: '/games', label: 'الألعاب', icon: Gamepad2 },
    { path: '/library', label: 'الأناشيد', icon: Music },
    { path: '/quiz', label: 'الاختبار', icon: ClipboardCheck },
    { path: '/stories', label: 'القصص', icon: BookOpen },
  ];

  return (
    <PullToRefresh onRefresh={handleRefresh}>
      <div className="space-y-5 animate-fade-in">
        {/* Prayer time widget */}
        <PrayerWidget />

        {/* Quick access — 4x2 grid of gold-outlined cards */}
        <div>
          <h3 className="text-sm font-bold text-gold mb-3 px-1 flex items-center gap-2">
            <span className="w-1 h-4 rounded-full bg-gold" />
            {t('home.quick_access')}
          </h3>
          <div className="grid grid-cols-4 gap-2.5">
            {quickAccess.map(item => {
              const Icon = item.icon;
              return (
                <Link key={item.path} to={item.path} className="pressable group">
                  <div className="aspect-square rounded-2xl flex flex-col items-center justify-center gap-1.5 transition-all relative overflow-hidden"
                    style={{
                      border: '1px solid hsl(46 65% 52% / 0.25)',
                      background: 'linear-gradient(180deg, hsl(218 16% 12% / 0.6), hsl(216 20% 8% / 0.5))',
                    }}>
                    <div className="absolute inset-0 opacity-0 group-active:opacity-100 transition-opacity"
                      style={{ background: 'radial-gradient(circle at center, hsl(46 65% 52% / 0.10), transparent 70%)' }} />
                    <Icon className="w-5 h-5 text-gold relative z-10" strokeWidth={1.5} />
                    <span className="text-[10px] text-foreground relative z-10 font-semibold">{item.label}</span>
                  </div>
                </Link>
              );
            })}
          </div>
        </div>

        {/* Verse of the day — with circular ayah number indicator */}
        <div className="luxury-card p-5 text-center">
          <p className="text-xs text-gold mb-2 font-bold tracking-wide">{t('home.verse_of_day')}</p>
          <p className="arabic-text text-lg text-foreground leading-relaxed">
            {verse.text}
            {ayahNumber && (
              <span className="inline-flex items-center justify-center w-7 h-7 rounded-full bg-gold/10 text-gold text-xs mx-1 font-sans align-middle">
                {ayahNumber}
              </span>
            )}
          </p>
          <p className="text-xs text-muted-foreground mt-3">{verse.ref}</p>
          <button onClick={() => navigator.share?.({ text: `${verse.text}\
\
${verse.ref}`, title: 'آية اليوم' }).catch(() => {})} className="mt-3 inline-flex items-center gap-1.5 text-xs text-gold bg-gold/10 rounded-xl px-3 py-1.5 active:scale-95"><Share2 className="w-3.5 h-3.5" /> مشاركة الآية</button>
        </div>

        {/* Hadith of the day — app icon emblem on right */}
        <div className="luxury-card p-5 flex gap-3">
          <img src="https://media.base44.com/images/public/6a833faeb9e42cca9a6576fa/f6fc7be6c_generated_image.png" alt="القرآن الكريم" className="w-14 h-14 rounded-2xl object-cover shrink-0" style={{ boxShadow: '0 0 14px hsl(46 65% 52% / 0.25)' }} />
          <div className="flex-1">
            <p className="text-xs text-gold mb-2 font-bold">الحديث الشريف</p>
            <p className="arabic-text text-base text-foreground leading-relaxed">قال رسول الله ﷺ: {hadith.text}</p>
            <p className="text-xs text-muted-foreground mt-3">{hadith.ref}</p>
          </div>
        </div>

        {/* Upcoming occasions */}
        <div>
          <div className="flex items-center justify-between mb-2 px-1">
            <h3 className="text-sm font-bold text-gold flex items-center gap-2">
              <span className="w-1 h-4 rounded-full bg-gold" />
              {t('home.upcoming_occasions')}
            </h3>
            <Link to="/calendar" className="text-xs text-gold flex items-center gap-1">
              الكل <ChevronLeft className="w-3 h-3" />
            </Link>
          </div>
          <div className="space-y-2">
            {occasions.slice(0, 3).map(occ => (
              <div key={occ.id} className="glass-card pressable rounded-2xl p-3 flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-gold/10 flex items-center justify-center shrink-0">
                  <Sparkles className="w-4 h-4 text-gold" />
                </div>
                <div className="flex-1">
                  <p className="text-sm font-bold text-foreground">{occ.name}</p>
                  <p className="text-xs text-muted-foreground">{occ.subtitle}</p>
                </div>
                <p className="text-xs text-gold">{occ.hijriDate}</p>
              </div>
            ))}
          </div>
        </div>


      </div>
    </PullToRefresh>
  );
}
