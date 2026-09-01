package ch.pfvr.internapp;

import java.util.List;

/** Pure hydrology chart/statistics helpers independent from Android UI. */
final class HydroMath {
    private HydroMath() {}

    static final class AxisScale {
        final double min;
        final double max;
        final double step;

        AxisScale(double min, double max, double step) {
            this.min = min;
            this.max = max;
            this.step = step;
        }
    }

    static final class Stats {
        final int count;
        final double first;
        final double last;
        final double min;
        final double max;
        final double mean;

        Stats(int count, double first, double last, double min, double max, double mean) {
            this.count = count;
            this.first = first;
            this.last = last;
            this.min = min;
            this.max = max;
            this.mean = mean;
        }

        double change() { return last - first; }

        boolean isValid() {
            return count > 0 && Double.isFinite(first) && Double.isFinite(last)
                    && Double.isFinite(min) && Double.isFinite(max) && Double.isFinite(mean);
        }
    }

    static AxisScale niceAxis(List<Double> values) {
        double dataMin = Double.POSITIVE_INFINITY;
        double dataMax = Double.NEGATIVE_INFINITY;
        for (Double value : values) {
            if (value == null || !Double.isFinite(value)) continue;
            dataMin = Math.min(dataMin, value);
            dataMax = Math.max(dataMax, value);
        }
        if (!Double.isFinite(dataMin) || !Double.isFinite(dataMax)) return new AxisScale(0, 1, 1);
        if (!(dataMax > dataMin)) {
            double magnitude = Math.abs(dataMin);
            double half = Math.max(magnitude * 0.02,
                    magnitude >= 100 ? 10 : (magnitude >= 10 ? 1 : 0.1));
            dataMin -= half;
            dataMax += half;
        }
        double step = niceStep((dataMax - dataMin) / 4d);
        double min = Math.floor(dataMin / step + 1e-9) * step;
        double max = Math.ceil(dataMax / step - 1e-9) * step;
        if (!(max > min)) {
            min -= step;
            max += step;
        }
        return new AxisScale(normalizeZero(min), normalizeZero(max), step);
    }

    static Stats stats(List<Double> values) {
        int count = 0;
        double first = Double.NaN;
        double last = Double.NaN;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0;
        for (Double value : values) {
            if (value == null || !Double.isFinite(value)) continue;
            if (count == 0) first = value;
            last = value;
            min = Math.min(min, value);
            max = Math.max(max, value);
            sum += value;
            count++;
        }
        if (count == 0) return new Stats(0, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        return new Stats(count, first, last, min, max, sum / count);
    }

    static int nearestIndex(List<Long> times, long target) {
        if (times == null || times.isEmpty()) return -1;
        int lo = 0;
        int hi = times.size() - 1;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (times.get(mid) < target) lo = mid + 1;
            else hi = mid;
        }
        if (lo == 0) return 0;
        long upper = Math.abs(times.get(lo) - target);
        long lower = Math.abs(times.get(lo - 1) - target);
        return lower <= upper ? lo - 1 : lo;
    }

    static long periodMillis(String code) {
        if ("1h".equals(code)) return 60L * 60L * 1000L;
        if ("7d".equals(code)) return 7L * 24L * 60L * 60L * 1000L;
        return 24L * 60L * 60L * 1000L;
    }

    private static double niceStep(double raw) {
        if (!Double.isFinite(raw) || raw <= 0) return 1;
        double exponent = Math.floor(Math.log10(raw));
        double base = Math.pow(10, exponent);
        double fraction = raw / base;
        double nice;
        if (fraction <= 1) nice = 1;
        else if (fraction <= 2) nice = 2;
        else if (fraction <= 5) nice = 5;
        else nice = 10;
        return nice * base;
    }

    private static double normalizeZero(double value) {
        return Math.abs(value) < 1e-12 ? 0 : value;
    }
}
