# Rhein-Schifffahrtslage

Stand: 2026-09-04

## Entscheidung

Die App leitet Warnung und Schifffahrtssperrung ausschließlich aus dem Pegel **Basel-Rheinhalle** ab. Maßgeblich sind die von den Schweizerischen Rheinhäfen veröffentlichten Hochwassermarken:

- 700 cm: Hochwassermarke I, Voralarm.
- 790 cm: Hochwassermarke IIb, Sperrung der Kleinschifffahrt und des Fährbetriebs zwischen Basel und Rheinfelden.
- 820 cm: Hochwassermarke IIa, Sperrung der Schifffahrt zwischen Rheinfelden und der Schleuse Kembs.

Der Abfluss in m³/s bleibt sichtbar, bestimmt aber keine offizielle Sperrstufe. Näherungsweise korrespondierende Abflusswerte werden nicht zur Statuslogik verwendet.

## Darstellung

- Pegel ist die priorisierte Größe der Rhein-Kurzkarten.
- Basel erhält eine stufenabhängige Pegelfarbe und einen Lage-Badge.
- Pegel- und Abflusskurve verwenden innerhalb einer Stufe unterschiedliche, aber verwandte Farbtöne. So bleibt erkennbar, welche Kurve welcher Messgröße gehört, während die Hochwasserstufe visuell erhalten bleibt.
- Historische Abschnitte im Basel-Graph werden anhand des zeitgleichen Basel-Pegels eingefärbt.
- Die Hochwassermarken werden auf der Pegelachse eingezeichnet.
- Rheinfelden wird nicht aus einem angenommenen Pegelnullpunkt oder lokalen Schwellen in eine offizielle Schifffahrtsstufe umgerechnet.

## Daten- und Quellenregel

Ableitungen werden nur verwendet, wenn ihr Bezug belastbar dokumentiert ist. Für Basel ist der Zusammenhang zwischen BAFU-Wasserstand und Pegel verifiziert. Für Rheinfelden wird ohne bestätigten aktuellen Pegelnullpunkt kein relativer cm-Pegel und keine lokale Sperrstufe erzeugt.

Primärquelle für Hochwassermarken und Sperrbedeutung: Schweizerische Rheinhäfen, `https://port-of-switzerland.ch/hafenservice/pegel/`.
