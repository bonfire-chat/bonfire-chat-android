# Code-Review vom 3. August 2015

**Durchgeführt von:**

**Geprüfte Codeteile:** Die Datenbankfunktionalität (Im Wesentlichen die Klasse BonfireData)

**Änderungen vorgenommen in Commit:** 5a26429def4f8278980232cac29e2cfd2d6afe32

### 1. Is the functionality correct?

Ja, abgesehen von der onUpgrade-Methode, die aber gewollt so implementiert ist, um die Entwicklung zu vereinfachen. Sie wird richtig implementiert werden, wenn die App veröffentlicht wird.

### 2. Are the classes, functions and variables named suitably?

Ja.

### 3. Is the class structure well designed and meeting all requirements or does it need improvements?

Keine Verbesserungen nötig.

### 4. Are there classes which have become obsolete because of a new implementation?

Nein.

### 5. Are there unnecessary overloaded functions or constructors?

Nein.

### 6. Is the perstistent database structure well designed?

Ja.

### 7. Does it contain functions that can be reused later? Are they placed in a Helper class?

Da es nur Methoden gibt, die Dinge in die Datenbank schreiben oder aus ihr lesen, kann keine Funktion wiederverwendet werden.

### 8. Are existing helper functions being used? Is no duplicate functionality implemented?

Es wurde keine doppelte Funktionalität implementiert.

### 9. Are all the input arguments being validated?

Die Eingabeparameter werden ausreichen überprüft.

### 10. How will the functionalities affect the performance of the app - power usage, time and memory?

Sehr einfache Funktionalität, fast keine Auswirkungen.

### 11. Is it absolutely necessary to run it on UI thread or would a background thread suffice?

Nicht relevant für diese Funktionalität.

### 12. Are all possible failures handled?

Ja.

### 13. Does it degrade gracefully in case of unknown failures?

Ja.

### 14. Are the best practices for app development, according to Android lint, being followed?

Nein, folgende Fehler wurden gefunden und behoben:

- Methode deleteContact gab immer true zurück. Gibt nun bei Fehlschlag false zurück.
- Das Feld helper wurde nicht benutzt und deshalb entfernt
- Die Methode getContactByXmppId wurde nicht benutzt und deshalb entfernt
- Der import von org.abstractj.kalium.keys.PublicKey wurde nicht benötigt und deshalb entfernt
- Diverse ungenutzte Variablen wurden entfernt

### 15. What pieces of the component can be executed in parallel?

Keine

### 16. Are the operations thread-safe if they need to be?

Müssen sie nicht.

### 17. Is the layout suitable for all screen dimensions?

Es gibt kein Layout.

### 18. Do all methods and fields have the proper access modifiers?

- Alle Methoden müssen public sein, da sie von anderen Klassen aus verwendet werden.
