package ch.pfvr.internapp;

import org.junit.Test;
import static org.junit.Assert.*;

public class PaymentAmountTest {
    @Test public void acceptsExactCentAmountsAndOpenAmount(){
        assertEquals("",PaymentAmount.normalize(""));
        assertEquals("",PaymentAmount.normalize("0,00"));
        assertEquals("12.50",PaymentAmount.normalize("12,5"));
        assertEquals("100000.00",PaymentAmount.normalize("100000"));
    }

    @Test public void rejectsSilentRoundingOrNonFiniteInputs(){
        for(String input:new String[]{"0.001","0.005","1.999","100000.01","NaN","Infinity","1e2","-1"})
            assertNull(input,PaymentAmount.normalize(input));
    }
}
