package ch.pfvr.internapp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AppLinkPolicyTest {
    @Test
    public void acceptsOnlyPfvrDomainAndSubdomains() {
        assertTrue(AppLinkPolicy.isPfvrHost("pfvr.ch"));
        assertTrue(AppLinkPolicy.isPfvrHost("www.pfvr.ch"));
        assertTrue(AppLinkPolicy.isPfvrHost("intern.pfvr.ch"));
        assertFalse(AppLinkPolicy.isPfvrHost("evilpfvr.ch"));
        assertFalse(AppLinkPolicy.isPfvrHost("pfvr.ch.example.org"));
    }

    @Test
    public void internalWebViewIsRestrictedToExactHost() {
        assertTrue(AppLinkPolicy.isInternalPfvrHost("intern.pfvr.ch"));
        assertFalse(AppLinkPolicy.isInternalPfvrHost("www.pfvr.ch"));
        assertFalse(AppLinkPolicy.isInternalPfvrHost("fakeintern.pfvr.ch"));
    }

    @Test
    public void publicWebViewAllowsOnlyRequiredGoogleHost() {
        assertTrue(AppLinkPolicy.mayStayInPublicWebView("calendar.google.com"));
        assertFalse(AppLinkPolicy.mayStayInPublicWebView("accounts.google.com"));
        assertFalse(AppLinkPolicy.mayStayInPublicWebView("google.com.evil.example"));
    }
    @Test public void externalLinksAllowOnlyExpectedSchemes() {
        assertTrue(AppLinkPolicy.mayOpenExternally("https"));assertTrue(AppLinkPolicy.mayOpenExternally("http"));
        assertTrue(AppLinkPolicy.mayOpenExternally("mailto"));assertTrue(AppLinkPolicy.mayOpenExternally("tel"));assertTrue(AppLinkPolicy.mayOpenExternally("geo"));
        assertFalse(AppLinkPolicy.mayOpenExternally("intent"));assertFalse(AppLinkPolicy.mayOpenExternally("file"));assertFalse(AppLinkPolicy.mayOpenExternally("javascript"));assertFalse(AppLinkPolicy.mayOpenExternally(null));
    }

}
