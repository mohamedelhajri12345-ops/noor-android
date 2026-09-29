import React, { createContext, useContext, useState, useEffect } from 'react';
import { translations, getInitialLanguage } from './i18n';

const LanguageContext = createContext();

export function LanguageProvider({ children }) {
  const [lang, setLang] = useState(getInitialLanguage);

  useEffect(() => {
    try { localStorage.setItem('nur_language', lang); } catch { /* */ }
    document.documentElement.lang = lang;
    document.documentElement.dir = lang === 'ar' ? 'rtl' : 'ltr';
  }, [lang]);

  const t = (key) => translations[lang]?.[key] || translations.ar[key] || key;

  return (
    <LanguageContext.Provider value={{ lang, setLang, t }}>
      {children}
    </LanguageContext.Provider>
  );
}

export const useLanguage = () => useContext(LanguageContext);
