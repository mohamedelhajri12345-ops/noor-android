import React, { useState, useEffect } from 'react';
import { Sparkles, Check, X, RotateCcw, Trophy, BookOpen, Brain } from 'lucide-react';
import { namesOfAllah } from '@/data/namesOfAllah';
import { toArabicNumber } from '@/lib/islamicUtils';

const STORAGE_KEY = 'nur_names_learned';

export default function NamesOfAllah() {
  const [mode, setMode] = useState('browse'); // browse | quiz
  const [learned, setLearned] = useState(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      return saved ? JSON.parse(saved) : [];
    } catch { return []; }
  });

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(learned));
  }, [learned]);

  const toggleLearned = (idx) => {
    setLearned(l => l.includes(idx) ? l.filter(i => i !== idx) : [...l, idx]);
  };

  const percentage = Math.round((learned.length / namesOfAllah.length) * 100);

  if (mode === 'quiz') {
    return <QuizMode onBack={() => setMode('browse')} learned={learned} />;
  }

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="text-center">
        <h3 className="text-lg font-bold text-gold">الأسماء الحسنى</h3>
        <p className="text-xs text-muted-foreground mt-1">{toArabicNumber(namesOfAllah.length)} اسم من أسماء الله الحسنى</p>
      </div>

      {/* Progress */}
      <div className="glass-card rounded-2xl p-4">
        <div className="flex items-center justify-between mb-2">
          <p className="text-sm font-bold text-foreground">الأسماء المتعلمة</p>
          <p className="text-sm text-gold">{toArabicNumber(learned.length)}/{toArabicNumber(namesOfAllah.length)}</p>
        </div>
        <div className="h-2.5 rounded-full bg-muted overflow-hidden mb-3">
          <div className="h-full bg-gold-gradient transition-all" style={{ width: `${percentage}%` }} />
        </div>
        <button onClick={() => setMode('quiz')} className="w-full bg-gold-gradient text-primary-foreground rounded-xl py-2.5 text-sm font-bold flex items-center justify-center gap-2">
          <Brain className="w-4 h-4" /> ابدأ اختبار الأسماء
        </button>
      </div>

      {/* Names list */}
      <div className="space-y-2 max-h-96 overflow-y-auto scrollbar-hide">
        {namesOfAllah.map((item, i) => {
          const isLearned = learned.includes(i);
          return (
            <div key={i} className={`glass-card rounded-xl p-3 flex items-center gap-3 ${isLearned ? 'border-gold/30' : ''}`}>
              <div className="w-9 h-9 rounded-lg bg-gold/10 flex items-center justify-center shrink-0">
                <span className="text-xs font-bold text-gold">{toArabicNumber(i + 1)}</span>
              </div>
              <div className="flex-1">
                <p className="text-base font-bold text-gold arabic-text">{item.name}</p>
                <p className="text-xs text-muted-foreground">{item.meaning}</p>
              </div>
              <button onClick={() => toggleLearned(i)} className={`w-8 h-8 rounded-full flex items-center justify-center transition-all ${isLearned ? 'bg-gold/20 text-gold' : 'glass text-muted-foreground'}`}>
                {isLearned ? <Check className="w-4 h-4" /> : <Sparkles className="w-4 h-4" />}
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function QuizMode({ onBack, learned }) {
  const [pool, setPool] = useState([]);
  const [idx, setIdx] = useState(0);
  const [selected, setSelected] = useState(null);
  const [score, setScore] = useState(0);
  const [finished, setFinished] = useState(false);

  useEffect(() => {
    const shuffled = [...namesOfAllah].sort(() => Math.random() - 0.5).slice(0, 20);
    setPool(shuffled);
  }, []);

  if (pool.length === 0) return null;

  const q = pool[idx];
  if (!q) return null;

  // Generate 3 wrong options
  const wrongOptions = namesOfAllah.filter(n => n.name !== q.name).sort(() => Math.random() - 0.5).slice(0, 3);
  const options = [q, ...wrongOptions].sort(() => Math.random() - 0.5);

  const answer = (name) => {
    if (selected !== null) return;
    setSelected(name);
    if (name === q.name) setScore(s => s + 1);
    setTimeout(() => {
      if (idx + 1 >= pool.length) setFinished(true);
      else { setIdx(i => i + 1); setSelected(null); }
    }, 1200);
  };

  if (finished) {
    return (
      <div className="space-y-4 text-center animate-fade-in">
        <Trophy className="w-12 h-12 text-gold mx-auto mt-8" />
        <h3 className="text-lg font-bold text-gold">انتهى الاختبار!</h3>
        <p className="text-3xl font-bold text-foreground">{toArabicNumber(score)}/{toArabicNumber(pool.length)}</p>
        <button onClick={onBack} className="w-full bg-gold-gradient text-primary-foreground rounded-2xl py-3 font-bold">
          رجوع
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-4 animate-fade-in">
      <button onClick={onBack} className="text-muted-foreground hover:text-foreground text-sm">→ رجوع</button>
      <div className="flex items-center justify-between">
        <p className="text-xs text-muted-foreground">السؤال {toArabicNumber(idx + 1)}/{toArabicNumber(pool.length)}</p>
        <p className="text-xs text-gold">النتيجة: {toArabicNumber(score)}</p>
      </div>

      <div className="glass-card rounded-2xl p-5 text-center">
        <p className="text-xs text-muted-foreground mb-2">ما معنى اسم الله:</p>
        <p className="text-3xl font-bold text-gold arabic-text">{q.name}</p>
      </div>

      <div className="space-y-2">
        {options.map((opt, i) => {
          const isCorrect = opt.name === q.name;
          const isSelected = selected === opt.name;
          let style = 'glass-card text-foreground hover:glass-hover active:scale-95';
          if (selected !== null) {
            if (isCorrect) style = 'bg-gold/20 text-gold border border-gold/40';
            else if (isSelected) style = 'bg-destructive/20 text-destructive';
            else style = 'glass opacity-50';
          }
          return (
            <button key={i} onClick={() => answer(opt.name)} disabled={selected !== null}
              className={`w-full rounded-xl p-3 text-sm text-right transition-all ${style}`}>
              <div className="flex items-center justify-between">
                <span>{opt.meaning}</span>
                {selected !== null && isCorrect && <Check className="w-4 h-4" />}
                {selected !== null && isSelected && !isCorrect && <X className="w-4 h-4" />}
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
}
