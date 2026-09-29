import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, BookOpen } from 'lucide-react';
import { surahs } from '@/data/surahs';
import { toArabicNumber } from '@/lib/islamicUtils';
import QuranDownloads from '@/components/QuranDownloads';

export default function Quran() {
  const navigate = useNavigate();
  const [search, setSearch] = useState('');

  const filtered = useMemo(() => {
    if (!search) return surahs;
    return surahs.filter(s =>
      s.name.includes(search) ||
      s.englishName.toLowerCase().includes(search.toLowerCase()) ||
      String(s.number).includes(search)
    );
  }, [search]);

  return (
    <div className="space-y-4 animate-fade-in overscroll-y-contain">
      <div className="text-center pt-2">
        <h2 className="text-xl font-bold text-gold">القرآن الكريم</h2>
        <p className="text-xs text-muted-foreground mt-1">١١٤ سورة · ٦٢٣٦ آية</p>
      </div>

      <div className="glass-card rounded-2xl p-3 flex items-center gap-2">
        <Search className="w-4 h-4 text-muted-foreground" />
        <input
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder="ابحث عن سورة..."
          className="flex-1 bg-transparent text-sm text-foreground placeholder:text-muted-foreground outline-none"
        />
      </div>

      <QuranDownloads />

      <div className="space-y-2 overscroll-y-contain">
        {filtered.map(surah => (
          <button
            key={surah.number}
            onClick={() => navigate(`/quran/${surah.number}`)}
            className="w-full glass-card pressable rounded-2xl p-3 flex items-center gap-3 hover:glass-hover transition-all text-right"
          >
            <div className="w-10 h-10 rounded-xl bg-gold/10 flex items-center justify-center shrink-0">
              <span className="text-sm font-bold text-gold">{toArabicNumber(surah.number)}</span>
            </div>
            <div className="flex-1">
              <p className="text-sm font-bold text-foreground arabic-text">سورة {surah.name}</p>
              <p className="text-xs text-muted-foreground">{surah.type} · {toArabicNumber(surah.ayahs)} آية</p>
            </div>
            <BookOpen className="w-4 h-4 text-muted-foreground" />
          </button>
        ))}
      </div>
    </div>
  );
}
