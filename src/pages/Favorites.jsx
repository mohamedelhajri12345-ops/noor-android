import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Heart, BookOpen, BookMarked, ArrowRight } from 'lucide-react';
import { getFavorites } from '@/lib/favorites';
import { toArabicNumber } from '@/lib/islamicUtils';

export default function Favorites() {
  const [favs, setFavs] = useState(() => getFavorites());

  useEffect(() => {
    const handler = () => setFavs(getFavorites());
    window.addEventListener('favorites-changed', handler);
    return () => window.removeEventListener('favorites-changed', handler);
  }, []);

  const surahFavs = favs.filter(f => f.type === 'surah');
  const storyFavs = favs.filter(f => f.type === 'story');

  const isEmpty = favs.length === 0;

  return (
    <div className="space-y-5 animate-fade-in">
      <div className="text-center pt-2">
        <div className="w-16 h-16 rounded-3xl bg-gold/10 flex items-center justify-center mx-auto mb-3">
          <Heart className="w-8 h-8 text-gold" fill="currentColor" />
        </div>
        <h2 className="text-xl font-bold text-gold">المفضلة</h2>
        <p className="text-xs text-muted-foreground mt-1">السور والقصص التي تتابعها</p>
      </div>

      {isEmpty && (
        <div className="glass-card rounded-2xl p-8 text-center">
          <Heart className="w-12 h-12 text-muted-foreground/40 mx-auto mb-3" />
          <p className="text-sm text-muted-foreground">لا توجد عناصر في المفضلة بعد</p>
          <p className="text-xs text-muted-foreground/70 mt-1">اضغط على ♡ بجانب أي سورة أو قصة لحفظها هنا</p>
          <div className="flex gap-2 mt-4 justify-center">
            <Link to="/quran" className="glass-card rounded-xl px-4 py-2 text-xs text-gold flex items-center gap-1.5">
              <BookOpen className="w-3.5 h-3.5" /> القرآن
            </Link>
            <Link to="/stories" className="glass-card rounded-xl px-4 py-2 text-xs text-gold flex items-center gap-1.5">
              <BookMarked className="w-3.5 h-3.5" /> القصص
            </Link>
          </div>
        </div>
      )}

      {/* Surahs */}
      {surahFavs.length > 0 && (
        <div>
          <h3 className="text-sm font-bold text-foreground mb-2 px-1 flex items-center gap-1.5">
            <BookOpen className="w-4 h-4 text-gold" /> السور المفضلة
          </h3>
          <div className="space-y-2">
            {surahFavs.map(fav => (
              <Link
                key={`surah-${fav.id}`}
                to={`/quran/${fav.id}`}
                className="glass-card pressable rounded-2xl p-3 flex items-center gap-3 hover:glass-hover transition-all"
              >
                <div className="w-11 h-11 rounded-xl bg-gold/10 flex items-center justify-center shrink-0">
                  <span className="text-gold font-bold text-sm">{toArabicNumber(fav.id)}</span>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-bold text-foreground">سورة {fav.title}</p>
                  <p className="text-xs text-muted-foreground">{fav.subtitle}</p>
                </div>
                <ArrowRight className="w-4 h-4 text-muted-foreground rotate-180" />
              </Link>
            ))}
          </div>
        </div>
      )}

      {/* Stories */}
      {storyFavs.length > 0 && (
        <div>
          <h3 className="text-sm font-bold text-foreground mb-2 px-1 flex items-center gap-1.5">
            <BookMarked className="w-4 h-4 text-gold" /> القصص المفضلة
          </h3>
          <div className="grid grid-cols-2 gap-3">
            {storyFavs.map(fav => (
              <Link
                key={`story-${fav.id}`}
                to="/stories"
                className="glass-card pressable rounded-2xl overflow-hidden text-right hover:glass-hover transition-all"
              >
                <div className="relative aspect-video bg-muted overflow-hidden">
                  {fav.thumb ? (
                    <img src={fav.thumb} alt={fav.title} loading="lazy" className="w-full h-full object-cover" />
                  ) : (
                    <div className="w-full h-full bg-gold/10 flex items-center justify-center">
                      <BookMarked className="w-8 h-8 text-gold" />
                    </div>
                  )}
                  <div className="absolute top-1.5 right-1.5">
                    <Heart className="w-4 h-4 text-gold fill-current" />
                  </div>
                </div>
                <div className="p-2.5">
                  <p className="text-xs font-bold text-foreground line-clamp-2 leading-snug">{fav.title}</p>
                  <span className="pill-tag mt-1 inline-block">{fav.prophet}</span>
                </div>
              </Link>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
