import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Wifi, User, Info, RotateCcw, Volume2, Languages, Type, AudioLines, Trash2 } from 'lucide-react';
import { setTheme, getDataSaver, setDataSaver } from '@/lib/theme';
import { reciters } from '@/data/surahs';
import { useLanguage } from '@/lib/LanguageContext';
import { languages } from '@/lib/i18n';
import { base44 } from '@/api/base44Client';

export default function Settings() {
  const navigate = useNavigate();
  const { lang, setLang, t } = useLanguage();

  const [dataSaver, setDataSaverState] = useState(getDataSaver());
  const [reciter, setReciter] = useState(() => localStorage.getItem('quran_reciter') || 'alafasy');
  const [audioQuality, setAudioQuality] = useState(() => localStorage.getItem('nur_audio_quality') || 'high');
  const [fontSize, setFontSize] = useState(() => localStorage.getItem('nur_font_size') || 'medium');

  const toggleDataSaver = () => {
    const next = !dataSaver;
    setDataSaver(next);
    setDataSaverState(next);
  };

  const changeReciter = (id) => {
    localStorage.setItem('quran_reciter', id);
    setReciter(id);
  };

  const changeAudioQuality = (q) => {
    localStorage.setItem('nur_audio_quality', q);
    setAudioQuality(q);
  };

  const changeFontSize = (s) => {
    localStorage.setItem('nur_font_size', s);
    setFontSize(s);
    const sizes = { small: '14px', medium: '16px', large: '18px', xlarge: '20px' };
    document.body.style.fontSize = sizes[s] || '16px';
  };

  const [user, setUser] = useState(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    base44.auth.me().then(setUser).catch(() => setUser(null));
  }, []);

  const handleDeleteAccount = async () => {
    if (!window.confirm('هل أنت متأكد من حذف حسابك؟ لا يمكن التراجع عن هذا الإجراء.')) return;
    setDeleting(true);
    try {
      if (user?.id) {
        await base44.entities.User.delete(user.id);
      }
      await base44.auth.logout();
      localStorage.clear();
      window.location.href = '/login';
    } catch { /* */ }
    setDeleting(false);
  };

  const resetSettings = () => {
    localStorage.removeItem('nur_theme');
    localStorage.removeItem('nur_data_saver');
    localStorage.removeItem('nur_audio_quality');
    localStorage.removeItem('nur_font_size');
    setTheme('dark');
    setDataSaver(false);
    setDataSaverState(false);
    setAudioQuality('high');
    setFontSize('medium');
    document.body.style.fontSize = '16px';
  };

  return (
    <div className="space-y-4 animate-fade-in">
      <h2 className="text-xl font-bold text-gold">{t('settings.title')}</h2>

      {/* Data saving */}
      <SettingsSection icon={Wifi} title="البيانات">
        <SettingsRow label="وضع توفير البيانات" desc="يقلل جودة الصوت ويحفظ الردود محليًا (يوفر ~٦٠٪)">
          <Toggle on={dataSaver} onToggle={toggleDataSaver} />
        </SettingsRow>
      </SettingsSection>

      {/* Language */}
      <SettingsSection icon={Languages} title={t('settings.language')}>
        <div className="px-4 py-3">
          <p className="text-xs text-muted-foreground mb-2">{t('settings.language_desc')}</p>
          <div className="flex gap-2">
            {languages.map(l => (
              <button key={l.code} onClick={() => setLang(l.code)}
                className={`flex-1 rounded-xl py-2.5 text-sm flex items-center justify-center gap-1.5 transition-all ${lang === l.code ? 'bg-gold text-primary-foreground font-bold' : 'glass text-foreground'}`}>
                <span>{l.flag}</span> {l.name}
              </button>
            ))}
          </div>
        </div>
      </SettingsSection>

      {/* Quran reciter */}
      <SettingsSection icon={Volume2} title="القرآن الكريم">
        <div className="px-4 py-2">
          <p className="text-xs text-muted-foreground mb-2">القارئ الافتراضي</p>
          <div className="flex gap-2 overflow-x-auto scrollbar-hide pb-1">
            {reciters.map(r => (
              <button key={r.id} onClick={() => changeReciter(r.id)}
                className={`px-3 py-1.5 rounded-xl text-xs whitespace-nowrap transition-all ${reciter === r.id ? 'bg-gold text-primary-foreground font-bold' : 'glass text-foreground'}`}>
                {r.name}
              </button>
            ))}
          </div>
        </div>
      </SettingsSection>

      {/* Audio quality */}
      <SettingsSection icon={AudioLines} title="جودة الصوت">
        <div className="px-4 py-3">
          <p className="text-xs text-muted-foreground mb-2">جودة التلاوة والأناشيد</p>
          <div className="flex gap-2">
            {[
              { id: 'normal', name: 'عادية (٦٤kbps)' },
              { id: 'high', name: 'عالية (١٢٨kbps)' },
            ].map(q => (
              <button key={q.id} onClick={() => changeAudioQuality(q.id)}
                className={`flex-1 rounded-xl py-2.5 text-xs transition-all ${audioQuality === q.id ? 'bg-gold text-primary-foreground font-bold' : 'glass text-foreground'}`}>
                {q.name}
              </button>
            ))}
          </div>
        </div>
      </SettingsSection>

      {/* Font size */}
      <SettingsSection icon={Type} title="حجم الخط">
        <div className="px-4 py-3">
          <p className="text-xs text-muted-foreground mb-2">حجم نص التطبيق</p>
          <div className="flex gap-2">
            {[
              { id: 'small', name: 'صغير' },
              { id: 'medium', name: 'متوسط' },
              { id: 'large', name: 'كبير' },
              { id: 'xlarge', name: 'كبير جداً' },
            ].map(s => (
              <button key={s.id} onClick={() => changeFontSize(s.id)}
                className={`flex-1 rounded-xl py-2.5 text-xs transition-all ${fontSize === s.id ? 'bg-gold text-primary-foreground font-bold' : 'glass text-foreground'}`}>
                {s.name}
              </button>
            ))}
          </div>
        </div>
      </SettingsSection>

      {/* About */}
      <SettingsSection icon={Info} title="حول التطبيق">
        <div className="px-4 py-3 space-y-1">
          <p className="text-sm font-bold text-foreground">نور</p>
          <p className="text-xs text-muted-foreground">تطبيق الإسلام الشامل</p>
          <p className="text-xs text-muted-foreground">القرآن · الأذكار · الصلاة · القبلة · القصص · الألعاب · الذكاء الاصطناعي</p>
          <p className="text-[10px] text-muted-foreground mt-2">جميع البيانات من مصادر إسلامية موثوقة</p>
          <button onClick={() => navigate('/privacy')} className="text-xs text-gold hover:underline mt-2 text-right">
            سياسة الخصوصية ←
          </button>
        </div>
      </SettingsSection>

      {/* Account */}
      {user && (
        <SettingsSection icon={User} title="الحساب">
          <div className="px-4 py-3 space-y-2">
            <p className="text-sm text-foreground">{user.email || user.full_name || 'مستخدم'}</p>
            <button onClick={handleDeleteAccount} disabled={deleting}
              className="w-full bg-destructive/10 text-destructive rounded-xl py-2.5 text-sm font-bold flex items-center justify-center gap-2 active:scale-95 transition-transform disabled:opacity-50">
              <Trash2 className="w-4 h-4" /> {deleting ? 'جارٍ الحذف...' : 'حذف الحساب نهائياً'}
            </button>
          </div>
        </SettingsSection>
      )}

      {/* Reset */}
      <button onClick={resetSettings} className="w-full glass-card rounded-2xl p-4 flex items-center justify-center gap-2 text-sm text-destructive hover:bg-destructive/10 transition-colors">
        <RotateCcw className="w-4 h-4" /> إعادة الإعدادات الافتراضية
      </button>
    </div>
  );
}

function SettingsSection({ icon: Icon, title, children }) {
  return (
    <div className="glass-card rounded-2xl overflow-hidden">
      <div className="flex items-center gap-2 px-4 py-3 border-b border-gold/10">
        <Icon className="w-4 h-4 text-gold" />
        <h3 className="text-sm font-bold text-foreground">{title}</h3>
      </div>
      {children}
    </div>
  );
}

function SettingsRow({ label, desc, children }) {
  return (
    <div className="flex items-center justify-between px-4 py-3 border-b border-gold/5 last:border-0">
      <div className="flex-1">
        <p className="text-sm text-foreground">{label}</p>
        <p className="text-xs text-muted-foreground mt-0.5">{desc}</p>
      </div>
      {children}
    </div>
  );
}

function Toggle({ on, onToggle }) {
  return (
    <button onClick={onToggle}
      className={`w-12 h-7 rounded-full transition-colors shrink-0 relative ${on ? 'bg-gold' : 'bg-muted'}`}>
      <div className={`absolute top-1 w-5 h-5 rounded-full bg-white transition-all ${on ? 'left-1' : 'right-1'}`}
        style={{ left: on ? '2px' : 'auto', right: on ? 'auto' : '2px' }} />
    </button>
  );
}
