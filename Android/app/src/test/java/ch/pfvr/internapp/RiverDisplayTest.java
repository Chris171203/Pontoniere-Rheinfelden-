package ch.pfvr.internapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RiverDisplayTest {
    @Test public void convertsBaselAbsoluteWaterLevelToOperationalGaugeCentimetres(){
        assertEquals(720.0, RiverDisplay.levelValue(HydroStation.BASEL_RHEINHALLE,247.20), 0.0001);
        assertEquals(500.0, RiverDisplay.levelValue(HydroStation.BASEL_RHEINHALLE,245.00), 0.0001);
        assertEquals("cm", RiverDisplay.levelUnit(HydroStation.BASEL_RHEINHALLE));
        assertEquals(0, RiverDisplay.levelDecimals(HydroStation.BASEL_RHEINHALLE));
    }

    @Test public void keepsRheinfeldenWaterLevelAsAbsoluteMetresAboveSea(){
        assertEquals(268.34, RiverDisplay.levelValue(HydroStation.RHEINFELDEN,268.34), 0.0001);
        assertEquals("m ü.M.", RiverDisplay.levelUnit(HydroStation.RHEINFELDEN));
        assertEquals(2, RiverDisplay.levelDecimals(HydroStation.RHEINFELDEN));
    }

    @Test public void preservesMissingValues(){
        assertTrue(Double.isNaN(RiverDisplay.levelValue(HydroStation.BASEL_RHEINHALLE,Double.NaN)));
    }
}
