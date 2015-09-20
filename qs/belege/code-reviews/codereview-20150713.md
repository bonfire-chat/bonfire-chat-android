
## Code-Review vom 13. Juli 2015

**Durchgeführt von:** Max Weller, Alexander Frömmgen, Jens Heuschkel

**Geprüfte Codeteile:** models-Package, ConnectionManager, EchoProtocol, GcmProtocol, TracerouteHandler

**Änderungen vorgenommen in Commit:** da815895bedcd619aaf928dc29c80a433cdc0ba4

#### 1. Ist die Funktionalität korrekt?

- Im ConnectionManager in der SENDMESSAGE\_ACTION wird eine RuntimeException nicht geworfen, sondern einfach einer Variable zugewiesen. Dies wurde geändert, sodass die Exception nun geworfen wird.

#### 2. Sind die Klassen, Funktionen und Variablen angemessen benannt?

Ja

#### 3. Wurde die Klassenstruktur gut entworfen und erfüllt sie alle Anforderungen, oder sind Verbesserungen nötig?

Einige Konstanten und einige Funkionen für die HTTP-API sind nicht an einer sinnvollen Stelle definiert, sonder immer in der Klasse, in der sie gerade benutzt wurden. Als Konsequenz wurde eine eigene Klasse für die API angelegt (BonfireAPI.java), in die diese Konstanten und Funktionen  verlagert wurden.

#### 4. Gibt es Klassen, die aufgrund neuer Implementierungen überflüssig geworden sind?

Nein

#### 5. Gibt es unnötig überladene Funktionen oder Konstruktoren?

Nein

#### 6. Wurde die Datenbankstruktur gut entworfen?

Ist nicht relevant für die geprüften Codeteile.

#### 7. Enthält das Codeteil Funktionen, die wiederverwendet werden können? Sind diese in einer Helper-Klasse untergebracht?

Nein

#### 8. Wurden existierende Helper-Funktionen benutzt? Ist keine doppelte Funktionalität implementiert?

Es wurde keine doppelte Funktionalität implementiert.

#### 9. Werden alle Eingabeparameter validiert?

Ja

#### 10. Wie beeinflusst die Funktionalität die Performance der App - Stromverbrauch, Rechenzeit und Speicherbedarf?

Allein aus dem Grund, dass viele Netzwerkressourcen angefordert werden, wird der Stromverbrauch negativ beeinflusst, dies liegt aber nicht an der Implementierung, sondern an der Natur der Sache.

#### 11. Ist es unbedingt nötig, den Code in einem UI-Thread laufen zu lassen oder würde ein Background-Thread ausreichen?

Nicht relevant

#### 12. Werden alle möglichen Fehlschläge behandelt?

Ja

#### 13. Findet eine "Graceful Degradation" statt?

Wird nicht betrachtet.

#### 14. Werden die Best Practices zur Appentwicklung, laut Android Lint, befolgt?

Ja

#### 15. Welche Teile können parallel ausgeführt werden?

Ist für diesen Codeteil nicht relevant.

#### 16. Sind die Operationen, bei denen Threadsicherheit benötigt wird, threadsicher?

Ist für diesen Codeteil nicht relevant.

#### 17. Ist das Layout passend für alle Bildschirmdimensionen?

Ist für diesen Codeteil nicht relevant.

#### 18. Haben alle Methoden und Felder die richtigen Zugriffsmodifier?

Sehr viele Variablen, die final sein sollten, sind nicht final deklariert. Dies wurde korrigert.
