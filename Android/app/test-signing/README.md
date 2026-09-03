# PFVR Android test signing

`pfvr-test.keystore.b64` ist ein bewusst öffentlicher **Testschlüssel** ausschließlich für `ch.pfvr.app.test`.
Gradle dekodiert ihn bei jedem Build in das ignorierte `build/`-Verzeichnis. Dadurch lässt sich der gleiche Signierschlüssel reproduzierbar lokal und in GitHub Actions verwenden.

Ab Testversion 0.9.5 können spätere Test-APKs bei höherem `versionCode` als Update über die bestehende Test-App installiert werden.

Dieser Schlüssel darf niemals für `ch.pfvr.app`, Google Play oder andere Produktions-/Release-Builds verwendet werden. Die Produktionssignierung bleibt geheim und separat.

Certificate SHA-256: `0521e6bc43e2868177609dea69d074ed14bb594bad28adee98293e586d3d46bf`
