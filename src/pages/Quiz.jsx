import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Brain, Check, X, RotateCcw, Trophy } from 'lucide-react';
import { getQuizQuestions } from '@/data/quizQuestions';
import { toArabicNumber } from '@/lib/islamicUtils';

export default function Quiz() {
  const location = useLocation();
  const navigate = useNavigate();
  const started = location.pathname === '/quiz/play';
  const [questions, setQuestions] = useState([]);
  const [currentIdx, setCurrentIdx] = useState(0);
  const [selectedAnswer, setSelectedAnswer] = useState(null);
  const [score, setScore] = useState(0);
  const [finished, setFinished] = useState(false);
  const [totalPool, setTotalPool] = useState(0);

  const startQuiz = () => {
    setQuestions(getQuizQuestions(10));
    setTotalPool(getQuizQuestions(0).length || 1000);
    setCurrentIdx(0);
    setSelectedAnswer(null);
    setScore(0);
    setFinished(false);
    navigate('/quiz/play');
  };

  if (!started) {
    return (
      <div className="space-y-4 animate-fade-in">
        <div className="text-center pt-4">
          <div className="w-20 h-20 rounded-3xl bg-gold/10 flex items-center justify-center mx-auto mb-4">
            <Brain className="w-10 h-10 text-gold" />
          </div>
          <h2 className="text-xl font-bold text-gold">الاختبار الديني</h2>
          <p className="text-sm text-muted-foreground mt-2">اختبر معلوماتك الدينية<br />١٠ أسئلة عشوائية من بنك أسئلة ضخم</p>
          <p className="text-xs text-gold mt-2">أكثر من ١٠٠٠ سؤال متاح</p>
        </div>
        <button onClick={startQuiz} className="w-full bg-gold-gradient text-primary-foreground rounded-2xl py-4 text-lg font-bold active:scale-95 transition-transform">
          ابدأ الاختبار
        </button>
      </div>
    );
  }

  if (finished) {
    const percentage = Math.round((score / questions.length) * 100);
    return (
      <div className="space-y-4 animate-fade-in text-center">
        <div className="w-20 h-20 rounded-3xl bg-gold/10 flex items-center justify-center mx-auto mt-8">
          <Trophy className="w-10 h-10 text-gold" />
        </div>
        <h2 className="text-xl font-bold text-gold">انتهى الاختبار!</h2>
        <p className="text-4xl font-bold text-foreground">{toArabicNumber(score)}/{toArabicNumber(questions.length)}</p>
        <p className="text-sm text-muted-foreground">نسبة الإجابات الصحيحة: {toArabicNumber(percentage)}%</p>
        <button onClick={startQuiz} className="w-full bg-gold-gradient text-primary-foreground rounded-2xl py-3 font-bold flex items-center justify-center gap-2">
          <RotateCcw className="w-4 h-4" /> إعادة الاختبار
        </button>
      </div>
    );
  }

  const q = questions[currentIdx];
  if (!q) return null;
  const answered = selectedAnswer !== null;

  const selectAnswer = (idx) => {
    if (answered) return;
    setSelectedAnswer(idx);
    if (idx === q.correct) setScore(s => s + 1);
    setTimeout(() => {
      if (currentIdx + 1 >= questions.length) {
        setFinished(true);
      } else {
        setCurrentIdx(i => i + 1);
        setSelectedAnswer(null);
      }
    }, 1200);
  };

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="flex items-center justify-between">
        <p className="text-xs text-muted-foreground">السؤال {toArabicNumber(currentIdx + 1)}/{toArabicNumber(questions.length)}</p>
        <p className="text-xs text-gold">النتيجة: {toArabicNumber(score)}</p>
      </div>

      <div className="h-1.5 rounded-full bg-muted overflow-hidden">
        <div className="h-full bg-gold-gradient transition-all" style={{ width: `${(currentIdx / questions.length) * 100}%` }} />
      </div>

      <div className="glass-card rounded-2xl p-5">
        <p className="arabic-text text-lg text-foreground mb-4">{q.q}</p>
        <div className="space-y-2">
          {q.options.map((opt, i) => {
            const isCorrect = i === q.correct;
            const isSelected = i === selectedAnswer;
            let style = 'glass text-foreground hover:text-gold';
            if (answered) {
              if (isCorrect) style = 'bg-gold/20 text-gold border border-gold/40';
              else if (isSelected) style = 'bg-destructive/20 text-destructive border border-destructive/40';
              else style = 'glass text-muted-foreground opacity-50';
            }
            return (
              <button key={i} onClick={() => selectAnswer(i)} disabled={answered}
                className={`w-full rounded-xl p-3 text-sm text-right transition-all ${style}`}>
                <div className="flex items-center justify-between">
                  <span>{opt}</span>
                  {answered && isCorrect && <Check className="w-4 h-4" />}
                  {answered && isSelected && !isCorrect && <X className="w-4 h-4" />}
                </div>
              </button>
            );
          })}
        </div>
        {answered && <p className="text-xs text-muted-foreground mt-3 text-center">{q.ref}</p>}
      </div>
    </div>
  );
}
