import React, { useState, useEffect } from 'react';
import { Landmark, ChevronDown, Check, Award, X, RotateCcw, ChevronRight } from 'lucide-react';
import { toArabicNumber } from '@/lib/islamicUtils';
import { hajjQuizQuestions } from '@/data/hajjQuiz';

const umrahSteps = [
  { title: 'الإحرام', desc: 'الإحرام هو نية الدخول في النسك. يُستحب الاغتسال والتطيب قبل الإحرام. يلبس الرجل إزاراً ورداءً أبيضين، أما المرأة فتلبس ما تشاء من ثيابها الشرعية. ثم ينوي بقلبه ويلبي بلسانه قائلاً: لبيك عمرة. ويبدأ بالتلبية: لبيك اللهم لبيك، لبيك لا شريك لك لبيك، إن الحمد والنعمة لك والملك، لا شريك لك.' },
  { title: 'الطواف', desc: 'بعد الوصول للمسجد الحرام، يطوف حول الكعبة سبعة أشواط. يبدأ الطواف من الحجر الأسود ويجعل الكعبة عن يساره. يُستحب للرجل أن يسرع المشي في الأشواط الثلاثة الأولى (الرمل) ويكشف كتفه الأيمن (الاضطباع). يبدأ كل شوط بالحجر الأسود وينتهي به. يُستحب مسح الحجر الأسود إن أمكن، وإلا أشار إليه.' },
  { title: 'السعي', desc: 'بعد الطواف، يخرج إلى الصفا ويسعى بين الصفا والمروة سبعة أشواط. يبدأ من الصفا ويمشي إلى المروة (شوط أول)، ثم يعود من المروة إلى الصفا (شوط ثاني)، وهكذا حتى يكمل سبعة أشواط. يُستحب الإسراع بين العلمين الأخضرين للرجال. ينتهي السعي عند المروة.' },
  { title: 'الحلق أو التقصير', desc: 'بعد إكمال السعي، يحلق المحرم شعر رأسه أو يقصره. الحلق أفضل للرجال، أما المرأة فتقصر من أطراف شعرها بمقدار أنملة. وبذلك تتم العمرة ويتحلل المحرم من إحرامه، ويباح له كل ما كان محظوراً بالإحرام.' },
];

const hajjSteps = [
  { title: 'الإحرام من الميقات', desc: 'يحرم الحاج من الميقات الذي يمر به. ينوي الحج بقلبه ويلبي بلسانه. إذا كان متمتعاً (أدى عمرة قبل الحج) فإنه يحرم بالحج في يوم التروية (٨ ذو الحجة) من مكة. يلبس ثياب الإحرام ويبدأ بالتلبية.' },
  { title: 'طواف القدوم', desc: 'بعد الوصول لمكة، يطوف الحاج طواف القدوم سبعة أشواط حول الكعبة. هذا الطواف سنة للحاج المفرد والقارن. يصلي ركعتين خلف مقام إبراهيم بعد الطواف.' },
  { title: 'السعي', desc: 'بعد طواف القدوم، يسعى الحاج بين الصفا والمروة سبعة أشواط. يبدأ من الصفا وينتهي عند المروة. المتمتع يسعى سعياً واحداً عن عمرته وحجه.' },
  { title: 'التوجه إلى منى (٨ ذو الحجة)', desc: 'في يوم التروية (٨ ذو الحجة) يتوجه الحاج إلى منى قبل الزوال. يصلي الظهر والعصر والمغرب والعشاء والفجر قصراً (الرباعية ركعتين) دون جمع. يبيت في منى ليلة عرفة استعداداً ليوم الوقوف بعرفة.' },
  { title: 'يوم عرفة (٩ ذو الحجة)', desc: 'هو أعظم أركان الحج. بعد شروق شمس يوم عرفة، يتوجه الحاج من منى إلى عرفة. يقف بعرفة بعد زوال الشمس حتى غروبها. يُكثر من الدعاء والتلبية والذكر. يجب الوقوف داخل حدود عرفة وليس في وادي عرنة. إذا لم يقف الحاج بعرفة فلا حج له.' },
  { title: 'المبيت بمزدلفة', desc: 'بعد غروب شمس يوم عرفة، يتوجه الحاج إلى مزدلفة. يصلي المغرب والعشاء جمعاً وقصراً. يبيت في مزدلفة حتى الفجر. يلتقط ٧٠ حصاة لرمي الجمرات. يجوز للضعفاء والنساء الانصراف بعد منتصف الليل.' },
  { title: 'رمي جمرة العقبة (١٠ ذو الحجة)', desc: 'بعد الفجر في يوم النحر (١٠ ذو الحجة)، يتوجه الحاج إلى منى. يرمي جمرة العقبة الكبرى بسبع حصيات متعاقبات، يكبر مع كل حصاة. بعد الرمي يذبح الهدي (للمتمتع والقارن)، ثم يحلق أو يقصر. بذلك يتحلل التحلل الأول، فيباح له كل شيء إلا النساء.' },
  { title: 'طواف الإفاضة', desc: 'بعد الرمي والحلق، يذهب الحاج إلى مكة لطواف الإفاضة (طواف الزيارة) سبعة أشواط. هذا الطواف ركن من أركان الحج. بعده يتحلل التحلل الثاني فيباح له كل شيء بما في ذلك النساء. إن لم يكن قد سعى قبل، يسعى بعد هذا الطواف.' },
  { title: 'أيام التشريق (١١-١٣ ذو الحجة)', desc: 'في أيام التشريق الثلاثة (١١-١٣ ذو الحجة)، يبيت الحاج في منى. يرمي الجمرات الثلاث كل يوم بعد الزوال: الصغرى ثم الوسطى ثم الكبرى (العقبة)، كل واحدة بسبع حصيات. يجوز التعجيل (الانصراف بعد يومين) أو التأخير (المبيت لليوم الثالث).' },
  { title: 'طواف الوداع', desc: 'آخر أعمال الحج. قبل مغادرة مكة، يطوف الحاج طواف الوداع سبعة أشواط حول الكعبة. هذا الطواف واجب لكل حاج إلا الحائض. بعده يغادر مكة. وبذلك يكتمل الحج.' },
];

const duas = [
  { text: 'لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ، لَبَّيْكَ لَا شَرِيكَ لَكَ لَبَّيْكَ، إِنَّ الْحَمْدَ وَالنِّعْمَةَ لَكَ وَالْمُلْكَ، لَا شَرِيكَ لَكَ', ref: 'التلبية' },
  { text: 'رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ', ref: 'دعاء الطواف' },
];

function StepCard({ step, index, understood, onUnderstand }) {
  const [open, setOpen] = useState(false);
  return (
    <div className="glass-card rounded-2xl overflow-hidden">
      <button onClick={() => setOpen(!open)} className="w-full p-3 flex items-center gap-3 text-right">
        <div className={`w-8 h-8 rounded-xl flex items-center justify-center shrink-0 ${understood ? 'bg-gold text-primary-foreground' : 'bg-gold/10'}`}>
          {understood ? <Check className="w-4 h-4" /> : <span className="text-xs font-bold text-gold">{toArabicNumber(index + 1)}</span>}
        </div>
        <p className="text-sm font-bold text-foreground flex-1">{step.title}</p>
        <ChevronDown className={`w-4 h-4 text-muted-foreground transition-transform ${open ? 'rotate-180' : ''}`} />
      </button>
      {open && (
        <div className="px-3 pb-3 pr-12">
          <p className="text-xs text-muted-foreground leading-relaxed">{step.desc}</p>
          {!understood && (
            <button onClick={onUnderstand} className="mt-3 bg-gold/10 text-gold rounded-xl px-4 py-2 text-xs font-bold active:scale-95 flex items-center gap-1.5">
              <Check className="w-3.5 h-3.5" /> فهمت
            </button>
          )}
        </div>
      )}
    </div>
  );
}

function HajjQuiz({ type, onRetake }) {
  const [current, setCurrent] = useState(0);
  const [score, setScore] = useState(0);
  const [selected, setSelected] = useState(null);
  const [showResult, setShowResult] = useState(false);
  const [answered, setAnswered] = useState(false);

  const questions = hajjQuizQuestions;
  const q = questions[current];

  const answer = (idx) => {
    if (answered) return;
    setSelected(idx);
    setAnswered(true);
    if (idx === q.answer) setScore(s => s + 1);
  };

  const next = () => {
    if (current + 1 >= questions.length) {
      setShowResult(true);
    } else {
      setCurrent(c => c + 1); setSelected(null); setAnswered(false);
    }
  };

  if (showResult) {
    const pct = Math.round((score / questions.length) * 100);
    return (
      <div className="space-y-4 animate-fade-in text-center">
        <div className="w-20 h-20 rounded-full bg-gold/10 flex items-center justify-center mx-auto">
          <Award className="w-10 h-10 text-gold" />
        </div>
        <h3 className="text-xl font-bold text-gold">انتهى الاختبار</h3>
        <p className="text-3xl font-bold text-foreground">{toArabicNumber(score)} / {toArabicNumber(questions.length)}</p>
        <p className="text-sm text-muted-foreground">{pct >= 80 ? 'ممتاز! أداء رائع 🌟' : pct >= 60 ? 'جيد جداً 👍' : 'تحتاج لمراجعة الخطوات 📚'}</p>
        <button onClick={onRetake} className="bg-gold-gradient text-primary-foreground rounded-xl px-6 py-3 font-bold active:scale-95 flex items-center gap-2 mx-auto">
          <RotateCcw className="w-4 h-4" /> إعادة الاختبار
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="flex items-center justify-between">
        <p className="text-xs text-muted-foreground">سؤال {toArabicNumber(current + 1)} من {toArabicNumber(questions.length)}</p>
        <p className="text-xs text-gold">النتيجة: {toArabicNumber(score)}</p>
      </div>
      <div className="h-1.5 rounded-full bg-gold/10 overflow-hidden">
        <div className="h-full bg-gold-gradient transition-all" style={{ width: `${((current) / questions.length) * 100}%` }} />
      </div>
      <div className="glass-card rounded-2xl p-4">
        <p className="text-sm font-bold text-foreground mb-4">{q.q}</p>
        <div className="space-y-2">
          {q.options.map((opt, i) => (
            <button key={i} onClick={() => answer(i)} disabled={answered}
              className={`w-full text-right p-3 rounded-xl text-sm font-medium transition-all ${
                !answered ? 'glass text-foreground active:scale-95' :
                i === q.answer ? 'bg-green-500/20 text-green-400 border border-green-500/40' :
                i === selected ? 'bg-red-500/20 text-red-400 border border-red-500/40' : 'glass text-muted-foreground opacity-60'
              }`}>
              {opt}
              {answered && i === q.answer && <Check className="w-4 h-4 inline mr-2" />}
              {answered && i === selected && i !== q.answer && <X className="w-4 h-4 inline mr-2" />}
            </button>
          ))}
        </div>
      </div>
      {answered && (
        <button onClick={next} className="w-full bg-gold-gradient text-primary-foreground rounded-xl py-3 font-bold active:scale-95">
          {current + 1 >= questions.length ? 'عرض النتيجة' : 'السؤال التالي'}
        </button>
      )}
    </div>
  );
}

export default function HajjGuide() {
  const [tab, setTab] = useState('umrah');
  const [quizMode, setQuizMode] = useState(null);
  const [quizKey, setQuizKey] = useState(0);
  const [understood, setUnderstood] = useState(() => {
    try { return JSON.parse(localStorage.getItem('nur_hajj_understood') || '{}'); } catch { return {}; }
  });

  const steps = tab === 'umrah' ? umrahSteps : tab === 'hajj' ? hajjSteps : [];
  const allUnderstood = steps.length > 0 && steps.every((_, i) => understood[`${tab}_${i}`]);

  const markUnderstood = (i) => {
    const key = `${tab}_${i}`;
    const updated = { ...understood, [key]: true };
    setUnderstood(updated);
    localStorage.setItem('nur_hajj_understood', JSON.stringify(updated));
  };

  if (quizMode) {
    return (
      <div className="space-y-4">
        <button onClick={() => setQuizMode(null)} className="flex items-center gap-1 text-xs text-gold">
          <ChevronRight className="w-3 h-3" /> رجوع للدليل
        </button>
        <HajjQuiz key={quizKey} type={quizMode} onRetake={() => setQuizKey(k => k + 1)} />
      </div>
    );
  }

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="text-center pt-2">
        <div className="w-16 h-16 rounded-3xl bg-gold/10 flex items-center justify-center mx-auto mb-3"><Landmark className="w-8 h-8 text-gold" /></div>
        <h2 className="text-xl font-bold text-gold">دليل الحج والعمرة</h2>
        <p className="text-xs text-muted-foreground mt-1">اقرأ الخطوات واضغط "فهمت" لإتمام الاختبار</p>
      </div>
      <div className="flex gap-2">
        <button onClick={() => setTab('umrah')} className={`flex-1 py-2.5 rounded-xl text-sm font-bold ${tab === 'umrah' ? 'bg-gold text-primary-foreground' : 'glass text-foreground'}`}>العمرة</button>
        <button onClick={() => setTab('hajj')} className={`flex-1 py-2.5 rounded-xl text-sm font-bold ${tab === 'hajj' ? 'bg-gold text-primary-foreground' : 'glass text-foreground'}`}>الحج</button>
        <button onClick={() => setTab('duas')} className={`flex-1 py-2.5 rounded-xl text-sm font-bold ${tab === 'duas' ? 'bg-gold text-primary-foreground' : 'glass text-foreground'}`}>أدعية</button>
      </div>

      {tab !== 'duas' && (
        <>
          <div className="space-y-2">
            {steps.map((s, i) => <StepCard key={i} step={s} index={i} understood={!!understood[`${tab}_${i}`]} onUnderstand={() => markUnderstood(i)} />)}
          </div>
          {allUnderstood ? (
            <button onClick={() => setQuizMode(tab)} className="w-full bg-gold-gradient text-primary-foreground rounded-xl py-3 font-bold active:scale-95 flex items-center justify-center gap-2">
              <Award className="w-4 h-4" /> ابدأ اختبار {tab === 'umrah' ? 'العمرة' : 'الحج'}
            </button>
          ) : (
            <p className="text-xs text-muted-foreground text-center">أكمل قراءة جميع الخطوات واضغط "فهمت" لفتح الاختبار</p>
          )}
        </>
      )}

      {tab === 'duas' && (
        <div className="space-y-3">
          {duas.map((d, i) => (
            <div key={i} className="glass-card rounded-2xl p-4">
              <p className="arabic-text text-base text-foreground leading-relaxed">{d.text}</p>
              <p className="text-xs text-gold mt-2">{d.ref}</p>
            </div>
          ))}
        </div>
      )}

      <p className="text-[10px] text-muted-foreground text-center px-4">هذا دليل مبسط. يُنصح بمراجعة كتب المناسك المتخصصة واستشارة العلماء.</p>
    </div>
  );
}
