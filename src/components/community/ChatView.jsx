import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Send, Mic, Image as ImageIcon, StopCircle, Play, Pause, Trash2, ChevronRight, Loader2 } from 'lucide-react';
import { base44 } from '@/api/base44Client';
import { toArabicNumber } from '@/lib/islamicUtils';

export default function ChatView({ conversation, currentUser, onBack, playerVisible }) {
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [recording, setRecording] = useState(false);
  const [recDuration, setRecDuration] = useState(0);
  const [playingId, setPlayingId] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [editText, setEditText] = useState('');
  const scrollRef = useRef(null);
  const fileInputRef = useRef(null);
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);
  const recStreamRef = useRef(null);
  const recTimerRef = useRef(null);
  const voicePlayRef = useRef(null);

  const loadMessages = useCallback(async () => {
    try {
      const list = await base44.entities.CommunityMessage.filter({ conversation_id: conversation.id }, '-created_date', 50);
      setMessages((list || []).reverse());
    } catch { setMessages([]); }
    setLoading(false);
  }, [conversation.id]);

  useEffect(() => { loadMessages(); }, [loadMessages]);

  useEffect(() => {
    const unsub = base44.entities.CommunityMessage.subscribe((event) => {
      if (event?.data?.conversation_id === conversation.id) {
        setMessages(prev => {
          if (prev.some(m => m.id === event.data.id)) return prev.map(m => m.id === event.data.id ? event.data : m);
          return [...prev, event.data];
        });
      }
    });
    return unsub;
  }, [conversation.id]);

  useEffect(() => { scrollRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);

  const send = async (extra = {}) => {
    const t = text.trim();
    if (!t && !extra.file_url) return;
    const tempId = `temp-${Date.now()}`;
    setMessages(prev => [...prev, {
      id: tempId, nickname: currentUser.name, conversation_id: conversation.id,
      text: t || '📎', avatar_url: currentUser.avatar || '', created_date: new Date().toISOString(), pending: true, ...extra
    }]);
    setText(''); setSending(true);
    try {
      const created = await base44.entities.CommunityMessage.create({
        nickname: currentUser.name, conversation_id: conversation.id,
        text: t || '📎', avatar_url: currentUser.avatar || '', ...extra
      });
      setMessages(prev => prev.map(m => m.id === tempId ? { ...created, pending: false } : m));
      await base44.entities.Conversation.update(conversation.id, {
        last_message: t || (extra.file_type === 'image' ? '📷 صورة' : '🎙️ رسالة صوتية'),
        last_message_time: new Date().toISOString(), last_sender: currentUser.name
      });
    } catch { setMessages(prev => prev.map(m => m.id === tempId ? { ...m, pending: false, failed: true } : m)); }
    setSending(false);
  };

  const uploadFile = async (file, fileType) => {
    if (!file) return;
    setUploading(true);
    try {
      const { file_url } = await base44.integrations.Core.UploadFile({ file });
      if (file_url) await send({ text: text.trim() || '📎', file_url, file_type: fileType });
    } catch { /* */ }
    setUploading(false);
  };

  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      recStreamRef.current = stream;
      const mimeTypes = ['audio/mp4', 'audio/webm;codecs=opus', 'audio/webm'];
      const mimeType = mimeTypes.find(t => MediaRecorder.isTypeSupported(t)) || '';
      const mr = new MediaRecorder(stream, mimeType ? { mimeType } : undefined);
      audioChunksRef.current = [];
      mr.ondataavailable = (e) => { if (e.data?.size > 0) audioChunksRef.current.push(e.data); };
      mr.onstop = async () => {
        if (recTimerRef.current) { clearInterval(recTimerRef.current); recTimerRef.current = null; }
        const chunks = audioChunksRef.current;
        if (!chunks.length) { setRecording(false); return; }
        const blob = new Blob(chunks, { type: mimeType || 'audio/webm' });
        const ext = mimeType.includes('mp4') ? 'm4a' : 'webm';
        const file = new File([blob], `voice_${Date.now()}.${ext}`, { type: mimeType || 'audio/webm' });
        await uploadFile(file, 'voice');
        stream.getTracks().forEach(t => t.stop()); recStreamRef.current = null;
      };
      mr.start(1000); mediaRecorderRef.current = mr;
      setRecording(true); setRecDuration(0);
      recTimerRef.current = setInterval(() => setRecDuration(d => d + 1), 1000);
    } catch { setRecording(false); }
  };

  const stopRecording = () => {
    if (mediaRecorderRef.current?.state !== 'inactive') mediaRecorderRef.current?.stop();
    if (recTimerRef.current) { clearInterval(recTimerRef.current); recTimerRef.current = null; }
    setRecording(false);
  };

  const playVoice = (url, id) => {
    if (playingId === id) { voicePlayRef.current?.pause(); setPlayingId(null); return; }
    if (voicePlayRef.current) voicePlayRef.current.pause();
    const a = new Audio(url); a.onended = () => setPlayingId(null); a.play();
    voicePlayRef.current = a; setPlayingId(id);
  };

  const handleDelete = async (msg) => {
    if (!window.confirm('حذف هذه الرسالة؟')) return;
    try { await base44.entities.CommunityMessage.delete(msg.id); setMessages(prev => prev.filter(m => m.id !== msg.id)); } catch { /* */ }
  };

  const saveEdit = async () => {
    if (!editText.trim() || !editingId) return;
    try { await base44.entities.CommunityMessage.update(editingId, { text: editText.trim(), edited: true });
      setMessages(prev => prev.map(m => m.id === editingId ? { ...m, text: editText.trim(), edited: true } : m)); } catch { /* */ }
    setEditingId(null); setEditText('');
  };

  useEffect(() => () => {
    if (voicePlayRef.current) voicePlayRef.current.pause();
    if (recStreamRef.current) recStreamRef.current.getTracks().forEach(t => t.stop());
    if (recTimerRef.current) clearInterval(recTimerRef.current);
  }, []);

  const convName = conversation.type === 'group' ? conversation.name :
    conversation.member_names?.[(conversation.member_handles || []).findIndex(h => h !== currentUser.handle)] || conversation.name;

  return (
    <div className="flex flex-col animate-fade-in" style={{ minHeight: 'calc(100vh - 180px)' }}>
      <div className="flex items-center gap-2 pb-2 border-b border-gold/10">
        <button onClick={onBack} className="w-10 h-10 rounded-xl flex items-center justify-center text-foreground hover:bg-gold/10 active:scale-90">
          <ChevronRight className="w-5 h-5" />
        </button>
        <div className="w-10 h-10 rounded-full overflow-hidden bg-gold/10 flex items-center justify-center shrink-0">
          {conversation.avatar_url ? <img src={conversation.avatar_url} alt="" className="w-full h-full object-cover" /> :
            <span className="text-sm font-bold text-gold">{convName?.charAt(0)}</span>}
        </div>
        <div>
          <p className="text-sm font-bold text-foreground">{convName}</p>
          <p className="text-[10px] text-muted-foreground">{conversation.type === 'group' ? `${toArabicNumber(conversation.member_handles?.length || 0)} أعضاء` : 'محادثة خاصة'}</p>
        </div>
      </div>

      <div className="flex-1 space-y-2.5 overflow-y-auto scrollbar-hide py-3">
        {loading ? (
          <div className="flex items-center justify-center py-8"><Loader2 className="w-6 h-6 animate-spin text-gold" /></div>
        ) : messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <p className="text-sm text-muted-foreground">لا توجد رسائل بعد</p>
            <p className="text-xs text-muted-foreground mt-1">ابدأ المحادثة الآن</p>
          </div>
        ) : (
          messages.map(m => (
            <div key={m.id} className={`flex gap-2 ${m.nickname === currentUser.name ? 'flex-row-reverse' : ''}`}>
              <div className={`max-w-[78%] rounded-2xl p-3 ${m.nickname === currentUser.name ? 'bg-gold-gradient text-primary-foreground' : 'glass-card text-foreground'}`}>
                {m.nickname !== currentUser.name && <p className="text-[10px] text-gold mb-0.5">{m.nickname}</p>}
                {editingId === m.id ? (
                  <div className="flex gap-1">
                    <input value={editText} onChange={e => setEditText(e.target.value)} onKeyDown={e => e.key === 'Enter' && saveEdit()}
                      className="flex-1 bg-black/20 rounded px-2 py-1 text-sm outline-none" autoFocus />
                    <button onClick={saveEdit} className="text-xs bg-gold px-2 py-1 rounded">حفظ</button>
                  </div>
                ) : (
                  <>
                    {m.text && m.text !== '📎' && <p className="text-sm leading-relaxed whitespace-pre-wrap">{m.text}</p>}
                    {m.edited && <span className="text-[9px] opacity-60">• تم التعديل</span>}
                    {m.pending && <span className="text-[9px] opacity-60">⏳</span>}
                    {m.failed && <span className="text-[9px] text-red-300">⚠</span>}
                  </>
                )}
                {m.file_url && m.file_type === 'image' && <img src={m.file_url} alt="" className="rounded-xl mt-1 max-w-full max-h-48 object-cover" />}
                {m.file_url && (m.file_type === 'audio' || m.file_type === 'voice') && (
                  <div className="flex items-center gap-2 mt-1">
                    <button onClick={() => playVoice(m.file_url, m.id)} className="w-9 h-9 rounded-full bg-black/20 flex items-center justify-center shrink-0">
                      {playingId === m.id ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4 ml-0.5" />}
                    </button>
                    <span className="text-xs opacity-80">{m.file_type === 'voice' ? '🎙️ رسالة صوتية' : '🎵 ملف'}</span>
                  </div>
                )}
                <p className={`text-[9px] mt-1 ${m.nickname === currentUser.name ? 'text-primary-foreground/70' : 'text-muted-foreground'}`}>
                  {new Date(m.created_date).toLocaleTimeString('ar', { hour: '2-digit', minute: '2-digit' })}
                </p>
              </div>
              {m.nickname === currentUser.name && !editingId && (
                <div className="flex flex-col gap-1 self-center">
                  <button onClick={() => handleDelete(m)} className="text-muted-foreground hover:text-destructive"><Trash2 className="w-3.5 h-3.5" /></button>
                  <button onClick={() => { setEditingId(m.id); setEditText(m.text); }} className="text-muted-foreground hover:text-gold text-[10px]">✎</button>
                </div>
              )}
            </div>
          ))
        )}
        <div ref={scrollRef} />
      </div>

      <div className="glass-card rounded-2xl p-2 flex items-center gap-1.5 sticky" style={{ bottom: `calc(${playerVisible ? 120 : 56}px + env(safe-area-inset-bottom))` }}>
        <input ref={fileInputRef} type="file" accept="image/*" className="hidden" onChange={e => { const f = e.target.files?.[0]; if (f) uploadFile(f, 'image'); e.target.value = ''; }} />
        <button onClick={() => fileInputRef.current?.click()} disabled={uploading || recording}
          className="w-9 h-9 rounded-xl bg-gold/10 flex items-center justify-center text-gold shrink-0 active:scale-90 disabled:opacity-50">
          {uploading ? <Loader2 className="w-4 h-4 animate-spin" /> : <ImageIcon className="w-4 h-4" />}
        </button>
        {recording ? (
          <button onClick={stopRecording} className="w-9 h-9 rounded-xl bg-red-500 flex items-center justify-center text-white shrink-0 animate-pulse">
            <StopCircle className="w-5 h-5" />
          </button>
        ) : (
          <button onClick={startRecording} disabled={uploading}
            className="w-9 h-9 rounded-xl bg-gold/10 flex items-center justify-center text-gold shrink-0 active:scale-90 disabled:opacity-50">
            <Mic className="w-4 h-4" />
          </button>
        )}
        {recording ? (
          <div className="flex-1 flex items-center gap-2 px-2 min-w-0">
            <span className="w-2.5 h-2.5 rounded-full bg-red-500 animate-pulse shrink-0" />
            <span className="text-sm text-red-400 font-mono shrink-0">
              {toArabicNumber(String(Math.floor(recDuration / 60)).padStart(2, '0'))}:{toArabicNumber(String(recDuration % 60).padStart(2, '0'))}
            </span>
            <span className="text-xs text-muted-foreground truncate">جارٍ التسجيل...</span>
          </div>
        ) : (
          <input value={text} onChange={e => setText(e.target.value)} onKeyDown={e => e.key === 'Enter' && send()}
            placeholder="اكتب رسالتك..." maxLength={500}
            className="flex-1 bg-transparent text-sm text-foreground placeholder:text-muted-foreground outline-none px-2 min-w-0" />
        )}
        <button onClick={() => send()} disabled={sending || (!text.trim() && !uploading)}
          className="w-10 h-10 rounded-xl bg-gold-gradient flex items-center justify-center text-primary-foreground disabled:opacity-50 active:scale-95 shrink-0">
          {sending ? <Loader2 className="w-4 h-4 animate-spin" /> : <Send className="w-4 h-4" />}
        </button>
      </div>
    </div>
  );
}
