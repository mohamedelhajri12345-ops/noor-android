import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Trash2, PenLine, ChevronRight } from 'lucide-react';

const STORAGE_KEY = 'waha_journal';

export default function Journal() {
  const navigate = useNavigate();
  const [notes, setNotes] = useState(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    return saved ? JSON.parse(saved) : [];
  });
  const [adding, setAdding] = useState(false);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(notes));
  }, [notes]);

  const addNote = () => {
    if (!content.trim()) return;
    const note = {
      id: Date.now(),
      title: title.trim() || 'خاطرة',
      content: content.trim(),
      date: new Date().toLocaleDateString('ar-EG'),
    };
    setNotes(n => [note, ...n]);
    setTitle('');
    setContent('');
    setAdding(false);
  };

  const deleteNote = (id) => {
    setNotes(n => n.filter(note => note.id !== id));
  };

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-bold text-gold">المفكرة</h2>
        <button onClick={() => setAdding(true)} className="w-10 h-10 rounded-xl bg-gold-gradient flex items-center justify-center text-primary-foreground active:scale-95 transition-transform">
          <Plus className="w-5 h-5" />
        </button>
      </div>

      <p className="text-xs text-muted-foreground -mt-2">مساحتك الخاصة لتدوين الخواطر والأدعية</p>

      {adding && (
        <div className="glass-card rounded-2xl p-4 space-y-3 animate-slide-up">
          <input
            value={title}
            onChange={e => setTitle(e.target.value)}
            placeholder="العنوان"
            className="w-full bg-transparent text-sm text-foreground placeholder:text-muted-foreground outline-none border-b border-gold/20 pb-2"
          />
          <textarea
            value={content}
            onChange={e => setContent(e.target.value)}
            placeholder="اكتب خاطرتك هنا..."
            rows={4}
            className="w-full bg-transparent text-sm text-foreground placeholder:text-muted-foreground outline-none resize-none"
          />
          <div className="flex gap-2">
            <button onClick={addNote} className="flex-1 bg-gold-gradient text-primary-foreground rounded-xl py-2 text-sm font-bold">
              حفظ
            </button>
            <button onClick={() => { setAdding(false); setTitle(''); setContent(''); }} className="glass-card rounded-xl px-4 py-2 text-sm text-muted-foreground">
              إلغاء
            </button>
          </div>
        </div>
      )}

      {notes.length === 0 && !adding ? (
        <div className="text-center py-12">
          <PenLine className="w-12 h-12 text-muted-foreground/30 mx-auto mb-3" />
          <p className="text-sm text-muted-foreground">لا توجد خواطر بعد</p>
          <p className="text-xs text-muted-foreground mt-1">ابدأ بتدوين أول خاطرة</p>
        </div>
      ) : (
        <div className="space-y-3">
          {notes.map(note => (
            <div key={note.id} className="glass-card rounded-2xl p-4">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <p className="text-sm font-bold text-foreground">{note.title}</p>
                  <p className="text-xs text-muted-foreground mt-1">{note.date}</p>
                </div>
                <button onClick={() => deleteNote(note.id)} className="text-muted-foreground hover:text-destructive transition-colors">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
              <p className="text-sm text-foreground mt-2 leading-relaxed whitespace-pre-wrap">{note.content}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
