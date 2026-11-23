# Avenox keyboard layer

Avenox, LeanType'ın mevcut App Profiles, toolbar, Custom AI Keys ve InputConnection altyapısını ince bir overlay olarak kullanır. Ayrı bir klavye motoru veya ikinci profil veritabanı yoktur.

## Modes

Yerleşik modlar: `normal`, `beyin`, `coding`, `research`, `image`, `github`, `custom`.

- Mod uygulama bazında saklanır (`AppQuirk.avenoxMode`).
- Boş değer otomatik seçimdir. ChatGPT/Claude/Gemini -> Beyin, Perplexity -> Research, GitHub -> GitHub, diğer uygulamalar -> Normal.
- Klavyedeki Avenox mod düğmesine dokunmak sıradaki moda geçer; uzun basmak Normal'e döner.
- Modlar hızlı düğüm görünürlüğünü filtreler; uygulama profili prompt enjeksiyonu ayrıca yapılandırılır.

## App prompt injection

App Profile içindeki `avenoxPrompt` ve `avenoxPromptPosition` kullanılır.

Pozisyonlar:
- `off`
- `prefix`

Otomatik app prompt modunda suffix/wrapper kaldırılmıştır. Eski `suffix` / `wrapper` kayıtları güvenli biçimde `off` kabul edilir.

Şablon değişkenleri:
- `{input}`
- `{selected_text}`
- `{clipboard}`
- `{app}`
- `{date}`

Prefix modu açıksa prompt gönderme anında eklenmez. Kullanıcı ilk karakteri/metni girdiğinde gerçek editör alanının en başına görünür biçimde eklenir. Kullanıcının yazdığı bölüm backspace ile tamamen silinip yalnız otomatik prefix kaldığında prefix de otomatik temizlenir. Parola alanlarında Avenox prompt davranışı devre dışıdır.

Prefix modu açıkken Enter normal satır sonudur; gönderme eylemi değildir. Avenox satırının sağındaki sabit **GÖNDER** düğmesi uygulamanın IME send/action çağrısını çalıştırır. Host uygulamanın kendi gönder düğmesi de kullanılabilir çünkü prompt zaten editör metninin içindedir.

## Quick nodes

Mevcut 10 Custom AI Key aynı zamanda Avenox düğümleridir. Her düğüm için:

- metin/şablon ve kısa etiket,
- görünür modlar (`pref_custom_ai_modes_N`),
- isteğe bağlı uygulama kapsamı (`pref_custom_ai_apps_N`),
- işlemden sonra gönder (`pref_custom_ai_direct_send_N`),
- mevcut toolbar sıralaması üzerinden sıra

kullanılır. Boş mod/uygulama kapsamı tüm modlar/uygulamalar anlamına gelir. Uygulama kapsamı tam paket adı veya `com.google.*` gibi önek jokeri destekler.

Yerel işlemler:
- `#insert`: imlece ekle
- `#prefix`: editör metninin başına ekle
- `#replace`: seçimi, seçim yoksa tüm editörü değiştir
- `#localsend`: ekle + aktif prompt ile gönder
- `#rawsend`: aktif promptu uygulamadan gönder
- `#modeprefix`, `#modeoff`: mevcut uygulamanın canlı prefix promptunu hızlı aç/kapat

Dedicated Avenox satırı normal suggestion/toolbar satırının üstündedir ancak artık sürekli açık değildir. Normal şeritteki tek AI düğmesi satırı açıp kapatır; durum yerel olarak hatırlanır ve ilk kullanımda kapalıdır. Custom AI düğümleri normal toolbar/pinned satırında tekrarlanmaz, yalnız açılan Avenox satırında gösterilir. Düğüm sırası mevcut Toolbar ayarındaki sırayı izler. Düğüme uzun basmak doğrudan o Custom AI Key düzenleme ekranını açar. Prompt modu bildirimleri kısa toast süresi kullanır.

## Privacy and safety

Avenox şablonları ve kapsamları LeanType tercihleri/App Profiles içinde yerel tutulur. Avenox katmanı yeni telemetri eklemez. Clipboard yalnız şablon açıkça `{clipboard}` kullansa da değişken hazırlanırken en fazla 5000 karakter okunur; parola alanlarında Avenox prompt enjeksiyonu devre dışıdır.
