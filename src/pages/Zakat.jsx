import React, { useState } from 'react';
import { Calculator, Coins, ChevronRight, Check } from 'lucide-react';
import { toArabicNumber } from '@/lib/islamicUtils';

const KARAT_PURITY = { '24': 0.999, '22': 0.916, '21': 0.875, '18': 0.750 };

export default function Zakat() {
  const [category, setCategory] = useState(null);
  const [result, setResult] = useState(null);

  const categories = [
    { id: 'gold', label: 'الذهب', icon: '🥇', desc: 'زكاة الذهب حسب العيار' },
    { id: 'silver', label: 'الفضة', icon: '🥈', desc: 'زكاة الفضة' },
    { id: 'cash', label: 'النقود', icon: '💵', desc: 'زكاة النقود والمدخرات' },
    { id: 'merch', label: 'المعروضات', icon: '📦', desc: 'زكاة البضائع التجارية' },
    { id: 'mixed', label: 'المحصّل', icon: '🧮', desc: 'حساب شامل لكل ما تملك' },
  ];

  const [goldGrams, setGoldGrams] = useState('');
  const [goldKarat, setGoldKarat] = useState('21');
  const [goldPrice, setGoldPrice] = useState('70');
  const [silverGrams, setSilverGrams] = useState('');
  const [silverPrice, setSilverPrice] = useState('0.87');
  const [cash, setCash] = useState('');
  const [merchValue, setMerchValue] = useState('');

  const calcGold = () => {
    const grams = parseFloat(goldGrams) || 0;
    const purity = KARAT_PURITY[goldKarat] || 0.875;
    const pureGrams = grams * purity;
    const nisab = 85; // 85g pure gold
    const value = pureGrams * (parseFloat(goldPrice) || 70);
    const nisabValue = nisab * (parseFloat(goldPrice) || 70);
    const above = pureGrams >= nisab;
    setResult({ value, nisabValue, above, zakat: above ? value * 0.025 : 0, label: 'الذهب' });
  };

  const calcSilver = () => {
    const grams = parseFloat(silverGrams) || 0;
    const value = grams * (parseFloat(silverPrice) || 0.87);
    const nisab = 595; // 595g silver
    const nisabValue = nisab * (parseFloat(silverPrice) || 0.87);
    const above = grams >= nisab;
    setResult({ value, nisabValue, above, zakat: above ? value * 0.025 : 0, label: 'الفضة' });
  };

  const calcCash = () => {
    const amount = parseFloat(cash) || 0;
    const nisabValue = 85 * (parseFloat(goldPrice) || 70);
    const above = amount >= nisabValue;
    setResult({ value: amount, nisabValue, above, zakat: above ? amount * 0.025 : 0, label: 'النقود' });
  };

  const calcMerch = () => {
    const value = parseFloat(merchValue) || 0;
    const nisabValue = 85 * (parseFloat(goldPrice) || 70);
    const above = value >= nisabValue;
    setResult({ value, nisabValue, above, zakat: above ? value * 0.025 : 0, label: 'المعروضات' });
  };

  const calcMixed = () => {
    const gGrams = parseFloat(goldGrams) || 0;
    const purity = KARAT_PURITY[goldKarat] || 0.875;
    const goldVal = gGrams * purity * (parseFloat(goldPrice) || 70);
    const silverVal = (parseFloat(silverGrams) || 0) * (parseFloat(silverPrice) || 0.87);
    const cashVal = parseFloat(cash) || 0;
    const merchVal = parseFloat(merchValue) || 0;
    const total = goldVal + silverVal + cashVal + merchVal;
    const nisabValue = 85 * (parseFloat(goldPrice) || 70);
    const above = total >= nisabValue;
    setResult({ value: total, nisabValue, above, zakat: above ? total * 0.025 : 0, label: 'المحصّل' });
  };

  const calculate = () => {
    setResult(null);
    if (category === 'gold') calcGold();
    else if (category === 'silver') calcSilver();
    else if (category === 'cash') calcCash();
    else if (category === 'merch') calcMerch();
    else if (category === 'mixed') calcMixed();
  };

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="text-center pt-2">
        <div className="w-16 h-16 rounded-3xl bg-gold/10 flex items-center justify-center mx-auto mb-3"><Calculator className="w-8 h-8 text-gold" /></div>
        <h2 className="text-xl font-bold text-gold">حاسبة الزكاة</h2>
        <p className="text-xs text-muted-foreground mt-1">اختر نوع المال الذي تريد حساب زكاته</p>
      </div>

      {!category ? (
        <div className="space-y-2">
          {categories.map(c => (
            <button key={c.id} onClick={() => setCategory(c.id)}
              className="w-full glass-card pressable rounded-2xl p-4 flex items-center gap-3 text-right">
              <div className="w-12 h-12 rounded-xl bg-gold/10 flex items-center justify-center text-2xl shrink-0">{c.icon}</div>
              <div className="flex-1">
                <p className="text-sm font-bold text-foreground">{c.label}</p>
                <p className="text-xs text-muted-foreground">{c.desc}</p>
              </div>
              <ChevronRight className="w-4 h-4 text-muted-foreground" />
            </button>
          ))}
        </div>
      ) : (
        <div className="space-y-3">
          <button onClick={() => { setCategory(null); setResult(null); }} className="flex items-center gap-1 text-xs text-gold">
            <ChevronRight className="w-3 h-3" /> رجوع للاختيار
          </button>

          {(category === 'gold' || category === 'mixed') && (
            <div className="glass-card rounded-2xl p-4 space-y-3">
              <p className="text-sm font-bold text-gold">الذهب</p>
              <div>
                <label className="text-xs text-muted-foreground mb-1 block">الوزن (غرام)</label>
                <input type="number" value={goldGrams} onChange={e => setGoldGrams(e.target.value)} placeholder="0"
                  className="w-full bg-background/40 rounded-xl px-4 py-3 text-sm outline-none border border-gold/10 focus:border-gold/40" />
              </div>
              <div>
                <label className="text-xs text-muted-foreground mb-1 block">العيار</label>
                <div className="flex gap-2">
                  {['18', '21', '22', '24'].map(k => (
                    <button key={k} onClick={() => setGoldKarat(k)}
                      className={`flex-1 py-2 rounded-xl text-sm font-bold ${goldKarat === k ? 'bg-gold text-primary-foreground' : 'glass text-foreground'}`}>
                      {toArabicNumber(k)}
                    </button>
                  ))}
                </div>
              </div>
              <div>
                <label className="text-xs text-muted-foreground mb-1 block">سعر الغرام (دولار)</label>
                <input type="number" value={goldPrice} onChange={e => setGoldPrice(e.target.value)}
                  className="w-full bg-background/40 rounded-xl px-4 py-3 text-sm outline-none border border-gold/10 focus:border-gold/40" />
              </div>
            </div>
          )}

          {(category === 'silver' || category === 'mixed') && (
            <div className="glass-card rounded-2xl p-4 space-y-3">
              <p className="text-sm font-bold text-gold">الفضة</p>
              <div>
                <label className="text-xs text-muted-foreground mb-1 block">الوزن (غرام)</label>
                <input type="number" value={silverGrams} onChange={e => setSilverGrams(e.target.value)} placeholder="0"
                  className="w-full bg-background/40 rounded-xl px-4 py-3 text-sm outline-none border border-gold/10 focus:border-gold/40" />
              </div>
              <div>
                <label className="text-xs text-muted-foreground mb-1 block">سعر الغرام (دولار)</label>
                <input type="number" value={silverPrice} onChange={e => setSilverPrice(e.target.value)}
                  className="w-full bg-background/40 rounded-xl px-4 py-3 text-sm outline-none border border-gold/10 focus:border-gold/40" />
              </div>
            </div>
          )}

          {(category === 'cash' || category === 'mixed') && (
            <div className="glass-card rounded-2xl p-4 space-y-3">
              <p className="text-sm font-bold text-gold">النقود</p>
              <input type="number" value={cash} onChange={e => setCash(e.target.value)} placeholder="المبلغ (دولار)"
                className="w-full bg-background/40 rounded-xl px-4 py-3 text-sm outline-none border border-gold/10 focus:border-gold/40" />
            </div>
          )}

          {(category === 'merch' || category === 'mixed') && (
            <div className="glass-card rounded-2xl p-4 space-y-3">
              <p className="text-sm font-bold text-gold">المعروضات (البضائع التجارية)</p>
              <input type="number" value={merchValue} onChange={e => setMerchValue(e.target.value)} placeholder="قيمة البضاعة (دولار)"
                className="w-full bg-background/40 rounded-xl px-4 py-3 text-sm outline-none border border-gold/10 focus:border-gold/40" />
            </div>
          )}

          <button onClick={calculate} className="w-full bg-gold-gradient text-primary-foreground rounded-xl py-3 font-bold active:scale-95 flex items-center justify-center gap-2">
            <Coins className="w-4 h-4" /> احسب الزكاة
          </button>

          {result && (
            <div className="glass-card rounded-2xl p-4 space-y-3 animate-slide-up">
              <div className="flex justify-between"><span className="text-sm text-muted-foreground">قيمة {result.label}</span><span className="text-sm font-bold text-foreground">${toArabicNumber(result.value.toFixed(2))}</span></div>
              <div className="flex justify-between"><span className="text-sm text-muted-foreground">النصاب</span><span className="text-sm font-bold text-foreground">${toArabicNumber(result.nisabValue.toFixed(2))}</span></div>
              <div className={`rounded-xl p-3 ${result.above ? 'bg-gold/10' : 'bg-muted/10'}`}>
                {result.above ? (
                  <>
                    <p className="text-xs text-gold mb-1 flex items-center gap-1"><Check className="w-3 h-3" /> مالك يبلغ النصاب</p>
                    <p className="text-2xl font-bold text-gold">${toArabicNumber(result.zakat.toFixed(2))}</p>
                    <p className="text-xs text-muted-foreground mt-1">زكاتك المستحقة (٢٫٥٪)</p>
                  </>
                ) : <p className="text-sm text-muted-foreground text-center">مالك لا يبلغ النصاب، لا زكاة عليك</p>}
              </div>
            </div>
          )}

          <p className="text-[10px] text-muted-foreground text-center px-4">الزكاة واجبة إذا بلغ المال النصاب وحال عليه الحول. استشر عالماً للحالات الخاصة.</p>
        </div>
      )}
    </div>
  );
}
