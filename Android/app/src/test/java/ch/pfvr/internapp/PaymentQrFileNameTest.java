package ch.pfvr.internapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PaymentQrFileNameTest {
    @Test public void amountBecomesSearchableReusableFilename(){
        assertEquals("PFVR_12.50CHF.png",PaymentQrFileName.forAmount("12.50"));
        assertEquals("PFVR_5.00CHF.png",PaymentQrFileName.forAmount("5.00"));
    }

    @Test public void openAmountGetsStableFilename(){
        assertEquals("PFVR_offenCHF.png",PaymentQrFileName.forAmount(""));
        assertEquals("PFVR_offenCHF.png",PaymentQrFileName.forAmount(null));
    }
}
