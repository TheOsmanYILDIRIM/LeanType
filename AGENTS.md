# Avenox LeanType fork policy

Bu depo `LeanBitLab/LeanType` upstream'inin hızlı hareket eden fork'udur. Upstream özellikleri yeniden yazılmaz; Avenox değişiklikleri mümkün olan en ince, yeniden uygulanabilir overlay olarak tutulur.

- Upstream: `LeanBitLab/LeanType` / `main`.
- Avenox çekirdek kodu varsayılan olarak `app/src/main/java/helium314/keyboard/avenox/` altında yaşar.
- Mevcut App Profiles, toolbar, Custom AI Keys, Text Expander ve InputConnection altyapıları yeniden kullanılır; paralel ikinci sistem kurulmaz.
- Upstream dosyasına zorunlu dokunuşta `AVENOX-HOOK:<id>` marker'ı bulunur ve `docs/AVENOX_OVERLAY.json` içinde kayıtlıdır.
- `tools/check_avenox_overlay.py` izin verilmeyen fork delta dosyalarını CI'da reddeder.
- `.github/workflows/avenox-upstream-sync.yml` upstream'i düzenli alır; merge + guard + test + debug APK build geçmeden `main` güncellenmez.
- Force-push, test/güvenlik kapısı zayıflatma ve upstream kodunu Avenox klasörüne kopyalama yok.
- IME davranışı değişikliklerinde CI/APK kanıtı gerçek cihaz testinin yerine geçmez.
