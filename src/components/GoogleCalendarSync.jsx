import React, { useState, useEffect } from 'react';
import { Calendar, Loader2, CheckCircle2, Link2, LogIn, RefreshCw, AlertCircle } from 'lucide-react';
import { base44 } from '@/api/base44Client';

const CONNECTOR_ID = '6a89b87623c194840fcc4eb1';

// Google Calendar sync — creates prayer time events with native Android reminders
// This solves the Android APK notification problem: Google Calendar sends native push

export default function GoogleCalendarSync({ location }) {
  const [authed, setAuthed] = useState(false);
  const [connected, setConnected] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [syncResult, setSyncResult] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const isAuthed = await base44.auth.isAuthenticated();
        setAuthed(isAuthed);
        if (isAuthed && location) {
          await checkConnection();
        }
      } catch { /* */ }
      setLoading(false);
    })();
  }, [location]);

  const checkConnection = async () => {
    try {
      const res = await base44.functions.invoke('syncPrayerToCalendar', {
        lat: location?.lat,
        lng: location?.lng,
        checkOnly: true,
      });
      setConnected(res?.connected === true);
    } catch {
      setConnected(false);
    }
  };

  const handleConnect = async () => {
    if (!authed) {
      base44.auth.redirectToLogin();
      return;
    }
    try {
      const url = await base44.connectors.connectAppUser(CONNECTOR_ID);
      const popup = window.open(url, '_blank');
      const timer = setInterval(() => {
        if (!popup || popup.closed) {
          clearInterval(timer);
          checkConnection();
        }
      }, 500);
    } catch (err) {
      console.error('Connect error:', err);
    }
  };

  const handleSync = async () => {
    setSyncing(true);
    setSyncResult(null);
    try {
      const res = await base44.functions.invoke('syncPrayerToCalendar', {
        lat: location?.lat,
        lng: location?.lng,
      });
      setSyncResult(res);
      setConnected(true);
    } catch (err) {
      setSyncResult({ error: err.message || 'فشل التزامن' });
    }
    setSyncing(false);
  };

  if (loading) {
    return (
      <div className="w-full glass-card rounded-2xl p-3 flex items-center justify-center gap-2 text-sm text-muted-foreground">
        <Loader2 className="w-4 h-4 animate-spin" />
        <span>جاري التحقق من التقويم...</span>
      </div>
    );
  }

  if (!authed) {
    return (
      <button
        onClick={() => base44.auth.redirectToLogin()}
        className="w-full glass-card rounded-2xl p-3 flex items-center gap-2 text-sm text-gold hover:glass-hover transition-all"
      >
        <LogIn className="w-4 h-4" /> سجّل الدخول لمزامنة التقويم
      </button>
    );
  }

  if (!connected) {
    return (
      <button
        onClick={handleConnect}
        className="w-full glass-card rounded-2xl p-3 flex items-center gap-2 text-sm text-gold hover:glass-hover transition-all"
      >
        <Link2 className="w-4 h-4" /> اربط بتقويم جوجل لتنبيهات الأذان
      </button>
    );
  }

  return (
    <div className="space-y-2">
      <button
        onClick={handleSync}
        disabled={syncing}
        className="w-full glass-card rounded-2xl p-3 flex items-center justify-center gap-2 text-sm text-gold hover:glass-hover disabled:opacity-50 transition-all"
      >
        {syncing ? (
          <>
            <Loader2 className="w-4 h-4 animate-spin" />
            <span>جاري إضافة المواقيت...</span>
          </>
        ) : (
          <>
            <RefreshCw className="w-4 h-4" />
            <span>مزامنة مواقيت اليوم مع التقويم</span>
          </>
        )}
      </button>
      {syncResult?.success && (
        <div className="glass-card rounded-xl p-2.5 flex items-start gap-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
          <div className="text-xs">
            <p className="text-emerald-400 font-bold">
              تمت إضافة {syncResult.count} مواقيت صلاة لتقويمك
            </p>
            {syncResult.skipped > 0 && (
              <p className="text-muted-foreground mt-0.5">
                تم تخطي {syncResult.skipped} لأنها موجودة مسبقاً
              </p>
            )}
            <p className="text-muted-foreground mt-0.5">
              ستصلك تنبيهات جوجل قبل كل صلاة ✓
            </p>
          </div>
        </div>
      )}
      {syncResult?.error && (
        <div className="glass-card rounded-xl p-2.5 flex items-center gap-2">
          <AlertCircle className="w-4 h-4 text-destructive shrink-0" />
          <p className="text-xs text-destructive">{syncResult.error}</p>
        </div>
      )}
      {!syncResult && (
        <p className="text-xs text-muted-foreground text-center px-2 flex items-center justify-center gap-1">
          <Calendar className="w-3 h-3" /> مرتبط بجوجل — مزامنة اليوم تضيف تنبيهات أصيلة
        </p>
      )}
    </div>
  );
}
