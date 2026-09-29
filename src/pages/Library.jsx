import React, { useState, useEffect } from 'react';
import { Play, Pause, Music, Heart, Search, Loader2, Download, CheckCircle2 } from 'lucide-react';
import { nasheeds, nasheedCategories } from '@/data/nasheeds';
import { audioManager } from '@/lib/audioManager';
import { useLanguage } from '@/lib/LanguageContext';
import { toArabicNumber } from '@/lib/islamicUtils';

const FAV_KEY = 'nur_nasheed_favs';

// صور تعبيرية لكل تصنيف — تدرجات لونية وأيقونات
const categoryVisuals = {
  prophet: { gradient: 'from-amber-500/30 to-yellow-600/20', icon: '⭐', label: 'عن النبي ﷺ' },
  religious: { gradient: 'from-emerald-600/30 to-teal-700/20', icon: '🤲', label: 'دينية' },
  arabic: { gradient: 'from-cyan-600/30 to-blue-700/20', icon: '🌙', label: 'عربية' },
  english: { gradient: 'from-indigo-600/30 to-purple-700/20', icon: '🌍', label: 'إنجليزية' },
  occasions: { gradient: 'from-rose-500/30 to-pink-600/20', icon: '💒', label: 'مناسبات' },
};

export default function Library() {
  const { t } = useLanguage();
  const [category, setCategory] = useState('all');
  const [search, setSearch] = useState('');
  const [favorites, setFavorites] = useState(() => {
    try { return JSON.parse(localStorage.getItem(FAV_KEY) || '[]'); } catch { return []; }
  });
  const [audioState, setAudioState] = useState(audioManager.getState());
  const [downloading, setDownloading] = useState(false);
  const [downloadProgress, setDownloadProgress] = useState(0);
  const [downloadedAll, setDownloadedAll] = useState(() => localStorage.getItem('nur_nasheeds_downloaded') === 'true');

  useEffect(() => {
    return audioManager.subscribe(setAudioState);
  }, []);

  useEffect(() => {
    localStorage.setItem(FAV_KEY, JSON.stringify(favorites));
  }, [favorites]);

  const toggleFav = (id) => {
    setFavorites(f => f.includes(id) ? f.filter(i => i !== id) : [...f, id]);
  };

  let filtered = category === 'all' ? [...nasheeds] : nasheeds.filter(n => n.category === category);
  if (search) {
    filtered = filtered.filter(n => n.title.includes(search) || n.artist.includes(search));
  }

  // Pre-cache all nasheeds for offline use
  const downloadAll = async () => {
    setDownloading(true);
    setDownloadProgress(0);
    let success = 0;
    for (let i = 0; i < nasheeds.length; i++) {
      try {
        await fetch(nasheeds[i].url);
        success++;
      } catch { /* */ }
      setDownloadProgress(i + 1);
    }
    setDownloading(false);
    if (success > 0) {
      localStorage.setItem('nur_nasheeds_downloaded', 'true');
      setDownloadedAll(true);
    }
  };

  const playTrack = (track, allTracks) => {
    const queue = allTracks.map(f => ({ id: f.id, url: f.url, meta: { title: f.title, artist: f.artist } }));
    const idx = allTracks.findIndex(f => f.id === track.id);
    audioManager.playQueue(queue, Math.max(0, idx));
  };

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="text-center pt-2">
        <div className="w-16 h-16 rounded-3xl bg-gold/10 flex items-center justify-center mx-auto mb-3">
          <Music className="w-8 h-8 text-gold" />
        </div>
        <h2 className="text-xl font-bold text-gold">مكتبة الأناشيد</h2>
        <p className="text-xs text-muted-foreground mt-1">أناشيد إسلامية بدون موسيقى — {toArabicNumber(nasheeds.length)} نشيد</p>
      </div>

      {/* Search */}
      <div className="glass-card rounded-2xl p-3 flex items-center gap-2">
        <Search className="w-4 h-4 text-muted-foreground" />
        <input value={search} onChange={e => setSearch(e.target.value)}
          placeholder="ابحث عن نشيد أو منشد..."
          className="flex-1 bg-transparent text-sm text-foreground placeholder:text-muted-foreground outline-none" />
      </div>

      {/* Categories */}
      <div className="flex gap-2 overflow-x-auto scrollbar-hide pb-1">
        {nasheedCategories.map(cat => (
          <button key={cat.id} onClick={() => setCategory(cat.id)}
            className={`px-3 py-1.5 rounded-xl text-xs whitespace-nowrap transition-all ${category === cat.id ? 'bg-gold text-primary-foreground font-bold' : 'glass text-foreground'}`}>
            {cat.name}
          </button>
        ))}
      </div>

      {/* Download all for offline */}
      <button onClick={downloadAll} disabled={downloading}
        className="w-full glass-card rounded-2xl p-3 flex items-center justify-center gap-2 text-sm text-gold hover:glass-hover transition-all">
        {downloading ? (
          <>
            <Loader2 className="w-4 h-4 animate-spin" />
            <span>جارٍ التحميل... {toArabicNumber(downloadProgress)}/{toArabicNumber(nasheeds.length)}</span>
          </>
        ) : downloadedAll ? (
          <>
            <CheckCircle2 className="w-4 h-4" />
            <span>تم تحميل الأناشيد — تعمل بدون إنترنت ✓</span>
          </>
        ) : (
          <>
            <Download className="w-4 h-4" />
            <span>تحميل الكل للعمل بدون إنترنت</span>
          </>
        )}
      </button>

      {/* Nasheed list — ordered with expressive covers */}
      <div className="space-y-2">
        {filtered.length === 0 ? (
          <p className="text-center text-muted-foreground py-8">لا توجد نتائج</p>
        ) : (
          filtered.map((n, idx) => {
            const isPlaying = audioState.currentId === n.id && audioState.isPlaying;
            const isCurrent = audioState.currentId === n.id;
            const isFav = favorites.includes(n.id);
            const isLoading = isCurrent && audioState.isLoading;
            const visual = categoryVisuals[n.category] || categoryVisuals.religious;
            return (
              <div
                key={n.id}
                className={`glass-card rounded-2xl p-2.5 flex items-center gap-3 transition-all ${isCurrent ? 'border-gold/40 ring-1 ring-gold/20' : ''}`}
              >
                {/* Expressive cover image */}
                <div className={`relative w-14 h-14 rounded-xl bg-gradient-to-br ${visual.gradient} flex items-center justify-center shrink-0 overflow-hidden`}>
                  <span className="text-2xl">{visual.icon}</span>
                  {/* Track number */}
                  <span className="absolute bottom-0 right-0 text-[9px] bg-black/40 text-white/80 px-1 rounded-tl">
                    {toArabicNumber(idx + 1)}
                  </span>
                </div>

                {/* Track info */}
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-bold text-foreground truncate">{n.title}</p>
                  <p className="text-xs text-muted-foreground truncate">{n.artist}</p>
                  <div className="flex items-center gap-2 mt-0.5">
                    <span className="text-[10px] text-muted-foreground/70">{visual.label}</span>
                    <span className="text-[10px] text-muted-foreground/70">·</span>
                    <span className="text-[10px] text-muted-foreground/70 flex items-center gap-0.5">
                      <Music className="w-2.5 h-2.5" /> {n.duration}
                    </span>
                  </div>
                </div>

                {/* Favorite */}
                <button
                  onClick={() => toggleFav(n.id)}
                  className={`w-9 h-9 rounded-full flex items-center justify-center shrink-0 transition-colors active:scale-90 ${isFav ? 'text-gold' : 'text-muted-foreground hover:text-foreground'}`}
                  title="حفظ في المفضلة"
                >
                  <Heart className={`w-4 h-4 ${isFav ? 'fill-current' : ''}`} />
                </button>

                {/* Play / Pause — clear control button */}
                <button
                  onClick={() => playTrack(n, filtered)}
                  className={`w-12 h-12 rounded-full flex items-center justify-center shrink-0 active:scale-90 transition-all shadow-md ${
                    isCurrent
                      ? 'bg-gold-gradient text-primary-foreground'
                      : 'bg-gold/15 text-gold hover:bg-gold/25'
                  }`}
                  title={isPlaying ? 'إيقاف' : 'تشغيل'}
                >
                  {isLoading ? (
                    <Loader2 className="w-5 h-5 animate-spin" />
                  ) : isPlaying ? (
                    <Pause className="w-5 h-5" />
                  ) : (
                    <Play className="w-5 h-5 ml-0.5" />
                  )}
                </button>
              </div>
            );
          })
        )}
      </div>

      <p className="text-xs text-muted-foreground text-center pt-2">
        الأناشيد من مصادر مجانية عامة (Internet Archive)
      </p>
    </div>
  );
}
