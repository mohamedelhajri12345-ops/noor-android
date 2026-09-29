import React, { useState, useEffect } from 'react';
import { ChevronRight, Check, X, Lock, Trophy, RotateCcw, Star } from 'lucide-react';
import { toArabicNumber } from '@/lib/islamicUtils';

const STORAGE_KEY = 'nur_prophets_journey';

// 15 levels — each about a different prophet, 5 questions per level
const levels = [
  { prophet: 'آدم عليه السلام', title: 'أبو البشر', questions: [
    { q: 'من هو أول إنسان خلقه الله؟', options: ['آدم', 'نوح', 'إبراهيم', 'موسى'], correct: 0 },
    { q: 'مم خُلق آدم عليه السلام؟', options: ['من تراب', ' من نور', 'من ماء', 'من نار'], correct: 0 },
    { q: 'من زوجة آدم عليه السلام؟', options: ['حواء', 'سارة', 'هاجر', 'نوجة'], correct: 0 },
    { q: 'في أي جنة كان آدم عليه السلام؟', options: ['جنة الخلد', 'جنة عدن', 'الجنة', 'جنة النعيم'], correct: 2 },
    { q: 'ماذا أمر الله آدم أن لا يأكل؟', options: ['التفاح', 'الشجرة', 'العنب', 'التمر'], correct: 1 },
  ]},
  { prophet: 'نوح عليه السلام', title: 'شيخ المرسلين', questions: [
    { q: 'كم سنة دعا نوح قومه؟', options: ['٥٠٠', '٧٥٠', '٩٥٠', '١٠٠٠'], correct: 2 },
    { q: 'ماذا صنع نوح عليه السلام؟', options: ['قصرًا', 'سفينة', 'بيتًا', 'مسجدًا'], correct: 1 },
    { q: 'كم عدد أبناء نوح؟', options: ['٢', '٣', '٤', '٥'], correct: 2 },
    { q: 'ماذا كان قوم نوح يعبدون؟', options: ['الله', 'الأصنام', 'النجوم', 'الملائكة'], correct: 1 },
    { q: 'ما اسم السورة التي تذكر قصة نوح؟', options: ['البقرة', 'نوح', 'هود', 'كل ما سبق'], correct: 3 },
  ]},
  { prophet: 'إبراهيم عليه السلام', title: 'خليل الرحمن', questions: [
    { q: 'من هو أبو الأنبياء؟', options: ['نوح', 'إبراهيم', 'موسى', 'عيسى'], correct: 1 },
    { q: 'ماذا ألقِي إبراهيم فيه؟', options: ['الماء', 'النار', 'الكهف', 'السجن'], correct: 1 },
    { q: 'من ابن إبراهيم الذي أمر بذبحه؟', options: ['إسماعيل', 'إسحاق', 'يعقوب', 'يوسف'], correct: 0 },
    { q: 'ماذا بنى إبراهيم مع ابنه؟', options: ['السفينة', 'الكعبة', 'المسجد', 'القصر'], correct: 1 },
    { q: 'ما لقب إبراهيم عليه السلام؟', options: ['خليل الله', 'نبي الله', 'رسول الله', 'كل ما سبق'], correct: 3 },
  ]},
  { prophet: 'إسماعيل عليه السلام', title: 'الصديق الذبيح', questions: [
    { q: 'من والد إسماعيل؟', options: ['إبراهيم', 'نوح', 'موسى', 'آدم'], correct: 0 },
    { q: 'أين عاشت هاجر وإسماعيل؟', options: ['مكة', 'المدينة', 'الطائف', 'الشام'], correct: 0 },
    { q: 'ماذا فجر الله لإسماعيل وأمه؟', options: ['بئر زمزم', 'نهر', 'عين', 'لا شيء'], correct: 0 },
    { q: 'ماذا كان إسماعيل عليه السلام؟', options: ['صبّار', 'صادق الوعد', 'حليم', 'كل ما سبق'], correct: 3 },
    { q: 'كم مرة ذُكر إسماعيل في القرآن؟', options: ['٥', '٨', '١٠', '١٢'], correct: 3 },
  ]},
  { prophet: 'لوط عليه السلام', title: 'نبي الله', questions: [
    { q: 'أين كان قوم لوط يعيشون؟', options: ['مكة', 'سدوم', 'الشام', 'اليمن'], correct: 1 },
    { q: 'ماذا فعل الله بقوم لوط؟', options: ['أغرقهم', 'أهلكهم', 'أنزل عليهم حجارة', 'كل ما سبق'], correct: 2 },
    { q: 'من أرسل الله لإنقاذ لوط؟', options: ['ملائكة', 'إبراهيم', 'نوح', 'موسى'], correct: 0 },
    { q: 'ماذا كان ذنب قوم لوط؟', options: ['الشرك', 'الفاحشة', 'الكذب', 'السرقة'], correct: 1 },
    { q: 'من نجا من قوم لوط معه؟', options: ['أهله كلهم', 'بناته فقط', 'زوجته لم تنج', 'لا أحد'], correct: 2 },
  ]},
  { prophet: 'إسحاق عليه السلام', title: 'نبي الله', questions: [
    { q: 'من والد إسحاق؟', options: ['إبراهيم', 'إسماعيل', 'نوح', 'موسى'], correct: 0 },
    { q: 'من والدته؟', options: ['سارة', 'حواء', 'هاجر', 'نوجة'], correct: 0 },
    { q: 'ماذا بشر الله إبراهيم به؟', options: ['بإسحاق', 'بإسماعيل', 'بيوسف', 'بموسى'], correct: 0 },
    { q: 'من ابن إسحاق؟', options: ['يعقوب', 'يوسف', 'موسى', 'عيسى'], correct: 0 },
    { q: 'كم مرة ذُكر إسحاق في القرآن؟', options: ['٥', '١٠', '١٥', '٢٠'], correct: 2 },
  ]},
  { prophet: 'يعقوب عليه السلام', title: 'إسرائيل', questions: [
    { q: 'ما لقب يعقوب عليه السلام؟', options: ['إسرائيل', 'إسحاق', 'إبراهيم', 'نوح'], correct: 0 },
    { q: 'كم عدد أبناء يعقوب؟', options: ['١٠', '١٢', '١٤', '١٦'], correct: 1 },
    { q: 'من ابن يعقوب الذي أحبه كثيرًا؟', options: ['يوسف', 'بنيامين', 'يهوذا', 'لا أحد'], correct: 0 },
    { q: 'ماذا حدث ليعقوب بسبب يوسف؟', options: ['فقد بصره', 'مرض', 'سافر', 'لا شيء'], correct: 0 },
    { q: 'كم سنة بكي يعقوب على يوسف؟', options: ['٥', '١٠', '٢٠', '٤٠'], correct: 3 },
  ]},
  { prophet: 'يوسف عليه السلام', title: 'صفوة الأنبياء', questions: [
    { q: 'من والد يوسف؟', options: ['يعقوب', 'إبراهيم', 'إسحاق', 'نوح'], correct: 0 },
    { q: 'ماذا رأى يوسف في منامه؟', options: ['١١ نجمة', '٩ نجوم', '٧ نجوم', '٥ نجوم'], correct: 0 },
    { q: 'أين أُلقي يوسف في البئر؟', options: ['مصر', 'الشام', 'كنعان', 'مكة'], correct: 2 },
    { q: 'من اشترى يوسف في مصر؟', options: ['العزيز', 'الملك', 'التاجر', 'لا أحد'], correct: 0 },
    { q: 'كم سنة بقي يوسف في السجن؟', options: ['٣', '٥', '٧', '١٠'], correct: 2 },
  ]},
  { prophet: 'شعيب عليه السلام', title: 'نبي الله', questions: [
    { q: 'إلى أي قوم أُرسل شعيب؟', options: ['أصحاب الأيكة', 'قوم عاد', 'قوم ثمود', 'قوم نوح'], correct: 0 },
    { q: 'ماذا كان ذنب قوم شعيب؟', options: ['التطفيف', 'الشرك', 'الكذب', 'السرقة'], correct: 0 },
    { q: 'ماذا حدث لقوم شعيب؟', options: ['أخذتهم الصيحة', 'أغرقوا', 'أحرقوا', 'لا شيء'], correct: 0 },
    { q: 'من هو والد زوجة موسى؟', options: ['شعيب', 'إبراهيم', 'نوح', 'يوسف'], correct: 0 },
    { q: 'كم مرة ذُكر شعيب في القرآن؟', options: ['٥', '٨', '١١', '١٥'], correct: 2 },
  ]},
  { prophet: 'موسى عليه السلام', title: 'كليم الله', questions: [
    { q: 'من والد موسى؟', options: ['عمران', 'إبراهيم', 'نوح', 'يوسف'], correct: 0 },
    { q: 'ماذا أمر الله أم موسى؟', options: ['أرضعيه ثم ألقِيه في اليم', 'اختبئي به', 'سافري به', 'لا شيء'], correct: 0 },
    { q: 'من ربى موسى؟', options: ['فرعون', 'العزيز', 'الملك', 'لا أحد'], correct: 0 },
    { q: 'ما معجزة موسى الأولى؟', options: ['العصا', 'اليد', 'الطوفان', 'الغرق'], correct: 0 },
    { q: 'ماذا فعل موسى بالعصا؟', options: ['ألقاها فتحولت ثعبانًا', 'ضرب بها البحر', 'كلاهما', 'لا شيء'], correct: 2 },
  ]},
  { prophet: 'هارون عليه السلام', title: 'نبي الله', questions: [
    { q: 'من هو هارون عليه السلام؟', options: ['أخو موسى', 'ابن موسى', 'عم موسى', 'لا شيء'], correct: 0 },
    { q: 'ماذا طلب موسى من الله؟', options: ['أن يرسل معه هارون', 'أن ينصره', 'أن يرحمه', 'لا شيء'], correct: 0 },
    { q: 'كم مرة ذُكر هارون في القرآن؟', options: ['١٠', '١٥', '٢٠', '٢٥'], correct: 2 },
    { q: 'ماذا كان هارون؟', options: ['نبي', 'رسول', 'وزير', 'كل ما سبق'], correct: 3 },
    { q: 'من خلف موسى حين ذهب للقاء ربه؟', options: ['هارون', 'يوشع', 'العزيز', 'لا أحد'], correct: 0 },
  ]},
  { prophet: 'داود عليه السلام', title: 'نبي الله', questions: [
    { q: 'من قتل جالوت؟', options: ['داود', 'موسى', 'سليمان', 'طالوت'], correct: 0 },
    { q: 'ماذا أنزل الله على داود؟', options: ['الزبور', 'التوراة', 'الإنجيل', 'الصحف'], correct: 0 },
    { q: 'ماذا كان يصنع داود؟', options: ['الدرع', 'السفن', 'البيوت', 'السيوف'], correct: 0 },
    { q: 'كم مرة ذُكر داود في القرآن؟', options: ['١٠', '١٦', '٢٠', '٢٥'], correct: 1 },
    { q: 'من ابن داود؟', options: ['سليمان', 'موسى', 'عيسى', 'لا شيء'], correct: 0 },
  ]},
  { prophet: 'سليمان عليه السلام', title: 'نبي الله', questions: [
    { q: 'ماذا سخر الله لسليمان؟', options: ['الرياح', 'الجن', 'الطير', 'كل ما سبق'], correct: 3 },
    { q: 'ماذا كان يفعل سليمان بالطير؟', options: ['يكلمها', 'يأكلها', 'يبيعها', 'لا شيء'], correct: 0 },
    { q: 'ما قصة سليمان وبلقيس؟', options: ['ملكة سبأ', 'ملكة مصر', 'ملكة فارس', 'لا شيء'], correct: 0 },
    { q: 'ماذا كان يفعل الجن لسليمان؟', options: ['يبنون له', 'يحفرون له', 'يصنعون له', 'كل ما سبق'], correct: 3 },
    { q: 'كم مرة ذُكر سليمان في القرآن؟', options: ['١٠', '١٧', '٢٠', '٢٥'], correct: 1 },
  ]},
  { prophet: 'عيسى عليه السلام', title: 'روح الله', questions: [
    { q: 'من والدة عيسى؟', options: ['مريم', 'خديجة', 'آسية', 'سارة'], correct: 0 },
    { q: 'كم مرة ذُكر عيسى في القرآن؟', options: ['١٥', '٢٥', '٣٥', '٤٠'], correct: 1 },
    { q: 'ماذا أنزل الله على عيسى؟', options: ['الإنجيل', 'التوراة', 'الزبور', 'القرآن'], correct: 0 },
    { q: 'ما معجزة عيسى الأولى؟', options: ['الكلام في المهد', 'إحياء الموتى', 'خلق الطير', 'شفاء المرضى'], correct: 0 },
    { q: 'ماذا يحدث لعيسى في آخر الزمان؟', options: ['ينزل إلى الأرض', 'يرفع إلى السماء', 'يموت', 'لا شيء'], correct: 0 },
  ]},
  { prophet: 'محمد ﷺ', title: 'خاتم الأنبياء', questions: [
    { q: 'أين وُلد النبي ﷺ؟', options: ['مكة', 'المدينة', 'الطائف', 'الرياض'], correct: 0 },
    { q: 'كم كان عمره عند البعثة؟', options: ['٢٥', '٣٠', '٤٠', '٥٠'], correct: 2 },
    { q: 'في أي غار نزل الوحي؟', options: ['حراء', 'ثور', 'الكهف', 'لا شيء'], correct: 0 },
    { q: 'كم سنة مكث في مكة؟', options: ['١٠', '١٣', '١٥', '٢٠'], correct: 1 },
    { q: 'كم سنة مكث في المدينة؟', options: ['٥', '٨', '١٠', '١٣'], correct: 2 },
  ]},
];

export default function ProphetsJourney() {
  const [progress, setProgress] = useState(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      return saved ? JSON.parse(saved) : { completedLevels: [], currentLevel: 0 };
    } catch { return { completedLevels: [], currentLevel: 0 }; }
  });
  const [activeLevel, setActiveLevel] = useState(null);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(progress));
  }, [progress]);

  if (activeLevel !== null) {
    return (
      <LevelPlay
        level={levels[activeLevel]}
        levelIdx={activeLevel}
        onBack={() => setActiveLevel(null)}
        onComplete={() => {
          setProgress(p => ({
            completedLevels: [...new Set([...p.completedLevels, activeLevel])],
            currentLevel: Math.max(p.currentLevel, activeLevel + 1),
          }));
          setActiveLevel(null);
        }}
      />
    );
  }

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="text-center">
        <h3 className="text-lg font-bold text-gold">رحلة الأنبياء</h3>
        <p className="text-xs text-muted-foreground mt-1">{toArabicNumber(levels.length)} مستوى · {toArabicNumber(levels.length * 5)} سؤال</p>
      </div>

      <div className="glass-card rounded-2xl p-4">
        <div className="flex items-center justify-between mb-2">
          <p className="text-sm font-bold text-foreground">المستويات المكتملة</p>
          <p className="text-sm text-gold">{toArabicNumber(progress.completedLevels.length)}/{toArabicNumber(levels.length)}</p>
        </div>
        <div className="h-2.5 rounded-full bg-muted overflow-hidden">
          <div className="h-full bg-gold-gradient transition-all" style={{ width: `${(progress.completedLevels.length / levels.length) * 100}%` }} />
        </div>
      </div>

      <div className="space-y-2">
        {levels.map((level, i) => {
          const isCompleted = progress.completedLevels.includes(i);
          const isUnlocked = i <= progress.currentLevel;
          return (
            <button
              key={i}
              onClick={() => isUnlocked && setActiveLevel(i)}
              disabled={!isUnlocked}
              className={`w-full rounded-2xl p-4 flex items-center gap-3 transition-all text-right ${
                !isUnlocked ? 'glass-card opacity-40' : isCompleted ? 'bg-gold/10 border border-gold/30' : 'glass-card hover:glass-hover'
              }`}
            >
              <div className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 ${
                isCompleted ? 'bg-gold/20' : isUnlocked ? 'bg-gold/10' : 'bg-muted'
              }`}>
                {isCompleted ? <Trophy className="w-5 h-5 text-gold" /> : isUnlocked ? <span className="text-sm font-bold text-gold">{toArabicNumber(i + 1)}</span> : <Lock className="w-4 h-4 text-muted-foreground" />}
              </div>
              <div className="flex-1">
                <p className="text-sm font-bold text-foreground">{level.prophet}</p>
                <p className="text-xs text-muted-foreground">{level.title} · {toArabicNumber(level.questions.length)} أسئلة</p>
              </div>
              {isUnlocked && !isCompleted && <ChevronRight className="w-4 h-4 text-muted-foreground rotate-180" />}
            </button>
          );
        })}
      </div>
    </div>
  );
}

function LevelPlay({ level, levelIdx, onBack, onComplete }) {
  const [qIdx, setQIdx] = useState(0);
  const [selected, setSelected] = useState(null);
  const [score, setScore] = useState(0);

  const q = level.questions[qIdx];
  if (!q) return null;
  const answered = selected !== null;

  const answer = (i) => {
    if (answered) return;
    setSelected(i);
    if (i === q.correct) setScore(s => s + 1);
    setTimeout(() => {
      if (qIdx + 1 >= level.questions.length) {
        if (score + (i === q.correct ? 1 : 0) >= Math.ceil(level.questions.length * 0.6)) {
          onComplete();
        } else {
          setQIdx(0); setSelected(null); setScore(0);
        }
      } else {
        setQIdx(i => i + 1);
        setSelected(null);
      }
    }, 1200);
  };

  return (
    <div className="space-y-4 animate-fade-in">
      <button onClick={onBack} className="text-muted-foreground hover:text-foreground text-sm">→ رجوع</button>
      <div className="text-center">
        <h3 className="text-lg font-bold text-gold">{level.prophet}</h3>
        <p className="text-xs text-muted-foreground">{level.title}</p>
      </div>
      <div className="flex items-center justify-between">
        <p className="text-xs text-muted-foreground">السؤال {toArabicNumber(qIdx + 1)}/{toArabicNumber(level.questions.length)}</p>
        <p className="text-xs text-gold">النتيجة: {toArabicNumber(score)}</p>
      </div>
      <div className="h-1.5 rounded-full bg-muted overflow-hidden">
        <div className="h-full bg-gold-gradient transition-all" style={{ width: `${(qIdx / level.questions.length) * 100}%` }} />
      </div>
      <div className="glass-card rounded-2xl p-5">
        <p className="arabic-text text-lg text-foreground mb-4">{q.q}</p>
        <div className="space-y-2">
          {q.options.map((opt, i) => {
            const isCorrect = i === q.correct;
            const isSelected = i === selected;
            let style = 'glass text-foreground hover:text-gold';
            if (answered) {
              if (isCorrect) style = 'bg-gold/20 text-gold border border-gold/40';
              else if (isSelected) style = 'bg-destructive/20 text-destructive border border-destructive/40';
              else style = 'glass text-muted-foreground opacity-50';
            }
            return (
              <button key={i} onClick={() => answer(i)} disabled={answered}
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
      </div>
    </div>
  );
}
