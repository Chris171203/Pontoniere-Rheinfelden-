package ch.pfvr.internapp;

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
