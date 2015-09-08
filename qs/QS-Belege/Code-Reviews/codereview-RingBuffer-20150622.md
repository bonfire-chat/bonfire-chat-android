# Code-Review vom 22.06.2015

**Durchgeführt von:** Jens Heuschkel, Max Weller 

**Geprüfte Codeteile:** RingBuffer

### 1. Is the functionality correct?

Die contains-implementierung funktioniert nicht immer korrekt. TODO: Warum?

### 2. Are the classes, functions and variables named suitably?

Ja.

### 3. Is the class structure well designed and meeting all requirements or does it need improvements?

Die Struktur ist gut.

### 4. Are there classes which have become obsolete because of a new implementation?

Nein.

### 5. Are there unnecessary overloaded functions or constructors?

Nein.

### 6. Is the perstistent database structure well designed?

Der Codeteil besitzt keine Datenbankstruktur.

### 7. Does it contain functions that can be reused later? Are they placed in a Helper class?

Es sind keine wiederverwendbaren Funktionen vorhanden.

### 8. Are existing helper functions being used? Is no duplicate functionality implemented?

Es wurde keine doppelte Funktionalität implementiert.

### 9. Are all the input arguments being validated?

Das ist für diesen Codeteil nicht sinnvoll/relevant.

### 10. How will the functionalities affect the performance of the app - power usage, time and memory?

Da dies davon abhängig ist, wie der RingBuffer verwendet wird, kann keine Aussage darüber getroffen werden. Bei normaler Nutzung sollte es aber keine negativen Auswirkungen geben.

### 11. Is it absolutely necessary to run it on UI thread or would a background thread suffice?

Nicht relevant.

### 12. Are all possible failures handled?

Ja.

### 13. Does it degrade gracefully in case of unknown failures?

Nein, aber für diesen Codeteil sind unbekannte Fehler fast ausgeschlossen. Daher ist es nicht sinnvoll, dies zu implementieren.

### 14. Are the best practices for app development, according to Android lint, being followed?

Ja.

### 15. What pieces of the component can be executed in parallel?

Keine.

### 16. Are the operations thread-safe if they need to be?

Müssen sie nicht.

### 17. Is the layout suitable for all screen dimensions?

Diese Funktionalität hat kein Layout.

### 18. Do all methods and fields have the proper access modifiers?

- die Attribute content, length und insertPosition sollten alle Private sein, sind sie aber nicht
- content und length sollten beide final sein, sind sie aber nicht

Diese Fehler wurden korrigiert.
