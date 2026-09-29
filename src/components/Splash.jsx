import React, { useEffect } from 'react';

const APP_ICON = 'https://media.base44.com/images/public/6a833faeb9e42cca9a6576fa/f6fc7be6c_generated_image.png';

// Minimal splash — animated app icon with glow, app name, and prayer phrase. No background scenery.
export default function Splash({ onDone }) {
  useEffect(() => {
    const timer = setTimeout(onDone, 2600);
    return () => clearTimeout(timer);
  }, [onDone]);

  return (
    <div className="fixed inset-0 z-50 flex flex-col items-center justify-center overflow-hidden"
      style={{ background: 'linear-gradient(180deg, hsl(218 16% 10%) 0%, hsl(216 33% 6%) 60%, hsl(216 35% 4%) 100%)' }}>

      {/* Animated app icon with golden glow */}
      <div className="relative z-10" style={{ animation: 'float-crescent 3s ease-in-out infinite' }}>
        <div className="absolute inset-0 -m-8 rounded-full pointer-events-none" style={{
          background: 'radial-gradient(circle, hsl(46 65% 52% / 0.35) 0%, transparent 70%)',
          animation: 'glow-pulse 2.5s ease-in-out infinite',
        }} />
        <img src={APP_ICON} alt="القرآن الكريم" className="w-28 h-28 rounded-3xl object-cover relative"
          style={{ animation: 'fade-in-up 0.8s ease-out both', boxShadow: '0 0 30px hsl(46 65% 52% / 0.45), 0 0 60px hsl(46 65% 52% / 0.2)' }} />
      </div>

      {/* App name */}
      <div className="relative z-10 text-center mt-8" style={{ animation: 'fade-in-up 1s ease-out 0.4s both' }}>
        <h1 className="text-4xl font-bold shimmer-text" style={{ fontFamily: "'Amiri', 'Cairo', serif" }}>القرآن الكريم</h1>
        <p className="text-sm text-gold/70 mt-3" style={{ fontFamily: "'Cairo', sans-serif" }}>نور قلبك.. يقينك.. عبادتك</p>
      </div>

      {/* Closing prayer phrase */}
      <div className="relative z-10 mt-10 px-8" style={{ animation: 'fade-in-up 1s ease-out 0.9s both' }}>
        <p className="text-center text-[11px] text-muted-foreground leading-relaxed">
          نسأل الله أن يجعل هذا العمل خالصاً لوجهه الكريم
        </p>
      </div>
    </div>
  );
}
