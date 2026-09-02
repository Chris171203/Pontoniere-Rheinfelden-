package ch.pfvr.internapp;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Structured, locally bundled Vereinsbeiz price catalog. */
final class CashCatalog {
    private CashCatalog() {}

    static final class Item {
        final String id;
        final String name;
        final String variant;
        final double price;
        final boolean deposit;

        Item(String id, String name, String variant, double price, boolean deposit) {
            this.id = id;
            this.name = name;
            this.variant = variant;
            this.price = price;
            this.deposit = deposit;
        }

        String displayName() {
            return variant == null || variant.isBlank() ? name : name + " · " + variant;
        }
    }

    static final class Category {
        final String id;
        final String label;
        final List<Item> items;

        Category(String id, String label, List<Item> items) {
            this.id = id;
            this.label = label;
            this.items = Collections.unmodifiableList(items);
        }
    }

    static final class Catalog {
        final String currency;
        final String title;
        final String validFrom;
        final List<Category> categories;
        private final Map<String, Item> byId;

        Catalog(String currency, String title, String validFrom, List<Category> categories, Map<String, Item> byId) {
            this.currency = currency;
            this.title = title;
            this.validFrom = validFrom;
            this.categories = Collections.unmodifiableList(categories);
            this.byId = Collections.unmodifiableMap(byId);
        }

        Item item(String id) {
            return byId.get(id);
        }

        double total(Map<String, Integer> quantities) {
            double sum = 0d;
            if (quantities == null) return sum;
            for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
                Item item = byId.get(entry.getKey());
                int quantity = entry.getValue() == null ? 0 : Math.max(0, entry.getValue());
                if (item != null) sum += item.price * quantity;
            }
            return Math.round(sum * 100d) / 100d;
        }

        int itemCount(Map<String, Integer> quantities) {
            int count = 0;
            if (quantities == null) return count;
            for (Integer value : quantities.values()) if (value != null && value > 0) count += value;
            return count;
        }
    }

    static Catalog load(Context context) throws Exception {
        StringBuilder raw = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                context.getAssets().open("vereinsbeiz_prices.json"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) raw.append(line);
        }

        JSONObject root = new JSONObject(raw.toString());
        String currency = root.optString("currency", "CHF");
        String title = root.optString("title", "Vereinsbeiz");
        String validFrom = root.optString("validFrom", "");
        JSONArray categoryArray = root.getJSONArray("categories");
        List<Category> categories = new ArrayList<>();
        Map<String, Item> byId = new LinkedHashMap<>();

        for (int categoryIndex = 0; categoryIndex < categoryArray.length(); categoryIndex++) {
            JSONObject categoryJson = categoryArray.getJSONObject(categoryIndex);
            String categoryId = categoryJson.getString("id");
            String categoryLabel = categoryJson.getString("label");
            JSONArray itemArray = categoryJson.getJSONArray("items");
            List<Item> items = new ArrayList<>();
            for (int itemIndex = 0; itemIndex < itemArray.length(); itemIndex++) {
                JSONObject itemJson = itemArray.getJSONObject(itemIndex);
                Item item = new Item(
                        itemJson.getString("id"),
                        itemJson.getString("name"),
                        itemJson.optString("variant", ""),
                        itemJson.getDouble("price"),
                        itemJson.optBoolean("deposit", false));
                if (byId.put(item.id, item) != null) throw new IllegalArgumentException("Duplicate cash item id: " + item.id);
                items.add(item);
            }
            categories.add(new Category(categoryId, categoryLabel, items));
        }
        return new Catalog(currency, title, validFrom, categories, byId);
    }
}
