import React from 'react';
import { MessageCircle, Plus, Users } from 'lucide-react';

export default function ConversationList({ conversations, currentUser, onSelectConv, onNewChat }) {
  const sorted = [...conversations].sort((a, b) =>
    new Date(b.last_message_time || b.created_date || 0) - new Date(a.last_message_time || a.created_date || 0)
  );

  const getConvName = (conv) => {
    if (conv.type === 'group') return conv.name;
    const otherIdx = (conv.member_handles || []).findIndex(h => h !== currentUser.handle);
    return conv.member_names?.[otherIdx] || conv.member_handles?.[otherIdx] || conv.name;
  };

  const getConvAvatar = (conv) => {
    if (conv.type === 'group') return conv.avatar_url;
    const otherIdx = (conv.member_handles || []).findIndex(h => h !== currentUser.handle);
    return conv.member_avatars?.[otherIdx] || '';
  };

  const formatTime = (dateStr) => {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    const today = new Date();
    if (d.toDateString() === today.toDateString())
      return d.toLocaleTimeString('ar', { hour: '2-digit', minute: '2-digit' });
    return d.toLocaleDateString('ar', { day: 'numeric', month: 'short' });
  };

  return (
    <div className="space-y-3 animate-fade-in">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-bold text-gold">مجتمع القرآن الكريم</h2>
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-full overflow-hidden bg-gold/10 flex items-center justify-center text-xs font-bold text-gold">
            {currentUser.avatar ? <img src={currentUser.avatar} alt="" className="w-full h-full object-cover" /> : currentUser.name?.charAt(0)}
          </div>
          <div className="text-right">
            <p className="text-xs text-foreground font-bold">{currentUser.name}</p>
            <p className="text-[10px] text-gold">@{currentUser.handle}</p>
          </div>
        </div>
      </div>

      {sorted.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 text-center">
          <MessageCircle className="w-12 h-12 text-gold/30 mb-3" />
          <p className="text-sm text-muted-foreground">لا توجد محادثات بعد</p>
          <p className="text-xs text-muted-foreground mt-1">ابدأ محادثة جديدة بمعرف صديقك</p>
        </div>
      ) : (
        <div className="space-y-2">
          {sorted.map(conv => (
            <button key={conv.id} onClick={() => onSelectConv(conv)}
              className="w-full glass-card pressable rounded-2xl p-3 flex items-center gap-3 text-right">
              <div className="w-12 h-12 rounded-full overflow-hidden bg-gold/10 flex items-center justify-center shrink-0">
                {getConvAvatar(conv) ? <img src={getConvAvatar(conv)} alt="" className="w-full h-full object-cover" /> :
                  conv.type === 'group' ? <Users className="w-5 h-5 text-gold" /> :
                  <span className="text-sm font-bold text-gold">{getConvName(conv).charAt(0)}</span>}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-bold text-foreground truncate">{getConvName(conv)}</p>
                <p className="text-xs text-muted-foreground truncate">
                  {conv.last_sender && conv.last_sender !== currentUser.name ? `${conv.last_sender}: ` : ''}
                  {conv.last_message || 'ابدأ المحادثة'}
                </p>
              </div>
              <span className="text-[10px] text-muted-foreground shrink-0">{formatTime(conv.last_message_time || conv.created_date)}</span>
            </button>
          ))}
        </div>
      )}

      <button onClick={onNewChat}
        className="fixed bottom-20 left-1/2 -translate-x-1/2 w-14 h-14 rounded-full bg-gold-gradient flex items-center justify-center text-primary-foreground shadow-lg active:scale-90 transition-transform z-30">
        <Plus className="w-6 h-6" />
      </button>
    </div>
  );
}
