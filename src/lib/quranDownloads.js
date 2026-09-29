// Offline Quran audio downloads — module-level singleton so downloads
// PERSIST across component unmounts (background downloads continue even
// when the user navigates away or switches pages).
import { surahs, getSurahAudioUrl, getSurahAudioFallbacks } from '@/data/surahs';

const CACHE_NAMES = {
  alafasy: 'quran-audio-alafasy',
  dossari: 'quran-audio-dossari',
};

const PROGRESS_KEY = 'nur_quran_downloads';

const getProgressMap = () => {
  try { return JSON.parse(localStorage.getItem(PROGRESS_KEY) || '{}'); } catch { return {}; }
};
const saveProgressMap = (m) => {
  try { localStorage.setItem(PROGRESS_KEY, JSON.stringify(m)); } catch { /* */ }
};

export const getDownloadedSurahs = (reciterId) => {
  const m = getProgressMap();
  return m[reciterId] || [];
};

export const isSurahDownloaded = (reciterId, surahNumber) => {
  const m = getProgressMap();
  return (m[reciterId] || []).includes(surahNumber);
};

// Returns a blob object URL if the surah is cached offline, else null.
// Tries primary URL first, then fallbacks.
export const getCachedAudioUrl = async (reciterId, surahNumber) => {
  try {
    const cache = await caches.open(CACHE_NAMES[reciterId]);
    const urls = [getSurahAudioUrl(surahNumber, reciterId), ...getSurahAudioFallbacks(surahNumber, reciterId)];
    for (const url of urls) {
      const match = await cache.match(url);
      if (match) {
        const blob = await match.blob();
        return URL.createObjectURL(blob);
      }
    }
  } catch { /* */ }
  return null;
};

// ═══ Module-level download manager — persists across component unmounts ═══
const downloadManager = {
  active: new Map(), // reciterId -> { done, total, callbacks: Set, abort: boolean }

  start(reciterId, onProgress) {
    if (this.active.has(reciterId)) {
      this.active.get(reciterId).callbacks.add(onProgress);
      return;
    }
    const done = getDownloadedSurahs(reciterId).length;
    this.active.set(reciterId, { done, total: 114, callbacks: new Set([onProgress]), abort: false });
    this._run(reciterId);
  },

  stop(reciterId) {
    const state = this.active.get(reciterId);
    if (state) state.abort = true;
    this.active.delete(reciterId);
  },

  isDownloading(reciterId) { return this.active.has(reciterId); },

  getProgress(reciterId) { return this.active.get(reciterId); },

  subscribe(reciterId, callback) {
    if (this.active.has(reciterId)) {
      this.active.get(reciterId).callbacks.add(callback);
      return () => this.active.get(reciterId)?.callbacks.delete(callback);
    }
    return () => {};
  },

  async _run(reciterId) {
    const cache = await caches.open(CACHE_NAMES[reciterId]);
    const m = getProgressMap();
    const done = new Set(m[reciterId] || []);
    const toDownload = surahs.filter(s => !done.has(s.number));
    const MAX_PARALLEL = 20; // Increased from 12 for faster downloads
    let cursor = 0;

    const worker = async () => {
      while (cursor < toDownload.length) {
        const state = this.active.get(reciterId);
        if (!state || state.abort) return;
        const i = cursor++;
        const surah = toDownload[i];
        if (!surah) break;
        // Try primary URL first, then fallbacks for reliability
        const urls = [getSurahAudioUrl(surah.number, reciterId), ...getSurahAudioFallbacks(surah.number, reciterId)];
        for (const url of urls) {
          try {
            const res = await fetch(url);
            if (res.ok) {
              await cache.put(url, res.clone());
              done.add(surah.number);
              break;
            }
          } catch { /* try next URL */ }
        }
        m[reciterId] = [...done];
        saveProgressMap(m);
        const s = this.active.get(reciterId);
        if (s) {
          s.done = done.size;
          s.callbacks.forEach(cb => cb(done.size, 114));
        }
      }
    };

    const workers = Array.from({ length: Math.min(MAX_PARALLEL, toDownload.length) }, () => worker());
    await Promise.all(workers);
    this.active.delete(reciterId);
  },
};

export const startDownload = (reciterId, onProgress) => downloadManager.start(reciterId, onProgress);
export const stopDownload = (reciterId) => downloadManager.stop(reciterId);
export const isDownloading = (reciterId) => downloadManager.isDownloading(reciterId);
export const getDownloadProgress = (reciterId) => downloadManager.getProgress(reciterId);
export const subscribeToDownload = (reciterId, callback) => downloadManager.subscribe(reciterId, callback);

// Backward compatibility
export const downloadAllReciter = async (reciterId, onProgress) => {
  return new Promise((resolve) => {
    downloadManager.start(reciterId, (done, total) => {
      onProgress?.(done, total);
      if (done >= total) resolve(done);
    });
  });
};

export const deleteReciterDownloads = async (reciterId) => {
  downloadManager.stop(reciterId);
  await caches.delete(CACHE_NAMES[reciterId]);
  const m = getProgressMap();
  delete m[reciterId];
  saveProgressMap(m);
};
