
## Code-Review vom 31. August 2015

**Durchgeführt von:** Jens Heuschkel, Jonas Mönnig

**Geprüfte Codeteile:** GpsTracker

**Änderungen vorgenommen in Commit:** Im Zuge dieses Code-Reviews wurden keine Änderungen vorgenommen.

#### 1. Ist die Funktionalität korrekt?

Ja.

#### 2. Sind die Klassen, Funktionen und Variablen angemessen benannt?

Ja.

#### 3. Wurde die Klassenstruktur gut entworfen und erfüllt sie alle Anforderungen, oder sind Verbesserungen nötig?

Ja.

#### 4. Gibt es Klassen, die aufgrund neuer Implementierungen überflüssig geworden sind?

Nein.

#### 5. Gibt es unnötig überladene Funktionen oder Konstruktoren?

Nein.

#### 6. Wurde die Datenbankstruktur gut entworfen?

Für dieses Codeteil gibt es keine Datenbankstruktur.

#### 7. Enthält das Codeteil Funktionen, die wiederverwendet werden können? Sind diese in einer Helper-Klasse untergebracht?

Die Klasse enthält keine wiederverwendbaren Funktionen.

#### 8. Wurden existierende Helper-Funktionen benutzt? Ist keine doppelte Funktionalität implementiert?

Es wurde keine doppelte Funktionalität implementiert.

#### 9. Werden alle Eingabeparameter validiert?

Es gibt keine Eingabeparameter.

#### 10. Wie beeinflusst die Funktionalität die Performance der App - Stromverbrauch, Rechenzeit und Speicherbedarf?

Da GPS mit der maximalen Genauigkeit verwendet wird, wird der Stromverbrauch stark erhöht. Die hohe Genauigkeit wird aber benötigt, darum wird sie beibehalten.

#### 11. Ist es unbedingt nötig, den Code in einem UI-Thread laufen zu lassen oder würde ein Background-Thread ausreichen?

Für diesen Codeteil nicht relevant.

#### 12. Werden alle möglichen Fehlschläge behandelt?

Ja.

#### 13. Findet eine "Graceful Degradation" statt?

Ja.

#### 14. Werden die Best Practices zur Appentwicklung, laut Android Lint, befolgt?

Ja.

#### 15. Welche Teile können parallel ausgeführt werden?

Für diesen Codeteil nicht relevant.

#### 16. Sind die Operationen, bei denen Threadsicherheit benötigt wird, threadsicher?

Müssen sie nicht.

#### 17. Ist das Layout passend für alle Bildschirmdimensionen?

Diese Funktionalität hat kein Layout.

#### 18. Haben alle Methoden und Felder die richtigen Zugriffsmodifier?

Ja.
