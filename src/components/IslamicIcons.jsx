import React from 'react';

// Mosque icon — dome, minarets, arched door
export function MosqueIcon({ className = '', strokeWidth = 1.5 }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round" className={className}>
      <path d="M12 3c-2 0-3.5 1.5-3.5 3S10 9 12 9s3.5-1.5 3.5-3S14 3 12 3z" />
      <path d="M12 3v-1.5" />
      <path d="M6 22V11l6-4 6 4v11" />
      <path d="M9.5 22v-4a2.5 2.5 0 0 1 5 0v4" />
      <path d="M3 22V11" />
      <circle cx="3" cy="9.5" r="1" />
      <path d="M21 22V11" />
      <circle cx="21" cy="9.5" r="1" />
      <line x1="2" y1="22" x2="22" y2="22" />
    </svg>
  );
}

// Tasbih icon — circular prayer beads with tassel
export function TasbihIcon({ className = '', strokeWidth = 1.5 }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round" className={className}>
      <circle cx="12" cy="14" r="7" strokeDasharray="1.8 2" />
      <circle cx="12" cy="6" r="2" fill="currentColor" stroke="none" />
      <line x1="12" y1="4" x2="12" y2="2" />
      <line x1="10.5" y1="2" x2="13.5" y2="2" />
    </svg>
  );
}

// Athkar icon — beads in arc with tassel
export function AthkarIcon({ className = '', strokeWidth = 1.5 }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round" className={className}>
      <path d="M3 19 Q12 6 21 19" strokeDasharray="1.8 2" />
      <circle cx="3" cy="19" r="1.5" fill="currentColor" stroke="none" />
      <circle cx="21" cy="19" r="1.5" fill="currentColor" stroke="none" />
      <circle cx="12" cy="7.5" r="1.8" fill="currentColor" stroke="none" />
      <line x1="12" y1="6" x2="12" y2="3" />
    </svg>
  );
}

// Quran icon — book with decorative cover and crescent
export function QuranIcon({ className = '', strokeWidth = 1.5 }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round" className={className}>
      <path d="M5 4h14v16H5z" rx="1" />
      <line x1="5" y1="8" x2="19" y2="8" />
      <line x1="5" y1="16" x2="19" y2="16" />
      <path d="M12 11c-1 0-1.5 0.8-1.5 1.5S11 14 12 14s1.5-0.8 1.5-1.5S13 11 12 11z" fill="currentColor" stroke="none" />
      <path d="M12 4c-0.6 0.3-1 0.6-1 1s0.4 0.7 1 1c-0.8-0.2-1.2-0.5-1.2-1S11.2 4.2 12 4z" fill="currentColor" stroke="none" />
    </svg>
  );
}
