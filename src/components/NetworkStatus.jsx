import React, { useState, useEffect } from 'react';
import { WifiOff } from 'lucide-react';

export default function NetworkStatus() {
  const [online, setOnline] = useState(navigator.onLine);
  const [showNotice, setShowNotice] = useState(false);

  useEffect(() => {
    const handleOffline = () => {
      setOnline(false);
      setShowNotice(true);
    };
    const handleOnline = () => {
      setOnline(true);
      setShowNotice(false);
    };
    window.addEventListener('offline', handleOffline);
    window.addEventListener('online', handleOnline);
    return () => {
      window.removeEventListener('offline', handleOffline);
      window.removeEventListener('online', handleOnline);
    };
  }, []);

  if (online || !showNotice) return null;

  return (
    <div className="fixed top-16 left-1/2 -translate-x-1/2 z-50 glass-card rounded-full px-4 py-2 flex items-center gap-2 text-xs text-gold animate-fade-in">
      <WifiOff className="w-3.5 h-3.5" />
      بعض الميزات تحتاج اتصالاً بالإنترنت
    </div>
  );
}
