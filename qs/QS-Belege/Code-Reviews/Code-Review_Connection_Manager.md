# Code-Review vom 3. August 2015

**Durchgeführt von:**

**Geprüfte Codeteile:** Connection Manager 

### 1. Is the functionality correct?

Ja.

### 2. Are the classes, functions and variables named suitably?

Ja.

### 3. Is the class structure well designed and meeting all requirements or does it need improvements?

Ja sie ist sinnvoll erstellt und es sind keine Verbesserungen bekannt. 

### 4. Are there classes which have become obsolete because of a new implementation?

Nein.

### 5. Are there unnecessary overloaded functions or constructors?

Die Methode storeAndDisplayMessage kann in eine storeMessage und in ein displayMessage aufgeteilt werden. 

### 6. Is the perstistent database structure well designed?

Ja.

### 7. Does it contain functions that can be reused later? Are they placed in a Helper class?

Die Klasse ist dafür zuständig Nachrichten zu versenden und diese weiterzuleiten. Immer wenn dies geschieht erledigt das der ConnectionManager. Insofern gitb es keine Funktionen die später wiederverwendet werden können. 

### 8. Are existing helper functions being used? Is no duplicate functionality implemented?

Es gibt keine bereits existierenden Helper-Methoden.

### 9. Are all the input arguments being validated?

Es gibt keine Eingabeparameter im klassischen Sinne. Besitzt ein Handy keine Wifi oder Bluetooth funktionalität, dann unterliegt es den Protokolen dieser Klassen dies zu überprüfen. Werden Nachrichten an benachbarte Peers verschickt so ist es nicht möglich diese zu validieren, da diese sich außer Reichweite bewegt haben können.

### 10. How will the functionalities affect the performance of the app - power usage, time and memory?

Der Connection Manager startet die Protokolle Wifi, Bluetooth und GCM. Diese überprüfen häufig welche Geräte sich in der unmittelbaren Nähe befinden und verbrauchen damit Strom. Da dies jedoch eine wichtige Funktionalität ist für die Funktionalität der App, kann dies nicht vermieden werden. Will ein User nur eins der Protokole benutzen so kann er dies in den Einstellungen auswählen. 

### 11. Is it absolutely necessary to run it on UI thread or would a background thread suffice?

Ja. Der Connection Manager ist für das Versenden von Messages zuständig. Dies wird im UI thread eingeleitet. 

### 12. Are all possible failures handled?

Es gibt noch eine Methode in der mit TODO gekennzeichnet ist, ob hier nicht ein Fehler geworfen werden soll. Um alle anderen Fehler wird sich gekümmert.

### 13. Does it degrade gracefully in case of unknown failures?

Bei dem Handlen mit der Datenbank kann ein try-catch Block eingefügt werden.

### 14. Are the best practices for app development, according to Android lint, being followed?

Nein, folgende Fehler wurden gefunden und werden behoben:

- Fehler wurden nicht userfreundlich dargestellt
- Es befinden sich TODO Blöcke im Code, die abgearbeitet werden müssen.
- Die CanSend funktionaliät returned immer true und ist noch nicht implementiert -> BluetoothProtokol & WifiProtokol
- Die sendMessage Methode ruft nur die sendEnvelope Methode auf. Überbleibsel von altem Code.
- Ungenutzte Imports entfernen.

### 15. What pieces of the component can be executed in parallel?

Die Paralellisierung findet in den aufgerufenen Klassen statt und ist somit bereits vorhanden. 

### 16. Are the operations thread-safe if they need to be?

Müssen sie nicht.

### 17. Is the layout suitable for all screen dimensions?

Es gibt kein Layout.
