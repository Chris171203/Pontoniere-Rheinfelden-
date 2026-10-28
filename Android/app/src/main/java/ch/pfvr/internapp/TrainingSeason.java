package ch.pfvr.internapp;

import java.time.LocalDate;
import java.util.List;

/** Calendar anchors control inferred training only; explicit events remain authoritative. */
final class TrainingSeason {
    enum Season { SUMMER, WINTER, PAUSE }
    static final class Anchor {
        final LocalDate day;
        final String title;
        final boolean cancelled;
        Anchor(LocalDate day, String title, boolean cancelled) {
            this.day = day; this.title = title; this.cancelled = cancelled;
        }
    }
    private TrainingSeason() {}

    static boolean summerEnd(String title, LocalDate day) {
        if (day.getMonthValue() < 9 || day.getMonthValue() > 10) return false;
        String text = TrainingMatcher.normalize(title);
        return text.contains("abfahren") || text.contains("saisonabschluss") || text.contains("einwintern")
            || (text.contains("schiff") && (text.contains("verlad") || text.contains("reinig")));
    }

    static boolean winterStart(String title) {
        String text = TrainingMatcher.normalize(title);
        return text.contains("wintertraining") && (text.contains("start") || text.contains("beginn")
            || text.contains("auftakt") || text.contains("erstes") || text.contains("1. wintertraining"));
    }

    static boolean isAnchor(String title, LocalDate day) {
        return summerEnd(title, day) || winterStart(title);
    }

    static Season on(LocalDate day, List<Anchor> anchors) {
        int month = day.getMonthValue();
        // One winter cycle runs from September through March, including the year boundary.
        LocalDate winterFloor = LocalDate.of(month <= 3 ? day.getYear() - 1 : day.getYear(), 9, 1);
        boolean winter = false, ended = false;
        for (Anchor anchor : anchors) {
            if (anchor.cancelled || anchor.day.isAfter(day)) continue;
            if (winterStart(anchor.title) && !anchor.day.isBefore(winterFloor)) winter = true;
            if (anchor.day.getYear() == day.getYear() && summerEnd(anchor.title, anchor.day)) ended = true;
        }
        if ((month >= 9 || month <= 3) && winter) return Season.WINTER;
        if (month >= 4 && month <= 9 && !ended) return Season.SUMMER;
        return Season.PAUSE;
    }
}
