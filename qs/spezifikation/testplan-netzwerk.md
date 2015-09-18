Testplan für manuelles Testen der drahtlosen Übertragung sowie des Routing-Algorithmus

I) Definitionen
==========================

Allgemeine Ausgangskonfiguration der Knoten:
------------------------------------------------------

Ein Knoten ist, soweit nicht näher beschrieben, ein Android-Gerät, auf dem die aktuellste Version der App eingerichtet und die initiale Einrichtung (Eingabe eines Nicknames, dadurch Registrierung des Gerätes mit Nickname, GCM-ID und PublicKey am Server) abgeschlossen ist.


II) Testfälle für die allgemeine Übertragung
==========================

1. Test der direkten Übertragung via Flooding / Bluetooth
------------------------------------------------------------

| Benutzerinteraktion | erwartetes Verhalten der App |
| ------------------- | ---------------------------- |
| Auf zwei Knoten A und B, die sich in direkter Bluetooth-Reichweite befinden, wird zunächst die Ausgangskonfiguration hergestellt, anschließend werden in den Einstellungen alle Übertragungsverfahren außer Bluetooth deaktiviert. Die Kontaktdaten von A werden an B gesendet. Auf B wird eine neue Unterhaltung mit A gestartet. Auf B wird eine Nachricht an A eingegeben und abgesendet. | B sendet die Nachricht an alle per Bluetooth sichtbaren Geräte, insbesondere an A. Auf A wird die Nachricht empfangen und als per Bluetooth empfangen dargestellt. A sendet ein ACK an B, dieses wird auf B durch ein Häkchen an der Nachricht sichtbar. Weiterhin erscheint ein Bluetooth-Icon an der Nachricht, da die Nachricht per Bluetooth zugestellt wurde. Im Dashboard ist erkennbar, dass die Nachricht mit Routingmodus Flooding (0x01) versendet wurde, das ACK mit Routingmodus DSR (0x02). |

2. Test der direkten Übertragung via DSR / Bluetooth
-------------------------------------------------------

| Benutzerinteraktion | erwartetes Verhalten der App |
| ------------------- | ---------------------------- |
| Nach der erfolgreichen Durchführung von Test 1 wird eine weitere Nachricht von B an A gesendet, sowie eine Nachricht von A an B. | Nach Test 1 ist den Geräten A und B der schnellste Pfad zum jeweils anderen Gerät bekannt. Daher sendet B die Nachricht nicht per Routingmodus Flooding (0x01), sondern per Dynamic Source Routing (0x02), also unter der Angabe der gewünschten Übertragungspfades. Daher wird die Nachricht nur an Gerät A gesendet. Darüber hinaus identisch zu Test 1. |

3. Test der direkten Übertragung via Server / Google Cloud Messaging
-----------------------------------------------------------------------------

| Benutzerinteraktion | erwartetes Verhalten der App |
| ------------------- | ---------------------------- |
| Auf zwei Knoten A und B, die eine funktionierende Internetverbindung haben, wird zunächst die Ausgangskonfiguration hergestellt, anschließend werden in den Einstellungen alle Übertragungsverfahren außer "mobile Daten / Wifi" (Übertragung per Cloud) deaktiviert. Die Kontaktdaten von A werden an B gesendet. Auf B wird eine neue Unterhaltung mit A gestartet. Auf B wird eine Nachricht an A eingegeben und abgesendet. | B sendet die Nachricht per HTTP an den Server, welcher sie per GCM an Gerät A weiterleitet. Auf A wird die Nachricht empfangen und als per Cloud empfangen dargestellt. A sendet ein ACK an B, dieses wird auf B durch ein Häkchen an der Nachricht sichtbar. Weiterhin erscheint ein Cloud-Icon an der Nachricht, da die Nachricht per Cloud zugestellt wurde. Im Dashboard ist erkennbar, dass die Nachricht mit Routingmodus Flooding (0x01) versendet wurde, das ACK mit Routingmodus DSR (0x02). |

III) Testfälle für Wegfindung per Flooding und einfaches Routing
==============================================================================

In diesen Tests werden folgende Teile des Routing getestet:
* Finden des schnellsten Pfades: Flooding an alle Knoten sowie ACK auf dem Pfad, auf dem die Nachricht den Empfänger zuerst erreicht (= schnellster Pfad)
* Speichern des schnellsten Pfades zu einem Empfänger
* Verwenden des gespeicherten schnellsten Pfades für künftige Nachrichten

1. Bluetooth-Wegfindung - Flooding mit drei Knoten
------------------------------------------------------

| Benutzerinteraktion | erwartetes Verhalten der App |
| ------------------- | ---------------------------- |
| Auf drei Knoten A, B und C werden alle Übertragungsverfahren außer Bluetooth deaktiviert. Die Kontaktdetails von C werden an A gesendet. Die Knoten werden räumlich so angeordnet, dass eine Bluetoothverbindung zwischen A und B sowie zwischen B und C möglich ist, nicht jedoch zwischen A und C. Auf A wird eine Unterhaltung mit C begonnen und eine Nachricht an C gesendet. | Nachricht u. ACK kommen an, etc. |

2. Bluetooth-Wegfindung zum nächsten Knoten mit Internetverbindung
------------------------------------------------------

| Benutzerinteraktion | erwartetes Verhalten der App |
| ------------------- | ---------------------------- |
| Knoten A, B und C werden wie in Testfall III.1 vorbereitet. Auf Knoten C wird zusätzlich die Übertragung per Cloud aktiviert. Auf einem weiteren Knoten D wird nur die Übertragung per Cloud aktiviert. Es wird sichergestellt, dass C und D mit dem Internet verbunden sind. Die Kontaktdetails von D werden an A gesendet. A beginnt eine Unterhaltung mit D und sendet die Nachricht "alpha" an D. Nach Erhalt der Nachricht "alpha" sendet A eine weitere Nachricht "beta". | Die Nachricht "alpha" kommt per Flooding über B, C und Server bei D an, ACK "für alpha" geht auf direktem Pfad (D-Server-C-B-A) per DSR zurück an A. Die Nachricht "beta" wird auf direktem Pfad (A-B-C-Server-D) per DSR  an D gesendet, ACK "für beta" wie ACK "für alpha". Die empfangenen und weitergeleiteten Nachrichten und ihre Pfade sind entsprechend im Dashboard ersichtlich. |

3.
----------------------------



IV) Testfälle für sich verändernde Routen
=========================================

In diesen Tests wird überprüft, ob der Routingalgorithmus bei ausfallenden Pfaden korrekt reagiert.

1. "Abreißende" Bluetooth-Verbindung
----------------------------------

| Benutzerinteraktion | erwartetes Verhalten der App |
| ------------------- | ---------------------------- |
| Auf drei Knoten A wird nur Bluetooth aktiviert, auf B wird GCM und Bluetooth aktiviert. Von A wird eine Nachricht "alpha" an B gesendet. Nachdem diese zugestellt und acknowledged wurde, wird eine weitere Nachricht "beta" von A and B gesendet. Danach wird A räumlich so weit vom B entfernt, dass keine Bluetooth-Übertragung mehr möglich ist. Weiterhin wird auf A die Übertragung per GCM aktiviert. Anschließend wird eine weitere Nachricht "gamma" von A and B gesendet. | Die Nachricht "alpha" wird per Flooding gesendet, B sendet per DSR ein ACK "für alpha" zurück. Danach hat A den Pfad zu B gespeichert und die Nachricht "beta" wird direkt per DSR an B gesendet. Die Nachricht "gamma" wird auch versucht per DSR direkt via Bluetooth an B zu senden. Da dies fehlschlägt, wird beim Retry nach 20 Sekunden versucht, "gamma" per Flooding zuzustellen. Dies erfolgt dann über GCM. Das ACK "für gamma" von B an A erfolgt dann wiederum per DSR. |



V) Testfälle für Störeinflüsse im Netzwerk
==========================================

Hier soll überprüft werden, wie sich die App bei äußeren Störeinflüssen auf Netzwerkebene, also zum Beispiel während einer während der Datenübertragung abreißenden Verbindung, verhält. Es soll sichergestellt sein, dass stets angemessen reagiert wird und sowohl die App nicht abstürzt als auch der Benutzer sinnvolle Fehlermeldungen erhält.

Da dies teilweise sporadische Fehler sind, welche sich schwer reproduzieren lassen, ist die Aussagekraft der folgenden Tests leider nur bedingt gegeben.

1. "Socket wird unerwartet während der Verbindung geschlossen"
--------------------------------------------------------------

| Benutzerinteraktion | erwartetes Verhalten der App |
| ------------------- | ---------------------------- |


2. Benutzer schaltet Bluetooth im Telefon aus
-------------------------------

| Benutzerinteraktion | erwartetes Verhalten der App |
| ------------------- | ---------------------------- |

