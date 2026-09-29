import React, { useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ChevronRight, Check, X, RotateCcw, Trophy, Brain, Hash, ListOrdered } from 'lucide-react';
import { toArabicNumber } from '@/lib/islamicUtils';
import { BookOpen, MapPin, Sparkles } from 'lucide-react';
import QuranMemorization from '@/components/games/QuranMemorization';
import NamesOfAllah from '@/components/games/NamesOfAllah';
import ProphetsJourney from '@/components/games/ProphetsJourney';
import { trueFalseQuestions, numberQuestions as numberQs } from '@/data/gameQuestions';

const games = [
  { id: 'order', name: 'ترتيب الأنبياء', desc: 'رتّب الأنبياء حسب الترتيب الزمني', icon: ListOrdered },
  { id: 'truefalse', name: 'صح أم خطأ', desc: '١٠٠+ سؤال عن الدين', icon: Check },
  { id: 'numbers', name: 'اختبار الأرقام', desc: '٥٠+ سؤال عن الأرقام الإسلامية', icon: Hash },
  { id: 'quran_mem', name: 'حفظ القرآن', desc: 'تابع رحلة حفظك للقرآن الكريم', icon: BookOpen },
  { id: 'names', name: 'الأسماء الحسنى', desc: 'تعلّم واختبر أسماء الله الحسنى', icon: Sparkles },
  { id: 'journey', name: 'رحلة الأنبياء', desc: '١٥ مستوى عن قصص الأنبياء', icon: MapPin },
];

const prophetsOrder = ['آدم', 'نوح', 'إبراهيم', 'موسى', 'عيسى', 'محمد'];

const tfQuestions = trueFalseQuestions;
const numberQuestions = numberQs;

export default function Games() {
  const { gameId } = useParams();
  const navigate = useNavigate();

  if (!gameId) {
    return (
      <div className="space-y-4 animate-fade-in">
        <h2 className="text-xl font-bold text-gold pt-2">الألعاب الإسلامية</h2>
        <div className="space-y-3">
          {games.map(g => {
            const Icon = g.icon;
            return (
              <button key={g.id} onClick={() => navigate(`/games/${g.id}`)}
                className="w-full glass-card pressable rounded-2xl p-4 flex items-center gap-3 hover:glass-hover transition-all text-right">
                <div className="w-12 h-12 rounded-xl bg-gold/10 flex items-center justify-center shrink-0">
                  <Icon className="w-6 h-6 text-gold" />
                </div>
                <div className="flex-1">
                  <p className="text-sm font-bold text-foreground">{g.name}</p>
                  <p className="text-xs text-muted-foreground">{g.desc}</p>
                </div>
                <ChevronRight className="w-4 h-4 text-muted-foreground rotate-180" />
              </button>
            );
          })}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {gameId === 'order' && <OrderProphetsGame />}
      {gameId === 'truefalse' && <TrueFalseGame />}
      {gameId === 'numbers' && <NumbersGame />}
      {gameId === 'quran_mem' && <QuranMemorization />}
      {gameId === 'names' && <NamesOfAllah />}
      {gameId === 'journey' && <ProphetsJourney />}
    </div>
  );
}

function OrderProphetsGame() {
  const [round, setRound] = useState(0);
  const [shuffled, setShuffled] = useState(() => [...prophetsOrder].sort(() => Math.random() - 0.5));
  const [selected, setSelected] = useState([]);
  const [score, setScore] = useState(0);
  const [wrong, setWrong] = useState(null);

  const handleClick = (prophet) => {
    if (selected.includes(prophet)) return;
    const expectedIdx = selected.length;
    if (prophet === prophetsOrder[expectedIdx]) {
      const newSelected = [...selected, prophet];
      setSelected(newSelected);
      if (newSelected.length === prophetsOrder.length) {
        setScore(s => s + 1);
        setTimeout(() => {
          setRound(r => r + 1);
          setShuffled([...prophetsOrder].sort(() => Math.random() - 0.5));
          setSelected([]);
        }, 1500);
      }
    } else {
      setWrong(prophet);
      setTimeout(() => setWrong(null), 800);
    }
  };

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="text-center">
        <h3 className="text-lg font-bold text-gold">ترتيب الأنبياء</h3>
        <p className="text-xs text-muted-foreground mt-1">اضغط بالترتيب الزمني · الجولة {toArabicNumber(round + 1)}</p>
        <p className="text-sm text-gold mt-2">النتيجة: {toArabicNumber(score)}</p>
      </div>

      <div className="glass-card rounded-2xl p-4">
        <p className="text-xs text-muted-foreground text-center mb-3">التسلسل الصحيح: آدم ← نوح ← إبراهيم ← موسى ← عيسى ← محمد</p>
        <div className="grid grid-cols-2 gap-3">
          {shuffled.map(prophet => {
            const isSelected = selected.includes(prophet);
            const isWrong = wrong === prophet;
            return (
              <button key={prophet} onClick={() => handleClick(prophet)} disabled={isSelected}
                className={`rounded-xl p-4 text-sm font-bold transition-all ${
                  isSelected ? 'bg-gold/20 text-gold border border-gold/40' :
                  isWrong ? 'bg-destructive/20 text-destructive' :
                  'glass-card text-foreground hover:glass-hover'
                }`}>
                <div className="flex items-center justify-center gap-2">
                  {isSelected && <Check className="w-4 h-4" />}
                  {prophet}
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {selected.length === prophetsOrder.length && (
        <div className="glass-card rounded-2xl p-4 text-center animate-fade-in">
          <Trophy className="w-8 h-8 text-gold mx-auto mb-2" />
          <p className="text-sm font-bold text-gold">أحسنت! الترتيب صحيح</p>
        </div>
      )}
    </div>
  );
}

function TrueFalseGame() {
  const [idx, setIdx] = useState(0);
  const [score, setScore] = useState(0);
  const [answered, setAnswered] = useState(null);
  const [finished, setFinished] = useState(false);

  const q = tfQuestions[idx];

  const answer = (val) => {
    if (answered !== null) return;
    setAnswered(val);
    if (val === q.a) setScore(s => s + 1);
    setTimeout(() => {
      if (idx + 1 >= tfQuestions.length) setFinished(true);
      else { setIdx(i => i + 1); setAnswered(null); }
    }, 1200);
  };

  if (finished) {
    return (
      <div className="space-y-4 text-center animate-fade-in">
        <Trophy className="w-12 h-12 text-gold mx-auto mt-8" />
        <h3 className="text-lg font-bold text-gold">انتهت اللعبة!</h3>
        <p className="text-3xl font-bold text-foreground">{toArabicNumber(score)}/{toArabicNumber(tfQuestions.length)}</p>
        <button onClick={() => { setIdx(0); setScore(0); setAnswered(null); setFinished(false); }}
          className="w-full bg-gold-gradient text-primary-foreground rounded-2xl py-3 font-bold flex items-center justify-center gap-2">
          <RotateCcw className="w-4 h-4" /> إعادة
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="flex items-center justify-between">
        <p className="text-xs text-muted-foreground">السؤال {toArabicNumber(idx + 1)}/{toArabicNumber(tfQuestions.length)}</p>
        <p className="text-xs text-gold">النتيجة: {toArabicNumber(score)}</p>
      </div>

      <div className="glass-card rounded-2xl p-6 text-center">
        <p className="arabic-text text-lg text-foreground">{q.q}</p>
      </div>

      <div className="grid grid-cols-2 gap-3">
        <button onClick={() => answer(true)} disabled={answered !== null}
          className={`rounded-2xl py-6 text-lg font-bold transition-all ${
            answered !== null
              ? (q.a === true ? 'bg-gold/20 text-gold border border-gold/40' : answered === true ? 'bg-destructive/20 text-destructive' : 'glass opacity-50')
              : 'glass-card text-foreground hover:glass-hover active:scale-95'
          }`}>
          {answered !== null && q.a === true && <Check className="w-5 h-5 inline mb-1" />} صح
        </button>
        <button onClick={() => answer(false)} disabled={answered !== null}
          className={`rounded-2xl py-6 text-lg font-bold transition-all ${
            answered !== null
              ? (q.a === false ? 'bg-gold/20 text-gold border border-gold/40' : answered === false ? 'bg-destructive/20 text-destructive' : 'glass opacity-50')
              : 'glass-card text-foreground hover:glass-hover active:scale-95'
          }`}>
          {answered !== null && q.a === false && <Check className="w-5 h-5 inline mb-1" />} خطأ
        </button>
      </div>
    </div>
  );
}

function NumbersGame() {
  const [shuffled, setShuffled] = useState(() => [...numberQuestions].sort(() => Math.random() - 0.5));
  const [idx, setIdx] = useState(0);
  const [score, setScore] = useState(0);
  const [selected, setSelected] = useState(null);
  const [finished, setFinished] = useState(false);

  const q = shuffled[idx];

  const answer = (i) => {
    if (selected !== null) return;
    setSelected(i);
    if (i === q.correct) setScore(s => s + 1);
    setTimeout(() => {
      if (idx + 1 >= shuffled.length) setFinished(true);
      else { setIdx(i => i + 1); setSelected(null); }
    }, 1200);
  };

  if (finished) {
    return (
      <div className="space-y-4 text-center animate-fade-in">
        <Trophy className="w-12 h-12 text-gold mx-auto mt-8" />
        <h3 className="text-lg font-bold text-gold">انتهت اللعبة!</h3>
        <p className="text-3xl font-bold text-foreground">{toArabicNumber(score)}/{toArabicNumber(shuffled.length)}</p>
        <button onClick={() => { setShuffled([...numberQuestions].sort(() => Math.random() - 0.5)); setIdx(0); setScore(0); setSelected(null); setFinished(false); }}
          className="w-full bg-gold-gradient text-primary-foreground rounded-2xl py-3 font-bold flex items-center justify-center gap-2">
          <RotateCcw className="w-4 h-4" /> إعادة
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="flex items-center justify-between">
        <p className="text-xs text-muted-foreground">السؤال {toArabicNumber(idx + 1)}/{toArabicNumber(shuffled.length)}</p>
        <p className="text-xs text-gold">النتيجة: {toArabicNumber(score)}</p>
      </div>

      <div className="glass-card rounded-2xl p-6 text-center">
        <p className="arabic-text text-lg text-foreground">{q.q}</p>
      </div>

      <div className="grid grid-cols-2 gap-3">
        {q.options.map((opt, i) => {
          const isCorrect = i === q.correct;
          const isSelected = i === selected;
          let style = 'glass-card text-foreground hover:glass-hover active:scale-95';
          if (selected !== null) {
            if (isCorrect) style = 'bg-gold/20 text-gold border border-gold/40';
            else if (isSelected) style = 'bg-destructive/20 text-destructive';
            else style = 'glass opacity-50';
          }
          return (
            <button key={i} onClick={() => answer(i)} disabled={selected !== null}
              className={`rounded-2xl py-5 text-2xl font-bold transition-all ${style}`}>
              {opt}
            </button>
          );
        })}
      </div>
    </div>
  );
}
