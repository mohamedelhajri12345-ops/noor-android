import React, { useState, useEffect } from 'react';
import { Outlet, useLocation, useNavigate, useNavigationType, Link } from 'react-router-dom';
import { Home, Gamepad2, LayoutGrid, Sparkles, Settings as SettingsIcon, Heart, MessageCircle, ChevronRight } from 'lucide-react';
import { TasbihIcon, QuranIcon } from '@/components/IslamicIcons';
import AnimatedBackground from '@/components/AnimatedBackground';
import AudioPlayerBar from '@/components/AudioPlayerBar';
import InAppNotificationCenter from '@/components/InAppNotificationCenter';

import { useLanguage } from '@/lib/LanguageContext';

const navItems = [
  { path: '/', labelKey: 'nav.home', icon: Home },
  { path: '/quran', labelKey: 'nav.quran', icon: QuranIcon },
  { path: '/tasbih', labelKey: 'nav.tasbih', icon: TasbihIcon },
  { path: '/games', labelKey: 'nav.games', icon: Gamepad2 },
  { path: '/more', labelKey: 'nav.more', icon: LayoutGrid },
];

const ROOT_PATHS = ['/', '/quran', '/tasbih', '/games', '/more'];

const SCREEN_TITLES = {
  '/prayer': 'أوقات الصلاة',
  '/qibla': 'القبلة',
  '/tasbih': 'المسبحة',
  '/journal': 'المفكرة',
  '/calendar': 'التقويم الهجري',
  '/quiz': 'الاختبار الديني',
  '/stories': 'قصص الأنبياء',
  '/settings': 'الإعدادات',
  '/assistant': 'مساعد القرآن الكريم',
  '/donate': 'صدقة جارية',
  '/community': 'مجتمع القرآن الكريم',
  '/privacy': 'سياسة الخصوصية',
  '/library': 'مكتبة الأناشيد',
  '/favorites': 'المفضلة',
  '/athkar': 'الأذكار',
  '/zakat': 'حاسبة الزكاة',
  '/hajj': 'دليل الحج والعمرة',
  '/quran-tracker': 'متتبع ورد القرآن',
};

const getScreenTitle = (pathname) => {
  if (SCREEN_TITLES[pathname]) return SCREEN_TITLES[pathname];
  if (pathname.startsWith('/quran/')) return 'القرآن الكريم';
  if (pathname.startsWith('/stories/')) return 'قصص الأنبياء';
  if (pathname.startsWith('/athkar')) return 'الأذكار';
  return 'القرآن الكريم';
};

export default function Layout() {
  const location = useLocation();
  const navigate = useNavigate();
  const navType = useNavigationType();
  const [tabPaths, setTabPaths] = useState({});
  const { t } = useLanguage();

  // Track the last visited sub-route for each bottom tab
  useEffect(() => {
    const currentTab = navItems.find(item =>
      item.path === '/'
        ? location.pathname === '/'
        : location.pathname === item.path || location.pathname.startsWith(item.path + '/')
    );
    if (currentTab) {
      setTabPaths(prev => ({ ...prev, [currentTab.path]: location.pathname }));
    }
  }, [location.pathname]);

  const isTabActive = (tabPath) =>
    tabPath === '/'
      ? location.pathname === '/'
      : location.pathname === tabPath || location.pathname.startsWith(tabPath + '/');

  const handleTabClick = (tabPath) => {
    if (isTabActive(tabPath)) {
      navigate(tabPath);
    } else {
      navigate(tabPaths[tabPath] || tabPath);
    }
  };

  // Global ripple effect for .pressable elements
  useEffect(() => {
    const handler = (e) => {
      const card = e.target.closest('.pressable');
      if (!card) return;
      const rect = card.getBoundingClientRect();
      const ripple = document.createElement('span');
      ripple.className = 'ripple';
      ripple.style.left = `${e.clientX - rect.left}px`;
      ripple.style.top = `${e.clientY - rect.top}px`;
      card.appendChild(ripple);
      setTimeout(() => ripple.remove(), 600);
    };
    document.addEventListener('pointerdown', handler);
    return () => document.removeEventListener('pointerdown', handler);
  }, []);

  const isRoot = ROOT_PATHS.includes(location.pathname);

  return (
    <div className="min-h-screen flex flex-col" dir="rtl">
      <AnimatedBackground />
      <InAppNotificationCenter />

      {/* Header — night sky semi-transparent with blur */}
      <header className="sticky top-0 z-40 border-b border-gold/10 safe-top" style={{ background: 'hsl(216 33% 6% / 0.80)', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)' }}>
        <div className="max-w-2xl mx-auto px-3 py-1 flex items-center justify-between">
          {isRoot ? (
            <>
              {/* Logo: crescent moon + Noor text */}
              <Link to="/" className="flex items-center gap-2">
                <img src="https://media.base44.com/images/public/6a833faeb9e42cca9a6576fa/f6fc7be6c_generated_image.png" alt="نور" className="w-8 h-8 rounded-lg shrink-0 object-cover" style={{ boxShadow: '0 0 10px hsl(46 65% 52% / 0.35)' }} />
                <h1 className="text-sm font-bold leading-none" style={{ fontFamily: "'Amiri', 'Cairo', serif", color: 'hsl(45 90% 70%)' }}>القرآن الكريم</h1>
              </Link>
              {/* Action icons — unified color */}
              <div className="flex items-center gap-1">
                <a href="https://wa.me/212726626546" target="_blank" rel="noopener noreferrer"
                  className="w-11 h-11 rounded-xl border border-gold/20 bg-foreground/5 flex items-center justify-center text-gold hover:bg-gold/10 transition-colors active:scale-90"
                  title="تأكيد التبرع عبر واتساب">
                  <MessageCircle className="w-4 h-4" />
                </a>
                <button onClick={() => navigate('/donate')}
                  className="w-11 h-11 rounded-xl border border-gold/20 bg-foreground/5 flex items-center justify-center text-gold hover:bg-gold/10 transition-colors active:scale-90"
                  title="تبرع">
                  <Heart className="w-4 h-4" />
                </button>
                <button onClick={() => navigate('/assistant')}
                  className="w-11 h-11 rounded-xl border border-gold/20 bg-foreground/5 flex items-center justify-center text-gold hover:bg-gold/10 transition-colors active:scale-90"
                  title="مساعد الذكاء الإصطناعي">
                  <Sparkles className="w-4 h-4" />
                </button>
                <button onClick={() => navigate('/settings')}
                  className="w-11 h-11 rounded-xl border border-gold/20 bg-foreground/5 flex items-center justify-center text-gold hover:bg-gold/10 transition-colors active:scale-90"
                  title="الإعدادات">
                  <SettingsIcon className="w-4 h-4" />
                </button>
              </div>
            </>
          ) : (
            <>
              <button onClick={() => navigate(-1)}
                className="w-11 h-11 rounded-xl flex items-center justify-center text-foreground hover:bg-gold/10 transition-colors active:scale-90"
                title="رجوع">
                <ChevronRight className="w-5 h-5" />
              </button>
              <h1 className="text-lg font-bold text-gold flex-1 text-center" style={{ fontFamily: "'Amiri', 'Cairo', serif" }}>
                {getScreenTitle(location.pathname)}
              </h1>
              <div className="w-11" />
            </>
          )}
        </div>
      </header>

      {/* Main content */}
      <main key={location.pathname} className={`flex-1 max-w-2xl mx-auto w-full px-4 py-4 pb-28 ${navType === 'POP' ? 'animate-route-backward' : 'animate-route-forward'}`}>
        <Outlet />
      </main>

      {/* Audio Player Bar */}
      <AudioPlayerBar />

      {/* Bottom navigation — midnight blue semi-transparent with blur */}
      <nav className="fixed bottom-0 left-0 right-0 z-40 border-t border-gold/10 safe-bottom" style={{ background: 'hsl(216 33% 6% / 0.90)', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)' }}>
        <div className="max-w-2xl mx-auto flex items-center justify-around px-1 py-0.5">
          {navItems.map((item) => {
            const Icon = item.icon;
            const active = isTabActive(item.path);
            return (
              <button
                key={item.path}
                onClick={() => handleTabClick(item.path)}
                className={`flex flex-col items-center justify-center gap-0.5 px-2 py-1.5 rounded-xl transition-all min-h-[44px] ${
                  active ? 'text-gold' : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                <Icon className={`w-4 h-4 ${active ? 'text-gold' : ''}`} />
                <span className={`text-[10px] ${active ? 'font-bold text-gold' : ''}`}>{t(item.labelKey)}</span>
                {active && <div className="w-1 h-1 rounded-full bg-gold mt-0.5" />}
              </button>
            );
          })}
        </div>
      </nav>
    </div>
  );
}
