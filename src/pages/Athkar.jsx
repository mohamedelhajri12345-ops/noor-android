import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ChevronRight, Check, RotateCcw } from 'lucide-react';
import { athkarCategories, athkar } from '@/data/athkar';
import { toArabicNumber } from '@/lib/islamicUtils';

export default function Athkar() {
  const { categoryId } = useParams();
  const navigate = useNavigate();
  const [counts, setCounts] = useState({});

  const resetAll = () => setCounts({});

  if (categoryId) {
    const items = athkar[categoryId] || [];
    const cat = athkarCategories.find(c => c.id === categoryId);
    return (
      <div className="space-y-4 animate-fade-in">
        <div className="flex items-center justify-end">
          <button onClick={resetAll} className="flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground">
            <RotateCcw className="w-3 h-3" /> إعادة
          </button>
        </div>

        <h2 className="text-xl font-bold text-gold">{cat?.name}</h2>
        <p className="text-xs text-muted-foreground -mt-2">{cat?.subtitle}</p>

        <div className="space-y-3 overscroll-y-contain">
          {items.map((item, i) => {
            const key = `${categoryId}-${i}`;
            const currentCount = counts[key] || 0;
            const done = currentCount >= item.count;
            return (
              <div key={i} className={`glass-card rounded-2xl p-4 transition-all ${done ? 'opacity-50' : ''}`}>
                <p className="arabic-text text-base text-foreground leading-relaxed">{item.text}</p>
                <div className="flex items-center justify-between mt-3">
                  <p className="text-xs text-muted-foreground">{item.ref}</p>
                  <button
                    onClick={() => !done && setCounts(c => ({ ...c, [key]: currentCount + 1 }))}
                    className={`px-4 py-1.5 rounded-xl text-sm font-bold transition-all ${done ? 'glass text-gold' : 'bg-gold-gradient text-primary-foreground active:scale-95'}`}
                  >
                    {done ? <Check className="w-4 h-4" /> : `${toArabicNumber(currentCount)}/${toArabicNumber(item.count)}`}
                  </button>
                </div>
                {item.count > 1 && !done && (
                  <div className="mt-2 h-1 rounded-full bg-muted overflow-hidden">
                    <div className="h-full bg-gold-gradient transition-all" style={{ width: `${(currentCount / item.count) * 100}%` }} />
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4 animate-fade-in">
      <h2 className="text-xl font-bold text-gold pt-2">الأذكار</h2>
      <div className="space-y-3 overscroll-y-contain">
        {athkarCategories.map(cat => (
          <button key={cat.id} onClick={() => navigate(`/athkar/${cat.id}`)} className="w-full glass-card rounded-2xl p-4 flex items-center gap-3 hover:glass-hover transition-all text-right">
            <div className="w-12 h-12 rounded-xl bg-gold/10 flex items-center justify-center shrink-0">
              <span className="text-sm font-bold text-gold">{toArabicNumber(cat.count)}</span>
            </div>
            <div className="flex-1">
              <p className="text-sm font-bold text-foreground">{cat.name}</p>
              <p className="text-xs text-muted-foreground">{cat.subtitle}</p>
            </div>
            <ChevronRight className="w-4 h-4 text-muted-foreground rotate-180" />
          </button>
        ))}
      </div>
    </div>
  );
}
