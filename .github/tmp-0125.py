from pathlib import Path


def replace_once(text, old, new, label):
    count=text.count(old)
    if count!=1:
        raise SystemExit(f"{label}: expected 1 occurrence, got {count}")
    return text.replace(old,new,1)

# --- MainActivity ---
path=Path("Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java")
s=path.read_text()

s=replace_once(
    s,
    '    private static final String PREF_CASH_CART = "cash_cart_v1";\n',
    '    private static final String PREF_CASH_CART = "cash_cart_v1";\n    private static final String PREF_CASH_PAYMENT_CONFIRM_PENDING = "cash_payment_confirm_pending_v1";\n',
    'pending payment pref'
)

s=replace_once(
    s,
    '    private EditText cashFreeAmountInput;\n    private volatile boolean weatherLoading = false;\n',
    '    private EditText cashFreeAmountInput;\n    private boolean cashPaymentConfirmationShowing = false;\n    private volatile boolean weatherLoading = false;\n',
    'confirmation showing field'
)

s=replace_once(
    s,
    '        if(dataRefreshHandler!=null){dataRefreshHandler.removeCallbacks(dataRefreshTick);dataRefreshHandler.postDelayed(dataRefreshTick,5L*60L*1000L);}\n    }\n\n    @Override protected void onPause(){\n',
    '        if(dataRefreshHandler!=null){dataRefreshHandler.removeCallbacks(dataRefreshTick);dataRefreshHandler.postDelayed(dataRefreshTick,5L*60L*1000L);}\n        new Handler(Looper.getMainLooper()).post(this::maybeShowCashPaymentConfirmation);\n    }\n\n    @Override protected void onPause(){\n',
    'resume confirmation check'
)

# Cart payment entry points only.
s=replace_once(s,'        if(input!=null)payWithPreferredBank(input);\n','        if(input!=null)payWithPreferredBank(input,true);\n','cart bank origin')
s=replace_once(s,'    cartQr.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)showPaymentQr(input);});\n','    cartQr.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)showPaymentQr(input,true);});\n','cart qr origin')
s=replace_once(s,'    cartTwint.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)openTwintDirect(input);});\n','    cartTwint.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)openTwintDirect(input,true);});\n','cart twint origin')

old='''    private void payWithPreferredBank(EditText amountInput){
        if(!hasPreferredBank()){
            Toast.makeText(this,ui("Bitte unter Einstellungen → Zahlung eine Banking-App festlegen."),Toast.LENGTH_LONG).show();
            openPaymentSettings();
            return;
        }
        sharePaymentQr(amountInput);
    }
'''
new='''    private void payWithPreferredBank(EditText amountInput){payWithPreferredBank(amountInput,false);}

    private void payWithPreferredBank(EditText amountInput,boolean fromCart){
        if(!hasPreferredBank()){
            Toast.makeText(this,ui("Bitte unter Einstellungen → Zahlung eine Banking-App festlegen."),Toast.LENGTH_LONG).show();
            openPaymentSettings();
            return;
        }
        sharePaymentQr(amountInput,fromCart);
    }
'''
s=replace_once(s,old,new,'preferred bank origin propagation')

# Pending state and confirmation dialog live next to cart persistence.
old='''    private void saveCashCart(){
        if(prefs==null)return;
        prefs.edit().putStringSet(PREF_CASH_CART,CashCartState.encode(cashCart)).apply();
    }

    private void clearCashCart(){
        cashCart.clear();
        saveCashCart();
        for(TextView quantity:cashQuantityViews.values())quantity.setText("0");
        updateCashSummary();
    }
'''
new='''    private void saveCashCart(){
        if(prefs==null)return;
        prefs.edit().putStringSet(PREF_CASH_CART,CashCartState.encode(cashCart)).apply();
    }

    private void markCashPaymentConfirmationPending(){
        if(prefs==null)return;
        CashCatalog.Catalog catalog=cashCatalog();
        if(catalog==null||catalog.itemCount(cashCart)<=0)return;
        prefs.edit().putBoolean(PREF_CASH_PAYMENT_CONFIRM_PENDING,true).apply();
    }

    private void maybeShowCashPaymentConfirmation(){
        if(prefs==null||cashPaymentConfirmationShowing||!prefs.getBoolean(PREF_CASH_PAYMENT_CONFIRM_PENDING,false))return;
        CashCatalog.Catalog catalog=cashCatalog();
        if(catalog==null||catalog.itemCount(cashCart)<=0){
            prefs.edit().remove(PREF_CASH_PAYMENT_CONFIRM_PENDING).apply();
            return;
        }
        cashPaymentConfirmationShowing=true;
        new AlertDialog.Builder(this,dialogTheme())
                .setTitle(ui("Bezahlung erfolgreich?"))
                .setMessage(ui("Die App kann den Zahlungserfolg nicht automatisch prüfen. War die Zahlung des Warenkorbs erfolgreich? Bei Ja wird der Warenkorb geleert. Bei Nein bleibt er erhalten."))
                .setPositiveButton(ui("Ja"),(dialog,which)->{
                    cashPaymentConfirmationShowing=false;
                    clearCashCart();
                    Toast.makeText(this,ui("Warenkorb geleert."),Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(ui("Nein"),(dialog,which)->{
                    cashPaymentConfirmationShowing=false;
                    prefs.edit().remove(PREF_CASH_PAYMENT_CONFIRM_PENDING).apply();
                })
                .setCancelable(false)
                .show();
    }

    private void clearCashCart(){
        cashCart.clear();
        saveCashCart();
        if(prefs!=null)prefs.edit().remove(PREF_CASH_PAYMENT_CONFIRM_PENDING).apply();
        for(TextView quantity:cashQuantityViews.values())quantity.setText("0");
        updateCashSummary();
    }
'''
s=replace_once(s,old,new,'payment confirmation state and clear')

old='''    private void openTwintDirect(EditText amountInput){
        String a=amount(amountInput==null?null:amountInput.getText().toString());
        if(a==null){Toast.makeText(this,ui("Bitte einen gültigen CHF-Betrag eingeben oder das Feld leer lassen."),Toast.LENGTH_LONG).show();return;}
        if(!a.isBlank())copy("PFVR TWINT-Betrag",a,"CHF "+a+" "+(UiLanguage.isSwissGerman(uiMode())?"kopiert – uf de PFVR-Site iitrage.":"kopiert – auf der PFVR-Seite eintragen."));
        external(TWINT_DIRECT_URL);
    }

    private void sharePaymentQr(EditText amountInput){
'''
new='''    private void openTwintDirect(EditText amountInput){openTwintDirect(amountInput,false);}

    private void openTwintDirect(EditText amountInput,boolean fromCart){
        String a=amount(amountInput==null?null:amountInput.getText().toString());
        if(a==null){Toast.makeText(this,ui("Bitte einen gültigen CHF-Betrag eingeben oder das Feld leer lassen."),Toast.LENGTH_LONG).show();return;}
        if(!a.isBlank())copy("PFVR TWINT-Betrag",a,"CHF "+a+" "+(UiLanguage.isSwissGerman(uiMode())?"kopiert – uf de PFVR-Site iitrage.":"kopiert – auf der PFVR-Seite eintragen."));
        external(TWINT_DIRECT_URL,fromCart);
    }

    private void sharePaymentQr(EditText amountInput){sharePaymentQr(amountInput,false);}

    private void sharePaymentQr(EditText amountInput,boolean fromCart){
'''
s=replace_once(s,old,new,'twint and share origin propagation')

# Propagate cart origin through all successful external bank handoffs and preserve it in fallbacks.
s=replace_once(s,'                if(tryQrImageHandoff(preferred,uri,paymentText))return;\n','                if(tryQrImageHandoff(preferred,uri,paymentText,fromCart))return;\n','direct QR origin')
s=replace_once(s,'                    showBankFileImportFallback(qr,value,paymentText,preferred);\n','                    showBankFileImportFallback(qr,value,paymentText,preferred,fromCart);\n','file import origin')
s=replace_once(s,'                    launchPreferredBankWithCopiedData(preferred,paymentText,"QR-Bildübergabe wurde von dieser App nicht angeboten – Banking-App geöffnet und Zahlungsdaten kopiert.");\n','                    launchPreferredBankWithCopiedData(preferred,paymentText,"QR-Bildübergabe wurde von dieser App nicht angeboten – Banking-App geöffnet und Zahlungsdaten kopiert.",fromCart);\n','scan only origin')
s=replace_once(s,'                if(startIfResolvable(textShare)){\n','                if(startIfResolvable(textShare,fromCart)){\n','text share origin')
s=replace_once(s,'                launchPreferredBankWithCopiedData(preferred,paymentText,"Direkter QR-Import wurde von dieser App nicht angeboten – Zahlungsdaten wurden kopiert.");\n','                launchPreferredBankWithCopiedData(preferred,paymentText,"Direkter QR-Import wurde von dieser App nicht angeboten – Zahlungsdaten wurden kopiert.",fromCart);\n','fallback launch origin')
s=replace_once(s,'                showPaymentQr(amountInput);\n                return;\n            }\n            startActivity(Intent.createChooser(send,"Swiss QR an Banking-App übergeben"));\n','                showPaymentQr(amountInput,fromCart);\n                return;\n            }\n            startActivity(Intent.createChooser(send,"Swiss QR an Banking-App übergeben"));\n            if(fromCart)markCashPaymentConfirmationPending();\n','generic chooser origin')
s=replace_once(s,'            showPaymentQr(amountInput);\n        }\n    }\n\n    private Intent qrShareIntent','            showPaymentQr(amountInput,fromCart);\n        }\n    }\n\n    private Intent qrShareIntent','exception fallback origin')

old='''    private boolean startIfResolvable(Intent intent){
        try{
            if(intent.resolveActivity(getPackageManager())==null)return false;
            startActivity(intent);
            return true;
        }catch(Exception ignored){return false;}
    }

    private boolean tryQrImageHandoff(String preferred,Uri uri,String paymentText){
'''
new='''    private boolean startIfResolvable(Intent intent){return startIfResolvable(intent,false);}

    private boolean startIfResolvable(Intent intent,boolean fromCart){
        try{
            if(intent.resolveActivity(getPackageManager())==null)return false;
            startActivity(intent);
            if(fromCart)markCashPaymentConfirmationPending();
            return true;
        }catch(Exception ignored){return false;}
    }

    private boolean tryQrImageHandoff(String preferred,Uri uri,String paymentText,boolean fromCart){
'''
s=replace_once(s,old,new,'start helper origin propagation')

# Exactly three payment handoff starts inside tryQrImageHandoff.
needle='startIfResolvable(direct)'
if s.count(needle)!=1: raise SystemExit(f'direct start helper expected once, got {s.count(needle)}')
s=s.replace(needle,'startIfResolvable(direct,fromCart)',1)
needle='startIfResolvable(imageView)'
if s.count(needle)!=1: raise SystemExit(f'image view start helper expected once, got {s.count(needle)}')
s=s.replace(needle,'startIfResolvable(imageView,fromCart)',1)
needle='startIfResolvable(genericImage)'
if s.count(needle)!=1: raise SystemExit(f'generic image start helper expected once, got {s.count(needle)}')
s=s.replace(needle,'startIfResolvable(genericImage,fromCart)',1)

old='''    private void launchPreferredBankWithCopiedData(String preferred,String paymentText,String reason){
        Intent launch=getPackageManager().getLaunchIntentForPackage(preferred);
        if(launch==null){
            Toast.makeText(this,ui("Die gewählte Banking-App ist nicht mehr verfügbar."),Toast.LENGTH_LONG).show();
            openPaymentSettings();
            return;
        }
        copy("PFVR Zahlung",paymentText,"Zahlungsdaten kopiert");
        startActivity(launch);
        Toast.makeText(this,ui(reason),Toast.LENGTH_LONG).show();
    }

    private void showBankFileImportFallback(Bitmap qr,String value,String paymentText,String preferred){
'''
new='''    private void launchPreferredBankWithCopiedData(String preferred,String paymentText,String reason){launchPreferredBankWithCopiedData(preferred,paymentText,reason,false);}

    private void launchPreferredBankWithCopiedData(String preferred,String paymentText,String reason,boolean fromCart){
        Intent launch=getPackageManager().getLaunchIntentForPackage(preferred);
        if(launch==null){
            Toast.makeText(this,ui("Die gewählte Banking-App ist nicht mehr verfügbar."),Toast.LENGTH_LONG).show();
            openPaymentSettings();
            return;
        }
        copy("PFVR Zahlung",paymentText,"Zahlungsdaten kopiert");
        startActivity(launch);
        if(fromCart)markCashPaymentConfirmationPending();
        Toast.makeText(this,ui(reason),Toast.LENGTH_LONG).show();
    }

    private void showBankFileImportFallback(Bitmap qr,String value,String paymentText,String preferred,boolean fromCart){
'''
s=replace_once(s,old,new,'launch and file fallback origin')
s=replace_once(s,'                .setPositiveButton(ui("Banking-App öffnen"),(d,w)->launchPreferredBankWithCopiedData(preferred,paymentText,"Zahlungsdaten kopiert – QR-Datei bei Bedarf in der Banking-App auswählen."))\n','                .setPositiveButton(ui("Banking-App öffnen"),(d,w)->launchPreferredBankWithCopiedData(preferred,paymentText,"Zahlungsdaten kopiert – QR-Datei bei Bedarf in der Banking-App auswählen.",fromCart))\n','file fallback button origin')

old='''    private void showPaymentQr(EditText amountInput) {
        String a=amount(amountInput==null?null:amountInput.getText().toString());
'''
new='''    private void showPaymentQr(EditText amountInput){showPaymentQr(amountInput,false);}

    private void showPaymentQr(EditText amountInput,boolean fromCart) {
        String a=amount(amountInput==null?null:amountInput.getText().toString());
'''
s=replace_once(s,old,new,'QR dialog origin overload')
s=replace_once(s,'                    .setPositiveButton(ui("Direkt an Banking-App"),(d,w)->sharePaymentQr(amountInput))\n','                    .setPositiveButton(ui("Direkt an Banking-App"),(d,w)->sharePaymentQr(amountInput,fromCart))\n','QR dialog handoff origin')

# external() overload is used only by explicitly cart-origin TWINT flow; all old callers stay one-arg.
old='''    private void external(String url){
        try{
            Uri uri=Uri.parse(url);
            if(!AppLinkPolicy.mayOpenExternally(uri.getScheme())){Toast.makeText(this,ui("Dieser Linktyp wird aus Sicherheitsgründen nicht geöffnet."),Toast.LENGTH_SHORT).show();return;}
            startActivity(new Intent(Intent.ACTION_VIEW,uri));
        }catch(Exception e){Toast.makeText(this,ui("Link konnte nicht geöffnet werden."),Toast.LENGTH_SHORT).show();}
    }
'''
new='''    private void external(String url){external(url,false);}
    private boolean external(String url,boolean fromCart){
        try{
            Uri uri=Uri.parse(url);
            if(!AppLinkPolicy.mayOpenExternally(uri.getScheme())){Toast.makeText(this,ui("Dieser Linktyp wird aus Sicherheitsgründen nicht geöffnet."),Toast.LENGTH_SHORT).show();return false;}
            startActivity(new Intent(Intent.ACTION_VIEW,uri));
            if(fromCart)markCashPaymentConfirmationPending();
            return true;
        }catch(Exception e){Toast.makeText(this,ui("Link konnte nicht geöffnet werden."),Toast.LENGTH_SHORT).show();return false;}
    }
'''
s=replace_once(s,old,new,'external origin overload')
path.write_text(s)

# --- Swiss-German localization ---
path=Path("Android/app/src/main/java/ch/pfvr/internapp/UiLanguage.java")
s=path.read_text()
anchor='''        put("Warenkorb leeren", "Warenchorb leere");
'''
entry=anchor+'''        put("Bezahlung erfolgreich?", "Zahlig erfolgreich?");
        put("Die App kann den Zahlungserfolg nicht automatisch prüfen. War die Zahlung des Warenkorbs erfolgreich? Bei Ja wird der Warenkorb geleert. Bei Nein bleibt er erhalten.", "D App cha de Zahligserfolg nöd automatisch prüefe. Isch d Zahlig vom Warenchorb erfolgreich gsi? Bi Ja wird de Warenchorb gleert. Bi Nei blibt er erhalte.");
        put("Ja", "Ja");
        put("Nein", "Nei");
        put("Warenkorb geleert.", "Warenchorb gleert.");
'''
s=replace_once(s,anchor,entry,'payment confirmation translations')
path.write_text(s)

# --- Regression test ---
Path("Android/app/src/test/java/ch/pfvr/internapp/CashPaymentConfirmationSourceTest.java").write_text(r'''package ch.pfvr.internapp;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CashPaymentConfirmationSourceTest {
    private static String source() throws Exception {
        String relative="src/main/java/ch/pfvr/internapp/MainActivity.java";
        Path[] candidates={Paths.get(relative),Paths.get("app",relative),Paths.get("Android","app",relative)};
        for(Path candidate:candidates)if(Files.isRegularFile(candidate))return new String(Files.readAllBytes(candidate), StandardCharsets.UTF_8);
        throw new IllegalStateException("MainActivity.java not found");
    }

    @Test public void onlyCartPaymentEntryPointsCarryConfirmationOrigin() throws Exception {
        String activity=source();
        assertTrue(activity.contains("PREF_CASH_PAYMENT_CONFIRM_PENDING = \"cash_payment_confirm_pending_v1\""));
        assertTrue(activity.contains("payWithPreferredBank(input,true)"));
        assertTrue(activity.contains("showPaymentQr(input,true)"));
        assertTrue(activity.contains("openTwintDirect(input,true)"));
        assertTrue(activity.contains("direct.setOnClickListener(v->payWithPreferredBank(amount))"));
        assertTrue(activity.contains("qr.setOnClickListener(v->showPaymentQr(amount))"));
        assertTrue(activity.contains("direct.setOnClickListener(v->openTwintDirect(cashOptionalAmountInput()))"));
    }

    @Test public void successfulExternalHandoffArmsPersistentQuestion() throws Exception {
        String activity=source();
        assertTrue(activity.contains("private boolean startIfResolvable(Intent intent,boolean fromCart)"));
        assertTrue(activity.contains("startActivity(intent);\n            if(fromCart)markCashPaymentConfirmationPending();"));
        assertTrue(activity.contains("startActivity(launch);\n        if(fromCart)markCashPaymentConfirmationPending();"));
        assertTrue(activity.contains("startActivity(new Intent(Intent.ACTION_VIEW,uri));\n            if(fromCart)markCashPaymentConfirmationPending();"));
        assertTrue(activity.contains("startActivity(Intent.createChooser(send,\"Swiss QR an Banking-App übergeben\"));\n            if(fromCart)markCashPaymentConfirmationPending();"));
    }

    @Test public void resumeAsksUserAndOnlyYesClearsCart() throws Exception {
        String activity=source();
        assertTrue(activity.contains("post(this::maybeShowCashPaymentConfirmation)"));
        int start=activity.indexOf("private void maybeShowCashPaymentConfirmation()");
        int end=activity.indexOf("private void clearCashCart()",start);
        assertTrue(start>=0&&end>start);
        String dialog=activity.substring(start,end);
        assertTrue(dialog.contains("setTitle(ui(\"Bezahlung erfolgreich?\"))"));
        assertTrue(dialog.contains("setPositiveButton(ui(\"Ja\")"));
        assertTrue(dialog.contains("clearCashCart();"));
        assertTrue(dialog.contains("setNegativeButton(ui(\"Nein\")"));
        assertTrue(dialog.contains("remove(PREF_CASH_PAYMENT_CONFIRM_PENDING)"));
        assertTrue(dialog.contains("setCancelable(false)"));
        String negative=dialog.substring(dialog.indexOf("setNegativeButton"));
        assertFalse(negative.contains("clearCashCart();"));
    }

    @Test public void manualCartClearAlsoCancelsStaleQuestion() throws Exception {
        String activity=source();
        int start=activity.indexOf("private void clearCashCart()");
        int end=activity.indexOf("private View cashSummaryRow",start);
        assertTrue(start>=0&&end>start);
        assertTrue(activity.substring(start,end).contains("remove(PREF_CASH_PAYMENT_CONFIRM_PENDING)"));
    }
}
''')

# --- Version/docs ---
path=Path("Android/app/build.gradle")
s=path.read_text()
s=replace_once(s,"versionCode 61","versionCode 62","version code")
s=replace_once(s,"versionName '0.12.4'","versionName '0.12.5'","version name")
path.write_text(s)

path=Path("PROJECT.md")
s=path.read_text()
s=replace_once(
    s,
    '- Vereinsbeiz: fixierter, dauerhaft lokal gespeicherter Warenkorb, der App-/Prozessneustarts übersteht und nur durch `Warenkorb leeren` zurückgesetzt wird; dazu anordenbare Kategorien Trinken/Essen/Feiern, freier Betrag, Swiss-QR-Zahlung, direkte Android-Übergabe an eine unter Einstellungen → Zahlung gewählte Banking-App und TWINT-Zahlungsweg.\n',
    '- Vereinsbeiz: fixierter, dauerhaft lokal gespeicherter Warenkorb, der App-/Prozessneustarts übersteht. Nach einer aus dem Warenkorb gestarteten externen Bank-/TWINT-Zahlung fragt die App beim Zurückkehren ausdrücklich nach dem Erfolg; `Ja` leert den Warenkorb, `Nein` lässt ihn unverändert. Ohne diese Bestätigung wird der Warenkorb nur durch `Warenkorb leeren` zurückgesetzt. Dazu kommen anordenbare Kategorien Trinken/Essen/Feiern, freier Betrag, Swiss-QR-Zahlung, direkte Android-Übergabe an eine unter Einstellungen → Zahlung gewählte Banking-App und TWINT-Zahlungsweg.\n',
    'project cart payment confirmation'
)
path.write_text(s)

path=Path("STATUS.md")
s=path.read_text()
s=replace_once(s,'Stand: Testversion `0.12.4` · aktualisiert 2026-09-09.','Stand: Testversion `0.12.5` · aktualisiert 2026-09-10.','status version')
anchor='## Aktueller Teststand\n\n'
entry='- `0.12.5` ergänzt für Zahlungen, die ausdrücklich aus dem Warenkorb an eine externe Banking-App, einen Android-Zahlungs-Chooser oder den TWINT-Webweg übergeben wurden, eine persistente Rückkehrbestätigung. Beim nächsten Zurückkehren fragt die App, ob die Zahlung erfolgreich war; `Ja` leert und speichert den leeren Warenkorb, `Nein` verwirft nur die offene Bestätigungsfrage und lässt den Warenkorb unverändert. Die App behauptet dabei keinen automatisch verifizierten Zahlungserfolg. Der offene Bestätigungsstatus übersteht auch einen Prozessneustart und wird bei manuellem `Warenkorb leeren` entfernt. Freie Beträge und andere nicht aus dem Warenkorb gestartete Zahlungen lösen die Frage nicht aus.\n'
if anchor not in s: raise SystemExit('STATUS anchor missing')
s=s.replace(anchor,anchor+entry,1)
path.write_text(s)
