# FitnessAI – AI Tabanlı Hareket Analizi

##  Proje Hakkında
FitnessAI, derin öğrenme ve bilgisayarlı görü teknikleri kullanarak squat gibi fitness hareketlerinin doğru yapılıp yapılmadığını analiz eden bir uygulamadır.  
Proje iki ana bileşenden oluşur:
- **Android Uygulaması (android-app/)** → ML Kit kullanarak gerçek zamanlı kamera ile squat açısı analizi ve kullanıcıya görsel geri bildirim.
- **Python Modeli (python-model/)** → Squat veri seti üzerinde model eğitimi ve sınıflandırma.
---

##  Kurulum

### Android (ML Kit ile)
1. Android Studio ile `android-app/` klasörünü açın.
2. ML Kit Pose Detection API’si kullanılarak kamera üzerinden squat açısı hesaplanır.
3. Build & Run ile uygulamayı çalıştırın.
4. Uygulama, kullanıcıya **Standing / HalfSquat / Squat** gibi etiketlerle gerçek zamanlı geri bildirim verir.

### Python
1. `python-model/` klasörüne gidin.
2. Gerekli bağımlılıkları yükleyin:
   ```bash
   pip install -r requirements.txt
   
# Modeli Eğitmek İçin;
python import_pandas_as_pd.py

# Veri Seti
squat_data.csv dosyası squat açısı ve etiketlerini içerir.

Örnek:

angle,label
120,Standing
85,Squat
100,HalfSquat

# Sonuçlar
Python tarafında eğitim sonrası doğruluk ve kayıp grafikleri (ileride results/ klasörüne eklenecek).

Android uygulaması ekran görüntüleri (docs/screenshots/).

Katkı
Repo düzeni:

Fitnessai/
├── android-app/      # Android kodları (ML Kit ile squat analizi)
├── python-model/     # Python kodları ve veri seti
└── docs/             # Raporlar ve görseller

