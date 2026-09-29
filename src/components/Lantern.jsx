import React, { useId } from 'react';

// Reusable Islamic lantern (fanous) — SVG with glow, gentle swing animation
export default function Lantern({ className = '', delay = 0 }) {
  const rawId = useId();
  const id = rawId.replace(/[^a-zA-Z0-9]/g, '');

  return (
    <div className={className} style={{
      animation: 'lantern-swing 4s ease-in-out infinite',
      animationDelay: `${delay}s`,
      transformOrigin: 'top center',
    }}>
      <svg viewBox="0 0 80 120" className="w-full h-full" fill="none">
        <defs>
          <radialGradient id={`glow-${id}`} cx="50%" cy="50%" r="50%">
            <stop offset="0%" stopColor="hsl(45 90% 70%)" stopOpacity="0.5" />
            <stop offset="100%" stopColor="hsl(45 90% 70%)" stopOpacity="0" />
          </radialGradient>
          <linearGradient id={`body-${id}`} x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stopColor="hsl(46 65% 52%)" />
            <stop offset="100%" stopColor="hsl(42 55% 40%)" />
          </linearGradient>
        </defs>

        {/* Glow halo */}
        <ellipse cx="40" cy="55" rx="32" ry="38" fill={`url(#glow-${id})`} />

        {/* Chain */}
        <line x1="40" y1="0" x2="40" y2="10" stroke="hsl(46 65% 52%)" strokeWidth="1" opacity="0.5" />

        {/* Top cap */}
        <path d="M28 10 Q40 6 52 10 L52 20 L28 20 Z" fill={`url(#body-${id})`} />

        {/* Top rim */}
        <rect x="26" y="20" width="28" height="2.5" rx="1" fill="hsl(46 65% 52%)" />

        {/* Body */}
        <path d="M28 23 L52 23 L49 68 L31 68 Z" fill="hsl(45 90% 70%)" fillOpacity="0.1" stroke="hsl(46 65% 52%)" strokeWidth="1.2" />

        {/* Glass panels */}
        <line x1="36" y1="23" x2="35" y2="68" stroke="hsl(46 65% 52%)" strokeWidth="0.7" opacity="0.4" />
        <line x1="44" y1="23" x2="45" y2="68" stroke="hsl(46 65% 52%)" strokeWidth="0.7" opacity="0.4" />

        {/* Center light */}
        <circle cx="40" cy="45" r="5" fill="hsl(45 95% 75%)" opacity="0.4" />
        <circle cx="40" cy="45" r="2.5" fill="hsl(45 95% 85%)" opacity="0.7" />

        {/* Bottom rim */}
        <rect x="31" y="68" width="18" height="2.5" rx="1" fill="hsl(46 65% 52%)" />

        {/* Bottom cap */}
        <path d="M31 71 L49 71 L45 80 L35 80 Z" fill={`url(#body-${id})`} />

        {/* Bottom ornament */}
        <circle cx="40" cy="83" r="1.5" fill="hsl(46 65% 52%)" />
        <line x1="40" y1="84.5" x2="40" y2="89" stroke="hsl(46 65% 52%)" strokeWidth="0.8" />
        <circle cx="40" cy="90" r="1.2" fill="hsl(46 65% 52%)" />
      </svg>
    </div>
  );
}
