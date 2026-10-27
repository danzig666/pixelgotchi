# Fejlesztői prompt: Lili és Léna pixelkedvencei

## 1. Feladat és a kívánt végeredmény

Tapasztalt Android-fejlesztőként, játéktervezőként és felülettervezőként készíts teljes, működő, telepíthető Android-játékot Lili és Léna számára. A játék a klasszikus, első generációs Tamagotchi élményét kövesse a lehető leghűségesebben: tojásból kikelő pixelállat, etetés, játék, takarítás, betegség, gyógyszer, alvás, figyelemkérés, tanítás, gondozástól függő fejlődés és végleges halál.

**Munkacím: „Lili és Léna pixelkedvencei”.**

A követelmények fontossági sorrendje:

1. Valóban működő gondozás és életciklus, elhanyagolás esetén tényleges halállal.
2. Az eredeti Tamagotchi felismerhető megjelenése és hangulata.
3. Gyermekek számára könnyű, teljesen magyar kezelés.
4. Megbízható működés Android 7.0–17 között, telefonon és tableten.
5. Tartós mentés, helyes időkezelés és alacsony akkumulátor-használat.

A teljes játékot valósítsd meg, minden szükséges forrásfájllal, grafikával, hanggal, buildbeállítással és érdemi teszttel. A kész eredményben minden látható gomb végezze a jelzett műveletet. A fontos funkciókhoz ne maradjon TODO, helykitöltő, szimulált mentés vagy működés nélküli felület.

A konkrét időzítések az alábbiakban a család napirendjéhez igazított saját játéktervezési szabályok. Ne tüntesd fel őket az eredeti Bandai-program pontosan visszafejtett algoritmusaként. Az eredetihez való hasonlóságot elsősorban a megjelenés, a gondozás, a kezelési logika és az életciklus adja.

## 2. Android és technológia

Készíts natív Android-alkalmazást Kotlin nyelven. A felülethez használj Jetpack Compose-t; a pixeles játéktérhez Compose Canvas vagy megfelelő egyedi rajzolókomponens használható.

Kötelező SDK-beállítások:

- `minSdk = 24`, azaz Android 7.0.
- `compileSdk = 37`.
- `targetSdk = 37`, azaz Android 17.
- Ne állíts be `maxSdkVersion` értéket.

A fejlesztés kezdetén ellenőrizd a hivatalos dokumentációból az Android SDK, az Android Gradle Plugin, a Gradle, a Kotlin, a Compose és a szükséges JDK együttműködő stabil verzióit. Rögzíts konkrét függőségverziókat; ne használj dinamikus „+” verziókat. Minden függőség ténylegesen támogassa az API 24-et.

Az újabb Android API-kat verzióellenőrzéssel vagy AndroidX kompatibilitási megoldással hívd. Ha `java.time` alapú időkezelést használsz, állítsd be az Android 7 számára szükséges core library desugaringot.

A játék teljes működéséhez ne kelljen böngésző, szerver, internetkapcsolat vagy Google Play Services. Ehhez a kis kétdimenziós játékhoz ne vezess be nagy játékmotort. Lehetőleg kerüld a natív `.so` függőségeket; ha mégis szükségesek, ellenőrizd a régi 32 bites eszközök, az arm64 eszközök és az új rendszerek lapméret-követelményeit is.

## 3. Lili és Léna profilja

Az első képernyőn két nagy, jól megkülönböztethető profilkártya legyen: **„Lili” és „Léna”**. Mindegyiken jelenjen meg a kisállat neve, képe és rövid állapota.

Mindkét profilhoz külön tartozzon:

- Egy saját kisállat, saját névvel és egyedi azonosítóval.
- Saját életkor, gondozási állapot, fejlődési előzmény és mentés.
- Választható készülékházszín.
- Saját korábbi állatokat tartalmazó emlékalbum.

Az első állat elnevezése egyszerű legyen. Adj választható neveket, például „Pötyi”, „Morzsi”, „Bogyó”, és lehessen saját nevet írni magyar ékezetekkel is.

A még el nem indított profilban ne induljon el az életciklus. Ne legyen olyan állat, amely már az első kiválasztása előtt meghal. A létrehozott tojás rövid keltetése viszont a megadott idő szerint folytatódjon.

A két már elindított kisállat ideje egymástól függetlenül teljen. Lili profiljának használata alatt Léna állata is éhezhet és megbetegedhet. **A profilváltás ne szüneteltesse az életet.**

Egyetlen telefonon vagy tableten is lehessen mindkét profilt használni. Külön készülékeken helyi, önálló mentések legyenek; az első változathoz nem szükséges szinkronizáció.

## 4. Megjelenés: klasszikus Tamagotchi

A főképernyő egy tojás alakú kézi készüléket idézzen. Legyen színes, enyhén térhatású ház, középen szürkészöld LCD-hatású kijelző, a kijelző körül gondozási ikonok, alul pedig három nagy gomb.

A kisállat és a játéktér fekete vagy nagyon sötét, éles pixelgrafikát használjon. A pixelállat legyen saját megrajzolt karakter, a klasszikus figurák egyszerűségével és szerethető arányaival. A képernyő és az animációk adjanak kilencvenes évekbeli digitálisállat-élményt.

Követelmények:

- A sprite-ok élesek maradjanak nagyítva is; ne mosd el őket interpolációval.
- Legyen járkálás, pislogás, evés, öröm, duzzogás, alvás, betegség, játék, fejlődés és halál animáció.
- A mozgás kevés képkockás, szándékosan pixeles legyen.
- Az LCD-n látszódjon a bent hagyott ürülék, a betegségjelzés és az alvás.
- Rövid, saját készítésű csipogások jelezzék a gombnyomást és a fontos eseményeket.
- A hang és a rezgés külön kikapcsolható legyen.
- Az alapnézetben az állat és a gondozási műveletek kapják a legtöbb helyet.

Minden grafika, ikon, betűkészlet és hang legyen a projekt része, helyben elérhető. Az átadott alkalmazás első indításkor, internet nélkül is teljes legyen.

## 5. Egyszerű kezelés és képernyőméretek

A három alsó gomb jelentése legyen következetes:

- **Bal: „Választás”** – továbblép a következő ikonra.
- **Középső: „Rendben”** – végrehajtja vagy megnyitja a kiválasztott műveletet.
- **Jobb: „Vissza”** – visszalép vagy megszakít.

Emellett a gondozási ikonokra közvetlenül rá lehessen bökni. A közvetlen érintés és a háromgombos vezérlés ugyanazt a műveletet hívja meg, ugyanazokkal a szabályokkal.

Az ikonokhoz tartozzon rövid magyar felirat: „Étel”, „Játék”, „Takarítás”, „Gyógyszer”, „Lámpa”, „Tanítás”, „Állapot”. A figyelemjelzés megérintése mondja el, mire van szükség.

A gyakori műveletek legfeljebb két érintéssel elérhetők legyenek. Etetéshez és takarításhoz ne kérj újra meg újra megerősítést. Az újrakezdés és az adattörlés viszont kapjon egyértelmű megerősítést.

A gombok lehetőleg legalább 56 × 56 dp érintési területűek legyenek; a kisebb kiegészítő vezérlők se menjenek 48 × 48 dp alá. Legyen jól olvasható magyar szöveg, megfelelő kontraszt, TalkBack-leírás és nagyított betűmérettel is használható elrendezés.

Telefonon a készülék és a fő gombok töltsék ki kényelmesen a rendelkezésre álló helyet. Fekvő tableten a készülék mellé kerülhet az állapot és a gondozási menü. A grafika arányosan méreteződjön; ne nyúljanak meg a pixelek vagy az állat.

Támogasd az álló és fekvő helyzetet, a 16:9, 16:10, 4:3 és hosszúkás telefonos képarányokat, valamint az ablakátméretezést. A rendszer navigációs területe, kamerakivágása és billentyűzete ne takarjon el lényeges vezérlőt.

## 6. Állapotmodell

A kisállatnak külön legyen életállapota, fejlődési szakasza és gondozási állapota. A betegség és az alvás egyszerre is fennállhat; az egyik ne törölje a másikat.

Tárold legalább:

- Profilazonosító és egyedi állatazonosító.
- Név, születés/kikelés időpontja, életkor, fejlődési szakasz és karakterváltozat.
- Jóllakottság: 0–4 szív.
- Jókedv: 0–4 szív.
- Életerő: belső 0–100 érték.
- Testsúly és tanultság.
- Bent lévő ürülék mennyisége és a tartós szennyezettség ideje.
- Betegség, annak kezdete és az összes betegen töltött idő.
- Alvási napirend, aktuális alvás és lámpaállapot.
- Gondozási hibák, tartós hiányállapotok és indokolatlan figyelemkérések.
- Az összes részidőzítő és a legutóbb feldolgozott idő.
- Halál esetén időpont, ok és végső állapot.
- Szabálykészlet-verzió és a szükséges véletlengenerátor-állapot.

A gyermeknek a főképernyőn elsősorban a két négyszíves sor és rövid állapotüzenet jelenjen meg. A technikai számlálók a belső modellhez tartoznak.

## 7. Kötelező gondozási alapértékek

Az alábbi szabályok legyenek az első teljes változat működő alapbeállításai. Egy központi `GameBalance` konfigurációban tárold őket. Az „ébren töltött idő” a kisállat napirend szerinti ébrenlétét jelenti, akkor is, ha az alkalmazás zárva van.

| Mechanika | Alapszabály |
|---|---|
| Kezdőállapot | Kikeléskor 4 jóllakottsági szív, 4 jókedvszív, 100 életerő, tiszta környezet, egészséges állat; a szükséglet- és ürülékidőzítők 0-ról indulnak. |
| Éhség | Minden 2 ébren töltött óra után fogyjon 1 jóllakottsági szív. |
| Jókedv | Minden 3 ébren töltött óra után fogyjon 1 jókedvszív. |
| Rendes étel | Egy adag adjon +1 jóllakottsági szívet, legfeljebb 4-ig, és +1 gramm testsúlyt. |
| Nasi | Egy adag adjon +1 jókedvszívet, legfeljebb 4-ig, és +2 gramm testsúlyt. Jóllakottságot ne adjon. |
| Sikeres minijáték | +1 jókedvszív és −1 gramm, a fejlődési szakasz minimális testsúlya alá csökkenés nélkül. |
| Ürülék | Minden 4 ébren töltött óra után keletkezzen egy adag. |
| Takarítás | Egy művelet távolítsa el az összes bent lévő ürüléket. |
| Betegség | Ha legalább 2 adag ürülék megszakítás nélkül 4 ébren töltött órán át bent marad, az állat betegedjen meg. |
| Éhezéskárosodás | 0 jóllakottsági szív mellett óránként 6 életerőpont fogyjon. |
| Betegségkárosodás | Kezeletlen betegség mellett óránként további 4 életerőpont fogyjon. |
| Gyógyszer | Egy kezelés szüntesse meg a betegséget, de ne töltse vissza az életerőt és ne takarítson ki. |
| Felépülés | Legalább 2 jóllakottsági szív, legalább 1 jókedvszív, tiszta környezet és betegségmentesség mellett, 1 teljes ébren töltött órányi stabil jó állapot után óránként +2 életerőpont járjon, legfeljebb 100-ig. |
| Halál | Amint az életerő eléri a 0-t, az állat végleg meghal. |

A szívfogyás időzítőinek maradékát őrizd meg. Az etetés ne indítsa újra a kétórás fogyási ciklust; a játék ne indítsa újra a jókedv fogyási ciklusát. A megnyitás, mentés és képernyőváltás sem nullázhat időzítőt.

Tele gyomornál a kisállat utasítsa vissza a további rendes ételt. A nasi legyen tudatos választás, és a túlzott nassolás, illetve súlygyarapodás befolyásolja a fejlődést. A nasi ne helyettesítse a rendes ételt. A nulla jókedv önmagában ne okozzon közvetlen halált, de legyen gondozási következménye.

A takarítás nullázza a tartós szennyezettség számlálóját, a már kialakult betegséget azonban nem gyógyítja. Tényleges betegség meggyógyítása után, ha a környezet továbbra is szennyezett, új négyórás betegségi kitettség kezdődjön; ne legyen azonnali visszabetegedési hurok. **Egészséges állatnál a gyógyszer legyen hatástalan, és ne nullázza a szennyezettségi időt.** A felépülés egyórás várakozása csak újonnan kialakult jó állapotnál induljon; az alkalmazás újranyitása ne indítsa újra.

A fejlődési szakaszok az első változatban ne módosítsák ezeket az alapfogyási sebességeket.

**Ellenőrző példa:** frissen kikelt, teljesen ellátott, tiszta, egészséges állat gondozás nélkül 8 ébren töltött óra után lesz éhes, a 12. órában betegszik meg. Ekkorra 76 életereje marad. Ezután óránként összesen 10 pontot veszít, ezért **19 óra 36 perc ébren töltött idő után meghal**. Ezt automatizált teszt igazolja.

Az egyensúly célja, hogy a reggeli, hazaérkezés utáni és esti rövid gondozás elegendő legyen. Egy jól ellátott állat éljen túl 8 óra iskolai távollétet. Teljes elhanyagolás mellett, a normál napirenddel, 48 valós órán belül pusztuljon el.

## 8. Alvás és lámpa

Alapértelmezésként **20:30 és 07:00 között aludjon**. Ez a védett éjszakai időszak a gyermekek napirendjéhez igazított tervezési döntés.

Alvás alatt álljon meg:

- A jóllakottság és jókedv fogyása.
- Az ürülék keletkezése és a szennyezettségi kitettség ideje.
- Az éhezés és betegség életerő-károsítása.
- Az egészség visszatöltődése.

Az életkor közben teljen. A meglévő éhség, betegség és kosz maradjon meg reggelre. **Az alvás ne etessen, ne gyógyítson és ne takarítson.**

Az alvást a napirend határozza meg. A lámpa lekapcsolásával napközben ne lehessen korlátlanul elaltatni az állatot. A sötétben ébren maradó állat szükségletei tovább változzanak.

A lámpa kezelése őrizze az eredeti játék hangulatát. Ha az elalvás után 30 valós perccel is ég a lámpa, abból éjszakánként legfeljebb egy gondozási hiba származzon a fejlődéshez, egészségvesztés nélkül. Ébredéskor jelenjen meg egyértelmű jelzés.

A szülői beállításban lehessen módosítani az alvás és ébredés idejét, naponta 8–12 órás alvási tartományban. A módosítás csak a következő napra hasson, soha ne számolja át a múltat. A napirendhez tárolj időzónát; kezeld helyesen a nyári és téli időszámítást.

## 9. Minijáték és tanítás

A fő minijáték az első generációt idéző balra/jobbra találgatás legyen:

- A gyermek két nagy nyíl közül választ.
- A kisállat balra vagy jobbra fordul.
- Egy menet öt fordulóból áll.
- Legalább három találat számít győzelemnek.
- A jutalom a gondozási táblázat szerinti +1 jókedvszív és testsúlyváltozás.
- A vereség ne csökkentse az életerőt.
- Megszakított játékért ne járjon jutalom.
- Egy befejezett menet jutalma pontosan egyszer legyen elszámolható, forgatás vagy újranyitás után is.

Legyen rövid útmutató: „Találd ki, merre fordulok!” Ne kérjen gyors olvasást vagy időre végzett összetett műveletet.

Az állat időnként indokolatlanul is kérhet figyelmet. Ilyenkor, ha jóllakott, vidám, egészséges és tiszta, a „Tanítás” legyen helyes válasz. Valódi éhség, betegség vagy szomorúság esetén a tanítás ne helyettesítse a szükséges gondozást.

A tanítás rövid, szelíd animáció legyen. Növelje a tanultságot, és hasson a fejlődési ágra. Ugyanarra a figyelemkérésre ne lehessen ismételt gombnyomásokkal korlátlan pontot szerezni. Az indokolatlan kérések gyakorisága legyen visszafogott és reprodukálható; ne legyen állandó csipogás.

## 10. Fejlődés és életciklus

Valósítsd meg ezt az életutat:

- **Tojás:** létrehozás után 60 másodperc keltetés.
- **Baba:** a kikeléstől 1 órás korig.
- **Gyermek:** 1 órás kortól 48 órás korig.
- **Kamasz:** 48 órás kortól 120 órás korig.
- **Felnőtt:** 120 órás kortól.
- **Idős állat:** 20 napos kortól.
- **Természetes élettartam vége:** 30 napos korban.

A fenti életkor a kikelés óta eltelt valós idő; az alvás is beleszámít. A felületen a kor napokban jelenjen meg, az első napon szükség esetén órákban.

Legyen legalább hét jól megkülönböztethető felnőtt pixelkarakter. A fejlődési ág függjön a gondozási hibáktól, a betegen és hiányállapotban töltött időtől, a játékos törődéstől, a tanultságtól és a nassolástól. A képernyő megnyitásainak száma ne számítson jó gondozásnak.

Készíts dokumentált, determinisztikus fejlődési táblát konkrét küszöbökkel. Az alapvető gondozási minőséget ne váltsa ki véletlensorsolás. Legfeljebb az azonos gondozási osztályba tartozó, egyenértékű külalakok között legyen tárolt seed alapján változatosság.

Egy folyamatos, jelzett hiányhelyzetből akkor keletkezzen gondozási hiba, ha 30 ébren töltött percig nem oldják meg. Ugyanazért a megszakítás nélküli hiányért csak egyszer számolj hibát; a további elhanyagolást annak időtartama és az életerővesztés mutassa.

Az időskor legyen látható a mozgáson és az állapotlapon. A természetes halál oka egyértelműen különüljön el az éhezéstől és a betegségtől. A kedvezőtlenebb gondozási ág állatát se nevezd „rossznak” vagy „értéktelennek”.

## 11. A halál valóban végleges legyen

**Ez a feladat lényegi, kötelező követelménye.**

Az elhanyagolt állat ténylegesen meghalhat, az alkalmazás bezárása és az értesítések letiltása közben is. A halott állat többé ne legyen etethető, gyógyítható vagy visszahozható.

Ne legyen halhatatlan mód, automatikus etetés, automatikus gyógyítás, távolléti bébiszitter, feltámasztó gomb vagy az alkalmazás bezárásakor aktiválódó szünet. A szülői beállítás se kapcsolja ki a halált. Fejlesztői állapotmódosító funkció csak külön debug-változatban lehet.

A halálképernyő közölje egyszerűen:

**„Pötyi meghalt.”**

Az oknak megfelelő szöveg például:

- „Túl sokáig maradt étel nélkül.”
- „Sokáig beteg volt.”
- „Éhes és beteg volt.”
- „Elérte élete végét.”

Legyen visszafogott pixelanimáció vagy emlékkép. Mutassa a nevet, a végső alakot, az életkort és a születés/halál időpontját. A szöveg legyen tényszerű, és ne hibáztassa a gyermeket. A halált ne nevezd alvásnak vagy elutazásnak.

Az „Új tojás” új állatot indítson, új azonosítóval. Előtte kérjen rövid megerősítést. A korábbi állat kerüljön az emlékalbumba. A régi állathoz tartozó későn befutó munka vagy értesítés ne változtathassa meg az új állatot.

## 12. Valós idő, háttér és offline állapotszámítás

**A játék időkezelése ne egy folyamatosan futó Android-szolgáltatáson múljon.**

Készíts Androidtól független, determinisztikus szimulációs motort. A motor a mentett állapotból és az eltelt időből számítsa ki az aktuális állapotot.

Az állapotfrissítés kötelező:

- Indításkor és előtérbe kerüléskor.
- Profil megnyitásakor.
- Minden gondozási művelet végrehajtása előtt.
- Az értesítési háttérmunka futásakor.
- Aktív használat közben a következő releváns eseménynél.

Mindig előbb dolgozd fel az eltelt időt, és csak utána hajtsd végre a most kért etetést, játékot vagy gyógyszerezést. **Ha az állat már korábban meghalt, egy gyors gombnyomás ne menthesse meg visszamenőleg.**

Eseményenként haladj: ébredés, elalvás, szívfogyás, ürülék, betegség, felépülés, fejlődés, halál. A folyamatos életerőváltozásnál a halál pontos idejét is számítsd ki. Hosszú távollétet ne másodperces ciklussal számolj végig.

Ne vágd le önkényesen az offline időt néhány órára vagy egy napra. Többhetes távollét esetén a tényleges múltbeli halálig számolj, majd zárd le az állat állapotát.

Legyen azonos eredménye annak, ha egy időszakot egy lépésben vagy több részletben számítasz ki. Az azonos időpont ismételt feldolgozása ne változtasson az állapoton. A felhasználói felület és a háttérmunka ne dolgozhassa fel kétszer ugyanazt az időt.

A folyamatkilövés, a telefon kikapcsolása, az akkumulátor lemerülése és az újraindítás sem adhat automatikus életmentést. Kikapcsolt készüléken nem fut a program; a visszatéréskor végzett helyes visszaszámítás biztosítsa a folytonos játékidőt.

## 13. Órakezelés és mentés

Azonos rendszerindításon belül használj monoton időforrást, például `SystemClock.elapsedRealtime()`-ot. Tárolj UTC-időbélyeget és a rendszerindítás felismeréséhez szükséges adatot is. Újraindítás után dokumentált UTC-alapú eljárás becsülje az eltelt időt.

A visszaállított óra ne fiatalítsa az állatot, ne fordítsa vissza a gondozást, és ne támasszon fel halott állatot. Őrizd meg a már feldolgozott állapotot; ne alkalmazz negatív időlépést. Az időzónaváltás ne számoljon el kétszer azonos időt. Tisztán offline működéshez ne ígérj teljes óramanipuláció- vagy külső mentésvisszaállítás-ellenes védelmet.

A játékállapotot Room adatbázisban tárold, a beállításokat szükség esetén DataStore-ban. Használj tranzakciókat és sorosított állapotmódosítást.

Minden sikeres gondozási művelet, fejlődés és halál után azonnal ments. Ne csak az alkalmazás bezárásakor próbálj menteni. A halál rögzítése és az emlékalbum bejegyzése legyen atomi és ismételt futtatásra biztonságos.

Legyen adatbázis-verziózás és valódi migráció; egy alkalmazásfrissítés ne törölje az állatokat. Sérült vagy olvashatatlan mentés esetén adj érthető magyar hibaüzenetet, és őrizd meg a hibás adatokat a helyreállításhoz. Ne cseréld ki csendben egy új állatra.

A normál visszatöltés és az Android automatikus mentése körüli döntéseket dokumentáld. Régebbi adatállapot visszaállítása ne legyen a gyermekeknek kínált feltámasztási funkció.

## 14. Értesítések és energiahasználat

Az értesítések segítsenek gondoskodni az állatról, de a játékszimuláció ne függjön a kézbesítésüktől.

Használj WorkManagert a nem percre pontos emlékeztetőkhöz. Ne kérj exact-alarm jogosultságot, ne használj állandó foreground service-t, folyamatos wake lockot vagy kötelező akkumulátorkímélési kivételt.

Android 13-tól megfelelően kezeld a `POST_NOTIFICATIONS` engedélyt. Előtte egyszerűen magyarázd el: „Szóljunk, ha a kisállatodnak szüksége van rád?” Elutasítás esetén a játék teljes értékűen működjön tovább.

Az értesítések magyarul nevezzék meg a profilt és az állatot, például:

„Lili – Pötyi éhes. Nézz rá, amikor tudsz!”

„Léna – Morzsi beteg. Gyógyszerre van szüksége.”

Az értesítés a megfelelő profilhoz vezessen. Kikapcsolt értesítéseknél is legyen látható figyelemjelzés a játékon belül.

Ugyanazt a megoldatlan problémát ne jelezd néhány percenként. Profilonként legfeljebb kétóránként ismételj emlékeztetőt; súlyosabb állapotba kerülés egyszer indokolhat új jelzést. Alvásidőben ne küldj hangos gondozási értesítést.

Hosszú távollét után ne küldd ki az összes elmaradt jelzést. Már meghalt vagy lecserélt állat gondozásáról ne érkezzen új értesítés. Kényszerített leállítás után ne ígérj biztos háttérértesítést; a következő megnyitáskor viszont számold ki a teljes eltelt időt.

Csak a látható játéktér animáljon. Háttérben ne fusson animációs ciklus. A memória-, processzor- és akkumulátor-használat régebbi tableten is legyen alacsony.

## 15. Magyar nyelv és beállítások

Az alkalmazás minden saját, felhasználónak látható szövege magyar legyen, akkor is, ha a készülék rendszernyelve más. A rendszer saját engedélykérő ablaka a rendszer nyelvét követheti.

A magyar szövegek erőforrásokban legyenek. Ügyelj az ő és ű karakterekre a betűkészletben is. A kezelőfelületen rövid mondatokat és hétköznapi szavakat használj.

Példák:

- „Éhes vagyok.”
- „Játsszunk egyet!”
- „Takaríts ki nálam!”
- „Beteg vagyok.”
- „Most alszom.”
- „Tele a pocakom.”
- „Már jobban vagyok.”

Az első használat legfeljebb három rövid képernyőből álljon: profilválasztás, névadás, a gondozás bemutatása. Egy rövid mondat tisztázza a következményt:

**„A kisállatod akkor is él tovább, amikor bezárod a játékot. Ha sokáig nem gondozod, meghalhat.”**

Legyen külön beállítás a hanghoz, rezgéshez, értesítésekhez és készülékházszínhez. A napirend és az adattörlés külön szülői részbe kerüljön, véletlen megnyitás ellen például hosszú gombnyomással. A hétköznapi gondozás ne kérjen PIN-kódot.

Ne legyen reklám, vásárlás, előfizetés, regisztráció, chat, ranglista, közösségi megosztás vagy analitika. A gyermekek nevét és játékadatait helyben tárold. Csak a tényleges működéshez szükséges engedélyeket deklaráld.

## 16. A kód felépítése

A megoldás maradjon áttekinthető. Válaszd szét a következő felelősségeket:

- A tiszta Kotlin állapotmodellt és szimulációs motort.
- A központi gondozási és fejlődési szabályokat.
- Az időforrás kezelését.
- A perzisztens adattárolást.
- Az alkalmazás állapotát kezelő ViewModelt.
- A Compose felületet és az animációkat.
- Az értesítési ütemezést.

Az állat egészsége ne a Composable újrarajzolásainak vagy egy animáció képkockáinak számától függjön. A szimulátor kapjon tesztelhető órát és determinisztikus véletlenforrást. A szabálykészlet-változásokat kezeld úgy, hogy egy frissítés ne alkalmazzon új sebességeket dokumentálatlanul a teljes korábbi távollétre.

## 17. Kötelező ellenőrzések

A tesztek elsősorban a valódi hibakockázatokat fedjék le: időkezelés, végleges halál, mentés, profilok elkülönítése és a régi Android-verzió.

Legalább ezeket ellenőrizd:

1. A gondozási táblázat minden művelete pontosan a leírt állapotváltozást okozza.
2. Az előtérben és bezárt alkalmazással eltelt azonos idő azonos eredményt ad.
3. A jól ellátott állat 8 óra távollét után életben van; 48 óra teljes elhanyagolás után halott.
4. A megadott tiszta kezdőállapotban 19 óra 36 perc ébrenlétnél következik be az elhanyagolás miatti halál.
5. Egyhetes távollétnél a múltbeli halál időpontja jelenik meg, és a szimuláció gyorsan végez.
6. Az alvás megőrzi a betegséget és a szükségleteket, de a meghatározott éjszakai fogyások szünetelnek.
7. A már korábban bekövetkezett halál után a megnyitáskori etetés és gyógyszer hatástalan.
8. Profilváltáskor a másik állat ideje is halad, de az állapotuk és emlékalbumuk nem keveredik.
9. A folyamatkilövés, force-stop, újraindítás és képernyőforgatás nem törli a mentést.
10. Egy időszak több részre bontott feldolgozása ugyanazt adja, mint az egyszeri feldolgozás; nincs dupla fogyás vagy dupla jutalom.
11. Az óra visszaállítása és az időzóna változása nem éleszt fel és nem dolgoz fel kétszer időt.
12. A felület és a háttérmunka egyidejű indulása nem okoz elvesző gondozást vagy kétszer elszámolt eseményt.
13. Az új állatot nem módosíthatja a régi állathoz tartozó munka.
14. Az értesítési engedély elutasítása nem töri el a játékot.
15. A természetes halál és az elhanyagolás miatti halál külön oka helyesen jelenik meg.

Készíts Android-kompatibilitási tesztmátrixot. Az API 24 és 37 legyen kiemelt ellenőrzési pont; az összes köztes támogatott főverzióra tervezz telepítési és indítási ellenőrzést. Ahol eszköz vagy emulátor rendelkezésre áll, futtasd is le. Jelöld külön, mi lett ténylegesen kipróbálva és mi maradt ellenőrizetlen.

Vizsgáld meg a felületet kis telefonon, 7–8 hüvelykes és 10–13 hüvelykes tableten, álló és fekvő helyzetben, nagyított betűvel is. Az állapotváltásokat gyorsított tesztórával ellenőrizd; a kiadott játékban normál valós idő működjön.

## 18. Átadás és munkamenet

Először röviden foglald össze a választott megvalósítást, a fejlődési táblát és a verziókat, majd készítsd el a projektet. A rutinszerű részletkérdésekben hozz a fenti célokkal összhangban álló döntést.

A fejlesztést működő lépésekben végezd: állapotmotor és mentés, gondozás és halál, felület és animációk, értesítések, majd eszközös ellenőrzés. A végén az összes kötelező funkció legyen összekapcsolva.

Add át:

1. A teljes Android Studio-projektet, Gradle Wrapperrel és minden szükséges erőforrással.
2. A forráskódot egyben, például ZIP-fájlként.
3. Ténylegesen lefordított, telefonra és tabletre telepíthető APK-t. Jelöld, ha debug-aláírású.
4. Release-build beállítást és az aláírás lépéseit; privát kulcsot ne építs a forráskódba.
5. Magyar README-t Windows 10/11 alatti build- és telepítési útmutatóval, a pontos JDK- és SDK-verziókkal.
6. A gondozási paraméterek, fejlődési ágak és halálszabályok rövid leírását.
7. Az elvégzett tesztek eredményét és a fennmaradt, konkrét korlátokat.

Futtasd a buildet, a releváns teszteket és a lintet, majd javítsd a feltárt hibákat. Ne állítsd, hogy az APK elkészült vagy egy Android-verzió tesztelve lett, ha ez nem történt meg. Ha a környezet valamely ellenőrzést nem tesz lehetővé, add át az összes elkészült fájlt, és pontosan nevezd meg a hiányzó lépést.

**Az átadás akkor tekinthető késznek, ha Lili vagy Léna kiválaszthatja a profilját, elnevezheti és kikeltheti a tojását, önállóan elláthatja a kisállatot, az állat több napon keresztül fejlődik, és tartós elhanyagolás esetén ténylegesen, véglegesen meghal.**

## Hivatalos referenciaforrások a megvalósításhoz

- [Android-verziók és API-szintek](https://source.android.com/docs/setup/reference/build-numbers)
- [Android 17 SDK](https://developer.android.com/about/versions/17/setup-sdk)
- [Android 17 képernyőméret- és orientációs szabályok, a játékokra vonatkozó kivételekkel](https://developer.android.com/about/versions/17/changes/ff-restrictions-ignored)
- [WorkManager periodikus munka](https://developer.android.com/reference/androidx/work/PeriodicWorkRequest)
- [Értesítési engedély](https://developer.android.com/develop/ui/compose/notifications/notification-permission)
- [Monoton időforrás](https://developer.android.com/reference/android/os/SystemClock)
- [Java API-kompatibilitás és desugaring](https://developer.android.com/studio/write/java8-support)
- [Force-stop viselkedés](https://developer.android.com/about/versions/15/behavior-changes-all#stopped-state)
- [Bandai Original Tamagotchi, első generáció és használati útmutatók](https://www.bandai.com/original-tamagotchi-white-with-black)
