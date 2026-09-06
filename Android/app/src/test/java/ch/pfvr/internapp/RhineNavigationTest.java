package ch.pfvr.internapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RhineNavigationTest {
    @Test public void appliesOfficialBaselGaugeThresholdsAtExactBoundaries(){
        assertEquals(RhineNavigation.Stage.NORMAL,RhineNavigation.fromBaselGaugeCm(699.9));
        assertEquals(RhineNavigation.Stage.HWM_I,RhineNavigation.fromBaselGaugeCm(700.0));
        assertEquals(RhineNavigation.Stage.HWM_I,RhineNavigation.fromBaselGaugeCm(789.9));
        assertEquals(RhineNavigation.Stage.HWM_IIB,RhineNavigation.fromBaselGaugeCm(790.0));
        assertEquals(RhineNavigation.Stage.HWM_IIB,RhineNavigation.fromBaselGaugeCm(819.9));
        assertEquals(RhineNavigation.Stage.HWM_IIA,RhineNavigation.fromBaselGaugeCm(820.0));
    }

    @Test public void convertsOfficialMarkersForBothGraphUnits(){
        assertEquals(700.0,RhineNavigation.thresholdGraphValue(RhineNavigation.Stage.HWM_I,true),0.0001);
        assertEquals(247.00,RhineNavigation.thresholdGraphValue(RhineNavigation.Stage.HWM_I,false),0.0001);
        assertEquals(247.90,RhineNavigation.thresholdGraphValue(RhineNavigation.Stage.HWM_IIB,false),0.0001);
        assertEquals(248.20,RhineNavigation.thresholdGraphValue(RhineNavigation.Stage.HWM_IIA,false),0.0001);
    }

    @Test public void reportsUnknownWhenBaselGaugeIsMissing(){
        assertEquals(RhineNavigation.Stage.UNKNOWN,RhineNavigation.fromBaselGaugeCm(Double.NaN));
        assertTrue(RhineNavigation.detail(RhineNavigation.Stage.HWM_IIB).contains("Kleinschifffahrt"));
        assertTrue(RhineNavigation.detail(RhineNavigation.Stage.HWM_IIA).contains("Rheinfelden"));
    }

    @Test public void currentNavigationRejectsOldMeasurementOrOldCache(){
        long now=10_000_000L;
        long fresh=now-30L*60L*1000L;
        long tooOld=now-RhineNavigation.MAX_CURRENT_AGE_MS-1L;
        assertTrue(RhineNavigation.isCurrent(fresh,fresh,now));
        assertFalse(RhineNavigation.isCurrent(tooOld,fresh,now));
        assertFalse(RhineNavigation.isCurrent(fresh,tooOld,now));
        assertFalse(RhineNavigation.isCurrent(0L,fresh,now));
        assertFalse(RhineNavigation.isCurrent(now+1L,fresh,now));
        assertEquals(RhineNavigation.Stage.HWM_IIB,RhineNavigation.fromCurrentBaselGaugeCm(790.0,fresh,fresh,now));
        assertEquals(RhineNavigation.Stage.UNKNOWN,RhineNavigation.fromCurrentBaselGaugeCm(790.0,tooOld,fresh,now));
    }
}
