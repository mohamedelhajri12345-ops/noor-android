import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Play, Pause, ChevronRight, Loader2, Heart, Sun, Moon } from 'lucide-react';
import { surahs, reciters, getSurahAudioUrl, getSurahAudioFallbacks, getSurahText } from '@/data/surahs';
import { audioManager } from '@/lib/audioManager';
import { getCachedAudioUrl } from '@/lib/quranDownloads';
import { toArabicNumber } from '@/lib/islamicUtils';
import { isFavorite, toggleFavorite } from '@/lib/favorites';

export default function QuranReader() {
  const { id } = useParams();
  const navigate = useNavigate();
  const surah = surahs.find(s => s.number === parseInt(id));
  const [ayahs, setAyahs] = useState(null);
  const [loading, setLoading] = useState(true);
  const [reciter, setReciter] = useState(() => localStorage.getItem('quran_reciter') || 'alafasy');
  const [audioState, setAudioState] = useState(audioManager.getState());
  const [, setFavVersion] = useState(0);
  const [readingMode, setReadingMode] = useState(() => localStorage.getItem('quran_reading_mode') || 'dark');

  useEffect(() => {
    setLoading(true);
    setAyahs(null);
    getSurahText(parseInt(id)).then(data => {
      setAyahs(data);
      setLoading(false);
    });
  }, [id]);

  useEffect(() => {
    return audioManager.subscribe(setAudioState);
  }, []);

  const reciterObj = reciters.find(r => r.id === reciter);
  const audioId = `quran-${id}`;
  const isPlaying = audioState.currentId === audioId && audioState.isPlaying;
  const isCurrent = audioState.currentId === audioId;
  const isLoading = isCurrent && audioState.isLoading;

  const buildQueue = async (reciterId) => {
    const queue = [];
    for (let i = 0; i < 5 && surah.number + i <= 114; i++) {
      const s = surahs.find(s => s.number === surah.number + i);
      if (s) {
        const cachedUrl = await getCachedAudioUrl(reciterId, s.number);
        queue.push({
          id: `quran-${s.number}`,
          url: cachedUrl || getSurahAudioUrl(s.number, reciterId),
          fallbackUrls: cachedUrl ? [] : getSurahAudioFallbacks(s.number, reciterId),
          meta: {
            title: `سورة ${s.name}`,
            artist: reciters.find(r => r.id === reciterId)?.name || '',
          },
        });
      }
    }
    return queue;
  };

  const toggleAudio = async () => {
    if (!surah) return;
    if (isCurrent) { audioManager.toggle(); return; }
    audioManager.playQueue(await buildQueue(reciter), 0);
  };

  const changeReciter = async (newReciter) => {
    setReciter(newReciter);
    localStorage.setItem('quran_reciter', newReciter);
    if (isCurrent && surah) {
      audioManager.playQueue(await buildQueue(newReciter), 0);
    }
  };

  if (!surah) return <div className="text-center text-muted-foreground py-12">السورة غير موجودة</div>;

  return (
    <div className="space-y-4 animate-fade-in">
      {/* Surah header with player */}
      <div className="glass-card rounded-2xl p-5 text-center relative overflow-hidden">
        <div className={`absolute inset-0 opacity-10 ${isPlaying ? 'animate-spin-slow' : ''}`}>
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 rounded-full border-2 border-gold" />
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-48 h-48 rounded-full border border-gold" />
        </div>

        <div className="relative z-10">
          <h2 className="text-2xl font-bold text-gold arabic-text">سورة {surah.name}</h2>
          <p className="text-xs text-muted-foreground mt-1">{surah.type} · {toArabicNumber(surah.ayahs)} آية</p>

          <button
            onClick={() => {
              toggleFavorite({ type: 'surah', id: surah.number, title: surah.name, subtitle: `${surah.type} · ${toArabicNumber(surah.ayahs)} آية` });
              setFavVersion(v => v + 1);
            }}
            className={`absolute top-3 left-3 w-10 h-10 rounded-xl flex items-center justify-center transition-all active:scale-90 ${isFavorite('surah', surah.number) ? 'text-gold bg-gold/10' : 'text-muted-foreground bg-foreground/5'}`}
            title="حفظ في المفضلة"
          >
            <Heart className={`w-5 h-5 ${isFavorite('surah', surah.number) ? 'fill-current' : ''}`} />
          </button>

          <button
            onClick={() => {
              const newMode = readingMode === 'dark' ? 'light' : 'dark';
              setReadingMode(newMode);
              localStorage.setItem('quran_reading_mode', newMode);
            }}
            className={`absolute top-3 right-3 w-10 h-10 rounded-xl flex items-center justify-center transition-all active:scale-90 ${readingMode === 'light' ? 'text-amber-500 bg-amber-500/10' : 'text-muted-foreground bg-foreground/5'}`}
            title={readingMode === 'dark' ? 'الوضع النهاري' : 'الوضع الليلي'}
          >
            {readingMode === 'dark' ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
          </button>

          <button onClick={toggleAudio} className="mt-4 w-16 h-16 rounded-full bg-gold-gradient flex items-center justify-center mx-auto text-primary-foreground shadow-lg active:scale-95 transition-transform">
            {isLoading ? <Loader2 className="w-6 h-6 animate-spin" /> : isPlaying ? <Pause className="w-7 h-7" /> : <Play className="w-7 h-7 ml-0.5" />}
          </button>
          <p className="text-xs text-muted-foreground mt-2">{isLoading ? 'جارٍ التحميل...' : isPlaying ? 'يعمل التشغيل' : 'تشغيل السورة كاملة'}</p>

          {isCurrent && audioState.duration > 0 && (
            <div className="mt-3 h-1.5 rounded-full bg-muted overflow-hidden">
              <div className="h-full bg-gold-gradient transition-all" style={{ width: `${(audioState.currentTime / audioState.duration) * 100}%` }} />
            </div>
          )}
        </div>
      </div>

      {/* Reciter selector — 3 reciters only */}
      <div className="glass-card rounded-2xl p-3">
        <p className="text-xs text-muted-foreground mb-2">القارئ</p>
        <div className="flex gap-2 overflow-x-auto scrollbar-hide pb-1">
          {reciters.map(r => (
            <button
              key={r.id}
              onClick={() => changeReciter(r.id)}
              className={`px-3 py-1.5 rounded-xl text-xs whitespace-nowrap transition-all ${reciter === r.id ? 'bg-gold text-primary-foreground font-bold' : 'glass text-foreground hover:text-gold'}`}
            >
              {r.name}
            </button>
          ))}
        </div>
      </div>

      {/* Ayahs */}
      {loading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="w-8 h-8 animate-spin text-gold" />
        </div>
      ) : ayahs ? (
        <div className={`rounded-2xl p-5 transition-all ${readingMode === 'light' ? 'bg-amber-50 border border-amber-200/40' : 'glass-card'}`}>
          <div className={`quran-text text-justify ${readingMode === 'light' ? 'text-stone-800' : 'text-foreground'}`} style={{ direction: 'rtl' }}>
            {ayahs.map(ayah => (
              <span key={ayah.number}>
                {ayah.text}
                <span className="inline-flex items-center justify-center w-7 h-7 rounded-full bg-gold/10 text-gold text-xs mx-1 font-sans align-middle">
                  {toArabicNumber(ayah.number)}
                </span>{' '}
              </span>
            ))}
          </div>
        </div>
      ) : (
        <div className="text-center text-muted-foreground py-8">
          <p>النص محفوظ للقراءة بدون إنترنت.</p>
          <p className="text-xs mt-1">إذا لم يظهر النص، تأكد من اتصالك بالإنترنت لتحميله مرة واحدة.</p>
        </div>
      )}
    </div>
  );
}
