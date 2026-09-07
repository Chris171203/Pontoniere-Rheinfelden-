from pathlib import Path


def replace_once(text, old, new, label):
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected 1 occurrence, got {count}")
    return text.replace(old, new, 1)


skin_path = Path("Android/app/src/main/java/ch/pfvr/internapp/InternalAttendanceSkin.java")
skin = skin_path.read_text()

# A person is eligible for automatic restore only when the app previously
# stored a concrete website option value for an explicit user selection.
old = """                    var missing=state.desired.find(function(name){
                      return !isHiddenPerson(state,name)&&!currentNames.some(function(current){return samePersonName(current,name);});
                    });
"""
new = """                    var missing=state.desired.find(function(name){
                      return state.restoreValues&&state.restoreValues[personKey(name)]&&!isHiddenPerson(state,name)&&!currentNames.some(function(current){return samePersonName(current,name);});
                    });
"""
skin = replace_once(skin, old, new, "restrict automatic restore to explicit app selections")

# If explicit selections exist, restore them automatically on a later load.
old = """                    if(toolInfo&&restoreRequested&&tryRestoreMissingPerson(toolInfo.select,allNames,peopleState))return false;
"""
new = """                    if(toolInfo&&(restoreRequested||Object.keys(peopleState.restoreValues||{}).length)&&tryRestoreMissingPerson(toolInfo.select,allNames,peopleState))return false;
"""
skin = replace_once(skin, old, new, "auto restore remembered app selections")

# The manager must still show remembered people even while the source page has
# not yet re-created their rows.
old = """                    (currentNames||state.desired).forEach(function(personName){
                      appendManagedPerson(list,state,personName);
                    });
"""
new = """                    dedupePeople((currentNames||[]).concat(state.desired||[])).forEach(function(personName){
                      appendManagedPerson(list,state,personName);
                    });
"""
skin = replace_once(skin, old, new, "show remembered people in manager")

# The manual restore action follows the same safety rule: only explicitly
# selected people with a saved option value are candidates.
old = """                    var missingDesired=(state.desired||[]).filter(function(name){return !isHiddenPerson(state,name)&&!(currentNames||[]).some(function(current){return samePersonName(current,name);});});
"""
new = """                    var missingDesired=(state.desired||[]).filter(function(name){return state.restoreValues&&state.restoreValues[personKey(name)]&&!isHiddenPerson(state,name)&&!(currentNames||[]).some(function(current){return samePersonName(current,name);});});
"""
skin = replace_once(skin, old, new, "manual restore candidate safety")
skin_path.write_text(skin)


# Update regression coverage.
test_path = Path("Android/app/src/test/java/ch/pfvr/internapp/InternalAttendanceSkinTest.java")
test = test_path.read_text()
old = """        assertTrue(script.contains(\"restoreSaved\"));
        assertTrue(script.contains(\"RESTORE_REQUEST_KEY\"));
        assertTrue(script.contains(\"restoreRequested&&tryRestoreMissingPerson\"));
        assertFalse(script.contains(\"if(toolInfo&&tryRestoreMissingPerson\"));
        assertFalse(script.contains(\"window.__pfvrBaseInternalUrl\"));
"""
new = """        assertTrue(script.contains(\"restoreSaved\"));
        assertTrue(script.contains(\"RESTORE_REQUEST_KEY\"));
        assertTrue(script.contains(\"state.restoreValues&&state.restoreValues[personKey(name)]\"));
        assertTrue(script.contains(\"restoreRequested||Object.keys(peopleState.restoreValues||{}).length\"));
        assertTrue(script.contains(\"dedupePeople((currentNames||[]).concat(state.desired||[]))\"));
        assertFalse(script.contains(\"if(toolInfo&&tryRestoreMissingPerson\"));
        assertFalse(script.contains(\"window.__pfvrBaseInternalUrl\"));
"""
test = replace_once(test, old, new, "update person restore regression assertions")
test_path.write_text(test)


# Version bump: 0.12.0 already escaped into device testing, so this is a
# patch release rather than silently replacing the same version.
gradle_path = Path("Android/app/build.gradle")
gradle = gradle_path.read_text()
gradle = replace_once(gradle, "versionCode 57", "versionCode 58", "version code")
gradle = replace_once(gradle, "versionName '0.12.0'", "versionName '0.12.1'", "version name")
gradle_path.write_text(gradle)


status_path = Path("STATUS.md")
status = status_path.read_text()
status = replace_once(status, "Stand: Testversion `0.12.0`", "Stand: Testversion `0.12.1`", "status version")
anchor = "## Aktueller Teststand\n\n"
entry = "- `0.12.1` behebt die Persistenz ausdrücklich hinzugefügter Personen in der internen App-Ansicht. Eine Person, die der Nutzer einmal über `Personen` ausgewählt hat, trägt bereits einen gespeicherten Website-Optionswert (`restoreValues`) und wird bei späteren Seitenaufrufen automatisch wiederhergestellt, sofern sie im Original-Auswahlfeld weiterhin existiert. Allgemein aus der Website gelesene Personen werden weiterhin nicht automatisch zurückgeschrieben. Die Personenverwaltung zeigt gespeicherte Zusatzpersonen auch während eines noch ausstehenden Restores an.\n"
if anchor not in status:
    raise SystemExit("STATUS anchor missing")
status = status.replace(anchor, anchor + entry, 1)
status_path.write_text(status)


review_path = Path("decisions/security-review-2026-09-06.md")
review = review_path.read_text()
old = """### Entscheidung

Automatisches Lesen/Skinning bleibt erlaubt; schreibende Aktionen sollen eine klare Nutzeraktion erfordern. Issue #15 verfolgt die Entfernung des automatischen Restore-Pfads. Eine fehlende lokal gemerkte Person soll nur angezeigt werden; Wiederhinzufügen erfolgt über `Personen` nach expliziter Auswahl.
"""
new = """### Entscheidung

Automatisches Lesen/Skinning bleibt erlaubt; schreibende Aktionen brauchen eine klare Nutzerentscheidung. Der frühere Restore aus der allgemeinen lokalen `desired`-Liste bleibt entfernt. Ab `0.12.1` gilt eine engere Persistenzregel: Wählt der Nutzer eine Zusatzperson ausdrücklich über `Personen`, speichert die App dafür den konkreten Website-Optionswert in `restoreValues`. Nur solche ausdrücklich gewählten Personen dürfen bei späteren Seitenaufrufen automatisch wiederhergestellt werden, und nur solange die Person im Original-Auswahlfeld weiterhin vorhanden ist. Allgemein aus der Website gelesene Personen erhalten keinen Restore-Marker und werden niemals aufgrund des lokalen Zustands automatisch zurückgeschrieben.
"""
review = replace_once(review, old, new, "update security decision for explicit persistent selections")
review_path.write_text(review)
