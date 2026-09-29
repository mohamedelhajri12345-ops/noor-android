import React, { useState, useMemo } from 'react';
import { BookOpen, Flame, Target, TrendingUp, Award, Calendar } from 'lucide-react';
import { toArabicNumber } from '@/lib/islamicUtils';
import TrackerReports from '@/components/quranTracker/TrackerReports';

const TRACKER_KEY = 'nur_quran_tracker';
const getTracker = () => { try { return JSON.parse(localStorage.getItem(TRACKER_KEY) || '{}'); } catch { return {}; } };
const saveTracker = (t) => localStorage.setItem(TRACKER_KEY, JSON.stringify(t));

// Circular progress ring (SVG)
function ProgressRing({ progress, size = 120, stroke = 8 }) {
  const radius = (size - stroke) / 2;
  const circ = 2 * Math.PI * radius;
  const offset = circ - (progress / 100) * circ;
  return (
    <svg width={size} height={size} className="-rotate-90">
      <circle cx={size/2} cy={size/2} r={radius} fill="none" stroke="hsl(46 65% 52% / 0.1)" strokeWidth={stroke} />
      <circle cx={size/2} cy={size/2} r={radius} fill="none" stroke="url(#goldGrad)" strokeWidth={stroke} strokeLinecap="round"
        strokeDasharray={circ} strokeDashoffset={offset} style={{ transition: 'stroke-dashoffset 0.5s ease' }} />
      <defs>
        <linearGradient id="goldGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="hsl(45 90% 70%)" />
          <stop offset="100%" stopColor="hsl(46 65% 52%)" />
        </linearGradient>
      </defs>
    </svg>
  );
}

export default function QuranTracker() {
  const [tracker, setTracker] = useState(getTracker);
  const [view, setView] = useState('tracker');
  const goal = tracker.goal || 5;
  const today = new Date().toDateString();
  const todayPages = tracker[today]?.pages || 0;
  const streak = tracker.streak || 0;

  // Weekly stats
  const weekData = useMemo(() => {
    const days = [];
    let total = 0, bestDay = 0;
    for (let i = 6; i >= 0; i--) {
      const d = new Date(); d.setDate(d.getDate() - i);
      const key = d.toDateString();
      const pages = tracker[key]?.pages || 0;
      total += pages;
      if (pages > bestDay) bestDay = pages;
      days.push({ day: d.toLocaleDateString('ar', { weekday: 'short' }), pages, date: key, completed: tracker[key]?.completed });
    }
    return { days, total, avg: Math.round(total / 7), best: bestDay };
  }, [tracker]);

  const updatePages = (delta) => {
    const t = { ...tracker };
    if (!t[today]) t[today] = { pages: 0 };
    t[today].pages = Math.max(0, (t[today].pages || 0) + delta);
    if (delta > 0 && t[today].pages >= goal && !t[today].completed) { t[today].completed = true; t.streak = (t.streak || 0) + 1; }
    setTracker(t); saveTracker(t);
  };
  const setGoalValue = (g) => { const t = { ...tracker, goal: g }; setTracker(t); saveTracker(t); };

  const progress = goal > 0 ? Math.min(100, (todayPages / goal) * 100) : 0;
  const allTimeTotal = useMemo(() => {
    return Object.keys(tracker).filter(k => k !== 'goal' && k !== 'streak').reduce((sum, k) => sum + (tracker[k]?.pages || 0), 0);
  }, [tracker]);

  // Achievement badges
  const badges = [
    { icon: '🔥', label: '٣ أيام', unlocked: streak >= 3 },
    { icon: '🌟', label: '٧ أيام', unlocked: streak >= 7 },
    { icon: '🏆', label: '٣٠ يوم', unlocked: streak >= 30 },
    { icon: '📚', label: '١٠٠ صفحة', unlocked: allTimeTotal >= 100 },
    { icon: '💎', label: '٥٠٠ صفحة', unlocked: allTimeTotal >= 500 },
    { icon: '👑', label: '٦٠٤ صفحة', unlocked: allTimeTotal >= 604 },
  ];

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="text-center pt-2">
        <div className="w-16 h-16 rounded-3xl bg-gold/10 flex items-center justify-center mx-auto mb-3"><BookOpen className="w-8 h-8 text-gold" /></div>
        <h2 className="text-xl font-bold text-gold">متتبع ورد القرآن</h2>
        <p className="text-xs text-muted-foreground mt-1">تابع تلاوتك اليومية باحترافية</p>
      </div>

      {/* Tab toggle */}
      <div className="flex gap-2">
        <button onClick={() => setView('tracker')} className={`flex-1 py-2 rounded-xl text-sm font-bold ${view === 'tracker' ? 'bg-gold text-primary-foreground' : 'glass text-foreground'}`}>المتتبع</button>
        <button onClick={() => setView('reports')} className={`flex-1 py-2 rounded-xl text-sm font-bold ${view === 'reports' ? 'bg-gold text-primary-foreground' : 'glass text-foreground'}`}>التقارير</button>
      </div>

      {view === 'tracker' ? (
      <>
      {/* Main progress card with circular ring */}
      <div className="glass-card rounded-2xl p-5">
        <div className="flex items-center justify-between mb-4">
          <div className="relative flex items-center justify-center">
            <ProgressRing progress={progress} size={120} stroke={8} />
            <div className="absolute text-center">
              <p className="text-2xl font-bold text-gold">{toArabicNumber(Math.round(progress))}٪</p>
              <p className="text-[10px] text-muted-foreground">{toArabicNumber(todayPages)}/{toArabicNumber(goal)} صفحة</p>
            </div>
          </div>
          <div className="flex flex-col gap-2">
            <div className="flex items-center gap-2 glass rounded-xl px-3 py-2">
              <Flame className="w-5 h-5 text-gold" />
              <div><p className="text-lg font-bold text-gold leading-none">{toArabicNumber(streak)}</p><p className="text-[10px] text-muted-foreground">أيام متتالية</p></div>
            </div>
            <div className="flex items-center gap-2 glass rounded-xl px-3 py-2">
              <BookOpen className="w-5 h-5 text-gold" />
              <div><p className="text-lg font-bold text-gold leading-none">{toArabicNumber(allTimeTotal)}</p><p className="text-[10px] text-muted-foreground">إجمالي الصفحات</p></div>
            </div>
          </div>
        </div>
        <div className="flex items-center justify-center gap-4">
          <button onClick={() => updatePages(-1)} className="w-12 h-12 rounded-xl bg-gold/10 text-gold text-xl font-bold active:scale-90">−</button>
          <button onClick={() => updatePages(1)} className="w-12 h-12 rounded-xl bg-gold/10 text-gold text-xl font-bold active:scale-90">+</button>
          <button onClick={() => updatePages(5)} className="px-3 h-12 rounded-xl bg-gold/10 text-gold text-sm font-bold active:scale-90">+٥</button>
        </div>
        {progress >= 100 && <p className="text-center text-sm text-gold font-bold mt-3">بارك الله فيك! أكملت ورد اليوم ✓</p>}
      </div>

      {/* Weekly stats */}
      <div className="grid grid-cols-3 gap-2">
        <div className="glass-card rounded-2xl p-3 text-center">
          <TrendingUp className="w-5 h-5 text-gold mx-auto mb-1" />
          <p className="text-lg font-bold text-foreground">{toArabicNumber(weekData.total)}</p>
          <p className="text-[10px] text-muted-foreground">صفحات هذا الأسبوع</p>
        </div>
        <div className="glass-card rounded-2xl p-3 text-center">
          <Calendar className="w-5 h-5 text-gold mx-auto mb-1" />
          <p className="text-lg font-bold text-foreground">{toArabicNumber(weekData.avg)}</p>
          <p className="text-[10px] text-muted-foreground">المتوسط اليومي</p>
        </div>
        <div className="glass-card rounded-2xl p-3 text-center">
          <Award className="w-5 h-5 text-gold mx-auto mb-1" />
          <p className="text-lg font-bold text-foreground">{toArabicNumber(weekData.best)}</p>
          <p className="text-[10px] text-muted-foreground">أفضل يوم</p>
        </div>
      </div>

      {/* Goal setting */}
      <div className="glass-card rounded-2xl p-4">
        <div className="flex items-center gap-2 mb-3"><Target className="w-4 h-4 text-gold" /><p className="text-sm font-bold text-foreground">الهدف اليومي</p></div>
        <div className="flex gap-2">
          {[3, 5, 10, 20].map(g => (
            <button key={g} onClick={() => setGoalValue(g)} className={`flex-1 py-2 rounded-xl text-sm font-bold ${goal === g ? 'bg-gold text-primary-foreground' : 'glass text-foreground'}`}>{toArabicNumber(g)} صفحات</button>
          ))}
        </div>
      </div>

      {/* Weekly calendar */}
      <div className="glass-card rounded-2xl p-4">
        <div className="flex items-center gap-2 mb-3"><Calendar className="w-4 h-4 text-gold" /><p className="text-sm font-bold text-foreground">آخر ٧ أيام</p></div>
        <div className="flex justify-between gap-1">
          {weekData.days.map((d, i) => (
            <div key={i} className="flex-1 flex flex-col items-center gap-1">
              <div className="w-full h-16 rounded-lg bg-gold/5 relative overflow-hidden flex items-end">
                <div className={`w-full rounded-lg transition-all ${d.completed ? 'bg-gold-gradient' : 'bg-gold/30'}`} style={{ height: `${Math.min(100, goal > 0 ? (d.pages / goal) * 100 : 0)}%` }} />
              </div>
              <span className="text-[9px] text-muted-foreground">{d.day}</span>
              <span className="text-[9px] text-gold font-bold">{toArabicNumber(d.pages)}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Achievement badges */}
      <div className="glass-card rounded-2xl p-4">
        <div className="flex items-center gap-2 mb-3"><Award className="w-4 h-4 text-gold" /><p className="text-sm font-bold text-foreground">الإنجازات</p></div>
        <div className="grid grid-cols-3 gap-2">
          {badges.map((b, i) => (
            <div key={i} className={`rounded-xl p-3 text-center ${b.unlocked ? 'bg-gold/10' : 'bg-muted/5 opacity-40'}`}>
              <p className="text-2xl mb-1">{b.icon}</p>
              <p className="text-[10px] text-muted-foreground">{b.label}</p>
            </div>
          ))}
        </div>
      </div>
      </>
      ) : (
        <TrackerReports tracker={tracker} />
      )}
    </div>
  );
}
