I) Definitionen
======
Allgemeine Ausgangskonfiguration der Knoten:
------------------

Ein Knoten ist, soweit nicht näher beschrieben, ein Android-Gerät, auf dem die aktuellste Version der App eingerichtet und die initiale Einrichtung (Eingabe eines Nicknames, dadurch Registrierung des Gerätes mit Nickname, GCM-ID und PublicKey am Server) abgeschlossen ist. 

Eigenschaften:
---------------------
1. Geringe Distanz: Die Knoten stehen nahe zu ihren Nachbarn (1m).
2. Mittlere Distanz: Die Knoten stehen etwas entfernt zu ihren Nachbarn (5m).
3. Große Distanz: Die Knoten stehen weit entfernt zu ihren Nachbarn (10m).
4. Stillstehend: Die Knoten bewegen sicht nicht. 
5. Viel Bewegung: Die Knoten bewegen sicht alle in verschiedenen Richtungen mit Schrittgeschwindigkeit.
6. Linien Topologie: Die Knoten befinnden sich in einer Linie.
7. Kugel Topologie: Die Knoten sind gleichmäßig im Raum verteilt und Bilden einen ausgefüllten Kreis.
8. Getrennte Gruppen: Es gibt zwei Gruppen, wobei kein Knoten der einen Gruppe eine Direktverbindung mit einem Knoten der anderen Gruppe hat.
9. Kein Internetzugang: Kein knoten hat Internetzugang.
10. Geringer Internetzugang: 20% der Knoten hat Internetzugang.
11. Serieller Verbindungsaufbau: Die Bluetooth-Verbindung wird zu mehreren Knoten seriell aufgebaut.
12. Parallerler Verbindungsaufbau: Die Bluetooth-Verbindung wird zu mehreren Knoten parallel aufgebaut.
13. Verzögerter Verbindungsaufbau Parallel: Die Verbindungsanfragen werden leicht versetzt gesendet.
14. Wenig Nachrichten: Jeder Knoten sendet permanent alle 20 Sekunden eine Nachricht an einen Beliebigen anderen Knoten.
15. Viele Nachrichten: Jeder Knoten sendet permanent alle 5 Sekunden eine Nachricht an einen Beliebigen anderen Knoten.

II) Testfälle für Statistik Erhebungen
==================
Testaufbau
-----------------
Jeder läuft 5 min und alle Eigenschaften müssen beibehlten werden. Kleine Abweichungen bei Distanz oder Sendegeschwindigkeit werden hingenommen.

1. Test
-------------
Eigenschaft Geringe Distanz & Stillstehend & Kugel Topologie & Kein Internetzugang & Parallerler Verbindungsaufbau & Viele Nachrichten sind erfüllt.

2. Test
-------------
Eigenschaft Geringe Distanz & Viel Bewegung & Kugel Topologie & Kein Internetzugang & Parallerler Verbindungsaufbau & Viele Nachrichten sind erfüllt.

3. Test
-------------
Eigenschaft Mittlere Distanz & Stillstehend & Kugel Topologie & Kein Internetzugang & Parallerler Verbindungsaufbau & Viele Nachrichten sind erfüllt.

4. Test
-------------
Eigenschaft Mittlere Distanz & Viel Bewegung & Kugel Topologie & Kein Internetzugang & Parallerler Verbindungsaufbau & Viele Nachrichten sind erfüllt.

5. Test
-------------
Eigenschaft Große Distanz & Stillstehend & Kugel Topologie & Kein Internetzugang & Parallerler Verbindungsaufbau & Viele Nachrichten sind erfüllt.

6. Test
-------------
Eigenschaft Große Distanz & Viel Bewegung & Kugel Topologie & Kein Internetzugang & Parallerler Verbindungsaufbau & Viele Nachrichten sind erfüllt.

7. Test 
-------------
Eigenschaft Geringe Distanz & Stillstehend & Linien Topologie & Kein Internetzugang & Parallerler Verbindungsaufbau & Viele Nachrichten sind erfüllt.

8. Test 
-------------
Eigenschaft Mittlere Distanz & Stillstehend & Linien Topologie & Kein Internetzugang & Parallerler Verbindungsaufbau & Viele Nachrichten sind erfüllt.

9. Test 
-------------
Eigenschaft Große Distanz & Stillstehend & Linien Topologie & Kein Internetzugang & Parallerler Verbindungsaufbau & Viele Nachrichten sind erfüllt.

10.Test
-------------
Eigenschaft Mittlere Distanz & Stillstehend & Kugel Topologie & Kein Internetzugang & Parallerler Verbindungsaufbau & Viele Nachrichten sind erfüllt.

11.Test
-------------
Eigenschaft Mittlere Distanz & Stillstehend & Kugel Topologie & Getrennte Gruppen & Geringer Internetzugang & Parallerler Verbindungsaufbau & Viele Nachrichten sind erfüllt.

12.Test
-------------
Eigenschaft Mittlere Distanz & Stillstehend & Kugel Topologie & Kein Internetzugang & Serieller Verbindungsaufbau & Viele Nachrichten sind erfüllt.

13.Test
-------------
Eigenschaft Mittlere Distanz & Stillstehend & Kugel Topologie & Kein Internetzugang & Parallerler Verbindungsaufbau & Viele Nachrichten sind erfüllt.

14.Test
-------------
Eigenschaft Mittlere Distanz & Stillstehend & Kugel Topologie & Kein Internetzugang & Verzögerter Verbindungsaufbau Parallel & Viele Nachrichten sind erfüllt.

