import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Sparkles, ChevronRight } from 'lucide-react';
import { getHijriDate, toArabicNumber } from '@/lib/islamicUtils';
import { occasions, hijriMonths, hijriMonthDays } from '@/data/occasions';

export default function CalendarPage() {
  const navigate = useNavigate();
  const [hijriDate, setHijriDate] = useState(null);

  useEffect(() => {
    getHijriDate().then(setHijriDate);
  }, []);

  const currentDay = hijriDate ? parseInt(hijriDate.day) : new Date().getDate();
  const currentMonth = hijriDate ? hijriMonths.findIndex(m => hijriDate.month.includes(m)) : 0;
  const currentYear = hijriDate ? hijriDate.year : '1448';
  const daysInMonth = hijriMonthDays[currentMonth] || 29;

  return (
    <div className="space-y-4 animate-fade-in">
      <h2 className="text-xl font-bold text-gold text-center">التقويم الهجري</h2>

      {hijriDate && (
        <div className="glass-card rounded-2xl p-4 text-center">
          <p className="text-xs text-muted-foreground">اليوم</p>
          <p className="text-2xl font-bold text-gold mt-1">{hijriDate.full}</p>
          <p className="text-xs text-muted-foreground mt-1">{hijriDate.weekday}</p>
        </div>
      )}

      <div className="glass-card rounded-2xl p-4">
        <p className="text-sm font-bold text-foreground text-center mb-3">
          {hijriMonths[currentMonth]} {toArabicNumber(currentYear)} هـ
        </p>
        <div className="grid grid-cols-7 gap-1 mb-2">
          {['الأحد', 'الإثنين', 'الثلاثاء', 'الأربعاء', 'الخميس', 'الجمعة', 'السبت'].map(d => (
            <p key={d} className="text-[10px] text-muted-foreground text-center">{d.slice(0, 3)}</p>
          ))}
        </div>
        <div className="grid grid-cols-7 gap-1">
          {Array.from({ length: daysInMonth }, (_, i) => {
            const day = i + 1;
            const isToday = day === currentDay;
            return (
              <div key={day} className={`aspect-square rounded-lg flex items-center justify-center text-xs transition-all ${
                isToday ? 'bg-gold text-primary-foreground font-bold' : 'text-foreground hover:bg-gold/5'
              }`}>
                {toArabicNumber(day)}
              </div>
            );
          })}
        </div>
      </div>

      <div>
        <h3 className="text-sm font-bold text-foreground mb-2 px-1">المناسبات القادمة</h3>
        <div className="space-y-2">
          {occasions.map(occ => (
            <div key={occ.id} className="glass-card rounded-2xl p-3 flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-gold/10 flex items-center justify-center shrink-0">
                <Sparkles className="w-4 h-4 text-gold" />
              </div>
              <div className="flex-1">
                <p className="text-sm font-bold text-foreground">{occ.name}</p>
                <p className="text-xs text-muted-foreground">{occ.subtitle}</p>
              </div>
              <div className="text-left">
                <p className="text-xs text-gold">{occ.hijriDate}</p>
                <p className="text-[10px] text-muted-foreground">{occ.gregorianApprox}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
