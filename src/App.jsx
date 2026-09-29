import React, { useState, useEffect } from 'react';
import { Toaster } from "@/components/ui/toaster"
import { QueryClientProvider } from '@tanstack/react-query'
import { queryClientInstance } from '@/lib/query-client'
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import PageNotFound from './lib/PageNotFound';
import { AuthProvider } from '@/lib/AuthContext';
import ScrollToTop from './components/ScrollToTop';
import Splash from '@/components/Splash';
import { initTheme } from '@/lib/theme';
import Layout from '@/components/Layout';
import NetworkStatus from '@/components/NetworkStatus';
import NotificationPrompt from '@/components/NotificationPrompt';
import { LanguageProvider } from '@/lib/LanguageContext';

// Lazy load all pages for code splitting — faster initial load
const Home = React.lazy(() => import('@/pages/Home'));
const More = React.lazy(() => import('@/pages/More'));
const Quran = React.lazy(() => import('@/pages/Quran'));
const QuranReader = React.lazy(() => import('@/pages/QuranReader'));
const Athkar = React.lazy(() => import('@/pages/Athkar'));
const Tasbih = React.lazy(() => import('@/pages/Tasbih'));
const Prayer = React.lazy(() => import('@/pages/Prayer'));
const Library = React.lazy(() => import('@/pages/Library'));
const Qibla = React.lazy(() => import('@/pages/Qibla'));
const Journal = React.lazy(() => import('@/pages/Journal'));
const CalendarPage = React.lazy(() => import('@/pages/CalendarPage'));
const Quiz = React.lazy(() => import('@/pages/Quiz'));
const Games = React.lazy(() => import('@/pages/Games'));
const Stories = React.lazy(() => import('@/pages/Stories'));
const Favorites = React.lazy(() => import('@/pages/Favorites'));
const StoryDetail = React.lazy(() => import('@/pages/StoryDetail'));
const Settings = React.lazy(() => import('@/pages/Settings'));
const AIAssistant = React.lazy(() => import('@/pages/AIAssistant'));
const Donation = React.lazy(() => import('@/pages/Donation'));
const Community = React.lazy(() => import('@/pages/Community'));
const PrivacyPolicy = React.lazy(() => import('@/pages/PrivacyPolicy'));
const Zakat = React.lazy(() => import('@/pages/Zakat'));
const HajjGuide = React.lazy(() => import('@/pages/HajjGuide'));
const QuranTracker = React.lazy(() => import('@/pages/QuranTracker'));
const Login = React.lazy(() => import('@/pages/Login'));
const Register = React.lazy(() => import('@/pages/Register'));
const ForgotPassword = React.lazy(() => import('@/pages/ForgotPassword'));
const ResetPassword = React.lazy(() => import('@/pages/ResetPassword'));

function App() {
  const [showSplash, setShowSplash] = useState(() => !sessionStorage.getItem('nur_splash_shown'));

  useEffect(() => {
    initTheme();
    if (showSplash) sessionStorage.setItem('nur_splash_shown', 'true');
  }, [showSplash]);

  return (
    <AuthProvider>
      <LanguageProvider>
      <QueryClientProvider client={queryClientInstance}>
        <Router>
          <ScrollToTop />
          <NetworkStatus />
          <NotificationPrompt />
          {showSplash && <Splash onDone={() => setShowSplash(false)} />}
          <React.Suspense fallback={<div className="flex items-center justify-center min-h-[60vh]"><div className="w-8 h-8 border-2 border-gold border-t-transparent rounded-full animate-spin" /></div>}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />} />
            <Route element={<Layout />}>
              <Route path="/" element={<Home />} />
              <Route path="/quran" element={<Quran />} />
              <Route path="/quran/:id" element={<QuranReader />} />
              <Route path="/athkar" element={<Athkar />} />
              <Route path="/athkar/:categoryId" element={<Athkar />} />
              <Route path="/tasbih" element={<Tasbih />} />
              <Route path="/prayer" element={<Prayer />} />
              <Route path="/library" element={<Library />} />
              <Route path="/qibla" element={<Qibla />} />
              <Route path="/journal" element={<Journal />} />
              <Route path="/calendar" element={<CalendarPage />} />
              <Route path="/quiz" element={<Quiz />} />
              <Route path="/quiz/play" element={<Quiz />} />
              <Route path="/games" element={<Games />} />
              <Route path="/games/:gameId" element={<Games />} />
              <Route path="/stories" element={<Stories />} />
              <Route path="/stories/:id" element={<StoryDetail />} />
              <Route path="/favorites" element={<Favorites />} />
              <Route path="/more" element={<More />} />
              <Route path="/settings" element={<Settings />} />
              <Route path="/assistant" element={<AIAssistant />} />
              <Route path="/donate" element={<Donation />} />
            <Route path="/community" element={<Community />} />
            <Route path="/privacy" element={<PrivacyPolicy />} />
              <Route path="/zakat" element={<Zakat />} />
              <Route path="/hajj" element={<HajjGuide />} />
              <Route path="/quran-tracker" element={<QuranTracker />} />
            </Route>
            <Route path="*" element={<PageNotFound />} />
          </Routes>
          </React.Suspense>
          <Toaster />
        </Router>
      </QueryClientProvider>
      </LanguageProvider>
    </AuthProvider>
  );
}

export default App;
