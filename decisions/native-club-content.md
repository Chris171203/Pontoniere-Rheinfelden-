# Native Vereinsinhalte

Datum: 2026-09-14 · Android 0.12.9; fachliche Referenz für den späteren iOS-Abgleich.

## Quelle und Darstellung

`ClubPageRepository` lädt fünf explizit erlaubte öffentliche WordPress-Seiten. Die validierte Rohantwort wird pro Seite für 24 Stunden gespeichert; Fehler ersetzen keinen gültigen Stand. Das WordPress-Feld `modified` ist das Änderungsdatum des Quellartikels, der Präferenz-Zeitstempel der letzte App-Abruf. Keines davon bestätigt die heutige Gültigkeit alter Vereinsangaben.

`ClubContentParser` erzeugt aus Überschriften, Absätzen, Bildern und Vorstands-Spalten ein reines Präsentationsmodell. Kein zweiter Faktenbestand. Die komplette bereinigte HTML-Textquelle bleibt für Rückfall und Details erhalten. Formulare und aktive Elemente werden entfernt. Der native Renderer führt Links zu bekannten Vereinsseiten bzw. Kalender wieder in die App; Telefon und Mail öffnen Gerätefunktionen. PDFs und Archive bleiben extern.

Mitgliederzahlen mit Bezugsjahr 2021 stehen nur in der Originalansicht. Nachwuchs-Ausschnitte übernehmen neutrale Ausbildungs-/Lagertexte und lassen die alten Alters-/Wettkampfkategorien dort weg. Die Originalansicht behält die Quelle mit sichtbarem Datum und Hinweis auf aktuelle Auskunft beim JP-Leiter. Historische Stationen werden nur erzeugt, wenn der Text ein konkretes Ereignis mit Datum belegt.

## Bilder und Lebenszyklus

Nur HTTPS-Rasterbilder aus `www.pfvr.ch/wp-content/uploads/`; jeder Redirect wird vor dem Abruf geprüft. Auswahl der kleinsten vorhandenen `srcset`-Variante ab 640 px, sonst Original-URL. Maximal ein Bild je Abschnitt, keine Galerie. Native Darstellung erhält das Seitenverhältnis und schneidet Motive nicht ab.

`ClubImageLoader` startet Downloads beim Einblenden mit zwei Hintergrund-Threads, maximal 5 MB je Antwort, Decode-Ziel maximal 1024 px. Private App-Dateien enthalten nur die verkleinerte JPEG-Version: maximal 16 MiB und 64 Einträge, Tagescache. Vorhandene Bilder werden auch bei einem fehlgeschlagenen neuen Abruf angezeigt. Ein atomarer Dateiaustausch schützt den letzten gültigen Bildstand; Cache-Leeren invalidiert laufende Schreibvorgänge. Die Activity beendet den Loader beim Zerstören; Bild-Callbacks halten Ansichten nur schwach.

Die Textansicht funktioniert unabhängig von Bildern. Ein fehlendes Bild zeigt eine knappe Meldung; Text, Quellen und Kontaktfunktionen bleiben erreichbar. Der Betrieb ohne vorherigen erfolgreichen Abruf kann naturgemäss noch keine Inhalte aus dem Cache anzeigen.

## Prüfung

Parser-Tests prüfen Quellzuordnung, Rückfall, alte Zahlen, Kontakt-Entitäten, Alters-Ausschnitte, belegte Daten und Bild-URLs. Robolectric prüft Bildcache nach Neustart/Netzfehler, fehlerhafte Downloads und Cache-Leeren während eines Downloads. Native Darstellung bei 320 dp und 150 Prozent Schrift in beiden Themen und Sprachen; Screenshots als CI-Artefakt. Weiterhin keine physische Geräteabnahme.
