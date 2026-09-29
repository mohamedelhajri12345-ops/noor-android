import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { RotateCcw, ChevronRight } from 'lucide-react';
import { tasbihPresets } from '@/data/athkar';
import { toArabicNumber } from '@/lib/islamicUtils';

export default function Tasbih() {
  const navigate = useNavigate();
  const [selectedIdx, setSelectedIdx] = useState(0);
  const [count, setCount] = useState(0);
  const [total, setTotal] = useState(() => parseInt(localStorage.getItem('tasbih_total') || '0'));
  const preset = tasbihPresets[selectedIdx];

  useEffect(() => {
    localStorage.setItem('tasbih_total', String(total));
  }, [total]);

  const increment = () => {
    setCount(c => c + 1);
    setTotal(t => t + 1);
    if (navigator.vibrate) navigator.vibrate(30);
  };

  const reset = () => setCount(0);

  const progress = Math.min((count / preset.target) * 100, 100);
  const rounds = Math.floor(count / preset.target);

  return (
    <div className="space-y-4 animate-fade-in">
      <h2 className="text-xl font-bold text-gold text-center">المسبحة الإلكترونية</h2>

      <div className="flex gap-2 overflow-x-auto scrollbar-hide pb-1">
        {tasbihPresets.map((p, i) => (
          <button key={i} onClick={() => { setSelectedIdx(i); setCount(0); }}
            className={`px-3 py-2 rounded-xl text-xs whitespace-nowrap transition-all ${selectedIdx === i ? 'bg-gold text-primary-foreground font-bold' : 'glass text-foreground'}`}>
            {p.text}
          </button>
        ))}
      </div>

      <div className="glass-card rounded-3xl p-8 text-center">
        <p className="arabic-text text-2xl text-gold mb-6">{preset.text}</p>

        <div className="relative w-48 h-48 mx-auto">
          <svg className="w-full h-full -rotate-90" viewBox="0 0 200 200">
            <circle cx="100" cy="100" r="90" fill="none" stroke="hsl(var(--muted))" strokeWidth="8" />
            <circle cx="100" cy="100" r="90" fill="none" stroke="hsl(var(--gold))" strokeWidth="8"
              strokeDasharray={`${(progress / 100) * 565.48} 565.48`}
              strokeLinecap="round" className="transition-all duration-300" />
          </svg>
          <div className="absolute inset-0 flex flex-col items-center justify-center">
            <p className="text-5xl font-bold text-foreground">{toArabicNumber(count)}</p>
            <p className="text-xs text-muted-foreground mt-1">من {toArabicNumber(preset.target)}</p>
          </div>
        </div>

        <p className="text-sm text-muted-foreground mt-4">الجولات: {toArabicNumber(rounds)} · المجموع: {toArabicNumber(total)}</p>
      </div>

      <button onClick={increment} className="w-full bg-gold-gradient text-primary-foreground rounded-2xl py-6 text-lg font-bold active:scale-95 transition-transform">
        اضغط للعدّ
      </button>

      <button onClick={reset} className="w-full glass-card rounded-2xl py-3 text-sm text-muted-foreground hover:text-foreground flex items-center justify-center gap-2">
        <RotateCcw className="w-4 h-4" /> إعادة العدّ
      </button>
    </div>
  );
}
