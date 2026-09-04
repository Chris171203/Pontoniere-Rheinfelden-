package ch.pfvr.internapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Test;

public class PublicLinksTest {
    @Test public void joinPageUsesTheOfficialPfvrMembershipPage() throws Exception {
        URI uri = new URI(PublicLinks.JOIN);
        assertEquals("https", uri.getScheme());
        assertEquals("www.pfvr.ch", uri.getHost());
        assertEquals("/schnuppertraining-mitglied-werden-formulare/", uri.getPath());
        assertFalse(PublicLinks.JOIN.contains("?"));
    }

    @Test public void socialLinksUseTheOfficialAccounts() throws Exception {
        URI facebook = new URI(PublicLinks.FACEBOOK);
        URI instagram = new URI(PublicLinks.INSTAGRAM);
        assertEquals("www.facebook.com", facebook.getHost());
        assertEquals("/PontoniereRheinfelden/", facebook.getPath());
        assertEquals("www.instagram.com", instagram.getHost());
        assertEquals("/pontoniererheinfelden/", instagram.getPath());
        assertTrue(PublicLinks.FACEBOOK.startsWith("https://"));
        assertTrue(PublicLinks.INSTAGRAM.startsWith("https://"));
    }
}
