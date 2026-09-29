import React, { useState } from 'react';
import { Users, Loader2, Camera, Check, X } from 'lucide-react';
import { base44 } from '@/api/base44Client';

export default function RegistrationScreen({ userEmail, onRegistered }) {
  const [handle, setHandle] = useState('');
  const [name, setName] = useState('');
  const [avatar, setAvatar] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [checking, setChecking] = useState(false);
  const [handleAvailable, setHandleAvailable] = useState(null);

  const checkHandle = async (h) => {
    if (h.length < 3) { setHandleAvailable(null); return; }
    setChecking(true);
    try {
      const existing = await base44.entities.CommunityProfile.filter({ handle: h });
      setHandleAvailable(existing.length === 0);
    } catch { setHandleAvailable(null); }
    setChecking(false);
  };

  const uploadAvatar = async (file) => {
    try {
      const { file_url } = await base44.integrations.Core.UploadFile({ file });
      setAvatar(file_url);
    } catch { /* */ }
  };

  const register = async () => {
    if (handle.length < 3) { setError('المعرف يجب أن يكون ٣ أحرف على الأقل'); return; }
    if (!name.trim()) { setError('أدخل اسمك'); return; }
    setLoading(true); setError('');
    try {
      const profile = await base44.entities.CommunityProfile.create({
        handle: handle.toLowerCase().trim(), display_name: name.trim(),
        avatar_url: avatar, user_email: userEmail,
      });
      localStorage.setItem('nur_community_handle', profile.handle);
      localStorage.setItem('nur_community_name', profile.display_name);
      localStorage.setItem('nur_community_avatar', profile.avatar_url || '');
      onRegistered(profile);
    } catch { setError('تعذر إنشاء الحساب، حاول مرة أخرى'); }
    setLoading(false);
  };

  return (
    <div className="space-y-4 animate-fade-in flex flex-col items-center justify-center min-h-[60vh]">
      <div className="w-16 h-16 rounded-2xl bg-gold/10 flex items-center justify-center"><Users className="w-8 h-8 text-gold" /></div>
      <div className="text-center">
        <h2 className="text-xl font-bold text-gold">التسجيل في المجتمع</h2>
        <p className="text-sm text-muted-foreground mt-1">أنشئ معرفك الخاص للمشاركة</p>
      </div>
      <div className="glass-card rounded-2xl p-4 w-full max-w-sm space-y-3">
        {/* Avatar */}
        <div className="flex flex-col items-center gap-2">
          <label className="relative cursor-pointer">
            <div className="w-20 h-20 rounded-full overflow-hidden bg-gold/10 flex items-center justify-center border-2 border-gold/20">
              {avatar ? <img src={avatar} alt="" className="w-full h-full object-cover" /> : <Camera className="w-6 h-6 text-gold" />}
            </div>
            <input type="file" accept="image/*" className="hidden" onChange={e => { const f = e.target.files?.[0]; if (f) uploadAvatar(f); }} />
          </label>
          <p className="text-[10px] text-muted-foreground">صورة شخصية (اختياري)</p>
        </div>
        {/* Handle */}
        <div>
          <label className="text-xs text-muted-foreground mb-1 block">المعرف (اسم فريد لك)</label>
          <div className="flex items-center gap-2 glass rounded-xl px-3 py-2.5">
            <span className="text-gold text-sm">@</span>
            <input value={handle} onChange={e => { const h = e.target.value.replace(/\s/g, '').toLowerCase(); setHandle(h); setHandleAvailable(null); checkHandle(h); }}
              placeholder="مثال: ahmed" maxLength={20}
              className="flex-1 bg-transparent text-sm outline-none text-foreground placeholder:text-muted-foreground" />
            {checking ? <Loader2 className="w-3.5 h-3.5 animate-spin text-muted-foreground" /> :
              handleAvailable === true ? <Check className="w-3.5 h-3.5 text-green-500" /> :
              handleAvailable === false ? <X className="w-3.5 h-3.5 text-destructive" /> : null}
          </div>
          <p className="text-[10px] text-muted-foreground mt-1">شارك هذا المعرف مع أصدقائك ليبدؤوا محادثة معك</p>
        </div>
        {/* Name */}
        <div>
          <label className="text-xs text-muted-foreground mb-1 block">الاسم المعروض</label>
          <input value={name} onChange={e => setName(e.target.value)} placeholder="اسمك" maxLength={30}
            className="w-full glass rounded-xl px-4 py-2.5 text-sm outline-none text-foreground placeholder:text-muted-foreground" />
        </div>
        <button onClick={register} disabled={loading || !handleAvailable || !name.trim()}
          className="w-full bg-gold-gradient text-primary-foreground rounded-xl py-3 font-bold disabled:opacity-50 active:scale-95">
          {loading ? <Loader2 className="w-4 h-4 animate-spin mx-auto" /> : 'إنشاء الحساب'}
        </button>
        {error && <p className="text-xs text-destructive text-center">{error}</p>}
      </div>
    </div>
  );
}
