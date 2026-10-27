# Lili és Léna pixelkedvencei

Magyar, teljesen offline Android-pixelállatjáték. Két külön profil, saját név és tojás, gondozás, balra/jobbra minijáték, hét felnőtt alak, végleges halál és emlékalbum. Nincs reklám, regisztráció, analitika, internetjogosultság vagy vásárlás.

**Az állat akkor is él tovább, amikor a játék be van zárva.** A már bekövetkezett halált a megnyitáskori etetés nem fordítja vissza. Új tojás új azonosítót kap.

## Telepítés

A GitHub **Actions → Android APK → sikeres futás → Artifacts → pixelgotchi-apk-and-reports** csomagban található az `app-debug.apk`. Ez **debug-aláírású, valóban telepíthető APK**, személyes használatra. A ZIP-et előbb bontsd ki. Androidon engedélyezd az APK-t megnyitó alkalmazás számára az ismeretlen alkalmazások telepítését. A release `app-release-unsigned.apk` aláírás előtt nem telepíthető.

Android 7.0 / API 24 vagy újabb szükséges. Nincs `maxSdkVersion`. Az első indítás is internet nélkül teljes. A fájlok a telefon helyi, privát alkalmazáskönyvtárába kerülnek; másik telefonon külön játék indul.

## Pontos buildváltozatok

| Összetevő | Rögzített verzió |
|---|---|
| Android Gradle Plugin | 9.4.0, beépített Kotlin-támogatás |
| Gradle Wrapper | 9.6.0, SHA-256 ellenőrzéssel |
| JDK | 17, Java/Kotlin JVM-cél: 17 |
| Kotlin / Compose Compiler plugin | 2.2.10 / 2.2.10 |
| KSP | 2.3.12 |
| Compose BOM | 2025.08.01 |
| Activity Compose | 1.10.1 |
| Lifecycle | 2.9.2 |
| Room | 2.7.2 |
| WorkManager | 2.10.3 |
| Gson | 2.13.2 |
| Desugaring | 2.1.5 |
| minSdk / compileSdk / targetSdk | 24 / 37 / 37 |
| SDK Platform / Build Tools | Android 37.0 / 37.0.0 |

Az AGP 9.4 hivatalos kompatibilitási táblája API 37-et, minimum Gradle 9.6-ot és JDK 17-et ír elő. Az AGP publikált POM-ja Kotlin 2.2.10-et tartalmaz. A Compose Compiler plugint ehhez igazítottuk. A KSP 2.3.12 támogatja a beépített Kotlin forráskezelését. A függőségek API 24-en is elérhetők; a `java.time` használatát core library desugaring teszi lehetővé. Nincs natív játékmotor vagy saját `.so` könyvtár. A Compose csomagolhat AndroidX grafikai natív segédkönyvtárat; az APK ABI- és lapméret-ellenőrzését lásd a tesztjelentésben.

Hivatalos források, ellenőrizve: 2026. október 27.:

- [Android 17 SDK](https://developer.android.com/about/versions/17/setup-sdk)
- [AGP 9.4 kompatibilitás](https://developer.android.com/build/releases/gradle-plugin)
- [Beépített Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin)
- [Compose és Kotlin](https://developer.android.com/jetpack/androidx/releases/compose-kotlin)
- [SDK csomagjegyzék](https://dl.google.com/android/repository/repository2-3.xml)

## Fordítás Windows 10/11 alatt

1. Telepíts 64 bites **JDK 17**-et és friss Android Studiót, amely támogatja az AGP 9.4-et. Állítsd a `JAVA_HOME` változót a JDK könyvtárára. Az Android Studio Gradle JDK beállítása is JDK 17 legyen.
2. Klónozd ezt a repót, vagy bontsd ki a forrás-ZIP-et. Nyisd meg a gyökérmappát Android Studióban.
3. Az SDK Managerben telepítsd az Android SDK Platform **37.0**, Build-Tools **37.0.0** és Platform-Tools csomagokat. Fogadd el az SDK-licenceket.
4. Az Android Studio létrehozza a `local.properties` fájlt. Parancssoros fordításnál ebbe írd az SDK útját, például `sdk.dir=C\:\\Users\\SajatNev\\AppData\\Local\\Android\\Sdk`.
5. PowerShellben a projekt gyökeréből:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

A telepíthető eredmény: `app\build\outputs\apk\debug\app-debug.apk`.

USB-hibakereséssel:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Az első fordításhoz internet kell a fordítóeszközök és függőségek letöltéséhez; magának a játéknak nem. Az emulátoros tesztekhez indíts egy API 24 vagy 37 emulátort, majd:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## Release aláírás

A forrás nem tartalmaz privát aláírókulcsot. Készíts saját kulcsot, és őrizd meg biztonságosan: az alkalmazás frissítéséhez ugyanaz kell. A debug és a saját release aláírás különbözik; azonos csomagnéven nem írhatják felül egymást. Az eltávolítás törli a mentést.

```powershell
keytool -genkeypair -v -keystore pixelgotchi-release.jks -alias pixelgotchi -keyalg RSA -keysize 4096 -validity 10000
$env:PIXELGOTCHI_KEYSTORE="C:\keys\pixelgotchi-release.jks"
$env:PIXELGOTCHI_STORE_PASSWORD="a-sajat-jelszavad"
$env:PIXELGOTCHI_KEY_ALIAS="pixelgotchi"
$env:PIXELGOTCHI_KEY_PASSWORD="a-sajat-kulcsjelszavad"
.\gradlew.bat assembleRelease
```

A kulcsot és jelszavakat ne commitold. CI-ben csak védett secretsből add át őket. Környezeti változók nélkül a release aláíratlan marad; a debug változat a fejlesztői kulccsal automatikusan aláíródik. Nyilvános, tartós kiadáshoz saját release kulcs ajánlott.

## Kezelés

A kezdőképernyőn Lili vagy Léna kártyáját válaszd, majd nevezd el az állatot. A még el nem indított profil ideje nem telik. A már létrehozott állatok profilváltáskor is tovább élnek. A tojás 60 másodperc múlva kikel.

Az ikonokat közvetlenül megérintheted. A három alsó gomb: **Választás → következő ikon; Rendben → megnyitás/végrehajtás; Vissza → megszakítás/profilok**. Az ételmenüben a rendes étel +1 jóllakottságot, a nasi +1 jókedvet ad. A fő játékban öt forduló alatt legalább három találat kell. A jutalom egyszer jár, megszakításért nem. A beteg állatot gyógyszerrel kezeld; a takarítás nem gyógyszer. A tanítás csak indokolatlan figyelemkérésre helyes válasz.

A Beállításokban külön kapcsolható a hang, rezgés és emlékeztető, illetve profilhoz választható készülékszín. A szülői részt hosszan nyomva nyithatod meg; ott a következő naptól érvényes napirend és a megerősítést kérő adattörlés található.

## Működés és adatbiztonság

- A `Simulator` tiszta Kotlin, eseményenként számol. Nem animációk, képkockák vagy folyamatos szolgáltatás éltetik az állatot.
- A szükségletek és az életerő egészekkel, milliszekundumos fixpontos számlálókkal működnek. A többrészes és egyben végzett szimuláció ugyanazt adja.
- Minden művelet előtt előbb feldolgozzuk a múltat. A foreground és WorkManager közös singleton repót, Mutexet és Room-tranzakciót használ; nincs külön alkalmazásfolyamat.
- A két profil, beállítások és óra egy verziózott JSON-pillanatképben, Room-táblában vannak. A halál és az egyedi állatazonosítóval kulcsolt emlékbejegyzés ugyanabban az atomi tranzakcióban mentődik.
- Room adatbázisverzió: 2. A valós 1→2 migráció a meglevő pillanatkép törlése nélkül létrehozza az emléktáblát. A sémák az `app/schemas` mappában vannak. Nincs destruktív migrációs fallback.
- Sérült/ismeretlen mentéskor magyar hibaoldal jelenik meg, nincs csendes új állat. Az eredeti adatbázist nem írjuk felül. Helyreállításhoz őrizd meg az alkalmazás adatait és az adatbázis/WAL/SHM fájlokat; ne töröld az alkalmazást.
- A rendszerindítás számlálója és `elapsedRealtime()` együtt ad monoton időt ugyanazon boot alatt. Boot után `max(utolsó_logikai_UTC, jelenlegi_UTC)` a kezdőpont; onnan újra monoton idő telik. Óravisszaállítás nem fordítja vissza az állapotot. Másik időzónába utazás nem módosítja automatikusan a mentett napirend zónáját.
- Tiszta offline játék nem tud teljes védelmet adni óracsalás vagy külső, régi adatmentés visszatöltése ellen. Automatikus felhőmentés és eszközátvitel tiltva van; nincs visszaállítás/feltámasztás funkció. Egyes gyártói átviteli eszközök ettől eltérhetnek.
- A szabályverzió 1 a mentés része. Ismeretlen szabályverziót elutasítunk, nem számoljuk át más sebességgel a múltat. Jövőbeli egyensúlyváltozáshoz külön, határidőpontot őrző migráció szükséges.
- WorkManager 30 perces, pontatlan munkát használ. Maximum kétóránként emlékeztet profilonként, súlyosabb állapot egyszer hamarabb jelezhet. Éjjel, halál után és lecserélt állatról nincs gondozási értesítés. Nincs exact alarm, foreground service vagy folyamatos saját wake lock. Force-stop után a háttérmunka nem garantált; megnyitáskor a teljes múlt feldolgozódik.

A gondozási paramétereket, fejlődési küszöböket és tesztállapotot külön dokumentáljuk:

- [Játékszabályok](docs/JATEKSZABALYOK.md)
- [Ellenőrzések és kompatibilitás](docs/TESZTEK.md)
- [Eredeti követelmények](docs/KOVETELMENYEK.md)

A sprite-ok és WAV-csipogások saját készítésűek. A csomagolt DejaVu Sans betűkészlet licence: `FONT-LICENSE.txt`. A függőségek saját nyílt forrású licenceik szerint használhatók.
