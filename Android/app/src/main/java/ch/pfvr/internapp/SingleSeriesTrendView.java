package ch.pfvr.internapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Compact single-series trend chart used for measurements that should not
 * share an axis with another physical quantity, such as water temperature.
 *
 * <p>This view is instantiated programmatically only; it is never inflated
 * from XML, so an XML-inflation constructor is intentionally not provided.</p>
 */
@SuppressLint("ViewConstructor")
final class SingleSeriesTrendView extends View {
    private final List<Long> times;
    private final List<Double> values;
    private final boolean weekRange;
    private final String rangeLabel;
    private final String seriesLabel;
    private final String unit;
    private final int decimals;
    private final int lineColor;
    private final int gridColor;
    private final int labelColor;
    private final int surfaceColor;
    private final int tooltipColor;
    private final int tooltipTextColor;
    private final ZoneId zone = ZoneId.of("Europe/Zurich");
    private final DateTimeFormatter timeFormatter;
    private final DateTimeFormatter tooltipFormatter;

    private final Paint grid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint point = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint crosshair = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltip = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path seriesPath = new Path();
    private final Path areaPath = new Path();
    private final RectF tooltipBox = new RectF();
    private int selectedIndex = -1;

    SingleSeriesTrendView(
            Context context,
            List<Long> times,
            List<Double> values,
            boolean weekRange,
            String rangeLabel,
            String seriesLabel,
            String unit,
            int decimals,
            int lineColor,
            int gridColor,
            int labelColor,
            int surfaceColor,
            int tooltipColor,
            int tooltipTextColor
    ) {
        super(context);
        this.times = new ArrayList<>(times);
        this.values = new ArrayList<>(values);
        this.weekRange = weekRange;
        this.rangeLabel = rangeLabel;
        this.seriesLabel = seriesLabel;
        this.unit = unit;
        this.decimals = decimals;
        this.lineColor = lineColor;
        this.gridColor = gridColor;
        this.labelColor = labelColor;
        this.surfaceColor = surfaceColor;
        this.tooltipColor = tooltipColor;
        this.tooltipTextColor = tooltipTextColor;
        this.timeFormatter = weekRange
                ? DateTimeFormatter.ofPattern("EE dd.", Locale.GERMAN)
                : DateTimeFormatter.ofPattern("HH:mm", Locale.GERMAN);
        this.tooltipFormatter = weekRange
                ? DateTimeFormatter.ofPattern("EE dd.MM. · HH:mm", Locale.GERMAN)
                : DateTimeFormatter.ofPattern("HH:mm", Locale.GERMAN);

        setClickable(true);
        setFocusable(true);
        grid.setStrokeWidth(dp(1));
        label.setTextSize(sp(9));
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(dp(2.4f));
        line.setStrokeCap(Paint.Cap.ROUND);
        line.setStrokeJoin(Paint.Join.ROUND);
        fill.setStyle(Paint.Style.FILL);
        point.setStyle(Paint.Style.FILL);
        crosshair.setStrokeWidth(dp(1));
        tooltip.setStyle(Paint.Style.FILL);
        tooltipText.setTextSize(sp(10));
        tooltipText.setTypeface(Typeface.DEFAULT_BOLD);
        setContentDescription(seriesLabel + " · " + rangeLabel);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (times.size() < 2 || times.size() != values.size()) {
            return super.onTouchEvent(event);
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP) {
            float left = dp(52);
            float right = getWidth() - dp(10);
            float x = Math.max(left, Math.min(right, event.getX()));
            long minTime = times.get(0);
            long maxTime = times.get(times.size() - 1);
            long target = minTime + Math.round((maxTime - minTime) * (x - left) / Math.max(1f, right - left));
            selectedIndex = HydroMath.nearestIndex(times, target);
            invalidate();
            if (action == MotionEvent.ACTION_UP) performClick();
            return true;
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            selectedIndex = -1;
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values.size() < 2 || times.size() != values.size()) return;

        float left = dp(52);
        float right = getWidth() - dp(10);
        float top = dp(17);
        float bottom = getHeight() - dp(30);
        long minTime = times.get(0);
        long maxTime = times.get(times.size() - 1);
        if (maxTime <= minTime) maxTime = minTime + 1;
        HydroMath.AxisScale scale = HydroMath.niceAxis(values);

        grid.setColor(gridColor);
        label.setColor(labelColor);
        for (int index = 0; index <= 4; index++) {
            float y = top + (bottom - top) * index / 4f;
            canvas.drawLine(left, y, right, y, grid);
            double axisValue = scale.max - (scale.max - scale.min) * index / 4d;
            String axisText = axisLabel(axisValue, scale.step);
            canvas.drawText(axisText, left - dp(5) - label.measureText(axisText), y + dp(3), label);
        }
        drawTimeGrid(canvas, left, right, top, bottom, minTime, maxTime);

        seriesPath.reset();
        boolean started = false;
        int lastFinite = -1;
        for (int index = 0; index < values.size(); index++) {
            double value = values.get(index);
            if (!Double.isFinite(value)) continue;
            float x = seriesX(index, left, right, minTime, maxTime);
            float y = seriesY(value, scale, top, bottom);
            if (!started) {
                seriesPath.moveTo(x, y);
                started = true;
            } else {
                seriesPath.lineTo(x, y);
            }
            lastFinite = index;
        }
        if (!started) return;

        areaPath.set(seriesPath);
        areaPath.lineTo(right, bottom);
        areaPath.lineTo(left, bottom);
        areaPath.close();
        fill.setColor(Color.argb(28, Color.red(lineColor), Color.green(lineColor), Color.blue(lineColor)));
        canvas.drawPath(areaPath, fill);
        line.setColor(lineColor);
        canvas.drawPath(seriesPath, line);

        if (lastFinite >= 0) {
            float lastX = seriesX(lastFinite, left, right, minTime, maxTime);
            float lastY = seriesY(values.get(lastFinite), scale, top, bottom);
            point.setColor(lineColor);
            canvas.drawCircle(lastX, lastY, dp(3.5f), point);
            point.setStyle(Paint.Style.STROKE);
            point.setStrokeWidth(dp(2));
            point.setColor(surfaceColor);
            canvas.drawCircle(lastX, lastY, dp(5f), point);
            point.setStyle(Paint.Style.FILL);
        }

        if (selectedIndex >= 0 && selectedIndex < values.size() && Double.isFinite(values.get(selectedIndex))) {
            drawSelection(canvas, scale, left, right, top, bottom, minTime, maxTime);
        }
    }

    private void drawTimeGrid(Canvas canvas, float left, float right, float top, float bottom, long minTime, long maxTime) {
        int intervals = "1h".equals(rangeLabel) ? 3 : 4;
        for (int index = 0; index <= intervals; index++) {
            float fraction = index / (float) intervals;
            float x = left + (right - left) * fraction;
            if (index > 0 && index < intervals) canvas.drawLine(x, top, x, bottom, grid);
            long timestamp = minTime + Math.round((maxTime - minTime) * fraction);
            String value = Instant.ofEpochMilli(timestamp).atZone(zone).format(timeFormatter);
            float textWidth = label.measureText(value);
            float textX = Math.max(left, Math.min(right - textWidth, x - textWidth / 2f));
            canvas.drawText(value, textX, bottom + dp(18), label);
        }
    }

    private void drawSelection(
            Canvas canvas,
            HydroMath.AxisScale scale,
            float left,
            float right,
            float top,
            float bottom,
            long minTime,
            long maxTime
    ) {
        float x = seriesX(selectedIndex, left, right, minTime, maxTime);
        float y = seriesY(values.get(selectedIndex), scale, top, bottom);

        crosshair.setColor(Color.argb(120, Color.red(labelColor), Color.green(labelColor), Color.blue(labelColor)));
        canvas.drawLine(x, top, x, bottom, crosshair);
        point.setColor(lineColor);
        canvas.drawCircle(x, y, dp(5), point);
        point.setStyle(Paint.Style.STROKE);
        point.setStrokeWidth(dp(2));
        point.setColor(surfaceColor);
        canvas.drawCircle(x, y, dp(7), point);
        point.setStyle(Paint.Style.FILL);

        ZonedDateTime timestamp = Instant.ofEpochMilli(times.get(selectedIndex)).atZone(zone);
        String text = timestamp.format(tooltipFormatter) + " · " + formatValue(values.get(selectedIndex));
        tooltipText.setColor(tooltipTextColor);
        float textWidth = tooltipText.measureText(text);
        float textHeight = Math.abs(tooltipText.ascent()) + Math.abs(tooltipText.descent());
        float boxWidth = Math.min(right - left, textWidth + dp(16));
        float boxLeft = Math.max(left, Math.min(right - boxWidth, x - boxWidth / 2f));
        tooltipBox.set(boxLeft, top + dp(4), boxLeft + boxWidth, top + textHeight + dp(14));
        tooltip.setColor(tooltipColor);
        canvas.drawRoundRect(tooltipBox, dp(8), dp(8), tooltip);
        canvas.save();
        canvas.clipRect(tooltipBox);
        canvas.drawText(text, tooltipBox.left + dp(8), tooltipBox.bottom - dp(6), tooltipText);
        canvas.restore();
        setContentDescription(seriesLabel + " " + text);
    }

    private float seriesX(int index, float left, float right, long minTime, long maxTime) {
        return left + (right - left) * (times.get(index) - minTime) / (float) (maxTime - minTime);
    }

    private float seriesY(double value, HydroMath.AxisScale scale, float top, float bottom) {
        return (float) (bottom - (value - scale.min) / (scale.max - scale.min) * (bottom - top));
    }

    private String axisLabel(double value, double step) {
        double absolute = Math.abs(step);
        if (absolute >= 1) return String.format(Locale.GERMAN, "%.0f", value);
        if (absolute >= 0.1) return String.format(Locale.GERMAN, "%.1f", value);
        if (absolute >= 0.01) return String.format(Locale.GERMAN, "%.2f", value);
        return String.format(Locale.GERMAN, "%.3f", value);
    }

    private String formatValue(double value) {
        return String.format(Locale.GERMAN, "%." + Math.max(0, decimals) + "f %s", value, unit);
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
