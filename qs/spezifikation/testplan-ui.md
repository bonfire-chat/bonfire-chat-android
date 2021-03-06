Da uns der Aufwand für die Nutzung eines Instrumented-Test-Frameworks für das recht einfache User Interface der App unverhältnismäßig erscheint, werden manuelle Tests anhand der folgenden Testpläne vorgenommen.

Die einzelnen Testabläufe sind nach funktionalen Bereichen der App aufgeteilt.

I) Darstellung
================

1. Darstellung der Kontaktübersicht
-----------------------------------

Die Kontaktübersicht(ContactsActivity) soll so übersichtlich wie möglich gehalten sein. Dazu sind einige Dinge zu beachten.

| Benutzeraktion | Erwartetes Verhalten der App |
| -------------- | ---------------------------- |
| Der Benutzer öffnet die Kontaktübersicht | Dem Benutzer werden alle Kontakte aufsteigend alphabetisch angezeigt, also oben beginnend mit Kontaktnamen die mit A anfangen usw. |

2. Darstellung der Unterhaltungsübersicht
-----------------------------------------

Die Unterhaltungsübersicht(ConversationsActivity) soll so übersichtlich wie möglich gehalten sein. Dazu sind einige Dinge zu beachten.

| Benutzeraktion | Erwartetes Verhalten der App |
| -------------- | ---------------------------- |
| Der Benutzer öffnet die Unterhaltungsübersicht | Dem Benutzer werden alle Unterhaltungen angezeigt, und zwar geordnet nach der Zeit der letzten Nachricht, beginnend mit der Unterhaltung mit der neuesten Nachricht. Die aktuellste Nachricht wird unter dem Namen der Unterhaltung angezeigt |

3. Darstellung der Chatansicht
------------------------------

Der Benutzer soll möglicht übersichtlich Nachrichten lesen können. Dementsprechend muss die Chatansicht (MessagesActivity) gestaltet sein.

### Allgemeine Darstellung der Nachrichten

Nachrichten in der Chatansicht werden grundsätzlich wie hier beschrieben dargestellt. Die neuesten Nachrichten sind unten zu finden, die ältesten oben. Nachrichten des Benutzers werden rechts angezeigt, die seines Chatpartners links. Neben der Nachricht wird das Kontaktbild des Absenders angezeigt. Unter dem Nachrichtentext sind durch folgende Symbole die Nachrichtendetail dargestellt:

- Ein Schlosssymbol, wenn die Nachricht verschlüsselt ist
- Das Bluetooth-Symbol, wenn die Nachricht über Bluetooth versendet wurde
- Das WiFi-Symbol, wenn die Nachricht über WiFi peer-to-peer versendet wurde
- Eine Wolke, wenn die Nachricht über Google-Cloud-Messaging versendet wurde
- Ein rotes Dreieck mit Ausrufezeichen darin, wenn die Nachricht nicht angekommen ist

Außerdem wird noch der genaue Sendezeitpunkt angezeigt. 

| Benutzeraktion | Erwartetes Verhalten der App |
| -------------- | ---------------------------- |
| Der Benutzer öffnet eine Unterhaltung | Die Tastatur öffnet sich und der Benutzer kann einen Text eingeben. Gleichzeitig werden die vergangenen Nachrichten des Chats angezeigt wie in [Allgemeine Darstellung von Nachrichten](Manueller-Test-UI#allgemeine-darstellung-der-nachrichten) beschrieben | 

II) Erster Start und Einstellungen
=====================================

1. Willkommensseite
---------------------

| Benutzeraktion | Erwartetes Verhalten der App |
| -------------- | ---------------------------- |
| Die App wird nach Neuinstallation / Löschen der App-Daten geöffnet. | Im Hintergrund wird ein Schlüsselpaar für das Gerät generiert. Es öffnet sich eine Begrüßungsseite, auf der der Nutzer zur Eingabe eines Namens (Nickname) aufgefordert wird. |
| Es wird ein Nickname eingetragen, der bereits vergeben ist. Der Bestätigungsbutton wird betätigt. | Es wird ein HTTP-Post-Request an das Server-API gesendet, welcher Public-Key, Nickname und optional Handynummer enthält. |
|  | Es wird eine Fehlermeldung angezeigt, die den Nutzer auffordert, einen anderen Nickname zu wählen. Die Begrüßungsseite bleibt sichtbar. |
| Es wird ein neuer Nickname eingetragen. Der Bestätigungsbutton wird betätigt. | Die Begrüßungsseite schließt sich und der Benutzer gelangt in die Haupt-Activity der App, wobei der Navigations-Drawer geöffnet ist. |
| Die App wird durch Betätigen der Back-Taste verlassen. | |

2. Automatisches Öffnen des NavigationDrawer
---------------------------------------------

Um dem Benutzer die Navigation in der App nahezubringen, soll, wie in den Android-UI-Richtlinien empfohlen, das Navigationsmenü (NavigationDrawer) so lange beim App-Start automatisch eingeblendet werden, bis der Benutzer es einmal selbstständig geöffnet hat.

| Benutzeraktion | Erwartetes Verhalten der App |
| -------------- | ---------------------------- |
| Die App wird aus der App-Liste gestartet | Die MainActivity der App öffnet sich, der Menüpunkt "Unterhaltungen" ist ausgewählt, der NavigationDrawer ist geöffnet. |
| Schließen des NavigationDrawer durch Klick auf "Unterhaltungen". Wieder Öffnen des NavigationDrawer durch Hereinwischen vom linken Bildschirmrand. | Der NavigationDrawer schließt sich und öffnet sich wieder. |
| Beenden der App durch Betätigen der Back-Taste | |
| Die App wird erneut aus  der App-Liste gestartet | Die MainActivity der App öffnet sich, der Menüpunkt "Unterhaltungen" ist sichtbar, der NavigationDrawer ist **nicht** geöffnet. |
| Beenden der App durch Betätigen der Back-Taste | |

3. Tutorial beim ersten Start
-----------------------------

Um dem Benutzer den Einstieg in die Benutzung der App zu vereinfachen, wird er beim ersten Start der App durch ein interaktives Tutorial geführt. Dieses muss einwandfrei funktionieren, damit der Nutzer nicht verwirrt wird.

| Benutzeraktion | Erwartetes Verhalten der App |
| -------------- | ---------------------------- |
| Die App wird nach Neuinstallation / Löschen der App-Daten geöffnet. Der Benutzer gibt einen noch freien Benutzernamen und seine Telefonnummer ein und drückt auf OK | Dem Benutzer wird der NavigationDrawer gezeigt und darauf hingewiesen, dass er mit diesem in der App navigieren kann. Des Weiteren wird ein Knopf eingeblendet, mit dem der Benutzer zum nächsten Teil des Tutorials springen kann |
| Der Benutzer drückt auf den eben beschriebenen Knopf | Es wird der Share-Button zum Teilen der Kontaktinformationen hervorgehoben, und dem Benutzer seine Funktionsweise beschrieben. Des Weiteren wird ein Knopf eingeblendet, mit dem der Benutzer zum nächsten Teil des Tutorials springen kann|
| Der Benutzer drückt auf den eben beschriebenen Knopf | Der Benutzer wird aufgefordert, auf Unterhaltungen zu klicken |
| Der Benutzer klickt auf Unterhaltungen | Die Unterhaltungsübersicht öffnet sich, und es ist bereits eine Unterhaltung zu sehen. Der Benutzer wird aufgefordert, die Unterhaltung anzuklicken |
| Der Benutzer klickt die Unterhaltung an | Die Unterhaltung öffnet sich und der Benutzer wird als erstes auf die Titel-ändern-Funktion hingewiesen. Auch wird ihm wieder ein Weiter-Knopf angezeigt |
| Der Benutzer drückt auf den eben beschriebenen Knopf | Der Benutzer wird nun auf die Bilder-schicken-Funktion hingewiesen. Auch wird ihm wieder ein Weiter-Knopf angezeigt |
| Der Benutzer drückt auf den eben beschriebenen Knopf | Der Benutzer wird auf die Standort-Teilen-Funkiton hingewiesen. Auch wird ihm wieder ein Weiter-Knopf angezeigt | 
| Der Benutzer drückt auf den eben beschriebenen Knopf | Eine Nachricht wird hervorgehoben und dem Benutzer erklärt, dass er durch Tippen auf die Nachricht in die Nachrichtendetailansicht gelangen kann. Auch wird wieder ein Weiter-Knopf angezeigt |
| Der Benutzer drückt auf den eben beschriebenen Knopf | Der NavigationDrawer wird wieder eigeblendet und der Benutzer wird aufgefordert, auf Kontakte zu klicken. |
| Der Benutzer klickt auf Kontakte | Die Kontaktübersicht öffnet sich und der Benutzer wird auf die Kontaktdaten-teilen-Funktion hingewiesen. Auch wird wieder ein Weiter-Knopf angezeigt. |
| Der Benutzer drückt auf den eben beschriebenen Knopf | Der Benutzer wird auf die QR-Code-Scannen-Funktion hingewiesen. Auch wird wieder ein Weiter-Knopf angezeigt. |
| Der Benutzer drückt auf den eben beschriebenen Knopf | Der Benutzer wird auf die Kontakte-Suchen-Funktion hingewiesen. Nun wird ein "Los gehts"-Knopf angezeigt. |
| Der Benutzer drückt auf den eben beschriebenen Knopf | Der Benutzer gelangt wieder in die Unterhaltungsübersicht. Das Tutorial ist beendet. |

III) Senden und Empfangen von Nachrichten 
========================================

1. Korrektes Anzeigen von Benachrichtigungen
---------------------------------------------

Benachrichtigungen über neue Nachrichten sollen nur angezeigt werden, wenn das entsprechende Chatfenster nicht geöffnet ist. Außerdem sollen diese Benachrichtigungen automatisch verschwinden, wenn die betreffende Nachricht gelesen wurde.

| Benutzeraktion | Erwartetes Verhalten der App |
| -------------- | ---------------------------- |
| Der Benutzer befindet sich auf dem Startbildschirm der App, eine Nachricht trifft ein | Es wird eine Benachrichtigung in der Benachrichtigungsleiste angezeigt, beim Klicken auf die Benachrichtigung gelangt der Benutzer zum Chat, in dem die Nachricht gesendet wurde, die Benachtrichtigung verschwindet |
| Der Benutzer befindet sich in einem Chat (in der MessagesActivity), eine Nachricht trifft in diesem Chat ein. | Es wird keine Benachrichtigung angezeigt |
| Der Benutzer befindet sich in einem Zustand, in dem eine Benachrichtigung über eine Nachricht angezeigt wird. Dann liest der die entsprechende Nachricht, indem er direkt - ohne anklicken der Benachrichtigung - in den entsprchenden Chat geht | Die Benachrichtigung verschwindet |

2. Senden und empfangen von normalen Nachrichten
------------------------------------------------

| Benutzeraktion | Erwartetes Verhalten der App |
| -------------- | ---------------------------- |
| Benutzer befindet sich in der Chatansicht und tippt einen Nachrichtentext in das Textfeld ein. Dann drückt er die Sendetaste | Die Nachricht wird sofort an den Chatpartner gesendet. Die Nachricht wird sofort angezeigt, wie in [Allgemeine Darstellung von Nachrichten](Manueller-Test-UI#allgemeine-darstellung-der-nachrichten) beschrieben. |
| Der Benutzer empfängt eine Nachricht und geht dann in die entsprechende Chatansicht oder ist dort bereits | Alle Nachrichten werden wie in [Allgemeine Darstellung von Nachrichten](Manueller-Test-UI#allgemeine-darstellung-der-nachrichten) angezeigt. Die Empfangene Nachricht ist bereits enthalten. |

3. Senden und empfangen von Bildern
-----------------------------------

Das Senden von Bildern soll mögichst einfach in der Bedienung sein und den Benutzer nicht verwirren. Vor allem soll die App nicht bei unerwarteten Eingaben abstürzen.

| Benutzeraktion | Erwartetes Verhalten der App |
| -------------- | ---------------------------- |
| Der Benutzer klickt in der Chatansicht auf das Bild-Senden-Symbol | Es öffnet sich eine von Android bereitgestellte Liste mit versendbaren Bildern. |
| Der Benutzer klickt auf ein Bild in der eben beschriebenen Liste | Das Bild wird sofort an den Chatpartner gesendet. Die App kehrt zu betreffenden Chat zurück. Dort ist nun das Bild zu sehen. |
| Der Benztzer wählt in der Liste kein Bild aus und kehrt mittels des Zurück-Knopfs zum Chat zurück | Es passiert nichts, insbesondere stürzt die App nicht ab. |
| Der Benutzer versucht, ein nicht darstellbares Bild zu versenden | Es wird eine Fehlermeldung angezeigt, dass das Bild nicht versendet werden kann. Das Bild wird nicht versendet. Die App kehrt zur MessagesActivity zurück. |
| Der Benutzer bekommt in einem Chat ein Bild gesendet. Daraufhin wechselt er in diesen Chat | Der Benutzer sieht das ihm gesendete Bild |

4. Teilen des aktuellen Standorts
---------------------------------

| Benutzeraktion | Erwartetes Verhalten der App |
| -------------- | ---------------------------- |
| Der Benutzer befindet sich in der Chatansicht, er tippt auf das Kompass-Symbol in der Menüleiste. Sein Standort kann bestimmt werden | Der Standort wird zum Chatpartner gesendet. Es ist eine Google-Maps-Vorschau des Standorts zu sehen |
| Der Benutzer befindet sich in der Chatansicht, er tippt auf das Kompass-Symbol in der Menüleiste. Sein Standort kann nicht bestimmt werden. | Es wird eine Fehlermeldung eingeblendet, die aussagt, dass der Standort nicht bestimmt werden kann und deswegen auch nicht gesendet werden kann |
| Der Benutzer empfängt eine Standortteilung | Die App verhält sich wie bei normalen Nachrichten. In der Chatansicht sieht er eine Google-Maps-Vorschau des Standorts |
| Der Benutzer tippt auf eine Vorschau eines Standorts | Eine neue Ansicht öffnet sich, in der der Standort auf einer Karte in Großansicht angezeigt wird. | 

IV) Löschen von Elementen
=========================

1. Löschen von Kontakten
------------------------

| Benutzeraktion | Erwartetes Verhalten der App |
| -------------- | ---------------------------- |
| Der Benutzer ist in der Kontaktansicht und wählt durch langes Tippen auf einen Kontakt denselben aus | Die Menüleiste wird durch eine auswahlspezifische Menüleiste ersetzt, in der auch ein Mülleimer als Lösch-Symbol zu sehen ist |
| Der Benutzer wählt einen Kontakt aus und löscht ihn durch Tippen auf das Mülleimer-Symbol | Der Kontakt wird aus der Datenbank gelöscht und verschwindet sofort aus der Ansicht. Mit dem Kontakt werden auch sämtliche mit dem Kontakt geführte Unterhaltungen gelöscht, die ab sofort nicht mehr in der Unterhaltungsübersicht zu sehen sind | 
| Nach dem Löschen eines spezifischen Kontakts wechselt der Benutzer auf eine andere Ansicht und öffnet dann die Kontaktansicht erneut. | Der vorher gelöschte Kontakt ist immer noch nicht zu sehen |

2. Löschen von Unterhaltungen
-----------------------------

| Benutzeraktion | Erwartetes Verhalten der App |
| -------------- | ---------------------------- |
| Der Benutzer ist in der Unterhaltungsansicht und wählt durch langes Tippen auf eine Unterhaltung dieselbe aus | Die Menüleiste wird durch eine auswahlspezifische Menüleiste ersetzt, in der auch ein Mülleimer als Lösch-Symbol zu sehen ist |
| Der Benutzer wählt eine Unterhaltung aus und löscht sie durch Tippen auf das Mülleimer-Symbol | Die Unterhaltung wird aus der Datenbank gelöscht und verschwindet sofort aus der Ansicht. |
| Nach dem Löschen einer spezifischen Unterhaltung wechselt der Benutzer auf eine andere Ansicht und öffnet dann die Unterhaltungsansicht erneut. | Die vorher gelöschte Unterhaltung ist immer noch nicht zu sehen |

3. Löschen von Nachrichten
--------------------------

| Benutzeraktion | Erwartetes Verhalten der App |
| -------------- | ---------------------------- |
| Der Benutzer ist in der Chatansicht und wählt durch langes Tippen auf eine Nachricht dieselbe aus | Die Menüleiste wird durch eine auswahlspezifische Menüleiste ersetzt, in der auch ein Mülleimer als Lösch-Symbol zu sehen ist |
| Der Benutzer wählt eine Nachricht aus und löscht sie durch Tippen auf das Mülleimer-Symbol | Die Nachricht wird aus der Datenbank gelöscht und verschwindet sofort aus der Ansicht. | 
| Nach dem Löschen einer spezifischen Nachricht wechselt der Benutzer auf eine andere Ansicht und öffnet dann die Chatansicht erneut. | Die vorher gelöschte Nachricht ist immer noch nicht zu sehen |

V) Diverses
===========

1. Korrekte Funktionsweise der regelmäßigen Standortteilung
----------------------------------------------------------

Die Ansicht, unter der man den dauerhaft geteilten Standort eines Chatpartners sehen kann, soll automatisch aktualisiert werden, wenn ein Standortupdate eintrifft.

| Benutzeraktion | Erwartetes Verhalten der App |
| -------------- | ---------------------------- |
| Der Benutzer ist in der Kontaktansicht und wählt durch langes Tippen auf einen Kontakt denselben aus | Die Menüleiste wird durch eine auswahlspezifische Menüleiste ersetzt, in der auch ein Standort-Symbol zu sehen ist |
| Der Benutzer b1 hat den Kontakt b2 ausgewählt und tippt auf das Standort-Symbol. Der b2 hat seinen Standort noch nicht geteilt | Es wird dem Benutzer b1 ein Ansicht angezeigt, die ihm sagt, dass kein Standort vorliegt | 
| Benutzer b1 befindet sich in der eben beschriebenen Ansicht. b2 geht nun in das Kontakt-bearbeiten-Menü von b1 und setzt das "Diesem Kontakt meinen Standort mitteilen"-Häkchen | Die von b1 geöffnete Ansicht zeigt nun eine Karte, auf der der Standort von b2 zu sehen ist. |
| b1 befindet sich in der Ansicht, in der der Standort von b2 zu sehen ist. b2 entfernt sich um mindestens 50m von seinem Ausganspunkt. | b1 sieht nun innerhalb des aktuellen Aktualisierungsintervalls des Standorts eine veränderte Position von b2 auf der Karte |
