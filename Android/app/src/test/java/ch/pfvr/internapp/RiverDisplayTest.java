package ch.pfvr.internapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RiverDisplayTest {
    @Test public void keepsBothStationWaterLevelsAsAbsoluteMetresAboveSea(){
        assertEquals(245.00, RiverDisplay.levelValue(HydroStation.BASEL_RHEINHALLE,245.00), 0.0001);
        assertEquals(261.30, RiverDisplay.levelValue(HydroStation.RHEINFELDEN,261.30), 0.0001);
        assertEquals("m ü.M.", RiverDisplay.levelUnit(HydroStation.BASEL_RHEINHALLE));
        assertEquals("m ü.M.", RiverDisplay.levelUnit(HydroStation.RHEINFELDEN));
        assertEquals(2, RiverDisplay.levelDecimals(HydroStation.BASEL_RHEINHALLE));
        assertEquals(2, RiverDisplay.levelDecimals(HydroStation.RHEINFELDEN));
    }

    @Test public void derivesRelativeGaugeCentimetresForBothStations(){
        assertEquals(720.0, RiverDisplay.gaugeCentimetres(HydroStation.BASEL_RHEINHALLE,247.20), 0.0001);
        assertEquals(500.0, RiverDisplay.gaugeCentimetres(HydroStation.BASEL_RHEINHALLE,245.00), 0.0001);
        assertEquals(130.0, RiverDisplay.gaugeCentimetres(HydroStation.RHEINFELDEN,261.30), 0.0001);
        assertEquals(153.0, RiverDisplay.gaugeCentimetres(HydroStation.RHEINFELDEN,261.53), 0.0001);
    }

    @Test public void preservesMissingValues(){
        assertTrue(Double.isNaN(RiverDisplay.levelValue(HydroStation.BASEL_RHEINHALLE,Double.NaN)));
        assertTrue(Double.isNaN(RiverDisplay.gaugeCentimetres(HydroStation.RHEINFELDEN,Double.NaN)));
    }
}
