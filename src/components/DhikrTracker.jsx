import React, { useState, useEffect } from 'react';
import { RotateCcw, Target, Plus } from 'lucide-react';
import { toArabicNumber } from '@/lib/islamicUtils';

const STORAGE_KEY = 'nur_dhikr_tracker';
const TARGET = 33;

const getMonthKey = () => {
  const d = new Date();
  return `${d.getFullYear()}-${d.getMonth()}`;
};

export default function DhikrTracker() {
  const [data, setData] = useState(() => {
    const today = new Date().toDateString();
    const monthKey = getMonthKey();
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      const parsed = JSON.parse(saved);
      let updated = { ...parsed, date: today, monthKey };
      // Reset daily if new day
      if (parsed.date !== today) updated.dailyCount = 0;
      // Reset weekly & monthly if new month
      if (parsed.monthKey !== monthKey) {
        updated.weeklyCount = 0;
        updated.monthlyCount = 0;
      }
      return { dailyCount: 0, weeklyCount: 0, monthlyCount: 0, ...updated };
    }
    return { dailyCount: 0, weeklyCount: 0, monthlyCount: 0, date: today, monthKey };
  });

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
  }, [data]);

  const increment = () => {
    setData(d => ({ ...d, dailyCount: d.dailyCount + 1, weeklyCount: d.weeklyCount + 1, monthlyCount: (d.monthlyCount || 0) + 1 }));
  };

  const resetDaily = () => {
    setData(d => ({ ...d, dailyCount: 0 }));
  };

  const progress = Math.min((data.dailyCount / TARGET) * 100, 100);

  return (
    <div className="glass-card rounded-2xl p-4">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <Target className="w-4 h-4 text-gold" />
          <h3 className="text-sm font-bold text-foreground">هدف اليوم</h3>
        </div>
        <button onClick={increment} className="w-9 h-9 rounded-full bg-gold-gradient flex items-center justify-center text-primary-foreground active:scale-90 transition-transform" title="تسبيح">
          <Plus className="w-5 h-5" />
        </button>
      </div>

      {/* Orange progress bar — maxes at 33 */}
      <div className="h-2.5 rounded-full bg-muted overflow-hidden mb-2">
        <div className="h-full transition-all duration-300" style={{ width: `${progress}%`, background: 'linear-gradient(90deg, hsl(25 95% 55%), hsl(15 85% 50%))' }} />
      </div>

      {/* Counters row with reset button on the right */}
      <div className="flex items-center gap-2">
        <div className="grid grid-cols-3 gap-2 flex-1">
          <div className="glass rounded-xl p-1.5 text-center">
            <p className="text-[9px] text-muted-foreground">اليوم</p>
            <p className="text-sm font-bold text-foreground">{toArabicNumber(data.dailyCount)}</p>
          </div>
          <div className="glass rounded-xl p-1.5 text-center">
            <p className="text-[9px] text-muted-foreground">أسبوعياً</p>
            <p className="text-sm font-bold text-foreground">{toArabicNumber(data.weeklyCount)}</p>
          </div>
          <div className="glass rounded-xl p-1.5 text-center">
            <p className="text-[9px] text-muted-foreground">شهرياً</p>
            <p className="text-sm font-bold text-foreground">{toArabicNumber(data.monthlyCount || 0)}</p>
          </div>
        </div>
        <button onClick={resetDaily} className="w-9 h-9 rounded-xl glass-card flex items-center justify-center text-muted-foreground hover:text-foreground transition-colors shrink-0" title="إعادة العداد اليومي">
          <RotateCcw className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
