package ch.pfvr.internapp;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class CashCartPersistenceSourceTest {
    private static String source() throws Exception {
        String relative="src/main/java/ch/pfvr/internapp/MainActivity.java";
        Path[] candidates={Paths.get(relative),Paths.get("app",relative),Paths.get("Android","app",relative)};
        for(Path candidate:candidates)if(Files.isRegularFile(candidate))return new String(Files.readAllBytes(candidate), StandardCharsets.UTF_8);
        throw new IllegalStateException("MainActivity.java not found");
    }

    @Test public void cartLoadsOnStartupAndPersistsEveryQuantityChange() throws Exception {
        String activity=source();
        assertTrue(activity.contains("PREF_CASH_CART = \"cash_cart_v1\""));
        assertTrue(activity.contains("tileLayoutStore = new TileLayoutStore(prefs);\n        loadCashCart();"));
        assertTrue(activity.contains("saveCashCart();\n        TextView view=cashQuantityViews.get(itemId)"));
        assertTrue(activity.contains("clearCart.setOnClickListener(v->clearCashCart())"));
        assertTrue(activity.contains("putStringSet(PREF_CASH_CART,CashCartState.encode(cashCart))"));
    }
}
