import React, { useState, useEffect, useRef } from 'react';
import { Play, Pause, X, SkipBack, SkipForward, Repeat, Repeat1, Shuffle, Gauge, Moon, ChevronUp, ChevronDown } from 'lucide-react';
import { audioManager } from '@/lib/audioManager';
import { toArabicNumber } from '@/lib/islamicUtils';

const SPEEDS = [0.75, 1, 1.25, 1.5];
const SLEEP_OPTIONS = [
  { label: 'إيقاف', value: 0 },
  { label: 'نهاية السورة', value: -1 },
  { label: '٥ دقائق', value: 5 },
  { label: '١٥ دقيقة', value: 15 },
  { label: '٣٠ دقيقة', value: 30 },
  { label: '٦٠ دقيقة', value: 60 },
];

export default function AudioPlayerBar() {
  const [state, setState] = useState(audioManager.getState());
  const [expanded, setExpanded] = useState(false);
  const [showSpeed, setShowSpeed] = useState(false);
  const [showSleep, setShowSleep] = useState(false);
  const [sleepMinutes, setSleepMinutes] = useState(0);
  const [audioError, setAudioError] = useState(null);
  const barRef = useRef(null);

  useEffect(() => {
    return audioManager.subscribe(setState);
  }, []);

  useEffect(() => {
    const handler = (e) => {
      setAudioError(e.detail?.message || 'خطأ في التشغيل');
      setTimeout(() => setAudioError(null), 5000);
    };
    window.addEventListener('audio-error', handler);
    return () => window.removeEventListener('audio-error', handler);
  }, []);

  if (!state.meta || (!state.isPlaying && !state.currentId)) return null;

  const progress = state.duration ? (state.currentTime / state.duration) * 100 : 0;
  const meta = state.meta || {};

  const formatTime = (sec) => {
    if (!sec || isNaN(sec)) return '٠:٠٠';
    const m = Math.floor(sec / 60);
    const s = Math.floor(sec % 60);
    return `${toArabicNumber(m)}:${toArabicNumber(String(s).padStart(2, '0'))}`;
  };

  const handleSeek = (e) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const x = e.clientX || (e.touches && e.touches[0]?.clientX) || 0;
    const pct = Math.max(0, Math.min(1, (rect.width - (x - rect.left)) / rect.width)); // RTL
    audioManager.seek(pct * state.duration);
  };

  return (
    <>
      {/* Expanded full player */}
      {expanded && (
        <div className="fixed inset-0 z-50 flex flex-col justify-end bg-black/50 backdrop-blur-sm" onClick={() => setExpanded(false)}>
          <div ref={barRef} className="glass rounded-t-3xl p-5 pb-8 animate-slide-up max-w-2xl mx-auto w-full" onClick={e => e.stopPropagation()}>
            {/* Drag handle */}
            <div className="w-12 h-1 rounded-full bg-muted-foreground/30 mx-auto mb-4" />

            {/* Title */}
            <div className="text-center mb-4">
              <p className="text-lg font-bold text-foreground truncate">{meta.title || 'قيد التشغيل'}</p>
              <p className="text-sm text-gold truncate">{meta.artist || ''}</p>
            </div>

            {/* Progress bar — seekable */}
            <div className="mb-2" onClick={handleSeek} style={{ cursor: 'pointer' }}>
              <div className="h-2 rounded-full bg-muted overflow-hidden relative">
                <div className="h-full bg-gold-gradient absolute right-0" style={{ width: `${progress}%` }} />
              </div>
            </div>
            <div className="flex items-center justify-between text-xs text-muted-foreground mb-4">
              <span>{formatTime(state.currentTime)}</span>
              <span>{formatTime(state.duration)}</span>
            </div>

            {/* Main controls */}
            <div className="flex items-center justify-center gap-4 mb-4">
              <button onClick={() => audioManager.toggleShuffle()} className={`w-10 h-10 rounded-full flex items-center justify-center transition-colors ${state.shuffle ? 'text-gold bg-gold/10' : 'text-muted-foreground'}`}>
                <Shuffle className="w-4 h-4" />
              </button>
              <button onClick={() => audioManager.prev()} disabled={state.queueLength <= 1} className="w-12 h-12 rounded-full glass-card flex items-center justify-center text-foreground disabled:opacity-30">
                <SkipForward className="w-5 h-5" />
              </button>
              <button onClick={() => audioManager.toggle()} className="w-16 h-16 rounded-full bg-gold-gradient flex items-center justify-center text-primary-foreground active:scale-95 transition-transform">
                {state.isPlaying ? <Pause className="w-7 h-7" /> : <Play className="w-7 h-7 mr-0.5" />}
              </button>
              <button onClick={() => audioManager.next()} disabled={state.queueLength <= 1} className="w-12 h-12 rounded-full glass-card flex items-center justify-center text-foreground disabled:opacity-30">
                <SkipBack className="w-5 h-5" />
              </button>
              <button onClick={() => audioManager.toggleRepeat()} className={`w-10 h-10 rounded-full flex items-center justify-center transition-colors ${state.repeatMode !== 'none' ? 'text-gold bg-gold/10' : 'text-muted-foreground'}`}>
                {state.repeatMode === 'one' ? <Repeat1 className="w-4 h-4" /> : <Repeat className="w-4 h-4" />}
              </button>
            </div>

            {/* Secondary controls */}
            <div className="flex items-center justify-center gap-3">
              {/* Speed */}
              <div className="relative">
                <button onClick={() => { setShowSpeed(!showSpeed); setShowSleep(false); }} className="px-3 py-1.5 rounded-lg glass-card text-xs text-foreground flex items-center gap-1">
                  <Gauge className="w-3.5 h-3.5" /> {toArabicNumber(state.rate)}×
                </button>
                {showSpeed && (
                  <div className="absolute bottom-full mb-2 glass rounded-xl p-2 flex flex-col gap-1 min-w-[100px]">
                    {SPEEDS.map(s => (
                      <button key={s} onClick={() => { audioManager.setRate(s); setShowSpeed(false); }}
                        className={`px-3 py-1.5 rounded-lg text-xs text-right ${state.rate === s ? 'bg-gold text-primary-foreground font-bold' : 'text-foreground'}`}>
                        {toArabicNumber(s)}×
                      </button>
                    ))}
                  </div>
                )}
              </div>

              {/* Sleep timer */}
              <div className="relative">
                <button onClick={() => { setShowSleep(!showSleep); setShowSpeed(false); }} className={`px-3 py-1.5 rounded-lg glass-card text-xs flex items-center gap-1 ${state.sleepTimerActive ? 'text-gold' : 'text-foreground'}`}>
                  <Moon className="w-3.5 h-3.5" /> مؤقت
                </button>
                {showSleep && (
                  <div className="absolute bottom-full mb-2 glass rounded-xl p-2 flex flex-col gap-1 min-w-[120px]">
                    {SLEEP_OPTIONS.map(opt => (
                      <button key={opt.value} onClick={() => { audioManager.setSleepTimer(opt.value); setSleepMinutes(opt.value); setShowSleep(false); }}
                        className={`px-3 py-1.5 rounded-lg text-xs text-right ${sleepMinutes === opt.value ? 'bg-gold text-primary-foreground font-bold' : 'text-foreground'}`}>
                        {opt.label}
                      </button>
                    ))}
                  </div>
                )}
              </div>

              <button onClick={() => audioManager.stop()} className="px-3 py-1.5 rounded-lg glass-card text-xs text-destructive flex items-center gap-1">
                <X className="w-3.5 h-3.5" /> إيقاف
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Mini bar */}
      <div className="fixed bottom-14 left-0 right-0 z-40 px-3">
        <div className="max-w-2xl mx-auto glass rounded-2xl p-2.5 flex items-center gap-2.5 soft-shadow animate-slide-up">
          {/* Play/Pause */}
          <button onClick={() => audioManager.toggle()} className="w-10 h-10 rounded-full bg-gold-gradient flex items-center justify-center text-primary-foreground shrink-0 active:scale-95 transition-transform">
            {state.isPlaying ? <Pause className="w-5 h-5" /> : <Play className="w-5 h-5 mr-0.5" />}
          </button>

          {/* Title + Artist + mini progress */}
          <button onClick={() => setExpanded(true)} className="flex-1 min-w-0 text-right">
            <p className="text-sm font-bold text-foreground truncate">{meta.title || 'قيد التشغيل'}</p>
            <div className="flex items-center gap-2">
              <p className="text-xs text-muted-foreground truncate flex-1">{meta.artist || ''}</p>
              <div className="h-1 rounded-full bg-muted overflow-hidden w-16 shrink-0">
                <div className="h-full bg-gold-gradient" style={{ width: `${progress}%` }} />
              </div>
            </div>
          </button>

          {/* Next (if queue) */}
          {state.queueLength > 1 && (
            <button onClick={() => audioManager.next()} className="w-8 h-8 rounded-full glass-card flex items-center justify-center text-foreground shrink-0">
              <SkipBack className="w-4 h-4" />
            </button>
          )}

          {/* Expand */}
          <button onClick={() => setExpanded(true)} className="w-8 h-8 rounded-full glass-card flex items-center justify-center text-muted-foreground shrink-0">
            <ChevronUp className="w-4 h-4" />
          </button>

          {/* Buffering indicator */}
          {state.isBuffering && (
            <div className="w-6 h-6 rounded-full border-2 border-gold/30 border-t-gold animate-spin shrink-0" />
          )}
        </div>
      </div>

      {/* Audio error toast */}
      {audioError && (
        <div className="fixed bottom-28 left-1/2 -translate-x-1/2 z-50 max-w-sm w-[90%] animate-slide-up">
          <div className="glass rounded-2xl p-3 flex items-center gap-3 border border-destructive/30 soft-shadow">
            <div className="w-8 h-8 rounded-full bg-destructive/15 flex items-center justify-center shrink-0">
              <X className="w-4 h-4 text-destructive" />
            </div>
            <p className="text-xs text-foreground flex-1">{audioError}</p>
            <button onClick={() => { audioManager.retry(); setAudioError(null); }}
              className="px-3 py-1.5 rounded-lg bg-gold/15 text-gold text-xs font-bold shrink-0">
              إعادة
            </button>
          </div>
        </div>
      )}
    </>
  );
}
