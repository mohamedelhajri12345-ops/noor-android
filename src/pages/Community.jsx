import React, { useState, useEffect, useCallback } from 'react';
import { Users, Loader2 } from 'lucide-react';
import { base44 } from '@/api/base44Client';
import { useNavigate } from 'react-router-dom';
import { audioManager } from '@/lib/audioManager';
import ConversationList from '@/components/community/ConversationList';
import ChatView from '@/components/community/ChatView';
import NewChatDialog from '@/components/community/NewChatDialog';
import RegistrationScreen from '@/components/community/RegistrationScreen';

export default function Community() {
  const [authChecked, setAuthChecked] = useState(false);
  const [isAuthed, setIsAuthed] = useState(false);
  const [user, setUser] = useState(null);
  const [profile, setProfile] = useState(null);
  const [profileChecked, setProfileChecked] = useState(false);
  const [conversations, setConversations] = useState([]);
  const [selectedConv, setSelectedConv] = useState(null);
  const [showNewChat, setShowNewChat] = useState(false);
  const [playerVisible, setPlayerVisible] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const check = (s) => setPlayerVisible(!!s.meta && (s.isPlaying || !!s.currentId));
    check(audioManager.getState());
    return audioManager.subscribe(check);
  }, []);

  useEffect(() => {
    base44.auth.isAuthenticated().then(async (authed) => {
      setIsAuthed(authed); setAuthChecked(true);
      if (authed) { try { const me = await base44.auth.me(); setUser(me); } catch { /* */ } }
    });
  }, []);

  // Check for existing profile by user_email
  useEffect(() => {
    if (!user?.email) return;
    (async () => {
      try {
        const existing = await base44.entities.CommunityProfile.filter({ user_email: user.email });
        if (existing.length > 0) {
          setProfile(existing[0]);
          localStorage.setItem('nur_community_handle', existing[0].handle);
          localStorage.setItem('nur_community_name', existing[0].display_name);
          localStorage.setItem('nur_community_avatar', existing[0].avatar_url || '');
        }
      } catch { /* */ }
      setProfileChecked(true);
    })();
  }, [user?.email]);

  const loadConversations = useCallback(async () => {
    if (!profile?.handle) return;
    try {
      const all = await base44.entities.Conversation.list('-created_date', 100);
      setConversations((all || []).filter(c => c.member_handles?.includes(profile.handle)));
    } catch { setConversations([]); }
  }, [profile?.handle]);

  useEffect(() => { if (profile?.handle) loadConversations(); }, [profile?.handle, loadConversations]);

  useEffect(() => {
    if (!profile?.handle) return;
    const unsub = base44.entities.Conversation.subscribe(() => loadConversations());
    return unsub;
  }, [profile?.handle, loadConversations]);

  if (!authChecked) return <div className="flex items-center justify-center py-12"><Loader2 className="w-8 h-8 animate-spin text-gold" /></div>;

  if (!isAuthed) return (
    <div className="space-y-4 animate-fade-in flex flex-col items-center justify-center min-h-[60vh]">
      <div className="w-16 h-16 rounded-2xl bg-gold/10 flex items-center justify-center"><Users className="w-8 h-8 text-gold" /></div>
      <div className="text-center"><h2 className="text-xl font-bold text-gold">مجتمع القرآن الكريم</h2><p className="text-sm text-muted-foreground mt-1">يجب تسجيل الدخول للمشاركة</p></div>
      <button onClick={() => navigate('/login?returnTo=' + encodeURIComponent('/community'))}
        className="bg-gold-gradient text-primary-foreground rounded-xl px-8 py-3 font-bold active:scale-95">تسجيل الدخول</button>
    </div>
  );

  if (!profileChecked) return <div className="flex items-center justify-center py-12"><Loader2 className="w-8 h-8 animate-spin text-gold" /></div>;

  if (!profile) return <RegistrationScreen userEmail={user?.email} onRegistered={(p) => setProfile(p)} />;

  const currentUser = { handle: profile.handle, name: profile.display_name, avatar: profile.avatar_url || '' };

  if (selectedConv) return <ChatView conversation={selectedConv} currentUser={currentUser} onBack={() => setSelectedConv(null)} playerVisible={playerVisible} />;

  return (
    <>
      <ConversationList conversations={conversations} currentUser={currentUser} onSelectConv={setSelectedConv} onNewChat={() => setShowNewChat(true)} />
      {showNewChat && <NewChatDialog currentUser={currentUser} conversations={conversations} onClose={() => setShowNewChat(false)}
        onCreated={(conv) => { setShowNewChat(false); setSelectedConv(conv); loadConversations(); }} />}
    </>
  );
}
