# Code-Review vom 13.07.2015

**Durchgeführt von:** Max Weller, Alexander Frömmgen, Jens Heuschkel

**Geprüfte Codeteile:** models-Package, ConnectionManager, EchoProtocol, GcmProtocol, TracerouteHandler 

**Änderungen vorgenommen in Commit:** da815895bedcd619aaf928dc29c80a433cdc0ba4

### 1. Is the functionality correct?

- Im ConnectionManager in der SENDMESSAGE\_ACTION wird eine RuntimeException nicht geworfen, sondern einfach einer Variable zugewiesen. Dies wurde geändert, sodass die Exception nun geworfen wird.

### 2. Are the classes, functions and variables named suitably?

Ja

### 3. Is the class structure well designed and meeting all requirements or does it need improvements?

Einige Konstanten und einige Funkionen für die HTTP-API sind nicht an einer sinnvollen Stelle definiert, sonder immer in der Klasse, in der sie gerade benutzt wurden. Als Konsequenz wurde eine eigene Klasse für die API angelegt (BonfireAPI.java), in die diese Konstanten und Funktionen  verlagert wurden.

### 4. Are there classes which have become obsolete because of a new implementation?

Nein

### 5. Are there unnecessary overloaded functions or constructors?

Nein

### 6. Is the perstistent database structure well designed?

Ist nicht relevant für die geprüften Codeteile.

### 7. Does it contain functions that can be reused later? Are they placed in a Helper class?

Nein

### 8. Are existing helper functions being used? Is no duplicate functionality implemented?

Es wurde keine doppelte Funktionalität implementiert.

### 9. Are all the input arguments being validated?

Ja

### 10. How will the functionalities affect the performance of the app - power usage, time and memory?

Allein aus dem Grund, dass viele Netzwerkressourcen angefordert werden, wird der Stromverbrauch negativ beeinflusst, dies liegt aber nicht an der Implementierung, sondern an der Natur der Sache.

### 11. Is it absolutely necessary to run it on UI thread or would a background thread suffice?

Nicht relevant

### 12. Are all possible failures handled?

Ja

### 13. Does it degrade gracefully in case of unknown failures?

Wird nicht betrachtet.

### 14. Are the best practices for app development, according to Android lint, being followed?

Ja

### 15. What pieces of the component can be executed in parallel?

Ist für diesen Codeteil nicht relevant.

### 16. Are the operations thread-safe if they need to be?

Ist für diesen Codeteil nicht relevant.

### 17. Is the layout suitable for all screen dimensions?

Ist für diesen Codeteil nicht relevant.

### 18. Do all methods and fields have the proper access modifiers?

Sehr viele Variablen, die final sein sollten, sind nicht final deklariert. Dies wurde korrigert.
