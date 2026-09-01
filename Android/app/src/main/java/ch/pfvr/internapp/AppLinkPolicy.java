package ch.pfvr.internapp;

import java.util.Locale;

/** Central allow-list for URLs rendered inside the app's WebViews. */
final class AppLinkPolicy {
    private AppLinkPolicy() {}

    static boolean isPfvrHost(String host) {
        String normalized = normalize(host);
        return "pfvr.ch".equals(normalized) || normalized.endsWith(".pfvr.ch");
    }

    static boolean isInternalPfvrHost(String host) {
        return "intern.pfvr.ch".equals(normalize(host));
    }

    static boolean isGoogleCalendarHost(String host) {
        return "calendar.google.com".equals(normalize(host));
    }

    static boolean mayStayInPublicWebView(String host) {
        return isPfvrHost(host) || isGoogleCalendarHost(host);
    }

    private static String normalize(String host) {
        return host == null ? "" : host.trim().toLowerCase(Locale.ROOT);
    }
}
