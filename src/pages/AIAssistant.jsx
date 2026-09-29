import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Sparkles, Send, Loader2, BookOpen, Hand, Clock, Heart, User } from 'lucide-react';
import { base44 } from '@/api/base44Client';
import { audioManager } from '@/lib/audioManager';

const SYSTEM_PROMPT = `أنت "المساعد الذكي" في تطبيق "القرآن الكريم"، مساعد ذكاء اصطناعي إسلامي متخصص، تجيب على الأسئلة الإسلامية بناءً على القرآن الكريم والسنة النبوية المطهرة بفهم السلف الصالح.

## المبادئ الأساسية
- التزامك التام بالكتاب والسنة، ولا تفتي برأيك الشخصي.
- اعتمد على المصادر الموثوقة: القرآن الكريم، الكتب الستة (البخاري، مسلم، أبو داود، الترمذي، النسائي، ابن ماجه)، ومسند أحمد.
- عند ذكر الحديث: اذكر الراوي والدرجة (صحيح، حسن، ضعيف) إن أمكن.

## قواعد الإجابة
1- الدقة قبل كل شيء: لا تخمن. إذا لم تكن متأكدًا، قل صراحةً "لا أملك معلومة مؤكدة عن هذا" — هذا خير من الفتيا بلا علم.
2- الإيجاز: اجابة مركزة وواضحة (٥٠–١٥٠ كلمة عادةً)، إلا إذا طلب المستخدم التفصيل.
3- التأصيل الشرعي: اذكر الدليل (السورة والآية، أو الراوي ودرجة الحديث) عند كل حكم.
4- لا تُفتي في المسائل الخلافية بين المذاهب بقول واحد دون الإشارة لوجود خلاف، واذكر قول الجمهور ثم غيره بإيجاز.
5- ركّز على ما ينبني عليه العمل: العبادات، الأخلاق، المعاملات، العقيدة.
6- تعامل بلطف واحترام مع كل مستخدم، واستخدم خطاب الأخوة الإيمانية.

## حدود التخصص
- تخصصك المسائل الإسلامية. إذا كان السؤال غير ديني إطلاقًا، وجّه المستخدم بلطف: "تخصصي المسائل الإسلامية، لكن يسعدني مساعدتك في أي أمر ديني."
- لا تُجب على الأسئلة الطبية أو القانونية التي تتطلب اختصاصاً مهنياً — وجّه المستخدم لأهل الاختصاص.
- لا تتدخل في السياسة ولا في النزاعات الشخصية.

## الأسلوب
- عربي فصيح واضح، مع روح إيمانية دافئة.
- ابدأ التحية مرة واحدة فقط، ثم ادخل في صلب الإجابة.
- استخدم الترقيم والنقاط لتنظيم الإجابة الطويلة.`;

const suggestions = [
  { icon: Clock, text: 'ما هي أوقات الصلاة الخمس؟' },
  { icon: Hand, text: 'ما هي أذكار الصباح؟' },
  { icon: BookOpen, text: 'ما فضل سورة الإخلاص؟' },
  { icon: Heart, text: 'كيف أزيد في محبة النبي ﷺ؟' },
];

export default function AIAssistant() {
  const navigate = useNavigate();
  const [messages, setMessages] = useState([
    { role: 'assistant', text: 'السلام عليكم ورحمة الله 🌙\
أنا مساعدك الذكي في تطبيق "القرآن الكريم"، مساعدك في الأمور الإسلامية. كيف يمكنني مساعدتك اليوم؟' },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [isOffline, setIsOffline] = useState(!navigator.onLine);
  const scrollRef = useRef(null);
  const [playerVisible, setPlayerVisible] = useState(false);

  useEffect(() => {
    const check = (s) => setPlayerVisible(!!s.meta && (s.isPlaying || !!s.currentId));
    check(audioManager.getState());
    return audioManager.subscribe(check);
  }, []);

  useEffect(() => {
    const onOnline = () => setIsOffline(false);
    const onOffline = () => setIsOffline(true);
    window.addEventListener('online', onOnline);
    window.addEventListener('offline', onOffline);
    return () => {
      window.removeEventListener('online', onOnline);
      window.removeEventListener('offline', onOffline);
    };
  }, []);

  useEffect(() => {
    scrollRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  const send = async (text) => {
    if (!text.trim() || loading) return;
    const userMsg = { role: 'user', text };
    setMessages(m => [...m, userMsg]);
    setInput('');
    setLoading(true);

    try {
      const conversation = [...messages, userMsg]
        .map(m => m.role === 'user' ? `المستخدم: ${m.text}` : `المساعد: ${m.text}`)
        .join('\
');
      
      const res = await base44.integrations.Core.InvokeLLM({
        prompt: `${SYSTEM_PROMPT}\
\
${conversation}\
\
المساعد:`,
      });

      setMessages(m => [...m, { role: 'assistant', text: res || 'عذرًا، لم أتمكن من الرد الآن.' }]);
    } catch {
      const offlineMsg = !navigator.onLine
        ? '🌙 عذرًا، المساعد الذكي يحتاج إلى اتصال بالإنترنت. باقي خصائص التطبيق (القرآن، الأذكار، القبلة...) تعمل بدون إنترنت.'
        : 'عذرًا، حدث خطأ. تأكد من اتصالك بالإنترنت وحاول مرة أخرى.';
      setMessages(m => [...m, { role: 'assistant', text: offlineMsg }]);
    }
    setLoading(false);
  };

  return (
    <div className="space-y-4 animate-fade-in flex flex-col" style={{ minHeight: 'calc(100vh - 180px)' }}>
      <div className="flex items-center gap-2">
        <div className="w-8 h-8 rounded-full bg-gold/10 flex items-center justify-center">
          <Sparkles className="w-4 h-4 text-gold" />
        </div>
        <div>
          <h2 className="text-sm font-bold text-gold">مساعد القرآن الكريم</h2>
          <p className="text-[10px] text-muted-foreground">اسألني عن أي أمر ديني</p>
        </div>
      </div>

      {/* Offline banner */}
      {isOffline && (
        <div className="glass-card rounded-xl p-2.5 flex items-center gap-2 border border-gold/20">
          <div className="w-7 h-7 rounded-full bg-gold/10 flex items-center justify-center shrink-0">
            <span className="text-xs">🌙</span>
          </div>
          <p className="text-xs text-muted-foreground flex-1">أنت بدون اتصال — المساعد الذكي يحتاج إنترنت، باقي الخصائص تعمل أوفلاين</p>
        </div>
      )}

      {/* Messages */}
      <div className="flex-1 space-y-3 overflow-y-auto scrollbar-hide pb-2">
        {messages.map((msg, i) => (
          <div key={i} className={`flex gap-2 ${msg.role === 'user' ? 'flex-row-reverse' : ''}`}>
            <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 ${
              msg.role === 'user' ? 'bg-gold-gradient' : 'bg-gold/10'
            }`}>
              {msg.role === 'user' ? <User className="w-4 h-4 text-primary-foreground" /> : <Sparkles className="w-4 h-4 text-gold" />}
            </div>
            <div className={`max-w-[80%] rounded-2xl p-3 text-sm leading-relaxed whitespace-pre-wrap ${
              msg.role === 'user' ? 'bg-gold-gradient text-primary-foreground' : 'glass-card text-foreground'
            }`}>
              {msg.text}
            </div>
          </div>
        ))}
        {loading && (
          <div className="flex gap-2">
            <div className="w-8 h-8 rounded-full bg-gold/10 flex items-center justify-center shrink-0">
              <Sparkles className="w-4 h-4 text-gold" />
            </div>
            <div className="glass-card rounded-2xl p-3 flex items-center gap-2">
              <Loader2 className="w-4 h-4 animate-spin text-gold" />
              <span className="text-xs text-muted-foreground">المساعد يفكر...</span>
            </div>
          </div>
        )}
        <div ref={scrollRef} />
      </div>

      {/* Suggestions */}
      {messages.length <= 1 && (
        <div className="grid grid-cols-2 gap-2">
          {suggestions.map((s, i) => {
            const Icon = s.icon;
            return (
              <button key={i} onClick={() => send(s.text)} className="glass-card rounded-xl p-3 flex items-center gap-2 hover:glass-hover transition-all text-right">
                <Icon className="w-4 h-4 text-gold shrink-0" />
                <span className="text-xs text-foreground">{s.text}</span>
              </button>
            );
          })}
        </div>
      )}

      {/* Input */}
      <div className="glass-card rounded-2xl p-2 flex items-center gap-2 sticky" style={{ bottom: `calc(${playerVisible ? 120 : 56}px + env(safe-area-inset-bottom))` }}>
        <input
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && send(input)}
          placeholder="اكتب سؤالك هنا..."
          className="flex-1 bg-transparent text-sm text-foreground placeholder:text-muted-foreground outline-none px-2"
        />
        <button
          onClick={() => send(input)}
          disabled={loading || !input.trim()}
          className="w-10 h-10 rounded-xl bg-gold-gradient flex items-center justify-center text-primary-foreground disabled:opacity-50 active:scale-95 transition-transform"
        >
          <Send className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
