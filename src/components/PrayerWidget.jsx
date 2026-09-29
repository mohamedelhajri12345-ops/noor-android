import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getPrayerTimes, getNextPrayer, getCountdown, formatTime12, toArabicNumber, getHijriDate, getGreeting } from '@/lib/islamicUtils';

const HERO_IMG = 'https://media.base44.com/images/public/6a833faeb9e42cca9a6576fa/edd00f95f_generated_image.png';

// Hero prayer widget — mosque image background with greeting, hijri date,
// and prayer info all overlaid inside the image
export default function PrayerWidget() {
  const [nextPrayer, setNextPrayer] = useState(null);
  const [countdown, setCountdown] = useState(null);
  const [hijriDate, setHijriDate] = useState(null);
  const greeting = getGreeting();

  useEffect(() => {
    (async () => {
      const saved = localStorage.getItem('nur_selected_city');
      const loc = saved ? JSON.parse(saved) : { name: 'مكة المكرمة', lat: 21.3891, lng: 39.8579 };
      const t = await getPrayerTimes(loc.lat, loc.lng);
      if (t) setNextPrayer(getNextPrayer(t));
      const hd = await getHijriDate();
      setHijriDate(hd);
    })();
  }, []);

  useEffect(() => {
    if (!nextPrayer) return;
    const update = () => setCountdown(getCountdown(nextPrayer.time));
    update();
    const interval = setInterval(update, 1000);
    return () => clearInterval(interval);
  }, [nextPrayer]);

  return (
    <Link to="/prayer" className="block relative rounded-2xl overflow-hidden pressable">
      {/* Background mosque image — wide, fills width */}
      <img src={HERO_IMG} alt="" className="w-full h-56 object-cover" />

      {/* Gradient overlay — darker at top & bottom for text, clear in middle for mosque */}
      <div className="absolute inset-0" style={{
        background: 'linear-gradient(180deg, hsl(216 33% 6% / 0.78) 0%, hsl(216 33% 6% / 0.25) 38%, hsl(216 33% 6% / 0.72) 100%)'
      }} />

      {/* Content overlaid inside the image */}
      <div className="absolute inset-0 p-4 flex flex-col justify-between">
        {/* Top — greeting + hijri date */}
        <div className="text-center z-10">
          <p className="text-lg font-bold text-gold" style={{ fontFamily: "'Amiri', 'Cairo', serif", textShadow: '0 2px 8px rgba(0,0,0,0.8)' }}>
            {greeting}
          </p>
          {hijriDate && (
            <p className="text-[11px] text-gold/80 mt-0.5 font-semibold" style={{ textShadow: '0 1px 4px rgba(0,0,0,0.7)' }}>
              {hijriDate.full}
            </p>
          )}
        </div>

        {/* Bottom — countdown (left) + prayer name (right) */}
        <div className="flex items-end justify-between">
          {/* Countdown — left side */}
          <div className="text-left z-10">
            {countdown ? (
              <>
                <p className="text-2xl font-mono text-foreground leading-none" style={{ textShadow: '0 2px 8px rgba(0,0,0,0.8)' }}>
                  {toArabicNumber(String(countdown.hours).padStart(2, '0'))}:{toArabicNumber(String(countdown.minutes).padStart(2, '0'))}:{toArabicNumber(String(countdown.seconds).padStart(2, '0'))}
                </p>
                <p className="text-[10px] text-gold/80 mt-1.5" style={{ textShadow: '0 1px 4px rgba(0,0,0,0.7)' }}>الوقت المتبقي</p>
              </>
            ) : (
              <p className="text-sm text-muted-foreground">...</p>
            )}
          </div>
          {/* Prayer name — right side (RTL) */}
          <div className="text-right z-10">
            <p className="text-[10px] text-gold/80 font-semibold" style={{ textShadow: '0 1px 4px rgba(0,0,0,0.7)' }}>الصلاة القادمة</p>
            <p className="text-lg font-bold text-gold mt-0.5" style={{ fontFamily: "'Amiri', 'Cairo', serif", textShadow: '0 2px 8px rgba(0,0,0,0.8)' }}>
              {nextPrayer ? nextPrayer.name : '...'}
            </p>
            <p className="text-xs text-foreground/90" style={{ textShadow: '0 1px 4px rgba(0,0,0,0.7)' }}>
              {nextPrayer ? formatTime12(nextPrayer.time) : ''}
            </p>
          </div>
        </div>
      </div>
    </Link>
  );
}
