import React, { useState, useEffect } from 'react';
import { BookOpen, Check, Circle, Clock } from 'lucide-react';
import { surahs } from '@/data/surahs';
import { toArabicNumber } from '@/lib/islamicUtils';

const STORAGE_KEY = 'nur_quran_memorization';

export default function QuranMemorization() {
  const [progress, setProgress] = useState(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      return saved ? JSON.parse(saved) : {};
    } catch { return {}; }
  });

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(progress));
  }, [progress]);

  const cycleStatus = (number) => {
    setProgress(p => {
      const current = p[number] || 'not_started';
      const next = current === 'not_started' ? 'in_progress' : current === 'in_progress' ? 'memorized' : 'not_started';
      return { ...p, [number]: next };
    });
  };

  const stats = {
    memorized: Object.values(progress).filter(s => s === 'memorized').length,
    inProgress: Object.values(progress).filter(s => s === 'in_progress').length,
    notStarted: surahs.length - Object.values(progress).filter(s => s !== 'not_started').length,
  };
  const percentage = Math.round((stats.memorized / surahs.length) * 100);

  const statusConfig = {
    not_started: { icon: Circle, color: 'text-muted-foreground', bg: 'glass-card', label: '' },
    in_progress: { icon: Clock, color: 'text-gold-light', bg: 'bg-gold/10', label: 'قيد الحفظ' },
    memorized: { icon: Check, color: 'text-gold', bg: 'bg-gold/20 border border-gold/30', label: 'محفوظة' },
  };

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="text-center">
        <h3 className="text-lg font-bold text-gold">حفظ القرآن الكريم</h3>
        <p className="text-xs text-muted-foreground mt-1">تابع رحلة حفظك للقرآن الكريم · ١١٤ سورة</p>
      </div>

      {/* Progress overview */}
      <div className="glass-card rounded-2xl p-4">
        <div className="flex items-center justify-between mb-2">
          <p className="text-sm font-bold text-foreground">التقدم الكلي</p>
          <p className="text-sm text-gold">{toArabicNumber(percentage)}٪</p>
        </div>
        <div className="h-2.5 rounded-full bg-muted overflow-hidden mb-3">
          <div className="h-full bg-gold-gradient transition-all" style={{ width: `${percentage}%` }} />
        </div>
        <div className="grid grid-cols-3 gap-2 text-center">
          <div className="glass-card rounded-xl p-2">
            <p className="text-lg font-bold text-gold">{toArabicNumber(stats.memorized)}</p>
            <p className="text-[10px] text-muted-foreground">محفوظة</p>
          </div>
          <div className="glass-card rounded-xl p-2">
            <p className="text-lg font-bold text-gold-light">{toArabicNumber(stats.inProgress)}</p>
            <p className="text-[10px] text-muted-foreground">قيد الحفظ</p>
          </div>
          <div className="glass-card rounded-xl p-2">
            <p className="text-lg font-bold text-muted-foreground">{toArabicNumber(stats.notStarted)}</p>
            <p className="text-[10px] text-muted-foreground">لم تبدأ</p>
          </div>
        </div>
      </div>

      {/* Surah list */}
      <div className="space-y-2 max-h-96 overflow-y-auto scrollbar-hide">
        {surahs.map(surah => {
          const status = progress[surah.number] || 'not_started';
          const config = statusConfig[status];
          const StatusIcon = config.icon;
          return (
            <button
              key={surah.number}
              onClick={() => cycleStatus(surah.number)}
              className={`w-full rounded-xl p-3 flex items-center gap-3 transition-all ${config.bg}`}
            >
              <div className="w-9 h-9 rounded-lg bg-gold/10 flex items-center justify-center shrink-0">
                <span className="text-xs font-bold text-gold">{toArabicNumber(surah.number)}</span>
              </div>
              <div className="flex-1 text-right">
                <p className="text-sm font-bold text-foreground arabic-text">سورة {surah.name}</p>
                <p className="text-[10px] text-muted-foreground">{surah.type} · {toArabicNumber(surah.ayahs)} آية</p>
              </div>
              <StatusIcon className={`w-5 h-5 ${config.color} shrink-0`} />
            </button>
          );
        })}
      </div>
      <p className="text-xs text-muted-foreground text-center">اضغط على السورة لتغيير حالتها</p>
    </div>
  );
}
