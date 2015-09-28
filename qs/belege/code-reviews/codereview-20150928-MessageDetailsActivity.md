## Code-Review vom 28.09.2015

**Durchgeführt von:** Johannes Lauinger, Jens Heuschkel

**Geprüfte Codeteile:** MessageDetailsActivity

**Änderungen vorgenommen in Commit:** Es wurden keine Änderungen vorgenommen

#### 1. Ist die Funktionalität korrekt?

Ja.

#### 2. Sind die Klassen, Funktionen und Variablen angemessen benannt?

Ja.

#### 3. Wurde die Klassenstruktur gut entworfen und erfüllt sie alle Anforderungen, oder sind Verbesserungen nötig?

Die Klassenstruktur erfüllt alle Anforderungen.

#### 4. Gibt es Klassen, die aufgrund neuer Implementierungen überflüssig geworden sind?

Nein.

#### 5. Gibt es unnötig überladene Funktionen oder Konstruktoren?

Nein.

#### 6. Wurde die Datenbankstruktur gut entworfen?

Für diesen Codeteil nicht relevant.

#### 7. Enthält das Codeteil Funktionen, die wiederverwendet werden können? Sind diese in einer Helper-Klasse untergebracht?

Es gibt keine Funktionen, die wiederverwendet werden können.

#### 8. Wurden existierende Helper-Funktionen benutzt? Ist keine doppelte Funktionalität implementiert?

Es wurde keine doppelte Funktionalität implementiert.

#### 9. Werden alle Eingabeparameter validiert?

Die Klasse hat keine Eingabeparameter.

#### 10. Wie beeinflusst die Funktionalität die Performance der App - Stromverbrauch, Rechenzeit und Speicherbedarf?

Die Funktionalität beeinflusst die Performance nicht wesentlich, da nur einfache Standardfunktionalitäten implementiert sind, die nicht besonders Rechenaufwändig sind.

#### 11. Ist es unbedingt nötig, den Code in einem UI-Thread laufen zu lassen oder würde ein Background-Thread ausreichen?

Ja, da es sich um eine UI-Klasse handelt.

#### 12. Werden alle möglichen Fehlschläge behandelt?

Ja, werden sie.

#### 13. Findet eine "Graceful Degradation" statt?

Ja.

#### 14. Werden die Best Practices zur Appentwicklung, laut Android Lint, befolgt?

Ja, werden sie.

#### 15. Welche Teile können parallel ausgeführt werden?

Es gibt keine Teile, die parallel ausgeführt werden können.

#### 16. Sind die Operationen, bei denen Threadsicherheit benötigt wird, threadsicher?

Es gibt keine Operationen, bei denen Threadsicherheit benötigt wird.

#### 17. Ist das Layout passend für alle Bildschirmdimensionen?

Das Layout ist passend für alle Bildchirmdimensionen.

#### 18. Haben alle Methoden und Felder die richtigen Zugriffsmodifier?

Ja, haben sie.
