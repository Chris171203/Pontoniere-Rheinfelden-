package ch.pfvr.internapp;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Persists the user-defined order and visibility of dashboard tiles.
 *
 * The catalog is stable and versioned by tile id. Unknown ids are discarded and
 * new ids are appended automatically, so app updates keep existing layouts usable.
 */
final class TileLayoutStore {
    enum Area {
        HOME("home", "Home"),
        CASH("cash", "Kasse"),
        CLUB("club", "Verein");

        final String key;
        final String label;

        Area(String key, String label) {
            this.key = key;
            this.label = label;
        }
    }

    enum Width {
        COMPACT("Kompakte Kachel"),
        WIDE("Breite Kachel");

        final String label;

        Width(String label) {
            this.label = label;
        }
    }

    static final class Spec {
        final Area area;
        final String id;
        final String label;
        final Width width;
        final boolean pinned;

        Spec(Area area, String id, String label, Width width, boolean pinned) {
            this.area = area;
            this.id = id;
            this.label = label;
            this.width = width;
            this.pinned = pinned;
        }
    }

    private static final String ORDER_PREFIX = "tile_order_";
    private static final String HIDDEN_PREFIX = "tile_hidden_";
    private static final Map<Area, List<Spec>> CATALOG = new EnumMap<>(Area.class);

    static {
        CATALOG.put(Area.HOME, List.of(
                spec(Area.HOME, "home_weather", "Trainingswetter", Width.WIDE, false),
                spec(Area.HOME, "home_river_summary", "Rhein aktuell", Width.WIDE, false),
                spec(Area.HOME, "home_river_charts", "Rhein-Grafiken", Width.WIDE, false),
                spec(Area.HOME, "home_events", "Nächste Termine", Width.WIDE, false),
                spec(Area.HOME, "home_news", "Vereinsnews", Width.WIDE, false)
        ));
        CATALOG.put(Area.CASH, List.of(
                spec(Area.CASH, "cash_cart", "Warenkorb", Width.WIDE, true),
                spec(Area.CASH, "cash_drinks", "Trinken", Width.WIDE, false),
                spec(Area.CASH, "cash_food", "Essen", Width.WIDE, false),
                spec(Area.CASH, "cash_celebrations", "Feiern", Width.WIDE, false),
                spec(Area.CASH, "cash_free_amount", "Freier Betrag", Width.WIDE, false),
                spec(Area.CASH, "cash_twint", "TWINT", Width.WIDE, false),
                spec(Area.CASH, "cash_payment_details", "Zahlungsdaten", Width.WIDE, false)
        ));
        CATALOG.put(Area.CLUB, List.of(
                spec(Area.CLUB, "club_about", "Über den Verein", Width.WIDE, false),
                spec(Area.CLUB, "club_news", "Vereinsnews", Width.COMPACT, false),
                spec(Area.CLUB, "club_program", "Jahresprogramm", Width.COMPACT, false),
                spec(Area.CLUB, "club_board", "Vorstand", Width.COMPACT, false),
                spec(Area.CLUB, "club_history", "Geschichte", Width.COMPACT, false),
                spec(Area.CLUB, "club_depot", "Depot & Route", Width.COMPACT, false),
                spec(Area.CLUB, "club_phone", "Telefon", Width.COMPACT, false),
                spec(Area.CLUB, "club_email", "E-Mail", Width.COMPACT, false),
                spec(Area.CLUB, "club_contact", "Kontaktseite", Width.COMPACT, false)
        ));
    }

    private final SharedPreferences preferences;

    TileLayoutStore(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    List<Spec> ordered(Area area) {
        List<String> normalized = normalizeOrder(area, parseIds(preferences.getString(orderKey(area), "")));
        String serialized = serialize(normalized);
        if (!serialized.equals(preferences.getString(orderKey(area), ""))) {
            preferences.edit().putString(orderKey(area), serialized).apply();
        }
        Map<String, Spec> byId = specsById(area);
        List<Spec> result = new ArrayList<>();
        for (String id : normalized) {
            Spec spec = byId.get(id);
            if (spec != null) result.add(spec);
        }
        return result;
    }

    boolean isVisible(Spec spec) {
        if (spec.pinned) return true;
        return !hidden(spec.area).contains(spec.id);
    }

    void setVisible(Spec spec, boolean visible) {
        if (spec.pinned) return;
        LinkedHashSet<String> hidden = new LinkedHashSet<>(hidden(spec.area));
        if (visible) hidden.remove(spec.id);
        else hidden.add(spec.id);
        preferences.edit().putString(hiddenKey(spec.area), serialize(hidden)).apply();
    }

    boolean canMove(Area area, String id, int delta) {
        List<String> order = normalizeOrder(area, parseIds(preferences.getString(orderKey(area), "")));
        return !moveOrder(area, order, id, delta).equals(order);
    }

    void move(Area area, String id, int delta) {
        List<String> order = normalizeOrder(area, parseIds(preferences.getString(orderKey(area), "")));
        List<String> moved = moveOrder(area, order, id, delta);
        if (!moved.equals(order)) preferences.edit().putString(orderKey(area), serialize(moved)).apply();
    }

    void reset(Area area) {
        preferences.edit().remove(orderKey(area)).remove(hiddenKey(area)).apply();
    }

    static List<Spec> specs(Area area) {
        return CATALOG.getOrDefault(area, List.of());
    }

    static List<String> normalizeOrder(Area area, List<String> requested) {
        Map<String, Spec> known = specsById(area);
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (Spec spec : specs(area)) if (spec.pinned) result.add(spec.id);
        if (requested != null) {
            for (String id : requested) {
                Spec spec = known.get(id);
                if (spec != null && !spec.pinned) result.add(id);
            }
        }
        for (Spec spec : specs(area)) if (!spec.pinned) result.add(spec.id);
        return new ArrayList<>(result);
    }

    static List<String> moveOrder(Area area, List<String> current, String id, int delta) {
        List<String> order = normalizeOrder(area, current);
        if (delta == 0 || id == null) return order;
        Map<String, Spec> known = specsById(area);
        Spec spec = known.get(id);
        if (spec == null || spec.pinned) return order;
        int index = order.indexOf(id);
        if (index < 0) return order;
        int pinnedCount = 0;
        for (Spec item : specs(area)) if (item.pinned) pinnedCount++;
        int target = Math.max(pinnedCount, Math.min(order.size() - 1, index + (delta < 0 ? -1 : 1)));
        if (target == index) return order;
        Collections.swap(order, index, target);
        return order;
    }

    static Set<String> sanitizeHidden(Area area, Set<String> requested) {
        Map<String, Spec> known = specsById(area);
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (requested != null) {
            for (String id : requested) {
                Spec spec = known.get(id);
                if (spec != null && !spec.pinned) result.add(id);
            }
        }
        return result;
    }

    private Set<String> hidden(Area area) {
        Set<String> normalized = sanitizeHidden(area, new LinkedHashSet<>(parseIds(preferences.getString(hiddenKey(area), ""))));
        String serialized = serialize(normalized);
        if (!serialized.equals(preferences.getString(hiddenKey(area), ""))) {
            preferences.edit().putString(hiddenKey(area), serialized).apply();
        }
        return normalized;
    }

    private static Map<String, Spec> specsById(Area area) {
        LinkedHashMap<String, Spec> result = new LinkedHashMap<>();
        for (Spec spec : specs(area)) result.put(spec.id, spec);
        return result;
    }

    private static Spec spec(Area area, String id, String label, Width width, boolean pinned) {
        return new Spec(area, id, label, width, pinned);
    }

    private static String orderKey(Area area) {
        return ORDER_PREFIX + area.key;
    }

    private static String hiddenKey(Area area) {
        return HIDDEN_PREFIX + area.key;
    }

    static List<String> parseIds(String value) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (value != null) {
            for (String part : value.split(",")) {
                String id = part.trim();
                if (!id.isEmpty()) result.add(id);
            }
        }
        return new ArrayList<>(result);
    }

    static String serialize(Iterable<String> ids) {
        List<String> values = new ArrayList<>();
        if (ids != null) for (String id : ids) if (id != null && !id.isBlank()) values.add(id.trim());
        return String.join(",", values);
    }
}
