import React, { useState, useRef } from 'react';
import { Loader2 } from 'lucide-react';

// Generic pull-to-refresh wrapper — works with both scrollable containers and window scroll
export default function PullToRefresh({ onRefresh, children, className }) {
  const [pullDistance, setPullDistance] = useState(0);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const ptrStartY = useRef(0);
  const ptrPulling = useRef(false);

  const onPtrStart = (e) => {
    const el = e.currentTarget;
    const scrollable = el.scrollHeight > el.clientHeight;
    const atTop = scrollable ? el.scrollTop === 0 : window.scrollY === 0;
    if (atTop) {
      ptrStartY.current = e.touches[0].clientY;
      ptrPulling.current = true;
    }
  };

  const onPtrMove = (e) => {
    if (!ptrPulling.current) return;
    const pull = e.touches[0].clientY - ptrStartY.current;
    if (pull > 0 && pull < 120) setPullDistance(pull);
  };

  const onPtrEnd = async () => {
    if (!ptrPulling.current) return;
    ptrPulling.current = false;
    if (pullDistance > 60) {
      setIsRefreshing(true);
      setPullDistance(0);
      try { await onRefresh(); } catch { /* */ }
      setIsRefreshing(false);
    } else {
      setPullDistance(0);
    }
  };

  return (
    <div className={className} onTouchStart={onPtrStart} onTouchMove={onPtrMove} onTouchEnd={onPtrEnd}>
      {(pullDistance > 0 || isRefreshing) && (
        <div className="flex items-center justify-center overflow-hidden transition-all" style={{ height: isRefreshing ? 40 : Math.min(pullDistance, 80) }}>
          {isRefreshing ? (
            <Loader2 className="w-5 h-5 animate-spin text-gold" />
          ) : (
            <span className="text-xs text-muted-foreground">
              {pullDistance > 60 ? 'تحديث...' : '↓ اسحب للتحديث'}
            </span>
          )}
        </div>
      )}
      {children}
    </div>
  );
}
