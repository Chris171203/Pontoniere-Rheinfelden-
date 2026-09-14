# PFVR für iOS

Native SwiftUI-App für iPhone und iPad ab iOS 17. Testversion **0.15.0**, Build **2**.

Der aktuelle Abgleich übernimmt die freigegebene Android-Vereinsansicht: direkt sichtbares Training, normale Themenseiten, öffentliche WordPress-Inhalte und ausgewählte Bilder mit lokalem Cache, Kontakt-/Social-Symbole sowie Deutsch/Schweizerdeutsch. Die interne An-/Abmeldung nutzt denselben exportierten JavaScript-Renderer wie Android.

- [Aktueller Portierungs- und Prüfstand](PORTING_STATUS.md)
- [Build und Tests](BUILD_TESTS.md)
- [Bisherige Testergebnisse](TEST_RESULTS.md)
- [Fachlicher Projektumfang](../PROJECT.md)

Die CI prüft Core, WebKit und native Bedienabläufe auf zwei iPhone-Grössen und einem iPad. Sie baut ausserdem ein unsigniertes Gerätearchiv. Eine installierbare Geräte-IPA oder TestFlight-Verteilung erfordert weiterhin Apple-Signierung und Provisionierung.
