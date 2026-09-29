import React, { useState, useEffect } from 'react';
import { Download, Trash2, Check, Loader2, WifiOff } from 'lucide-react';
import { reciters } from '@/data/surahs';
import { getDownloadedSurahs, startDownload, isDownloading, subscribeToDownload, deleteReciterDownloads } from '@/lib/quranDownloads';
import { toArabicNumber } from '@/lib/islamicUtils';

// Download manager — uses module-level singleton so downloads continue in background
export default function QuranDownloads() {
  const [counts, setCounts] = useState({});
  const [downloading, setDownloading] = useState(new Set());

  const refresh = () => {
    const c = {};
    const dl = new Set();
    reciters.forEach(r => {
      c[r.id] = getDownloadedSurahs(r.id).length;
      if (isDownloading(r.id)) dl.add(r.id);
    });
    setCounts(c);
    setDownloading(dl);
  };

  useEffect(() => {
    refresh();
    // Subscribe to any ongoing downloads (continues across remounts)
    const unsubscribers = [];
    reciters.forEach(r => {
      if (isDownloading(r.id)) {
        const unsub = subscribeToDownload(r.id, (done) => {
          setCounts(prev => ({ ...prev, [r.id]: done }));
          if (done >= 114) setDownloading(prev => { const n = new Set(prev); n.delete(r.id); return n; });
        });
        unsubscribers.push(unsub);
      }
    });
    return () => unsubscribers.forEach(u => u());
    // NOTE: downloads are NOT stopped on unmount — they continue in the background
  }, []);

  const handleDownload = (reciterId) => {
    setDownloading(prev => new Set([...prev, reciterId]));
    startDownload(reciterId, (done) => {
      setCounts(prev => ({ ...prev, [reciterId]: done }));
      if (done >= 114) setDownloading(prev => { const n = new Set(prev); n.delete(reciterId); return n; });
    });
  };

  const handleDelete = async (reciterId) => {
    if (!window.confirm('حذف جميع التلاوات المنزّلة لهذا القارئ؟')) return;
    await deleteReciterDownloads(reciterId);
    refresh();
  };

  return (
    <div className="space-y-2">
      <div className="flex items-center gap-2 px-1">
        <WifiOff className="w-4 h-4 text-gold" />
        <h3 className="text-sm font-bold text-gold">تنزيل التلاوات بدون إنترنت</h3>
      </div>
      <p className="text-[10px] text-muted-foreground px-1">التنزيل يعمل في الخلفية حتى لو غادرت الصفحة</p>
      <div className="grid grid-cols-2 gap-2">
        {reciters.map(r => {
          const done = counts[r.id] || 0;
          const pct = Math.round((done / 114) * 100);
          const isDownloading = downloading.has(r.id);
          return (
            <div key={r.id} className="glass-card rounded-xl p-2.5 flex flex-col">
              <div className="flex items-center justify-between mb-1.5">
                <p className="text-xs font-bold text-foreground truncate flex-1">{r.name}</p>
                {done > 0 && !isDownloading && (
                  <button onClick={() => handleDelete(r.id)} className="w-6 h-6 rounded-lg bg-destructive/10 flex items-center justify-center text-destructive active:scale-90 transition-transform shrink-0" title="حذف">
                    <Trash2 className="w-3 h-3" />
                  </button>
                )}
              </div>
              <p className="text-[10px] text-muted-foreground mb-1.5">{toArabicNumber(done)}/١١٤ سورة</p>
              {(pct > 0 || isDownloading) && (
                <div className="h-1 rounded-full bg-gold/10 overflow-hidden mb-2">
                  <div className="h-full bg-gold-gradient transition-all duration-300" style={{ width: `${pct}%` }} />
                </div>
              )}
              <button
                onClick={() => handleDownload(r.id)}
                disabled={isDownloading}
                className="w-full bg-gold/10 text-gold rounded-lg py-1.5 text-xs font-bold flex items-center justify-center gap-1 active:scale-95 transition-transform disabled:opacity-60"
              >
                {isDownloading
                  ? <><Loader2 className="w-3 h-3 animate-spin" /> {toArabicNumber(pct)}٪</>
                  : done > 0
                    ? <><Check className="w-3 h-3" /> أكمل</>
                    : <><Download className="w-3 h-3" /> تنزيل</>}
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
}
