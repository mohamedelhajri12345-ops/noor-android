import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { MapPin, Loader2, Search, Clock, ChevronDown, Globe } from 'lucide-react';
import { getPrayerTimes, getNextPrayer, getCountdown, formatTime12, toArabicNumber } from '@/lib/islamicUtils';
import { regions } from '@/data/cities';
import PullToRefresh from '@/components/PullToRefresh';
import GoogleCalendarSync from '@/components/GoogleCalendarSync';
import { useSearchParams } from 'react-router-dom';

export default function Prayer() {
  const [times, setTimes] = useState(null);
  const [loading, setLoading] = useState(true);
  const [nextPrayer, setNextPrayer] = useState(null);
  const [countdown, setCountdown] = useState(null);
  const [selectedCity, setSelectedCity] = useState(() => {
    const saved = localStorage.getItem('nur_selected_city');
    return saved ? JSON.parse(saved) : null;
  });
  const [searchParams, setSearchParams] = useSearchParams();
  const showPicker = searchParams.get('picker') === 'true';
  const [search, setSearch] = useState('');
  const [expandedRegion, setExpandedRegion] = useState(null);

  useEffect(() => {
    (async () => {
      let loc = selectedCity;
      if (!loc) {
        // Default to Makkah
        loc = { name: 'مكة المكرمة', country: 'السعودية', lat: 21.3891, lng: 39.8579 };
        setSelectedCity(loc);
        localStorage.setItem('nur_selected_city', JSON.stringify(loc));
      }
      if (loc) {
        const t = await getPrayerTimes(loc.lat, loc.lng);
        if (t) {
          setTimes(t);
          setNextPrayer(getNextPrayer(t));
          localStorage.setItem('nur_prayer_times', JSON.stringify(t));
        }
      }
      setLoading(false);
    })();
  }, [selectedCity]);

  useEffect(() => {
    if (!nextPrayer) return;
    const update = () => setCountdown(getCountdown(nextPrayer.time));
    update();
    const interval = setInterval(update, 1000);
    return () => clearInterval(interval);
  }, [nextPrayer]);

  const prayers = times ? [
    { name: 'الفجر', time: times.Fajr, icon: '🌅' },
    { name: 'الشروق', time: times.Sunrise, notPrayer: true, icon: '☀️' },
    { name: 'الظهر', time: times.Dhuhr, icon: '☀️' },
    { name: 'العصر', time: times.Asr, icon: '🌤️' },
    { name: 'المغرب', time: times.Maghrib, icon: '🌇' },
    { name: 'العشاء', time: times.Isha, icon: '🌙' },
  ] : [];

  const filteredRegions = useMemo(() => {
    if (!search) return regions;
    const q = search.toLowerCase();
    return regions.map(r => ({
      ...r,
      countries: r.countries
        .filter(c => c.name.includes(search) || c.cities.some(ci => ci.name.includes(search)))
        .map(c => ({
          ...c,
          cities: c.cities.filter(ci => ci.name.includes(search) || c.name.includes(search)),
        }))
        .filter(c => c.cities.length > 0),
    })).filter(r => r.countries.length > 0);
  }, [search]);

  const handleRefresh = useCallback(async () => {
    if (selectedCity) {
      const cacheKey = `nur_prayer_${Math.round(selectedCity.lat * 10)}_${Math.round(selectedCity.lng * 10)}_${new Date().toDateString()}`;
      localStorage.removeItem(cacheKey);
      const t = await getPrayerTimes(selectedCity.lat, selectedCity.lng);
      if (t) {
        setTimes(t);
        setNextPrayer(getNextPrayer(t));
      }
    }
  }, [selectedCity]);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="w-8 h-8 animate-spin text-gold" />
      </div>
    );
  }

  if (showPicker) {
    return (
      <div className="space-y-3 animate-fade-in">
        <button onClick={() => setSearchParams({})} className="text-muted-foreground hover:text-foreground text-sm">
          → رجوع
        </button>
        <h2 className="text-lg font-bold text-gold">اختر دولتك ومدينتك</h2>

        <div className="glass-card rounded-2xl p-3 flex items-center gap-2 sticky top-16 z-10">
          <Search className="w-4 h-4 text-muted-foreground" />
          <input
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="ابحث عن دولة أو مدينة..."
            className="flex-1 bg-transparent text-sm text-foreground placeholder:text-muted-foreground outline-none"
          />
        </div>

        <div className="space-y-2">
          {filteredRegions.map(region => (
            <div key={region.name} className="glass-card rounded-2xl overflow-hidden">
              <button
                onClick={() => setExpandedRegion(expandedRegion === region.name ? null : region.name)}
                className="w-full p-3 flex items-center justify-between text-right"
              >
                <span className="text-sm font-bold text-gold">{region.name}</span>
                <ChevronDown className={`w-4 h-4 text-muted-foreground transition-transform ${expandedRegion === region.name ? 'rotate-180' : ''}`} />
              </button>
              {expandedRegion === region.name && (
                <div className="px-3 pb-3 space-y-2">
                  {region.countries.map(country => (
                    <div key={country.name} className="rounded-xl bg-foreground/5 p-2">
                      <p className="text-xs font-bold text-foreground mb-1.5 px-1">{country.name}</p>
                      <div className="flex flex-wrap gap-1.5">
                        {country.cities.map(city => (
                          <button
                            key={`${country.name}-${city.name}`}
                            onClick={() => {
                              const selected = { ...city, country: country.name };
                              setSelectedCity(selected);
                              localStorage.setItem('nur_selected_city', JSON.stringify(selected));
                              setSearchParams({});
                              setLoading(true);
                            }}
                            className={`px-3 py-1.5 rounded-xl text-xs transition-all ${
                              selectedCity?.name === city.name && selectedCity?.country === country.name
                                ? 'bg-gold text-primary-foreground font-bold'
                                : 'glass text-foreground hover:text-gold'
                            }`}
                          >
                            {city.name}
                          </button>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <PullToRefresh onRefresh={handleRefresh}>
    <div className="space-y-4 animate-fade-in">
      <h2 className="text-xl font-bold text-gold text-center">أوقات الصلاة</h2>

      {/* Location selector */}
      <button
        onClick={() => setSearchParams({ picker: 'true' })}
        className="w-full glass-card rounded-2xl p-4 flex items-center gap-3 hover:glass-hover transition-all text-right"
      >
        <div className="w-10 h-10 rounded-xl bg-gold/10 flex items-center justify-center shrink-0">
          <Globe className="w-5 h-5 text-gold" />
        </div>
        <div className="flex-1">
          <p className="text-sm font-bold text-foreground">{selectedCity?.name || 'اختر مدينتك'}</p>
          <p className="text-xs text-muted-foreground">{selectedCity?.country || ''}</p>
        </div>
        <ChevronDown className="w-4 h-4 text-muted-foreground" />
      </button>

      {nextPrayer && countdown && (
        <div className="glass-card rounded-2xl p-5 text-center">
          <p className="text-xs text-muted-foreground">الصلاة القادمة</p>
          <p className="text-2xl font-bold text-gold mt-1">{nextPrayer.name}</p>
          <p className="text-sm text-foreground mt-1">{formatTime12(nextPrayer.time)}</p>
          <p className="text-3xl font-mono text-foreground mt-3">
            {toArabicNumber(String(countdown.hours).padStart(2, '0'))}:{toArabicNumber(String(countdown.minutes).padStart(2, '0'))}:{toArabicNumber(String(countdown.seconds).padStart(2, '0'))}
          </p>
        </div>
      )}

      {/* Prayer times list */}
      <div className="space-y-2">
        {prayers.map(p => (
          <div key={p.name} className={`glass-card rounded-2xl p-4 flex items-center justify-between ${p.notPrayer ? 'opacity-50' : ''}`}>
            <div className="flex items-center gap-3">
              <span className="text-xl">{p.icon}</span>
              <p className={`text-sm font-bold ${p.notPrayer ? 'text-muted-foreground' : 'text-foreground'}`}>{p.name}</p>
            </div>
            <p className={`text-sm font-mono ${p.notPrayer ? 'text-muted-foreground' : 'text-gold'}`}>{formatTime12(p.time)}</p>
          </div>
        ))}
      </div>

      {/* Google Calendar sync — native prayer alerts on phone */}
      <div className="pt-1">
        <GoogleCalendarSync location={selectedCity} />
      </div>

      {!times && (
        <div className="text-center text-muted-foreground py-8">
          <p>تعذّر الحصول على أوقات الصلاة</p>
          <button onClick={() => setSearchParams({ picker: 'true' })} className="text-xs text-gold mt-2 hover:underline">
            اختر مدينتك
          </button>
        </div>
      )}
    </div>
    </PullToRefresh>
  );
}
