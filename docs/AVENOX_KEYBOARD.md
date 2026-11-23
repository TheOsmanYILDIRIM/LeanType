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
- `suffix`
- `wrapper`

Şablon değişkenleri:
- `{input}`
- `{selected_text}`
- `{clipboard}`
- `{app}`
- `{date}`

`{input}` içeren şablon otomatik olarak wrapper semantiğine alınır. Aynı wrapper/prefix/suffix ikinci kez uygulanmaz. Parola alanlarında enjeksiyon yapılmaz. Android editöründen güvenle okunamayan çok uzun metinler değiştirilmez.

Host uygulamanın ekrandaki kendi gönder düğmesi IME'yi atlayabilir. Prompt modu açıkken klavye action tuşu Send'e çevrilir; garantili enjeksiyon için klavye Send veya bir Avenox direct-send düğümü kullanılmalıdır.

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
- `#suffix`: editör metninin sonuna ekle
- `#replace`: seçimi, seçim yoksa tüm editörü değiştir
- `#localsend`: ekle + aktif prompt ile gönder
- `#rawsend`: aktif promptu uygulamadan gönder
- `#modeprefix`, `#modesuffix`, `#modeoff`: mevcut uygulamanın prompt enjeksiyonunu hızlı değiştir

Dedicated Avenox satırı normal suggestion/toolbar satırının üstündedir ancak artık sürekli açık değildir. Normal şeritteki tek AI düğmesi satırı açıp kapatır; durum yerel olarak hatırlanır ve ilk kullanımda kapalıdır. Custom AI düğümleri normal toolbar/pinned satırında tekrarlanmaz, yalnız açılan Avenox satırında gösterilir. Düğüm sırası mevcut Toolbar ayarındaki sırayı izler. Düğüme uzun basmak doğrudan o Custom AI Key düzenleme ekranını açar. Prompt modu bildirimleri kısa toast süresi kullanır.

## Privacy and safety

Avenox şablonları ve kapsamları LeanType tercihleri/App Profiles içinde yerel tutulur. Avenox katmanı yeni telemetri eklemez. Clipboard yalnız şablon açıkça `{clipboard}` kullansa da değişken hazırlanırken en fazla 5000 karakter okunur; parola alanlarında Avenox prompt enjeksiyonu devre dışıdır.
