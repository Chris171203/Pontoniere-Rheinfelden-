package ch.pfvr.internapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class HydroMathTest {
    @Test
    public void roundsAxisToReadableValues() {
        HydroMath.AxisScale flow = HydroMath.niceAxis(Arrays.asList(406d, 686d));
        assertEquals(400d, flow.min, 0.0001);
        assertEquals(700d, flow.max, 0.0001);

        HydroMath.AxisScale temperature = HydroMath.niceAxis(Arrays.asList(21.63d, 22.93d));
        assertEquals(21.5d, temperature.min, 0.0001);
        assertEquals(23.0d, temperature.max, 0.0001);

        HydroMath.AxisScale level = HydroMath.niceAxis(Arrays.asList(261.03d, 261.83d));
        assertEquals(261.0d, level.min, 0.0001);
        assertEquals(262.0d, level.max, 0.0001);
    }

    @Test
    public void calculatesSeriesStatsIgnoringInvalidValues() {
        HydroMath.Stats stats = HydroMath.stats(Arrays.asList(10d, Double.NaN, 14d, 12d));
        assertTrue(stats.isValid());
        assertEquals(3, stats.count);
        assertEquals(10d, stats.first, 0.0001);
        assertEquals(12d, stats.last, 0.0001);
        assertEquals(2d, stats.change(), 0.0001);
        assertEquals(10d, stats.min, 0.0001);
        assertEquals(14d, stats.max, 0.0001);
        assertEquals(12d, stats.mean, 0.0001);
        assertFalse(HydroMath.stats(Collections.emptyList()).isValid());
    }

    @Test
    public void findsNearestTimePoint() {
        assertEquals(0, HydroMath.nearestIndex(Arrays.asList(100L, 200L, 300L), 110L));
        assertEquals(1, HydroMath.nearestIndex(Arrays.asList(100L, 200L, 300L), 240L));
        assertEquals(2, HydroMath.nearestIndex(Arrays.asList(100L, 200L, 300L), 290L));
        assertEquals(-1, HydroMath.nearestIndex(Collections.emptyList(), 100L));
    }

    @Test
    public void exposesExpectedPeriodWindows() {
        assertEquals(3_600_000L, HydroMath.periodMillis("1h"));
        assertEquals(86_400_000L, HydroMath.periodMillis("24h"));
        assertEquals(604_800_000L, HydroMath.periodMillis("7d"));
    }
}
