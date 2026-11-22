# Avenox overlay strategy

LeanType çok hızlı güncellendiği için fork, upstream'den kopan ayrı bir kod tabanı olarak yönetilmez. Avenox özellikleri küçük hook noktalarıyla mevcut LeanType altyapısına bağlanır.

- Uygulama tanıma ve uygulama bazlı davranış: mevcut `AppQuirksManager`.
- Hızlı düğmeler: mevcut 10 adet Custom AI toolbar tuşu.
- Etiketler ve toolbar yerleşimi: mevcut Toolbar özelleştirmesi.
- Mesaj gönderimi: mevcut InputConnection/editor action hattı.
- Avenox çekirdek mantığı: `helium314.keyboard.avenox`.

Dört saatte bir upstream kontrol edilir. Yeni commit varsa normal Git merge uygulanır. Çatışma, overlay guard, settings map, test veya debug APK build başarısızsa fork `main` güncellenmez. Başarılı aday tek merge commit olarak push edilir.
