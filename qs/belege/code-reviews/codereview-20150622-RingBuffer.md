## Code-Review vom 22. Juni 2015

**Durchgeführt von:** Jens Heuschkel, Max Weller

**Geprüfte Codeteile:** RingBuffer

**Änderungen vorgenommen in Commit:** ed7607da504e27a40cf2c99841c539be1d60c29a

#### 1. Ist die Funktionalität korrekt?

Die contains-implementierung funktioniert nicht immer korrekt. TODO: Warum?

#### 2. Sind die Klassen, Funktionen und Variablen angemessen benannt?

Ja.

#### 3. Wurde die Klassenstruktur gut entworfen und erfüllt sie alle Anforderungen, oder sind Verbesserungen nötig?

Die Struktur ist gut.

#### 4. Gibt es Klassen, die aufgrund neuer Implementierungen überflüssig geworden sind?

Nein.

#### 5. Gibt es unnötig überladene Funktionen oder Konstruktoren?

Nein.

#### 6. Wurde die Datenbankstruktur gut entworfen?

Der Codeteil besitzt keine Datenbankstruktur.

#### 7. Enthält das Codeteil Funktionen, die wiederverwendet werden können? Sind diese in einer Helper-Klasse untergebracht?

Es sind keine wiederverwendbaren Funktionen vorhanden.

#### 8. Wurden existierende Helper-Funktionen benutzt? Ist keine doppelte Funktionalität implementiert?

Es wurde keine doppelte Funktionalität implementiert.

#### 9. Werden alle Eingabeparameter validiert?

Das ist für diesen Codeteil nicht sinnvoll/relevant.

#### 10. Wie beeinflusst die Funktionalität die Performance der App - Stromverbrauch, Rechenzeit und Speicherbedarf?

Da dies davon abhängig ist, wie der RingBuffer verwendet wird, kann keine Aussage darüber getroffen werden. Bei normaler Nutzung sollte es aber keine negativen Auswirkungen geben.

#### 11. Ist es unbedingt nötig, den Code in einem UI-Thread laufen zu lassen oder würde ein Background-Thread ausreichen?

Nicht relevant.

#### 12. Werden alle möglichen Fehlschläge behandelt?

Ja.

#### 13. Findet eine "Graceful Degradation" statt?

Nein, aber für diesen Codeteil sind unbekannte Fehler fast ausgeschlossen. Daher ist es nicht sinnvoll, dies zu implementieren.

#### 14. Werden die Best Practices zur Appentwicklung, laut Android Lint, befolgt?

Ja.

#### 15. Welche Teile können parallel ausgeführt werden?

Keine.

#### 16. Sind die Operationen, bei denen Threadsicherheit benötigt wird, threadsicher?

Müssen sie nicht.

#### 17. Ist das Layout passend für alle Bildschirmdimensionen?

Diese Funktionalität hat kein Layout.

#### 18. Haben alle Methoden und Felder die richtigen Zugriffsmodifier?

- die Attribute content, length und insertPosition sollten alle Private sein, sind sie aber nicht
- content und length sollten beide final sein, sind sie aber nicht

Diese Fehler wurden korrigiert.
