import React, { useState, useMemo } from 'react';
import { X, UserPlus, Users, Loader2, Check, Search, Mail } from 'lucide-react';
import { base44 } from '@/api/base44Client';
import { toArabicNumber } from '@/lib/islamicUtils';

export default function NewChatDialog({ currentUser, conversations, onClose, onCreated }) {
  const [mode, setMode] = useState('direct');
  const [handle, setHandle] = useState('');
  const [groupName, setGroupName] = useState('');
  const [selectedContacts, setSelectedContacts] = useState(new Set());
  const [newHandle, setNewHandle] = useState('');
  const [searchMode, setSearchMode] = useState('handle');
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Extract contacts from existing conversations
  const contacts = useMemo(() => {
    const map = new Map();
    conversations.forEach(conv => {
      (conv.member_handles || []).forEach((h, i) => {
        if (h !== currentUser.handle && !map.has(h)) {
          map.set(h, { handle: h, name: conv.member_names?.[i] || h, avatar: conv.member_avatars?.[i] || '' });
        }
      });
    });
    return [...map.values()];
  }, [conversations, currentUser.handle]);

  const createDirect = async () => {
    setLoading(true); setError('');
    try {
      let profiles;
      if (searchMode === 'email') {
        const e = email.trim().toLowerCase();
        if (!e || !e.includes('@')) { setError('بريد إلكتروني غير صالح'); setLoading(false); return; }
        profiles = await base44.entities.CommunityProfile.filter({ user_email: e });
        if (profiles.length === 0) { setError('لا يوجد مستخدم بهذا البريد'); setLoading(false); return; }
      } else {
        const h = handle.trim().toLowerCase().replace(/^@/, '');
        if (!h || h.length < 3) { setError('معرف غير صالح'); setLoading(false); return; }
        if (h === currentUser.handle) { setError('لا يمكنك محادثة نفسك'); setLoading(false); return; }
        profiles = await base44.entities.CommunityProfile.filter({ handle: h });
        if (profiles.length === 0) { setError('لا يوجد مستخدم بهذا المعرف'); setLoading(false); return; }
      }
      const friend = profiles[0];
      if (friend.handle === currentUser.handle) { setError('لا يمكنك محادثة نفسك'); setLoading(false); return; }
      const conv = await base44.entities.Conversation.create({
        name: friend.display_name, type: 'direct',
        member_handles: [currentUser.handle, friend.handle],
        member_names: [currentUser.name, friend.display_name],
        member_avatars: [currentUser.avatar || '', friend.avatar_url || ''],
        last_message: '', last_sender: ''
      });
      onCreated(conv);
    } catch { setError('تعذر إنشاء المحادثة'); }
    setLoading(false);
  };

  const toggleContact = (h) => {
    setSelectedContacts(prev => {
      const n = new Set(prev);
      if (n.has(h)) n.delete(h); else n.add(h);
      return n;
    });
  };

  const addNewHandle = () => {
    const h = newHandle.trim().toLowerCase().replace(/^@/, '');
    if (h && h.length >= 3 && h !== currentUser.handle) {
      setSelectedContacts(prev => new Set([...prev, h]));
      setNewHandle('');
    }
  };

  const createGroup = async () => {
    if (!groupName.trim()) { setError('أدخل اسم المجموعة'); return; }
    if (selectedContacts.size === 0) { setError('اختر عضواً واحداً على الأقل'); return; }
    setLoading(true); setError('');
    try {
      const handles = [...selectedContacts];
      const names = [currentUser.name];
      const avatars = [currentUser.avatar || ''];
      const allHandles = [currentUser.handle, ...handles];

      for (const h of handles) {
        try {
          const profiles = await base44.entities.CommunityProfile.filter({ handle: h });
          if (profiles.length > 0) { names.push(profiles[0].display_name); avatars.push(profiles[0].avatar_url || ''); }
          else { names.push(h); avatars.push(''); }
        } catch { names.push(h); avatars.push(''); }
      }

      const conv = await base44.entities.Conversation.create({
        name: groupName.trim(), type: 'group',
        member_handles: allHandles, member_names: names, member_avatars: avatars,
        last_message: '', last_sender: ''
      });
      onCreated(conv);
    } catch { setError('تعذر إنشاء المجموعة'); }
    setLoading(false);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center" onClick={onClose}>
      <div className="absolute inset-0 bg-black/60" />
      <div className="relative glass-card rounded-2xl p-5 w-80 max-w-[90vw] max-h-[85vh] overflow-y-auto scrollbar-hide space-y-4" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-bold text-gold">محادثة جديدة</h3>
          <button onClick={onClose}><X className="w-4 h-4 text-muted-foreground" /></button>
        </div>
        <div className="flex gap-2">
          <button onClick={() => setMode('direct')} className={`flex-1 py-2 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 ${mode === 'direct' ? 'bg-gold text-primary-foreground' : 'glass text-foreground'}`}>
            <UserPlus className="w-3.5 h-3.5" /> محادثة خاصة
          </button>
          <button onClick={() => setMode('group')} className={`flex-1 py-2 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 ${mode === 'group' ? 'bg-gold text-primary-foreground' : 'glass text-foreground'}`}>
            <Users className="w-3.5 h-3.5" /> مجموعة
          </button>
        </div>
        {mode === 'direct' ? (
          <div className="space-y-2">
            {/* Search mode toggle */}
            <div className="flex gap-2">
              <button onClick={() => setSearchMode('handle')} className={`flex-1 py-1.5 rounded-lg text-xs font-bold ${searchMode === 'handle' ? 'bg-gold/15 text-gold' : 'glass text-muted-foreground'}`}>بالمعرف</button>
              <button onClick={() => setSearchMode('email')} className={`flex-1 py-1.5 rounded-lg text-xs font-bold ${searchMode === 'email' ? 'bg-gold/15 text-gold' : 'glass text-muted-foreground'}`}>بالبريد الإلكتروني</button>
            </div>
            {searchMode === 'handle' ? (
              <div className="flex items-center gap-2 glass rounded-xl px-3 py-2.5">
                <span className="text-gold text-sm">@</span>
                <input value={handle} onChange={e => setHandle(e.target.value.replace(/\s/g, '').toLowerCase())} placeholder="معرف صديقك..."
                  className="flex-1 bg-transparent text-sm outline-none text-foreground placeholder:text-muted-foreground" />
              </div>
            ) : (
              <div className="flex items-center gap-2 glass rounded-xl px-3 py-2.5">
                <Mail className="w-4 h-4 text-gold" />
                <input value={email} onChange={e => setEmail(e.target.value.trim())} type="email" placeholder="بريد صديقك الإلكتروني..."
                  className="flex-1 bg-transparent text-sm outline-none text-foreground placeholder:text-muted-foreground" />
              </div>
            )}
            <p className="text-[10px] text-muted-foreground">{searchMode === 'handle' ? 'أدخل معرف المستخدم الذي تريد محادثته' : 'أدخل بريد المستخدم الإلكتروني وابدأ المحادثة فوراً'}</p>
            <button onClick={createDirect} disabled={loading}
              className="w-full bg-gold-gradient text-primary-foreground rounded-xl py-3 text-sm font-bold disabled:opacity-50 active:scale-95">
              {loading ? <Loader2 className="w-4 h-4 animate-spin mx-auto" /> : 'بدء المحادثة'}
            </button>
          </div>
        ) : (
          <div className="space-y-3">
            <input value={groupName} onChange={e => setGroupName(e.target.value)} placeholder="اسم المجموعة..."
              className="w-full glass rounded-xl px-3 py-2.5 text-sm outline-none text-foreground placeholder:text-muted-foreground" />
            {contacts.length > 0 && (
              <div>
                <p className="text-xs text-muted-foreground mb-2">جهات الاتصال</p>
                <div className="space-y-1.5 max-h-40 overflow-y-auto scrollbar-hide">
                  {contacts.map(c => (
                    <button key={c.handle} onClick={() => toggleContact(c.handle)}
                      className={`w-full flex items-center gap-2 p-2 rounded-xl text-right ${selectedContacts.has(c.handle) ? 'bg-gold/15 border border-gold/30' : 'glass'}`}>
                      <div className="w-8 h-8 rounded-full overflow-hidden bg-gold/10 flex items-center justify-center shrink-0">
                        {c.avatar ? <img src={c.avatar} alt="" className="w-full h-full object-cover" /> :
                          <span className="text-xs font-bold text-gold">{c.name.charAt(0)}</span>}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-xs font-bold text-foreground truncate">{c.name}</p>
                        <p className="text-[10px] text-muted-foreground truncate">@{c.handle}</p>
                      </div>
                      {selectedContacts.has(c.handle) && <Check className="w-4 h-4 text-gold shrink-0" />}
                    </button>
                  ))}
                </div>
              </div>
            )}
            <div>
              <p className="text-xs text-muted-foreground mb-2">إضافة معرف جديد</p>
              <div className="flex gap-2">
                <div className="flex items-center gap-2 glass rounded-xl px-3 py-2.5 flex-1">
                  <span className="text-gold text-sm">@</span>
                  <input value={newHandle} onChange={e => setNewHandle(e.target.value.replace(/\s/g, '').toLowerCase())}
                    onKeyDown={e => e.key === 'Enter' && addNewHandle()}
                    placeholder="معرف..." className="flex-1 bg-transparent text-sm outline-none text-foreground placeholder:text-muted-foreground" />
                </div>
                <button onClick={addNewHandle} className="bg-gold/10 text-gold rounded-xl px-3 active:scale-90 text-lg">+</button>
              </div>
            </div>
            {selectedContacts.size > 0 && <p className="text-xs text-gold">{toArabicNumber(selectedContacts.size)} عضو محدد</p>}
            <button onClick={createGroup} disabled={loading}
              className="w-full bg-gold-gradient text-primary-foreground rounded-xl py-3 text-sm font-bold disabled:opacity-50 active:scale-95">
              {loading ? <Loader2 className="w-4 h-4 animate-spin mx-auto" /> : 'إنشاء المجموعة'}
            </button>
          </div>
        )}
        {error && <p className="text-xs text-destructive text-center">{error}</p>}
      </div>
    </div>
  );
}
