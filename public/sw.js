// Service Worker — offline caching + OneSignal push notifications
// Safe OneSignal import (wrapped in try-catch so caching works even if SDK fails)

try {
  importScripts("https://cdn.onesignal.com/sdks/web/v16/OneSignalSDK.sw.js");
} catch (e) {
  // OneSignal SDK not available — caching still works
}

// Cache names
const STATIC_CACHE = 'nur-static-v2';
const VIDEO_CACHE = 'nur-video-v1';
const AUDIO_CACHE = 'nur-audio-v1';
const API_CACHE = 'nur-api-v1';

// Domains for media caching
const VIDEO_DOMAINS = ['archive.org'];
const AUDIO_DOMAINS = ['cdn.islamic.network', 'islamcan.com', 'archive.org'];
const API_DOMAINS = ['api.aladhan.com'];

// Install — skip waiting so new SW activates immediately
self.addEventListener('install', (event) => {
  self.skipWaiting();
});

// Activate — clean old caches and claim clients
self.addEventListener('activate', (event) => {
  event.waitUntil(
    (async () => {
      const cacheNames = await caches.keys();
      await Promise.all(
        cacheNames
          .filter(name => ![STATIC_CACHE, VIDEO_CACHE, AUDIO_CACHE, API_CACHE].includes(name))
          .map(name => caches.delete(name))
      );
      await self.clients.claim();
    })()
  );
});

const matchesDomain = (url, domains) => domains.some(d => url.hostname.includes(d));

// Fetch handler — different strategies per resource type
self.addEventListener('fetch', (event) => {
  const request = event.request;
  const url = new URL(request.url);

  // Skip non-GET requests
  if (request.method !== 'GET') return;

  // Skip development/source files
  if (url.protocol === 'chrome-extension:' || url.pathname.includes('/src/') || url.pathname.startsWith('/@')) return;

  // ─── Video files — cache-first (supports no-cors opaque responses) ───
  if (matchesDomain(url, VIDEO_DOMAINS) && (request.destination === 'video' || url.pathname.endsWith('.mp4'))) {
    event.respondWith(
      (async () => {
        const cache = await caches.open(VIDEO_CACHE);
        const cached = await cache.match(request);
        if (cached) return cached;
        try {
          const response = await fetch(request, { mode: 'no-cors' });
          cache.put(request, response.clone());
          return response;
        } catch {
          return cached || Response.error();
        }
      })()
    );
    return;
  }

  // ─── Audio files — cache-first ───
  if (matchesDomain(url, AUDIO_DOMAINS) && (request.destination === 'audio' || url.pathname.endsWith('.mp3'))) {
    event.respondWith(
      (async () => {
        const cache = await caches.open(AUDIO_CACHE);
        const cached = await cache.match(request);
        if (cached) return cached;
        try {
          const response = await fetch(request);
          if (response.ok || response.type === 'opaque') {
            cache.put(request, response.clone());
          }
          return response;
        } catch {
          return cached || Response.error();
        }
      })()
    );
    return;
  }

  // ─── Aladhan API — network-first with cache fallback ───
  if (matchesDomain(url, API_DOMAINS)) {
    event.respondWith(
      (async () => {
        const cache = await caches.open(API_CACHE);
        try {
          const response = await fetch(request);
          if (response.ok) {
            cache.put(request, response.clone());
          }
          return response;
        } catch {
          const cached = await cache.match(request);
          return cached || Response.error();
        }
      })()
    );
    return;
  }

  // ─── Static assets — cache-first ───
  if (['style', 'script', 'image', 'font'].includes(request.destination)) {
    event.respondWith(
      (async () => {
        const cache = await caches.open(STATIC_CACHE);
        const cached = await cache.match(request);
        if (cached) return cached;
        try {
          const response = await fetch(request);
          if (response.ok) {
            cache.put(request, response.clone());
          }
          return response;
        } catch {
          return cached || Response.error();
        }
      })()
    );
  }
});
