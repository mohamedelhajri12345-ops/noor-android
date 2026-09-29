import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft, Shield } from 'lucide-react';

export default function PrivacyPolicy() {
  const navigate = useNavigate();

  const sections = [
    { num: '١', title: 'مقدمة', body: 'تطبيق "القرآن الكريم" هو تطبيق إسلامي مجاني يهدف إلى تقديم محتوى ديني موثوق. نلتزم بحماية خصوصيتك.' },
    { num: '٢', title: 'المعلومات التي نجمعها', body: 'حساب المستخدم: بريدك الإلكتروني (لتسجيل الدخول والمصادقة عبر رمز تحقق OTP). ملف المجتمع: الاسم المستعار (المعرّف الفريد) والصورة الشخصية (اختياري). الرسائل: نصوص الرسائل والملفات الصوتية والصور المرسلة في محادثات المجتمع. معلومات الاستخدام: عدد مرات فتح التطبيق والصفحات المزارة (للتحسين فقط). موقعك الجغرافي: فقط عند طلبك حساب مواقيت الصلاة واتجاه القبلة (اختياري). بيانات الإشعارات: لتفعيل تذكيرات الصلاة والأذكار (اختياري).' },
    { num: '٣', title: 'المعلومات التي لا نجمعها', body: 'لا نطلب رقم هاتفك أو عنوانك البريدي. لا نبيع بياناتك لأي طرف ثالث. لا نتتبع نشاطك خارج التطبيق. رسائلك في المجتمع مرئية فقط لأعضاء المحادثة، ولا يمكن لأي شخص خارجها الاطلاع عليها.' },
    { num: '٤', title: 'الإعلانات', body: 'نستخدم إعلانات من Start.io لتمويل التطبيق. قد تجمع شبكات الإعلانات بعض البيانات لتحسين الإعلانات. يمكنك التحكم في إعدادات الإعلانات من إعدادات هاتفك.' },
    { num: '٥', title: 'الإشعارات', body: 'إشعارات الصلاة والأذكار تعمل عبر OneSignal. لا نشارك بياناتك مع أطراف ثالثة.' },
    { num: '٦', title: 'المحتوى', body: 'القرآن الكريم من مصادر موثوقة (mp3quran.net). الأذكار والأحاديث من مصادر صحيحة. الأناشيد من مصادر مجانية (archive.org).' },
    { num: '٧', title: 'التبرعات', body: 'التبرعات طوعية وتذهب لصيانة التطبيق. بياناتك البنكية لا تمر عبر التطبيق.' },
    { num: '٨', title: 'حقوقك', body: 'يمكنك حذف التطبيق في أي وقت. يمكنك تعطيل الإشعارات من الإعدادات. يمكنك استخدام التطبيق بدون إنترنت (معظم الميزات).' },
    { num: '٩', title: 'التواصل والتعديلات', body: 'قد نحدث هذه السياسة من وقت لآخر. آخر تحديث: 2026.' },
  ];

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="text-center pt-2">
        <div className="w-14 h-14 rounded-2xl bg-gold/10 flex items-center justify-center mx-auto mb-2">
          <Shield className="w-7 h-7 text-gold" />
        </div>
        <h2 className="text-xl font-bold text-gold">سياسة الخصوصية</h2>
        <p className="text-xs text-muted-foreground mt-1">تطبيق القرآن الكريم — آخر تحديث: 2026</p>
      </div>

      <div className="space-y-3">
        {sections.map(s => (
          <div key={s.num} className="glass-card rounded-2xl p-4">
            <div className="flex items-center gap-2 mb-2">
              <span className="w-7 h-7 rounded-full bg-gold/10 flex items-center justify-center text-sm font-bold text-gold shrink-0">{s.num}</span>
              <h3 className="text-sm font-bold text-foreground">{s.title}</h3>
            </div>
            <p className="text-sm text-muted-foreground leading-relaxed pr-9">{s.body}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
