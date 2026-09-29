import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Heart, Copy, Check, MessageCircle, ChevronLeft } from 'lucide-react';
import { donationConfig } from '@/data/donationConfig';

export default function Donation() {
  const navigate = useNavigate();
  const [copiedField, setCopiedField] = useState(null);

  const copyText = (text, field) => {
    navigator.clipboard?.writeText(text);
    setCopiedField(field);
    setTimeout(() => setCopiedField(null), 2000);
  };

  const whatsappLink = `https://wa.me/${donationConfig.whatsapp_number}?text=${encodeURIComponent('السلام عليكم، قمت بالتبرع للتطبيق')}`;

  const bankFields = [
    { label: 'اسم المستفيد', value: donationConfig.beneficiary_name, field: 'name' },
    { label: 'اسم البنك', value: donationConfig.bank_name, field: 'bank' },
    { label: 'RIB', value: donationConfig.bank_rib, field: 'rib' },
    { label: 'SWIFT', value: donationConfig.swift_code, field: 'swift' },
    { label: 'IBAN', value: donationConfig.iban, field: 'iban' },
  ];

  return (
    <div className="space-y-4 animate-fade-in">
      {/* Header */}
      <div className="glass-card rounded-3xl p-6 text-center">
        <div className="w-14 h-14 rounded-2xl bg-gold-gradient flex items-center justify-center mx-auto mb-3">
          <Heart className="w-7 h-7 text-primary-foreground" />
        </div>
        <h2 className="text-2xl font-bold text-gold">صدقتك الجارية</h2>
        <p className="text-sm text-foreground mt-2 leading-relaxed">كل تبرع منك يُبقي هذا التطبيق حياً لآلاف المسلمين</p>
      </div>

      {/* Bank info cards */}
      <div className="glass-card rounded-3xl p-4 space-y-3">
        <h3 className="section-title text-sm mb-2">بيانات التبرع</h3>
        {bankFields.map(f => (
          <div key={f.field} className="rounded-2xl bg-background/40 p-3 border border-gold/10">
            <p className="text-xs text-muted-foreground mb-1">{f.label}</p>
            <div className="flex items-center justify-between gap-2">
              <p className="text-sm font-bold text-foreground break-all flex-1" dir="ltr">{f.value}</p>
              <button onClick={() => copyText(f.value, f.field)}
                className="w-9 h-9 rounded-xl bg-gold/10 flex items-center justify-center text-gold hover:bg-gold/20 transition-colors shrink-0">
                {copiedField === f.field ? <Check className="w-4 h-4" /> : <Copy className="w-4 h-4" />}
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Steps */}
      <div className="glass-card rounded-3xl p-4 space-y-3">
        <h3 className="section-title text-sm mb-2">خطوات التبرع</h3>
        <StepCard num="١" text="انسخ RIB أعلاه" />
        <StepCard num="٢" text="افتح تطبيق بنكك وحوّل المبلغ الذي تريده" />
        <StepCard num="٣" text="أرسل لنا إشعار التحويل عبر واتساب لنشكرك شخصياً" />
      </div>

      {/* WhatsApp confirmation button — inline, not floating */}
      <a href={whatsappLink} target="_blank" rel="noopener noreferrer"
        className="w-full rounded-2xl py-3.5 font-bold flex items-center justify-center gap-2.5 active:scale-95 transition-transform text-white"
        style={{ background: '#25D366' }}>
        <MessageCircle className="w-5 h-5" />
        <span>تأكيد التبرع عبر واتساب</span>
      </a>

      <p className="text-xs text-gold text-center pt-2">جزاك الله خيراً 🤲</p>
      <button onClick={() => navigate('/privacy')} className="w-full text-xs text-muted-foreground hover:text-gold text-center py-2 pb-4">
        سياسة الخصوصية
      </button>
    </div>
  );
}

function StepCard({ num, text }) {
  return (
    <div className="flex items-center gap-3">
      <div className="w-8 h-8 rounded-full bg-gold/10 flex items-center justify-center shrink-0">
        <span className="text-sm font-bold text-gold">{num}</span>
      </div>
      <p className="text-sm text-foreground">{text}</p>
    </div>
  );
}
