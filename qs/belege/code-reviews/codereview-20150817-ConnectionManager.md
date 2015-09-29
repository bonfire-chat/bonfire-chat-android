
## Code-Review vom 17. August 2015

**Durchgeführt von:** Simon Thiem, Alexander Frömmgen

**Geprüfte Codeteile:** ConnectionManager

**Änderungen vorgenommen in Commit:** d449431173173391974e8fdc320a9e6912dff7e7

#### 1. Ist die Funktionalität korrekt?

Ja.

#### 2. Sind die Klassen, Funktionen und Variablen angemessen benannt?

Ja.

#### 3. Wurde die Klassenstruktur gut entworfen und erfüllt sie alle Anforderungen, oder sind Verbesserungen nötig?

Ja, sie ist sinnvoll erstellt und es sind keine Verbesserungen nötig.

#### 4. Gibt es Klassen, die aufgrund neuer Implementierungen überflüssig geworden sind?

Nein.

#### 5. Gibt es unnötig überladene Funktionen oder Konstruktoren?

Nein.

#### 6. Wurde die Datenbankstruktur gut entworfen?

Ja.

#### 7. Enthält das Codeteil Funktionen, die wiederverwendet werden können? Sind diese in einer Helper-Klasse untergebracht?

Die Klasse ist dafür zuständig, Nachrichten zu versenden und diese weiterzuleiten. Immer wenn dies geschieht, erledigt das der ConnectionManager. Insofern gibt es keine Funktionen die später wiederverwendet werden können.

#### 8. Wurden existierende Helper-Funktionen benutzt? Ist keine doppelte Funktionalität implementiert?

Es gibt keine bereits existierenden Helper-Methoden.

#### 9. Werden alle Eingabeparameter validiert?

Es gibt keine Eingabeparameter im klassischen Sinne. Besitzt ein Handy keine Wifi- oder Bluetooth-Funktionalität, dann unterliegt es den Klassen dieser Protokolle dies zu überprüfen. Werden Nachrichten an benachbarte Peers verschickt, so ist es nicht möglich diese zu validieren, da diese sich außer Reichweite bewegt haben können.

#### 10. Wie beeinflusst die Funktionalität die Performance der App - Stromverbrauch, Rechenzeit und Speicherbedarf?

Der ConnectionManager startet die Protokolle Wifi, Bluetooth und GCM. Die Protokolle Wifi und Bluetooth überprüfen häufig, welche Geräte sich in der unmittelbaren Nähe befinden und verbrauchen damit Strom. Da dies jedoch eine wichtige Funktionalität ist für die Funktionalität der App, kann dies nicht vermieden werden. Will ein User nur eines der Protokole benutzen so kann er dies in den Einstellungen auswählen.

#### 11. Ist es unbedingt nötig, den Code in einem UI-Thread laufen zu lassen oder würde ein Background-Thread ausreichen?

Der Großteil des Codes im ConnectionManager läuft bereits in einem Hintergrundthread. Nur einige statische Methoden dienen als Helper für UI-Klassen, diese müssen daher im UI-Thread laufen.

#### 12. Werden alle möglichen Fehlschläge behandelt?

In der onPacketRecieved-Methode wird das Eintreffen eines Pakets unbekannten Typs nicht behandelt. Johannes Lauinger wird beauftragt, dies zu beheben.

**Update 20.08.15:** Dies wurde von Johannes Lauinger behoben. Es wird in diesem Fall nun eine Exception geworfen.

#### 13. Findet eine "Graceful Degradation" statt?

Bei Operationen, welche Zugriff auf die Datenbank beinhalten, kann ein try-catch Block eingefügt werden.

#### 14. Werden die Best Practices zur Appentwicklung, laut Android Lint, befolgt?

Ja, werden sie.

#### 15. Welche Teile können parallel ausgeführt werden?

Die Parallelisierung findet in den aufgerufenen Klassen statt und ist somit bereits vorhanden.

#### 16. Sind die Operationen, bei denen Threadsicherheit benötigt wird, threadsicher?

Müssen sie nicht, da ConnectionManager als Singleton realisiert ist.

#### 17. Ist das Layout passend für alle Bildschirmdimensionen?

Da es sich nicht um eine UI-Funktionalität handelt, gibt es auch kein Layout.

#### 18. Haben alle Methoden und Felder die richtigen Zugriffsmodifier?

Ja, haben sie.
