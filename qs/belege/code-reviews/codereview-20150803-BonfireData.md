
## Code-Review vom 3. August 2015

**Durchgeführt von:** Max Weller, Alexander Frömmgen

**Geprüfte Codeteile:** Die Datenbankfunktionalität (Im Wesentlichen die Klasse BonfireData)

**Änderungen vorgenommen in Commit:** 5a26429def4f8278980232cac29e2cfd2d6afe32

#### 1. Ist die Funktionalität korrekt?

Ja, abgesehen von der onUpgrade-Methode, die aber gewollt so implementiert ist, um die Entwicklung zu vereinfachen. Sie wird richtig implementiert werden, wenn die App veröffentlicht wird.

#### 2. Sind die Klassen, Funktionen und Variablen angemessen benannt?

Ja.

#### 3. Wurde die Klassenstruktur gut entworfen und erfüllt sie alle Anforderungen, oder sind Verbesserungen nötig?

Keine Verbesserungen nötig.

#### 4. Gibt es Klassen, die aufgrund neuer Implementierungen überflüssig geworden sind?

Nein.

#### 5. Gibt es unnötig überladene Funktionen oder Konstruktoren?

Nein.

#### 6. Wurde die Datenbankstruktur gut entworfen?

Ja.

#### 7. Enthält das Codeteil Funktionen, die wiederverwendet werden können? Sind diese in einer Helper-Klasse untergebracht?

Da es nur Methoden gibt, die Dinge in die Datenbank schreiben oder aus ihr lesen, kann keine Funktion wiederverwendet werden.

#### 8. Wurden existierende Helper-Funktionen benutzt? Ist keine doppelte Funktionalität implementiert?

Es wurde keine doppelte Funktionalität implementiert.

#### 9. Werden alle Eingabeparameter validiert?

Die Eingabeparameter werden ausreichend überprüft.

#### 10. Wie beeinflusst die Funktionalität die Performance der App - Stromverbrauch, Rechenzeit und Speicherbedarf?

Sehr einfache Funktionalität, fast keine Auswirkungen.

#### 11. Ist es unbedingt nötig, den Code in einem UI-Thread laufen zu lassen oder würde ein Background-Thread ausreichen?

Nicht relevant für diese Funktionalität.

#### 12. Werden alle möglichen Fehlschläge behandelt?

Ja.

#### 13. Findet eine "Graceful Degradation" statt?

Ja.

#### 14. Werden die Best Practices zur Appentwicklung, laut Android Lint, befolgt?

Ja.

#### 15. Welche Teile können parallel ausgeführt werden?

Keine

#### 16. Sind die Operationen, bei denen Threadsicherheit benötigt wird, threadsicher?

Es gibt keine Operationen, bei denen Threadsicherheit benötigt wird.

#### 17. Ist das Layout passend für alle Bildschirmdimensionen?

Da es sich nicht um eine UI-Funktionalität handelt, gibt es auch kein Layout.

#### 18. Haben alle Methoden und Felder die richtigen Zugriffsmodifier?

Alle Methoden müssen public sein, da sie von anderen Klassen verwendet werden.
