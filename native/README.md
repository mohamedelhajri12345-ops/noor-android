# تحويل NOOR إلى تطبيق هاتف حقيقي (APK / iOS)

هذا المشروع مجهز بالكامل للتحويل إلى تطبيق يثبّت على الهاتف عبر Capacitor.

## المتطلبات (مرة واحدة على جهازك)

1. Node.js 18+: https://nodejs.org
2. Android Studio (يأتي مع SDK وGradle): https://developer.android.com/studio
3. لجهاز iPhone: حتماً MacBook مع Xcode

## خطوات إنتاج APK لأندرويد

```
npm install
npm run build
npm install @capacitor/core @capacitor/cli @capacitor/android
npx cap add android
npx cap sync android
npx cap open android
```

في Android Studio:
1. انتظر مزامنة Gradle
2. Build > Build Bundle(s) / APK(s) > Build APK(s)
3. الملف الناتج: android/app/build/outputs/apk/debug/app-debug.apk

لإصدار النهائي للنشر على Google Play: Build > Generate Signed Bundle / APK مع مفتاح توقيع خاص بك.

## الأيقونات

الأيقونة الجاهزة في native/icons/icon-1024.png (1024×1024). لتوليد كل المقاسات:

```
npm install -D @capacitor/assets
npx capacitor-assets generate --android --ios
```

(ضع icon-1024.png في resources/ قبل الأمر أو عدّل المسار)

## ملاحظات مهمة

1. تطبيقك يعتمد على خدمات Base44 الخلفية (قاعدة البيانات، تسجيل الدخول، الدوال المجدولة). التطبيق المثبت يتصل بها عبر الإنترنت، فتسجيل الدخول والحسابات يعملان كما في نسخة الويب.
2. جرّب تدفق تسجيل الدخول (خصوصاً Google) داخل النسخة المثبتة قبل النشر، لأن OAuth قد يحتاج إعداد deep links في AndroidManifest.xml.
3. الإشعارات (الأذان) حالياً تعمل عبر OneSignal في الويب؛ داخل Capacitor ستحتاج @capacitor/push-notifications أو إبقاء الوضع كما هو مع اختبار.
4. هذا المسار ينتج تطبيقاً حقيقياً يثبّت من المتاجر ويعمل بشاشة كاملة وبدون متصفح، لكن نواة الواجهة تبقى بنفس كود React الحالي. إعادة الكتابة بـ Kotlin/Swift الكامل تعني مشروعاً منفصلاً لكل منصة وكل مزايا المنصة (المصادقة، الكيانات، الدوال المجدولة) ستحتاج بناءها من الصفر بـ APIs.
