import React, { useMemo } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell, RadialBarChart, RadialBar, PolarAngleAxis } from 'recharts';
import { TrendingUp, BookOpen, Target, Award } from 'lucide-react';
import { toArabicNumber } from '@/lib/islamicUtils';

const TOTAL_PAGES = 604;

export default function TrackerReports({ tracker }) {
  const goal = tracker.goal || 5;

  // Last 14 days data for daily rate chart
  const dailyData = useMemo(() => {
    const days = [];
    for (let i = 13; i >= 0; i--) {
      const d = new Date(); d.setDate(d.getDate() - i);
      const key = d.toDateString();
      const pages = tracker[key]?.pages || 0;
      days.push({
        day: d.toLocaleDateString('ar', { weekday: 'short' }),
        date: d.toLocaleDateString('ar', { day: 'numeric', month: 'numeric' }),
        pages,
      });
    }
    return days;
  }, [tracker]);

  // All-time total
  const allTimeTotal = useMemo(() => {
    return Object.keys(tracker)
      .filter(k => k !== 'goal' && k !== 'streak')
      .reduce((sum, k) => sum + (tracker[k]?.pages || 0), 0);
  }, [tracker]);

  // Khatma progress
  const completedKhatmas = Math.floor(allTimeTotal / TOTAL_PAGES);
  const currentKhatmaPages = allTimeTotal % TOTAL_PAGES;
  const khatmaProgress = Math.round((currentKhatmaPages / TOTAL_PAGES) * 100);

  // Weekly data (last 4 weeks)
  const weeklyData = useMemo(() => {
    const weeks = [
      { label: 'الأسبوع ١', pages: 0 },
      { label: 'الأسبوع ٢', pages: 0 },
      { label: 'الأسبوع ٣', pages: 0 },
      { label: 'الأسبوع ٤', pages: 0 },
    ];
    for (let i = 29; i >= 0; i--) {
      const d = new Date(); d.setDate(d.getDate() - i);
      const key = d.toDateString();
      const pages = tracker[key]?.pages || 0;
      const weekIdx = Math.min(3, Math.floor((29 - i) / 7));
      weeks[weekIdx].pages += pages;
    }
    return weeks;
  }, [tracker]);

  // Stats
  const activeDays = dailyData.filter(d => d.pages > 0).length;
  const avgDaily = activeDays > 0 ? Math.round(allTimeTotal / Math.max(1, activeDays)) : 0;
  const bestDay = Math.max(...dailyData.map(d => d.pages), 0);
  const khatmaData = [{ name: 'khatma', value: khatmaProgress, fill: 'hsl(46 65% 52%)' }];

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="text-center pt-2">
        <div className="w-16 h-16 rounded-3xl bg-gold/10 flex items-center justify-center mx-auto mb-3">
          <TrendingUp className="w-8 h-8 text-gold" />
        </div>
        <h2 className="text-xl font-bold text-gold">تقارير التلاوة</h2>
        <p className="text-xs text-muted-foreground mt-1">تحليل معدل تلاوتك وتقدمك في الختمة</p>
      </div>

      {/* Khatma progress — radial chart */}
      <div className="glass-card rounded-2xl p-5">
        <div className="flex items-center gap-2 mb-3">
          <BookOpen className="w-4 h-4 text-gold" />
          <p className="text-sm font-bold text-foreground">التقدم في الختمة الحالية</p>
        </div>
        <div className="flex items-center gap-4">
          <div className="relative shrink-0" style={{ width: 140, height: 140 }}>
            <ResponsiveContainer width="100%" height="100%">
              <RadialBarChart innerRadius="70%" outerRadius="100%" data={khatmaData} startAngle={90} endAngle={-270}>
                <PolarAngleAxis type="number" domain={[0, 100]} tick={false} />
                <RadialBar background={{ fill: 'hsl(46 65% 52% / 0.1)' }} dataKey="value" cornerRadius={10} />
              </RadialBarChart>
            </ResponsiveContainer>
            <div className="absolute inset-0 flex flex-col items-center justify-center">
              <p className="text-2xl font-bold text-gold">{toArabicNumber(khatmaProgress)}٪</p>
              <p className="text-[10px] text-muted-foreground">{toArabicNumber(currentKhatmaPages)}/{toArabicNumber(TOTAL_PAGES)}</p>
            </div>
          </div>
          <div className="flex-1 space-y-2">
            <div className="glass rounded-xl px-3 py-2">
              <p className="text-[10px] text-muted-foreground">الختمات المكتملة</p>
              <p className="text-lg font-bold text-gold">{toArabicNumber(completedKhatmas)}</p>
            </div>
            <div className="glass rounded-xl px-3 py-2">
              <p className="text-[10px] text-muted-foreground">إجمالي الصفحات</p>
              <p className="text-lg font-bold text-gold">{toArabicNumber(allTimeTotal)}</p>
            </div>
            <div className="glass rounded-xl px-3 py-2">
              <p className="text-[10px] text-muted-foreground">المتبقي للختمة</p>
              <p className="text-lg font-bold text-gold">{toArabicNumber(TOTAL_PAGES - currentKhatmaPages)} صفحة</p>
            </div>
          </div>
        </div>
      </div>

      {/* Daily recitation rate — bar chart */}
      <div className="glass-card rounded-2xl p-5">
        <div className="flex items-center gap-2 mb-3">
          <TrendingUp className="w-4 h-4 text-gold" />
          <p className="text-sm font-bold text-foreground">معدل التلاوة اليومي (آخر ١٤ يوم)</p>
        </div>
        <ResponsiveContainer width="100%" height={200}>
          <BarChart data={dailyData} margin={{ top: 10, right: 0, left: -20, bottom: 0 }}>
            <XAxis dataKey="day" tick={{ fontSize: 10, fill: 'hsl(45 10% 55%)' }} axisLine={{ stroke: 'hsl(46 65% 52% / 0.2)' }} tickLine={false} />
            <YAxis tick={{ fontSize: 10, fill: 'hsl(45 10% 55%)' }} axisLine={false} tickLine={false} />
            <Tooltip
              contentStyle={{ background: 'hsl(218 16% 10%)', border: '1px solid hsl(46 65% 52% / 0.3)', borderRadius: '12px', fontSize: 12 }}
              labelStyle={{ color: 'hsl(46 65% 52%)' }}
              itemStyle={{ color: 'hsl(0 0% 100%)' }}
              formatter={(v) => [`${toArabicNumber(v)} صفحة`, 'التلاوة']}
            />
            <Bar dataKey="pages" radius={[6, 6, 0, 0]}>
              {dailyData.map((entry, i) => (
                <Cell key={i} fill={entry.pages >= goal ? 'hsl(46 65% 52%)' : 'hsl(46 65% 52% / 0.4)'} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>

      {/* Weekly summary — bar chart */}
      <div className="glass-card rounded-2xl p-5">
        <div className="flex items-center gap-2 mb-3">
          <Target className="w-4 h-4 text-gold" />
          <p className="text-sm font-bold text-foreground">ملخص آخر ٤ أسابيع</p>
        </div>
        <ResponsiveContainer width="100%" height={180}>
          <BarChart data={weeklyData} margin={{ top: 10, right: 0, left: -20, bottom: 0 }}>
            <XAxis dataKey="label" tick={{ fontSize: 10, fill: 'hsl(45 10% 55%)' }} axisLine={{ stroke: 'hsl(46 65% 52% / 0.2)' }} tickLine={false} />
            <YAxis tick={{ fontSize: 10, fill: 'hsl(45 10% 55%)' }} axisLine={false} tickLine={false} />
            <Tooltip
              contentStyle={{ background: 'hsl(218 16% 10%)', border: '1px solid hsl(46 65% 52% / 0.3)', borderRadius: '12px', fontSize: 12 }}
              labelStyle={{ color: 'hsl(46 65% 52%)' }}
              itemStyle={{ color: 'hsl(0 0% 100%)' }}
              formatter={(v) => [`${toArabicNumber(v)} صفحة`, 'الإجمالي']}
            />
            <Bar dataKey="pages" fill="hsl(46 65% 52%)" radius={[6, 6, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      {/* Summary stats */}
      <div className="grid grid-cols-3 gap-2">
        <div className="glass-card rounded-2xl p-3 text-center">
          <TrendingUp className="w-5 h-5 text-gold mx-auto mb-1" />
          <p className="text-lg font-bold text-foreground">{toArabicNumber(avgDaily)}</p>
          <p className="text-[10px] text-muted-foreground">المتوسط اليومي</p>
        </div>
        <div className="glass-card rounded-2xl p-3 text-center">
          <Award className="w-5 h-5 text-gold mx-auto mb-1" />
          <p className="text-lg font-bold text-foreground">{toArabicNumber(bestDay)}</p>
          <p className="text-[10px] text-muted-foreground">أفضل يوم</p>
        </div>
        <div className="glass-card rounded-2xl p-3 text-center">
          <BookOpen className="w-5 h-5 text-gold mx-auto mb-1" />
          <p className="text-lg font-bold text-foreground">{toArabicNumber(activeDays)}</p>
          <p className="text-[10px] text-muted-foreground">أيام التلاوة</p>
        </div>
      </div>
    </div>
  );
}
