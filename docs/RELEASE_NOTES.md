Lili és Léna teljesen magyar, offline pixelállatjátéka Android 7.0–17 rendszerre.

Letöltés és telepítés:
- pixelgotchi-debug.apk: telepíthető, debug-aláírású alkalmazás telefonra és tabletre.
- pixelgotchi-source.zip: teljes Android Studio-projekt, Gradle Wrapperrel, saját grafikával/hanggal, magyar útmutatóval és tesztekkel.

Két külön profil, 60 másodperces keltetés, valós időben működő gondozás és fejlődés, hét felnőtt karakter, ötfordulós minijáték, végleges halál és emlékalbum. A játék bezárva is folytatódik. Nincs reklám, vásárlás vagy internetigény.

Fordítás: API 37, minSdk 24, JDK 17, AGP 9.4.0, Gradle 9.6.0. A saját release-aláírás lépései a README-ben szerepelnek; privát release-kulcs nincs a forrásban. A debug-aláírás személyes tesztelésre való.

A tényleges teszteredmények és a nem ellenőrzött eszközök a forrás docs/TESZTEK.md dokumentumában és a GitHub Actions futási riportjaiban találhatók. Az APK telepítéséhez engedélyezni kell az ismeretlen alkalmazások telepítését az APK-t megnyitó alkalmazásnál.

Ellenőrzött eredmény: 25 JVM-teszt, 0 lint-hiba, Android 7.0/API 24 és Android 17/API 37.0 emulátoron 6–6 sikeres eszközteszt. Sikeres GitHub-futás: https://github.com/danzig666/pixelgotchi/actions/runs/40513709119

A PNG-k valódi emulátoros képernyőképek. A köztes API-k és fizikai ARM-eszközök teljes kézi mátrixa még nincs végigpróbálva.
