# Code-Review vom 31.08.2015

**Durchgeführt von:** Jens Heuschkel, Jonas Mönnig

**Geprüfte Codeteile:** GpsTracker

**Änderungen vorgenommen in Commit:** Im Zuge dieses Code-Reviews wurden keine Änderungen vorgenommen.

### 1. Is the functionality correct?

Ja.

### 2. Are the classes, functions and variables named suitably?

Ja. 

### 3. Is the class structure well designed and meeting all requirements or does it need improvements?

Ja.

### 4. Are there classes which have become obsolete because of a new implementation?

Nein.

### 5. Are there unnecessary overloaded functions or constructors?

Nein.

### 6. Is the perstistent database structure well designed?

Für dieses Codeteil gibt es keine Datenbankstruktur.

### 7. Does it contain functions that can be reused later? Are they placed in a Helper class?

Die Klasse enthält keien wiederverwendbaren Funktionen.

### 8. Are existing helper functions being used? Is no duplicate functionality implemented?

Es wurde keine doppelte Funktionalität implementiert.

### 9. Are all the input arguments being validated?

Es  gibt keine Eingabeparameter.

### 10. How will the functionalities affect the performance of the app - power usage, time and memory?

Da GPS mit der maximalen Genauigkeit verwendet wird, wird der Stromverbrauch stark erhöht. Die hohe Genauigkeit wird aber benötigt, darum wird sie beibehalten.

### 11. Is it absolutely necessary to run it on UI thread or would a background thread suffice?

Für diesen Codeteil nicht relevant.

### 12. Are all possible failures handled?

Ja.

### 13. Does it degrade gracefully in case of unknown failures?

Ja.

### 14. Are the best practices for app development, according to Android lint, being followed?

Ja.

### 15. What pieces of the component can be executed in parallel?

Für diesen Codeteil nicht relevant.

### 16. Are the operations thread-safe if they need to be?

Müssen sie nicht.

### 17. Is the layout suitable for all screen dimensions?

Diese Funktionalität hat kein Layout.

### 18. Do all methods and fields have the proper access modifiers?

Ja.
