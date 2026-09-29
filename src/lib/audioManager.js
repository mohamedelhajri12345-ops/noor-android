// Singleton audio manager — clean, reliable, instant playback.
// NO crossOrigin (causes CORS rejection on many CDNs).
// Single Audio element reused for all playback.

class AudioManager {
  constructor() {
    this.audio = new Audio();
    this.audio.preload = 'auto';
    this._setupAudioEvents();

    this.currentId = null;
    this.listeners = new Set();
    this.currentMeta = null;

    this.queue = [];
    this.currentIndex = -1;
    this.shuffle = false;
    this.repeatMode = 'none';

    this.rate = 1;
    this.isBuffering = false;
    this.isLoading = false;
    this._fallbackIndex = 0;
    this.sleepTimerId = null;
    this.sleepUntilEnd = false;
    this.gapTimeoutId = null;
    this.wakeLock = null;

    // Re-acquire wake lock when page becomes visible again
    if (typeof document !== 'undefined') {
      document.addEventListener('visibilitychange', () => {
        if (this.wakeLock && document.visibilityState === 'visible' && !this.audio.paused) {
          this._acquireWakeLock();
        }
      });
    }
  }

  _acquireWakeLock() {
    if (this.wakeLock) return;
    if ('wakeLock' in navigator) {
      navigator.wakeLock.request('screen').then(wl => {
        this.wakeLock = wl;
      }).catch(() => {});
    }
  }

  _releaseWakeLock() {
    if (this.wakeLock) {
      this.wakeLock.release().catch(() => {});
      this.wakeLock = null;
    }
  }

  subscribe(cb) {
    this.listeners.add(cb);
    return () => this.listeners.delete(cb);
  }

  notify() {
    const state = this.getState();
    this.listeners.forEach(cb => cb(state));
  }

  getState() {
    return {
      isPlaying: !this.audio.paused && !this.audio.ended,
      currentId: this.currentId,
      currentTime: this.audio ? this.audio.currentTime : 0,
      duration: this.audio && this.audio.duration ? this.audio.duration : 0,
      meta: this.currentMeta,
      queue: this.queue,
      currentIndex: this.currentIndex,
      queueLength: this.queue.length,
      shuffle: this.shuffle,
      repeatMode: this.repeatMode,
      rate: this.rate,
      isBuffering: this.isBuffering,
      isLoading: this.isLoading,
      sleepTimerActive: !!this.sleepTimerId || this.sleepUntilEnd,
    };
  }

  playQueue(tracks, startIndex = 0) {
    if (!tracks || tracks.length === 0) return;
    this.queue = tracks;
    this.currentIndex = startIndex;
    this._playCurrent();
  }

  play(id, url, meta = {}, fallbackUrls = []) {
    if (this.currentId === id) {
      this.toggle();
      return;
    }
    this.queue = [{ id, url, meta, fallbackUrls }];
    this.currentIndex = 0;
    this._playCurrent();
  }

  _playCurrent() {
    if (this.currentIndex < 0 || this.currentIndex >= this.queue.length) return;
    const track = this.queue[this.currentIndex];
    if (!track) return;

    if (this.gapTimeoutId) { clearTimeout(this.gapTimeoutId); this.gapTimeoutId = null; }
    this.audio.pause();
    try { this.audio.currentTime = 0; } catch {}

    this.isBuffering = true;
    this.isLoading = true;
    this.currentId = track.id;
    this.currentMeta = track.meta || {};
    this.audio.playbackRate = this.rate;
    this._fallbackIndex = 0;
    this.audio.src = track.url;

    const playPromise = this.audio.play();
    if (playPromise && typeof playPromise.catch === 'function') {
      playPromise.catch(() => {
        this.isLoading = false;
        this.isBuffering = false;
        this.notify();
      });
    }

    this._acquireWakeLock();
    this._updateMediaSession();
    this.notify();
  }

  _setupAudioEvents() {
    this.audio.addEventListener('timeupdate', () => this.notify());
    this.audio.addEventListener('loadedmetadata', () => this.notify());
    this.audio.addEventListener('durationchange', () => this.notify());
    this.audio.addEventListener('ended', () => this._onTrackEnd());
    this.audio.addEventListener('error', () => {
      const track = this.queue[this.currentIndex];
      if (track && this._retryWithFallback()) return;
      this.isLoading = false;
      this.isBuffering = false;
      if (track?.id) {
        window.dispatchEvent(new CustomEvent('audio-track-broken', { detail: { id: track.id } }));
      }
      window.dispatchEvent(new CustomEvent('audio-error', {
        detail: { message: 'تعذّر تشغيل الملف', retry: () => this.retry(), changeSource: () => this.next() }
      }));
      this.notify();
    });
    this.audio.addEventListener('waiting', () => { this.isBuffering = true; this.notify(); });
    this.audio.addEventListener('playing', () => {
      this.isBuffering = false;
      this.isLoading = false;
      this.notify();
    });
    this.audio.addEventListener('canplay', () => {
      this.isBuffering = false;
      this.isLoading = false;
      this.notify();
    });
    this.audio.addEventListener('stalled', () => { this.isBuffering = true; this.notify(); });
  }

  retry() {
    this._playCurrent();
  }

  _retryWithFallback() {
    const track = this.queue[this.currentIndex];
    if (!track?.fallbackUrls || this._fallbackIndex >= track.fallbackUrls.length) return false;
    const url = track.fallbackUrls[this._fallbackIndex];
    this._fallbackIndex++;
    this.isBuffering = true;
    this.audio.src = url;
    this.audio.play().catch(() => {});
    this.notify();
    return true;
  }

  _onTrackEnd() {
    if (this.sleepUntilEnd) {
      this.sleepUntilEnd = false;
      this._stopAudio();
      this.currentId = null;
      this.currentMeta = null;
      this.notify();
      return;
    }
    if (this.repeatMode === 'one') {
      this._playCurrent();
      return;
    }
    this.gapTimeoutId = setTimeout(() => this.next(), 300);
  }

  _stopAudio() {
    if (this.gapTimeoutId) { clearTimeout(this.gapTimeoutId); this.gapTimeoutId = null; }
    this._releaseWakeLock();
    this.audio.pause();
    try { this.audio.currentTime = 0; } catch {}
    this.audio.removeAttribute('src');
    try { this.audio.load(); } catch {}
    this.isBuffering = false;
    this.isLoading = false;
  }

  next() {
    if (this.queue.length === 0) return;
    if (this.shuffle) {
      let nextIdx;
      do { nextIdx = Math.floor(Math.random() * this.queue.length); }
      while (nextIdx === this.currentIndex && this.queue.length > 1);
      this.currentIndex = nextIdx;
    } else {
      this.currentIndex++;
      if (this.currentIndex >= this.queue.length) {
        if (this.repeatMode === 'all') {
          this.currentIndex = 0;
        } else {
          this._stopAudio();
          this.currentId = null;
          this.currentMeta = null;
          this.notify();
          return;
        }
      }
    }
    this._playCurrent();
  }

  prev() {
    if (this.queue.length === 0) return;
    if (this.audio && this.audio.currentTime > 3) {
      this.audio.currentTime = 0;
      this.notify();
      return;
    }
    this.currentIndex--;
    if (this.currentIndex < 0) {
      this.currentIndex = this.repeatMode === 'all' ? this.queue.length - 1 : 0;
    }
    this._playCurrent();
  }

  toggle() {
    if (!this.audio || !this.currentId) return;
    if (this.audio.paused) {
      this.audio.play().catch(() => {});
      this._acquireWakeLock();
    } else {
      this.audio.pause();
      this._releaseWakeLock();
    }
    this._updateMediaSession();
    this.notify();
  }

  stop() {
    this._stopAudio();
    this.clearSleepTimer();
    this.currentId = null;
    this.currentMeta = null;
    this.queue = [];
    this.currentIndex = -1;
    this.notify();
  }

  seek(time) {
    if (this.audio && this.audio.duration) {
      this.audio.currentTime = Math.max(0, Math.min(time, this.audio.duration));
      this.notify();
    }
  }

  setRate(rate) {
    this.rate = rate;
    if (this.audio) this.audio.playbackRate = rate;
    this.notify();
  }

  toggleShuffle() {
    this.shuffle = !this.shuffle;
    this.notify();
  }

  toggleRepeat() {
    const modes = ['none', 'all', 'one'];
    const idx = modes.indexOf(this.repeatMode);
    this.repeatMode = modes[(idx + 1) % modes.length];
    this.notify();
  }

  setSleepTimer(minutes) {
    this.clearSleepTimer();
    if (minutes === -1) {
      this.sleepUntilEnd = true;
      this.notify();
      return;
    }
    if (minutes > 0) {
      this.sleepTimerId = setTimeout(() => {
        this._stopAudio();
        this.sleepTimerId = null;
        this.currentId = null;
        this.currentMeta = null;
        this.notify();
      }, minutes * 60 * 1000);
      this.notify();
    }
  }

  clearSleepTimer() {
    if (this.sleepTimerId) {
      clearTimeout(this.sleepTimerId);
      this.sleepTimerId = null;
    }
    this.sleepUntilEnd = false;
    this.notify();
  }

  _updateMediaSession() {
    if (!('mediaSession' in navigator)) return;
    try {
      navigator.mediaSession.metadata = new MediaMetadata({
        title: this.currentMeta?.title || 'نور',
        artist: this.currentMeta?.artist || '',
        album: 'نور — تطبيق الإسلام الشامل',
      });
      navigator.mediaSession.playbackState = this.audio.paused ? 'paused' : 'playing';
      navigator.mediaSession.setActionHandler('play', () => this.toggle());
      navigator.mediaSession.setActionHandler('pause', () => this.toggle());
      navigator.mediaSession.setActionHandler('previoustrack', () => this.prev());
      navigator.mediaSession.setActionHandler('nexttrack', () => this.next());
      navigator.mediaSession.setActionHandler('seekto', (d) => { if (d.seekTime != null) this.seek(d.seekTime); });
      navigator.mediaSession.setActionHandler('stop', () => this.stop());
    } catch { /* */ }
  }
}

export const audioManager = new AudioManager();
