import React from 'react';

// Night sky background — golden glow, detailed SVG mosque at bottom
export default function AnimatedBackground({ variant = 'default' }) {
  return (
    <div className="fixed inset-0 overflow-hidden pointer-events-none -z-10">
      {/* Base gradient — deep midnight blue */}
      <div className="absolute inset-0" style={{
        background: 'linear-gradient(180deg, hsl(218 16% 10%) 0%, hsl(216 33% 6%) 50%, hsl(216 35% 4%) 100%)'
      }} />

      {/* Soft golden glow at top center */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[500px] h-[300px] rounded-full opacity-[0.08] blur-3xl"
        style={{ background: 'radial-gradient(circle, hsl(46 65% 52%) 0%, transparent 70%)' }} />

      {/* Detailed realistic SVG Mosque silhouette — at bottom, screen-blend */}
      <div className="absolute bottom-0 left-0 right-0 h-[38%]" style={{ opacity: 0.82, mixBlendMode: 'screen', filter: 'drop-shadow(0 0 30px hsl(46 85% 55% / 0.5))' }}>
        <svg viewBox="0 0 400 220" className="w-full h-full" preserveAspectRatio="xMidYMax slice">
          <defs>
            <linearGradient id="mosqueGold" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="hsl(50 100% 78%)" />
              <stop offset="35%" stopColor="hsl(47 80% 60%)" />
              <stop offset="70%" stopColor="hsl(44 65% 50%)" />
              <stop offset="100%" stopColor="hsl(40 55% 38%)" />
            </linearGradient>
            <linearGradient id="domeGold" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="hsl(52 100% 82%)" />
              <stop offset="50%" stopColor="hsl(48 85% 62%)" />
              <stop offset="100%" stopColor="hsl(44 65% 48%)" />
            </linearGradient>
          </defs>
          <g fill="url(#mosqueGold)">
            {/* ═══ Left minaret — 3 tiers with balconies ═══ */}
            <rect x="4" y="55" width="16" height="165" />
            <rect x="1" y="72" width="22" height="4" rx="1" />
            <rect x="2" y="95" width="20" height="3.5" rx="1" />
            <rect x="1" y="118" width="22" height="4" rx="1" />
            <rect x="2" y="140" width="20" height="3.5" rx="1" />
            <ellipse cx="12" cy="50" rx="11" ry="9" />
            <path d="M6 41 Q12 35 18 41 L18 50 L6 50 Z" />
            <line x1="12" y1="28" x2="12" y2="36" stroke="url(#mosqueGold)" strokeWidth="2" />
            <circle cx="12" cy="25" r="4" />
            {/* Crescent on left minaret */}
            <path d="M12 17 A5 5 0 1 0 12 27 A3.5 3.5 0 1 1 12 17Z" fill="hsl(45 95% 75%)" />

            {/* ═══ Right minaret — 3 tiers with balconies ═══ */}
            <rect x="380" y="55" width="16" height="165" />
            <rect x="377" y="72" width="22" height="4" rx="1" />
            <rect x="378" y="95" width="20" height="3.5" rx="1" />
            <rect x="377" y="118" width="22" height="4" rx="1" />
            <rect x="378" y="140" width="20" height="3.5" rx="1" />
            <ellipse cx="388" cy="50" rx="11" ry="9" />
            <path d="M382 41 Q388 35 394 41 L394 50 L382 50 Z" />
            <line x1="388" y1="28" x2="388" y2="36" stroke="url(#mosqueGold)" strokeWidth="2" />
            <circle cx="388" cy="25" r="4" />
            {/* Crescent on right minaret */}
            <path d="M388 17 A5 5 0 1 0 388 27 A3.5 3.5 0 1 1 388 17Z" fill="hsl(45 95% 75%)" />

            {/* ═══ Main building wall ═══ */}
            <rect x="35" y="110" width="330" height="110" />

            {/* ═══ Central grand dome ═══ */}
            <path d="M150 110 Q150 55 200 55 Q250 55 250 110 Z" fill="url(#domeGold)" />
            {/* Dome base drum */}
            <rect x="148" y="107" width="104" height="6" rx="2" />
            {/* Drum decorative band */}
            <rect x="150" y="100" width="100" height="3" rx="1" fill="hsl(44 60% 45%)" />
            {/* Finial */}
            <ellipse cx="200" cy="52" rx="7" ry="5" />
            <line x1="200" y1="35" x2="200" y2="47" stroke="url(#mosqueGold)" strokeWidth="2.5" />
            <circle cx="200" cy="32" r="4.5" />
            {/* Crescent atop central dome */}
            <path d="M200 20 A7 7 0 1 0 200 34 A5 5 0 1 1 200 20Z" fill="hsl(45 95% 75%)" />

            {/* ═══ Left medium dome ═══ */}
            <path d="M68 110 Q68 82 95 82 Q122 82 122 110 Z" fill="url(#domeGold)" />
            <rect x="66" y="107" width="58" height="5" rx="1.5" />
            <circle cx="95" cy="78" r="4" />
            <line x1="95" y1="68" x2="95" y2="74" stroke="url(#mosqueGold)" strokeWidth="1.5" />

            {/* ═══ Right medium dome ═══ */}
            <path d="M278 110 Q278 82 305 82 Q332 82 332 110 Z" fill="url(#domeGold)" />
            <rect x="276" y="107" width="58" height="5" rx="1.5" />
            <circle cx="305" cy="78" r="4" />
            <line x1="305" y1="68" x2="305" y2="74" stroke="url(#mosqueGold)" strokeWidth="1.5" />

            {/* ═══ Small flanking domes ═══ */}
            <path d="M35 110 Q35 96 48 96 Q61 96 61 110 Z" fill="url(#domeGold)" />
            <path d="M339 110 Q339 96 352 96 Q365 96 365 110 Z" fill="url(#domeGold)" />

            {/* ═══ Courtyard wall with crenellations ═══ */}
            <rect x="0" y="200" width="400" height="20" />
            {/* Crenellation merlons */}
            <rect x="5" y="194" width="6" height="6" />
            <rect x="20" y="194" width="6" height="6" />
            <rect x="35" y="194" width="6" height="6" />
            <rect x="360" y="194" width="6" height="6" />
            <rect x="375" y="194" width="6" height="6" />
            <rect x="390" y="194" width="6" height="6" />
          </g>

          {/* ═══ Central grand arched entrance ═══ */}
          <path d="M180 220 L180 165 Q180 145 200 145 Q220 145 220 165 L220 220 Z" fill="hsl(216 33% 4%)" />
          {/* Inner arch detail */}
          <path d="M185 220 L185 168 Q185 152 200 152 Q215 152 215 168 L215 220 Z" fill="hsl(216 30% 6%)" opacity="0.5" />

          {/* ═══ Arched windows — left side ═══ */}
          <path d="M75 220 L75 180 Q75 168 88 168 Q101 168 101 180 L101 220 Z" fill="hsl(216 33% 4%)" opacity="0.7" />
          <path d="M110 220 L110 180 Q110 168 123 168 Q136 168 136 180 L136 220 Z" fill="hsl(216 33% 4%)" opacity="0.7" />

          {/* ═══ Arched windows — right side ═══ */}
          <path d="M264 220 L264 180 Q264 168 277 168 Q290 168 290 180 L290 220 Z" fill="hsl(216 33% 4%)" opacity="0.7" />
          <path d="M299 220 L299 180 Q299 168 312 168 Q325 168 325 180 L325 220 Z" fill="hsl(216 33% 4%)" opacity="0.7" />

          {/* ═══ Decorative horizontal band on wall ═══ */}
          <rect x="35" y="155" width="330" height="2.5" fill="hsl(44 50% 40%)" opacity="0.4" />
          <rect x="35" y="175" width="330" height="2" fill="hsl(44 50% 40%)" opacity="0.3" />
        </svg>
      </div>

      {variant === 'player' && (
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[400px] h-[400px] opacity-[0.03] animate-spin-slow"
          style={{
            backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200' viewBox='0 0 200 200'%3E%3Cg fill='none' stroke='%23c5a059' stroke-width='1'%3E%3Cpath d='M100 10L120 80L190 100L120 120L100 190L80 120L10 100L80 80Z'/%3E%3Ccircle cx='100' cy='100' r='60'/%3E%3C/g%3E%3C/svg%3E")`,
            backgroundSize: '400px 400px',
            backgroundPosition: 'center',
          }}
        />
      )}
    </div>
  );
}
