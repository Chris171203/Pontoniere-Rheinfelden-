package ch.pfvr.internapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BankingAppRegistryTest {
    @Test public void knownCapabilitiesAreConservative(){
        assertEquals(BankingAppRegistry.Capability.DIRECT_SHARE,
                BankingAppRegistry.profile("com.yuh","Yuh",false).capability);
        assertEquals(BankingAppRegistry.Capability.FILE_IMPORT,
                BankingAppRegistry.profile("com.neonbanking.app","neon",false).capability);
        assertEquals(BankingAppRegistry.Capability.SCAN_ONLY,
                BankingAppRegistry.profile("com.revolut.revolut","Revolut",false).capability);
        assertEquals(BankingAppRegistry.Capability.SCAN_ONLY,
                BankingAppRegistry.profile("de.fiduciagad.banking.vr","VR Banking",false).capability);
    }

    @Test public void runtimeImageShareOverridesStaticFallback(){
        assertEquals(BankingAppRegistry.Capability.DIRECT_SHARE,
                BankingAppRegistry.profile("com.revolut.revolut","Revolut",true).capability);
        assertEquals(BankingAppRegistry.Capability.DIRECT_SHARE,
                BankingAppRegistry.profile("unknown.bank.app","Meine Bank",true).capability);
    }

    @Test public void bankingHeuristicFindsRelevantAppsButNotTwint(){
        assertTrue(BankingAppRegistry.looksLikeBankingApp("neon com.neonbanking.app"));
        assertTrue(BankingAppRegistry.looksLikeBankingApp("VR Banking de.fiduciagad.banking.vr"));
        assertTrue(!BankingAppRegistry.looksLikeBankingApp("Yuh TWINT com.yuh.twint"));
    }
}
