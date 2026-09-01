package ch.pfvr.internapp;

import java.text.Normalizer;
import java.util.Locale;

/** Pure matching rules for calendar entries that affect a regular training evening. */
final class TrainingMatcher {
    private TrainingMatcher() {}

    static int score(String title, String description, int startHour, boolean allDay) {
        String text = normalize((title == null ? "" : title) + " " + (description == null ? "" : description));
        int score = 0;
        if (containsAny(text, "training", "fahrubung", "jungpontonier", "pontonierkurs", "wintertraining")) score += 8;
        if (containsAny(text, "schiff", "hindernis", "parcours", "auswasser", "einwasser", "reinig", "material", "depot")) score += 4;
        if (!allDay && startHour >= 16 && startHour <= 22) score += 2;
        if (containsAny(text, "pensionar", "versammlung", "sitzung", "vorstand", "jass", "jubilar")) score -= 10;
        return score;
    }

    static boolean isRelevant(String title, String description, int startHour, boolean allDay) {
        return score(title, description, startHour, allDay) >= 4;
    }

    static boolean isExplicitTraining(String title, String description, int startHour, boolean allDay) {
        return score(title, description, startHour, allDay) >= 8;
    }

    static boolean isCancelled(String status, String title, String description) {
        if ("CANCELLED".equalsIgnoreCase(status)) return true;
        String text = normalize((title == null ? "" : title) + " " + (description == null ? "" : description));
        return containsAny(text, "abgesagt", "kein training", "fallt aus", "entfallt", "annulliert");
    }

    static String normalize(String value) {
        if (value == null) return "";
        String text = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return text.toLowerCase(Locale.GERMAN).replaceAll("\\s+", " ").trim();
    }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) if (text.contains(needle)) return true;
        return false;
    }
}
