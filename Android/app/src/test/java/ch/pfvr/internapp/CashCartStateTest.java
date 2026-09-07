package ch.pfvr.internapp;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CashCartStateTest {
    @Test public void roundTripsCartAcrossProcessLifetime(){
        Map<String,Integer> cart=new LinkedHashMap<>();
        cart.put("soft_5dl",2);
        cart.put("food_other",3);
        Map<String,Integer> restored=CashCartState.decode(CashCartState.encode(cart));
        assertEquals(cart,restored);
    }

    @Test public void ignoresEmptyAndCorruptEntriesAndClampsQuantity(){
        Map<String,Integer> restored=CashCartState.decode(Set.of("soft_5dl=120","bad","=4","food_other=x"));
        assertEquals(Integer.valueOf(99),restored.get("soft_5dl"));
        assertEquals(1,restored.size());
        assertFalse(restored.containsKey("food_other"));
    }
}
