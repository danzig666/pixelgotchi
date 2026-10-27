# Gondozás és életciklus

Ezek a család napirendjéhez választott saját szabályok, nem a Bandai eredeti programjának visszafejtett algoritmusai. Egyetlen központjuk: `engine/GameBalance`.

| Szabály | Érték |
|---|---|
| Keltetés | 60 valós másodperc |
| Kikelés | 4 étel, 4 jókedv, 100 életerő, tiszta és egészséges, nullázott részidőzítők |
| Éhség | −1 szív / 2 ébren töltött óra |
| Jókedv | −1 szív / 3 ébren töltött óra |
| Rendes étel | +1 ételszív (max. 4), +1 g; tele pocaknál visszautasítás |
| Nasi | +1 jókedv (max. 4), +2 g; nem ad ételszívet |
| Megnyert minijáték | +1 jókedv, −1 g, szakaszminimumig |
| Minimumtestsúly | baba: 5 g, gyermek: 10 g, kamasz: 15 g, felnőtt/idős: 20 g |
| Ürülék | 1 adag / 4 ébren töltött óra |
| Fertőzés | Legalább 2 adag 4 ébren töltött órán át folyamatosan bent marad |
| Éhezés | 0 ételszívnél −6 életerő / ébren töltött óra |
| Betegség | −4 életerő / ébren töltött óra |
| Gyógyszer | Megszünteti a betegséget; nem ad életerőt, nem takarít |
| Takarítás | Minden ürüléket és a koszkitettségi időt eltávolít; nem gyógyít |
| Felépülés | Legalább 2 étel, 1 jókedv, tisztaság, betegségmentesség: 1 stabil ébren töltött óra után +2 életerő/óra |
| Elhanyagolás miatti halál | Életerő 0, az adott károsodási szakaszon belül pontos időponttal |
| Alvás | 20:30–07:00 a mentett időzónában; éhség/jókedv/kosz/betegségkárosodás/felépülés szünetel |
| Bekapcsolt lámpa | Elalvás után 30 valós perc elteltével 1 gondozási hiba/éjszaka, egészségkár nélkül |

A fogyási ciklusok maradékát az etetés és a játék nem nullázza. Egészséges állatnak adott gyógyszer nem nullázza a koszkitettséget. Valós gyógyítás után bent maradt kosznál új négyórás fertőzési idő indul. A felépülés várakozása éjszaka szünetel és reggel folytatódik. A lámpa nem módosítja a napirendet.

Éhség (0), szomorúság (0), betegség és legalább két adag kosz külön folyamatos hiányhelyzetek. Mindegyik 30 ébren töltött perc után egyszer ad gondozási hibát, majd csak a probléma megoldása és új kialakulása után adhat újat. A hiányidőt és betegen töltött időt ébrenléti időben gyűjtjük. A nulla jókedv önmagában nem sebez.

Tiszta, frissen kikelt állat: a 8. ébren töltött óránál 0 ételszív; a 12. órában beteg és 76 életerő; utána −10/óra, tehát a halál 19 óra 36 perc ébrenlét után következik be. Alapnapirenddel ez 48 valós órán belül megtörténik. A játék a valós, múltbeli halálidőt menti, nem a megnyitás időpontját.

## Életkor

A kikeléstől eltelt valós idő számít, az éjszaka is. Baba: 0–1 óra; gyermek: 1–48 óra; kamasz: 48–120 óra; felnőtt: 120 órától; idős: 20 naptól; természetes halál: 30 nap. A szakaszok nem módosítják a fogyást. A fejlődési előzmények pontos eseményidővel mentődnek.

## Determinisztikus felnőtt fejlődési tábla

A táblázat **fentről lefelé az első illeszkedő sort** választja a 120 órás eseménynél. A felnőtt alak utána rögzített. A kedvezőtlenebb gondozásnál is szerethető, saját karaktert kapunk. Nincs véletlen minőségválasztás.

| Sorrend | Feltétel | Alak |
|---|---|---|
| 1 | Legalább 8 gondozási hiba VAGY legalább 24 ébren töltött óra hiányhelyzetben | Felhőcske |
| 2 | Legalább 5 hiba VAGY legalább 12 ébren töltött óra betegen | Rügyike |
| 3 | Legalább 12 nasi VAGY 35 g feletti testsúly | Gombócka |
| 4 | Legalább 4 tanítás ÉS 6 győzelem ÉS legfeljebb 1 hiba | Csillagfül |
| 5 | Legalább 5 győzelem ÉS legfeljebb 2 hiba | Ugrifüles |
| 6 | Legalább 3 tanítás ÉS legfeljebb 3 hiba | Cicabogyó |
| 7 | Minden más életben maradt állat | Gömböc |

Az indokolatlan figyelemkérések 6–9 ébren töltött órás közönként lehetségesek, tárolt LCG-seedből reprodukálhatók. Csak legalább 3 étel- és 3 jókedvszívvel, tisztán és egészségesen kér indokolatlan figyelmet. Egy kérés egy sikeres tanítást enged. A tanítás szelíd jelzés és tanultságnövelés; nem büntet és nem helyettesít ételt/gyógyszert. A megnyitások száma semmilyen pontot nem ad.

A minijáték irányai ugyanebből a mentett seedből, tranzakciónként keletkeznek. Öt forduló, legalább három találat. A menet, találatok, utolsó irány és jutalmazottság mentett adatok; forgatás/újraindítás nem ad új jutalmat. Megszakításért nincs jutalom.
