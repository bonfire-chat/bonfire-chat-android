
## Code-Review vom 14.09.2015

**Durchgeführt von:** Johannes Lauinger, Jens Heuschkel 

**Geprüfte Codeteile:** SendLocationBroadcastsService

**Änderungen vorgenommen in Commit:** Im Zuge dieses Code-Reviews wurden keine Änderungen vorgenommen.

#### 1. Ist die Funktionalität korrekt?

Ja

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

Keine Funktionen können wiederverwendet werden.

#### 8. Wurden existierende Helper-Funktionen benutzt? Ist keine doppelte Funktionalität implementiert?

Es wurde keine doppelte Funktionalität implementiert.

#### 9. Werden alle Eingabeparameter validiert?

Es gibt keine Eingabeparameter.

#### 10. Wie beeinflusst die Funktionalität die Performance der App - Stromverbrauch, Rechenzeit und Speicherbedarf?

Die Funktionalität fragt in regelmäßigen Abständen den Standort des Geräts ab und erhöht dadurch dem Stromverbrauch. Dies lässt sich aber nicht vermeiden.

#### 11. Ist es unbedingt nötig, den Code in einem UI-Thread laufen zu lassen oder würde ein Background-Thread ausreichen?

Dies ist für den Codeteil nicht relevant, da er bereits in einem Background-Thread läuft.

#### 12. Werden alle möglichen Fehlschläge behandelt?

Ja, insbesondere der Fall, dass der Standort nicht ermittelt werden kann, wird behandelt.

#### 13. Findet eine "Graceful Degradation" statt?

Ja.

#### 14. Werden die Best Practices zur Appentwicklung, laut Android Lint, befolgt?

Ja, werden sie.

#### 15. Welche Teile können parallel ausgeführt werden?

Die Klasse besteht im Wesentlichen nur aus einem Teil. Dieser Punkt ist daher nicht relevant.

#### 16. Sind die Operationen, bei denen Threadsicherheit benötigt wird, threadsicher?

Ja.

#### 17. Ist das Layout passend für alle Bildschirmdimensionen?

Da es sich nicht um eine UI-Funktionalität handelt, gibt es auch kein Layout.

#### 18. Haben alle Methoden und Felder die richtigen Zugriffsmodifier?

Ja, haben sie.
