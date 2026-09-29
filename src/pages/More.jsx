import React from 'react';
import { Link } from 'react-router-dom';
import { Clock, Compass, Music, PenLine, Calendar, BookMarked, Brain, Repeat, Gamepad2, Sparkles, Users, Settings as SettingsIcon, Heart, Coins, Landmark, BookCheck } from 'lucide-react';

const features = [
  { path: '/favorites', label: 'المفضلة', desc: 'السور والقصص المحفوظة', icon: Heart },
  { path: '/community', label: 'المجتمع', desc: 'دردشة الأعضاء', icon: Users },
  { path: '/prayer', label: 'الصلاة', desc: 'أوقات الصلاة', icon: Clock },
  { path: '/qibla', label: 'القبلة', desc: 'اتجاه القبلة', icon: Compass },
  { path: '/library', label: 'المكتبة', desc: 'الأناشيد', icon: Music },
  { path: '/journal', label: 'المفكرة', desc: 'تدوين الخواطر', icon: PenLine },
  { path: '/calendar', label: 'التقويم', desc: 'المناسبات الإسلامية', icon: Calendar },
  { path: '/stories', label: 'قصص الأنبياء', desc: 'قصص ملهمة', icon: BookMarked },
  { path: '/quiz', label: 'الاختبار الديني', desc: 'أسئلة وأجوبة', icon: Brain },
  { path: '/tasbih', label: 'السبحة', desc: 'عدّاد الذكر', icon: Repeat },
  { path: '/games', label: 'الألعاب', desc: 'ألعاب إسلامية', icon: Gamepad2 },
  { path: '/assistant', label: 'المساعد الذكي', desc: 'ذكاء اصطناعي إسلامي', icon: Sparkles },
  { path: '/zakat', label: 'حاسبة الزكاة', desc: 'احسب زكاتك', icon: Coins },
  { path: '/hajj', label: 'الحج والعمرة', desc: 'دليل المصطافين', icon: Landmark },
  { path: '/quran-tracker', label: 'ورد القرآن', desc: 'متتبع التلاوة', icon: BookCheck },
  { path: '/settings', label: 'الإعدادات', desc: 'إعدادات التطبيق', icon: SettingsIcon },
];

export default function More() {
  return (
    <div className="space-y-4 animate-fade-in">
      <h2 className="text-xl font-bold text-gold pt-2">الأقسام</h2>
      <div className="grid grid-cols-2 gap-3">
        {features.map(f => {
          const Icon = f.icon;
          return (
            <Link key={f.path} to={f.path} className="glass-card pressable rounded-2xl p-4 flex flex-col items-center gap-2 hover:glass-hover transition-all">
              <div className="w-14 h-14 rounded-2xl bg-gold/10 flex items-center justify-center">
                <Icon className="w-6 h-6 text-gold" />
              </div>
              <div className="text-center">
                <p className="text-sm font-bold text-foreground">{f.label}</p>
                <p className="text-xs text-muted-foreground">{f.desc}</p>
              </div>
            </Link>
          );
        })}
      </div>
    </div>
  );
}
