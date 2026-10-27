# Ellenőrzések

A végleges futási eredményeket a GitHub Actions futása és a hozzá csatolt HTML/XML riportok tartalmazzák. A teszt megléte nem jelenti a lefuttatását.

## Automatizált készlet

- JVM: gondozási műveletek és időzítőmaradékok, 8 órás túlélés, 48 órás elhanyagolás, 19:36 ébrenléti halál, egyhetes távollét gyors feldolgozása, egyben/perces/véletlen darabolás azonossága, éjszakai védelem, gyógyszer és takarítás különbsége, egészséges állat gyógyszere, stabil felépülés, valódi szükséglet és tanítás, egyszeri játékreward és megszakítás, egyszeri gondozási hiba, tojás keltetése, profilok elkülönítése, természetes halál, hét fejlődési ág, következő napi napirend, téli időszámítás és monoton/UTC óravisszaállítás.
- Android: valódi Room újranyitása és ékezetes adat, idempotens emlékalbum, valódi v1→v2 migráció adatvesztés nélkül, egyidejű UI/háttérműveletek, régi állat műveletének elutasítása, értesítések nélkül is bekövetkező halál, el nem indított profil védelme, UI Automator névadás, valódi etetés/takarítás/gyógyítás és Activity-rekreáció/forgatás.
- Build: debug APK, aláíratlan release APK, Android teszt-APK, Android lint.

## Android-verziók és eszközök

| Android | API | Telepítés / indítás / gondozás |
|---|---|---|
| 7.0 | 24 | Kiemelt GitHub emulátoros teszt |
| 7.1 | 25 | Tervezett, külön emulátoros kézi próba szükséges |
| 8.0 | 26 | Tervezett, külön emulátoros kézi próba szükséges |
| 8.1 | 27 | Tervezett, külön emulátoros kézi próba szükséges |
| 9 | 28 | Tervezett, külön emulátoros kézi próba szükséges |
| 10 | 29 | Tervezett, külön emulátoros kézi próba szükséges |
| 11 | 30 | Tervezett, külön emulátoros kézi próba szükséges |
| 12 | 31 | Tervezett, külön emulátoros kézi próba szükséges |
| 12L | 32 | Tervezett, külön emulátoros kézi próba szükséges |
| 13 | 33 | Tervezett, külön értesítési engedélyteszt szükséges |
| 14 | 34 | Tervezett, külön emulátoros kézi próba szükséges |
| 15 | 35 | Tervezett, force-stop és újraindítás próbája szükséges |
| 16 | 36 | Tervezett, külön emulátoros kézi próba szükséges |
| 17 | 37 | Kiemelt GitHub emulátoros teszt |

A GitHub workflow Nexus 7 profilú API 24/37 x86_64 emulátort indít. A tényleges eredmény a megfelelő futásban ellenőrizhető; az alábbi kézi mátrix nem tekinthető automatikusan teljesítettnek.

Kézi ellenőrzési mátrix: 320×568 dp kis telefon, hosszúkás telefon, 7–8 hüvelykes 16:10 tablet, 10–13 hüvelykes 4:3 és 16:10 tablet; mindegyik álló/fekvő/átméretezett ablak, 100% és 200% betűméret, TalkBack. Profilváltás, minden ikon és háromgombos vezérlés, billentyűzet, rendszer-navigáció és kivágás ellenőrzése. A felület görgethető és safeDrawing/IME inseteket használ; 800 dp felett két oszlopra vált.

Valódi force-stop, eszközkikapcsolás, lemerülés, reboot, gyártói értesítési korlátozások, 32 bites fizikai eszköz és API 37 nagy lapméretes ARM64 eszköz külön, fizikai tesztet igényel. Ezeket csak a futási jelentés kifejezett megjelölése esetén tekintjük kipróbáltnak. A jelen cloud környezetben nincs `/dev/kvm`; a gyorsított Android-eszköztesztek GitHubon futnak.

## Korlátok

A mentés helyi; nincs szinkronizáció vagy exportáló/feltámasztó gomb. A debug-aláírás fejlesztői. Az emlékeztetők nem pontos riasztások; force-stop után nem garantálhatók. Teljesen offline környezetben a rendszeróra és külső mentésvisszaállítás nem védhető tökéletesen. A saját animációk egyszerű, kevés képkockás Canvas-rajzok. A fenti mátrix végigpróbálása nélkül nem állítunk minden eszközre vizuális hibamentességet.

## Helyben ténylegesen elvégzett ellenőrzések (2026. október 27.)

- JDK 17.0.17, Gradle 9.6.0, AGP 9.4.0, SDK 37.0 / Build Tools 37.0.0 alatt a debug APK, aláíratlan release APK és Android teszt-APK lefordult.
- **25 JVM-teszt sikeres, 0 hiba.** A mentési körtesztek az el nem indított profilok és a halál pontos visszatöltését is ellenőrzik. A tesztkészlet a valódi 2026. október 25-i budapesti óraátállítás 11,5 órás védett éjszakáját, valamint gondozással a teljes 30 napos életutat és összes fejlődési szakaszt is ellenőrzi.
- Android lint sikeres, 0 error. A rögzített, kompatibilis függőségekhez újabb verziókat ajánló figyelmeztetések megmaradtak; ezeket nem tekintjük működési hibának.
- `apksigner verify --verbose`: a debug APK v2 aláírása érvényes.
- `zipalign -c -P 16 4`: sikeres. Az AndroidX `libandroidx.graphics.path.so` arm64-v8a, armeabi-v7a, x86 és x86_64 változatának minden ELF LOAD szegmense 16 384 bájtos igazítású. Ez statikus csomagellenőrzés, nem helyettesít fizikai ARM64/32 bites tesztet.
- Android-eszköztesztek helyben nem futottak; a GitHub Actions API 24 és 37.0 emulátoros futásának eredménye külön mérvadó.
