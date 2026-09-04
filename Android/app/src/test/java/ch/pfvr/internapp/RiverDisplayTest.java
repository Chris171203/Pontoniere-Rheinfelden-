package ch.pfvr.internapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RiverDisplayTest {
    @Test public void keepsBothStationWaterLevelsAsAbsoluteMetresAboveSea(){
        assertEquals(245.00, RiverDisplay.levelValue(HydroStation.BASEL_RHEINHALLE,245.00), 0.0001);
        assertEquals(261.30, RiverDisplay.levelValue(HydroStation.RHEINFELDEN,261.30), 0.0001);
        assertEquals("m ü.M.", RiverDisplay.levelUnit(HydroStation.BASEL_RHEINHALLE));
        assertEquals("m ü.M.", RiverDisplay.levelUnit(HydroStation.RHEINFELDEN));
    }

    @Test public void onlyBaselHasVerifiedRelativeGaugeCentimetres(){
        assertTrue(RiverDisplay.hasVerifiedGaugeCentimetres(HydroStation.BASEL_RHEINHALLE));
        assertFalse(RiverDisplay.hasVerifiedGaugeCentimetres(HydroStation.RHEINFELDEN));
        assertEquals(720.0, RiverDisplay.gaugeCentimetres(HydroStation.BASEL_RHEINHALLE,247.20), 0.0001);
        assertTrue(Double.isNaN(RiverDisplay.gaugeCentimetres(HydroStation.RHEINFELDEN,261.30)));
    }

    @Test public void graphCentimetreModeFallsBackToAbsoluteMetresWhenUnsupported(){
        assertEquals(720.0, RiverDisplay.graphLevelValue(HydroStation.BASEL_RHEINHALLE,247.20,true), 0.0001);
        assertEquals("cm", RiverDisplay.graphLevelUnit(HydroStation.BASEL_RHEINHALLE,true));
        assertEquals(0, RiverDisplay.graphLevelDecimals(HydroStation.BASEL_RHEINHALLE,true));
        assertEquals(261.30, RiverDisplay.graphLevelValue(HydroStation.RHEINFELDEN,261.30,true), 0.0001);
        assertEquals("m ü.M.", RiverDisplay.graphLevelUnit(HydroStation.RHEINFELDEN,true));
        assertEquals(2, RiverDisplay.graphLevelDecimals(HydroStation.RHEINFELDEN,true));
    }

    @Test public void preservesMissingValues(){
        assertTrue(Double.isNaN(RiverDisplay.levelValue(HydroStation.BASEL_RHEINHALLE,Double.NaN)));
        assertTrue(Double.isNaN(RiverDisplay.gaugeCentimetres(HydroStation.BASEL_RHEINHALLE,Double.NaN)));
    }
}
