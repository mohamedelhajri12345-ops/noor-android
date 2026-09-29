import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { MapPin, Navigation, Compass, ChevronDown, Search, Locate, ChevronRight } from 'lucide-react';
import { cities } from '@/data/cities';
import { toArabicNumber } from '@/lib/islamicUtils';

const MECCA = { lat: 21.4225, lng: 39.8262 };

// Calculate Qibla bearing from given coordinates
const calculateQibla = (lat, lng) => {
  const φ1 = lat * Math.PI / 180;
  const φ2 = MECCA.lat * Math.PI / 180;
  const Δλ = (MECCA.lng - lng) * Math.PI / 180;
  const y = Math.sin(Δλ);
  const x = Math.cos(φ1) * Math.tan(φ2) - Math.sin(φ1) * Math.cos(Δλ);
  let θ = Math.atan2(y, x) * 180 / Math.PI;
  return (θ + 360) % 360;
};

export default function Qibla() {
  const navigate = useNavigate();
  const [location, setLocation] = useState(null);
  const [cityName, setCityName] = useState('');
  const [qibla, setQibla] = useState(null);
  const [loading, setLoading] = useState(false);
  const [heading, setHeading] = useState(0);
  const [searchParams, setSearchParams] = useSearchParams();
  const showCityPicker = searchParams.get('picker') === 'true';
  const [searchCity, setSearchCity] = useState('');
  const [needsCalibration, setNeedsCalibration] = useState(false);
  const [permissionDenied, setPermissionDenied] = useState(false);
  const compassSupported = useRef(false);

  // Try GPS first, fall back to last known location or city picker
  const requestLocation = async () => {
    setLoading(true);
    setPermissionDenied(false);

    // Check for last known location in localStorage
    const saved = localStorage.getItem('nur_last_location');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        setLocation(parsed);
        setQibla(calculateQibla(parsed.lat, parsed.lng));
        setCityName(localStorage.getItem('nur_last_city') || 'آخر موقع معروف');
      } catch { /* */ }
    }

    if (!navigator.geolocation) {
      setPermissionDenied(true);
      setLoading(false);
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const loc = { lat: pos.coords.latitude, lng: pos.coords.longitude };
        setLocation(loc);
        setQibla(calculateQibla(loc.lat, loc.lng));
        setCityName('موقعك الحالي');
        setPermissionDenied(false);
        localStorage.setItem('nur_last_location', JSON.stringify(loc));
        localStorage.setItem('nur_last_city', 'موقعك الحالي');
        setLoading(false);
      },
      (err) => {
        setPermissionDenied(true);
        setLoading(false);
        // If no saved location, show city picker automatically
        if (!saved) setSearchParams({ picker: 'true' });
      },
      { enableHighAccuracy: true, timeout: 8000, maximumAge: 300000 }
    );
  };

  // Initialize — try GPS, but don't block on it
  useEffect(() => {
    requestLocation();
  }, []);

  // Compass / device orientation
  useEffect(() => {
    const handler = (e) => {
      let alpha = null;
      if (e.absolute || e.webkitCompassHeading !== undefined) {
        alpha = e.webkitCompassHeading ?? (e.alpha !== null ? 360 - e.alpha : null);
      }
      if (alpha !== null && alpha !== undefined && !isNaN(alpha)) {
        compassSupported.current = true;
        setHeading(alpha);
        setNeedsCalibration(false);
      }
    };

    // Request permission on iOS 13+
    if (typeof DeviceOrientationEvent !== 'undefined' && typeof DeviceOrientationEvent.requestPermission === 'function') {
      // Will request on user gesture instead
    } else {
      window.addEventListener('deviceorientation', handler, true);
    }

    return () => window.removeEventListener('deviceorientation', handler, true);
  }, []);

  // Request compass permission (iOS)
  const requestCompass = async () => {
    if (typeof DeviceOrientationEvent !== 'undefined' && typeof DeviceOrientationEvent.requestPermission === 'function') {
      try {
        const response = await DeviceOrientationEvent.requestPermission();
        if (response === 'granted') {
          window.addEventListener('deviceorientation', (e) => {
            const alpha = e.webkitCompassHeading ?? (e.alpha !== null ? 360 - e.alpha : null);
            if (alpha !== null && !isNaN(alpha)) {
              setHeading(alpha);
              setNeedsCalibration(false);
            }
          }, true);
        }
      } catch { /* */ }
    }
  };

  // Select city manually
  const selectCity = (city) => {
    const loc = { lat: city.lat, lng: city.lng };
    setLocation(loc);
    setQibla(calculateQibla(loc.lat, loc.lng));
    setCityName(`${city.name}، ${city.country}`);
    setSearchParams({});
    setPermissionDenied(false);
    localStorage.setItem('nur_last_location', JSON.stringify(loc));
    localStorage.setItem('nur_last_city', `${city.name}، ${city.country}`);
  };

  const filteredCities = cities.filter(c =>
    c.name.includes(searchCity) || c.country.includes(searchCity)
  );

  const rotation = qibla !== null ? qibla - heading : 0;

  return (
    <div className="space-y-4 animate-fade-in text-center">
      <h2 className="text-xl font-bold text-gold">القبلة</h2>

      {/* City / Location info */}
      {cityName && (
        <div className="glass-card rounded-2xl p-3 flex items-center justify-center gap-2">
          <MapPin className="w-4 h-4 text-gold" />
          <p className="text-xs text-foreground">{cityName}</p>
        </div>
      )}

      {/* Qibla compass */}
      {qibla !== null && (
        <>
          <div className="relative w-64 h-64 mx-auto mt-4">
            <div className="absolute inset-0 rounded-full border-2 border-gold/30 glass-card flex items-center justify-center"
              style={{ transform: `rotate(${-heading}deg)` }}>
              {/* Cardinal points */}
              <span className="absolute top-3 left-1/2 -translate-x-1/2 text-xs font-bold text-gold">ش</span>
              <span className="absolute bottom-3 left-1/2 -translate-x-1/2 text-xs text-muted-foreground">ج</span>
              <span className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-muted-foreground">غ</span>
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-xs text-muted-foreground">ق</span>

              {/* Qibla arrow */}
              <div className="absolute inset-0 flex items-center justify-center" style={{ transform: `rotate(${rotation}deg)` }}>
                <div className="flex flex-col items-center">
                  <div className="w-0 h-0" style={{
                    borderLeft: '10px solid transparent',
                    borderRight: '10px solid transparent',
                    borderBottom: '20px solid hsl(var(--gold))',
                  }} />
                  <div className="w-1.5 h-24 bg-gold-gradient rounded-full" />
                </div>
              </div>

              {/* Center Kaaba */}
              <div className="w-10 h-10 rounded bg-gold/20 border border-gold/40 absolute" />
            </div>
          </div>

          <div className="glass-card rounded-2xl p-4">
            <p className="text-3xl font-bold text-gold">{toArabicNumber(Math.round(qibla))}°</p>
            <p className="text-xs text-muted-foreground mt-1">من الشمال</p>
          </div>

          {/* Calibration hint */}
          {needsCalibration && (
            <div className="glass-card rounded-2xl p-3 border border-gold/20">
              <p className="text-xs text-gold mb-1">📐 لمعايرة البوصلة:</p>
              <p className="text-xs text-muted-foreground">حرّك جهازك بحركة ٨ على المستوى الأفقي</p>
            </div>
          )}

          <p className="text-xs text-muted-foreground px-4">
            ضع الجهاز أفقيًا بعيدًا عن المعادن للحصول على قراءة دقيقة
          </p>
        </>
      )}

      {/* Permission denied / No location — show city picker */}
      {permissionDenied && !qibla && (
        <div className="space-y-4">
          <div className="glass-card rounded-2xl p-5 text-center">
            <Navigation className="w-10 h-10 text-gold mx-auto mb-3" />
            <p className="text-sm font-bold text-foreground mb-1">تعذّر الوصول لموقعك تلقائيًا</p>
            <p className="text-xs text-muted-foreground mb-4">يمكنك اختيار مدينتك يدويًا لمعرفة اتجاه القبلة</p>
            <button onClick={() => setSearchParams({ picker: 'true' })}
              className="w-full bg-gold-gradient text-primary-foreground rounded-2xl py-3 font-bold flex items-center justify-center gap-2 active:scale-95 transition-transform">
              <MapPin className="w-4 h-4" /> اختر مدينتك
            </button>
            <button onClick={requestLocation}
              className="w-full glass-card rounded-2xl py-3 font-bold text-foreground mt-2 flex items-center justify-center gap-2 active:scale-95 transition-transform">
              <Locate className="w-4 h-4 text-gold" /> إعادة محاولة تحديد الموقع
            </button>
          </div>
        </div>
      )}

      {/* Action buttons when location is set */}
      {qibla !== null && (
        <div className="flex gap-2">
          <button onClick={() => setSearchParams({ picker: 'true' })}
            className="flex-1 glass-card rounded-2xl py-2.5 text-xs text-foreground flex items-center justify-center gap-1.5 active:scale-95 transition-transform">
            <MapPin className="w-3.5 h-3.5 text-gold" /> تغيير المدينة
          </button>
          <button onClick={requestLocation}
            className="flex-1 glass-card rounded-2xl py-2.5 text-xs text-foreground flex items-center justify-center gap-1.5 active:scale-95 transition-transform">
            <Locate className="w-3.5 h-3.5 text-gold" /> تحديد تلقائي
          </button>
        </div>
      )}

      {/* City picker modal */}
      {showCityPicker && (
        <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/50 backdrop-blur-sm" onClick={() => setSearchParams({})}>
          <div className="glass rounded-t-3xl w-full max-w-2xl p-5 pb-8 animate-slide-up max-h-[80vh] overflow-y-auto overscroll-y-contain" onClick={e => e.stopPropagation()}>
            <div className="w-12 h-1 rounded-full bg-muted-foreground/30 mx-auto mb-4" />
            <h3 className="text-lg font-bold text-gold mb-3 text-center">اختر مدينتك</h3>

            {/* Search */}
            <div className="relative mb-3">
              <Search className="w-4 h-4 text-muted-foreground absolute right-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={searchCity}
                onChange={e => setSearchCity(e.target.value)}
                placeholder="ابحث عن مدينة..."
                className="w-full glass-card rounded-2xl py-3 pr-10 pl-4 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-gold/40"
              />
            </div>

            {/* City list */}
            <div className="space-y-1.5 max-h-96 overflow-y-auto scrollbar-hide">
              {filteredCities.map((city, i) => (
                <button key={i} onClick={() => selectCity(city)}
                  className="w-full glass-card rounded-xl p-3 flex items-center gap-3 hover:glass-hover transition-all text-right active:scale-95">
                  <div className="w-9 h-9 rounded-lg bg-gold/10 flex items-center justify-center shrink-0">
                    <MapPin className="w-4 h-4 text-gold" />
                  </div>
                  <div className="flex-1">
                    <p className="text-sm font-bold text-foreground">{city.name}</p>
                    <p className="text-xs text-muted-foreground">{city.country}</p>
                  </div>
                </button>
              ))}
              {filteredCities.length === 0 && (
                <p className="text-center text-sm text-muted-foreground py-8">لا توجد نتائج</p>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Loading */}
      {loading && (
        <div className="flex items-center justify-center py-8">
          <div className="w-8 h-8 rounded-full border-2 border-gold/30 border-t-gold animate-spin" />
        </div>
      )}
    </div>
  );
}
