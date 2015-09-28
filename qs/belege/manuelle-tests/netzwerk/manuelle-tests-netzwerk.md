# Durchführung des manuellen UI-Testplans vom XXXXXXX

**Durchgeführt von: Matthias Hofmann**

**Änderungen vorgenommen in Commit: keine Änderungen nötig**

## Teil I: Allgemeine Informationen zu den Testergebnissen

### Teil I.1. Zeiten der Nachrichten im Dashboard
Die angegebenen Zeiten der Ereignisse im Dashboard wirken auf den ersten Blick unwahrscheinlich. 
Zum Beispeiel wird in Test II)1. ein Acknowledgement zuersst empfangen, bevor es gesendet wurde.
Dies liegt daran, dass die Übertragungn des Ackknowledgements per Bluetooth stattgefunden hat und das Gerät, 
dass diese Empfangen hat, eine schnellere Verbindung zum Dashboard server hatte. 
Somit wurde das Dashboard zuerst über das Ankommen des ACK informiert und dann erst über das Versenden.

### Teil I.2. Packet Attribute im Dashboard
In der sechsten Spalte im Dashboard werden Details der Übertragung dargestellt. 
Dabei ist die eindeutige ID das zweite Attributvom Packet.
"routing=1" steht dabei für senden via Flooding.
"routing=2" steht für senden via DRS-Adaption.


## Teil II: Testfälle für die allgemeine Übertragung

### Test II.1 Test der direkten Übertragung via Flooding / Bluetooth
Beschreibung des Ergebnisses in Test II)1.

### Test II.2

### Test II.3

## Teil III: Senden und Empfangen von Nachrichten

### Test III.1

### Test III.2

### Test III.3

### Test III.4

## Teil IV: Löschen von Elementen

### Test IV.1

### Test IV.2

### Test IV.3

## Teil V: Diverses

### Test V.1
