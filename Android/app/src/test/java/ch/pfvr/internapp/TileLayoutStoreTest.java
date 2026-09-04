package ch.pfvr.internapp;

import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TileLayoutStoreTest {
    @Test public void normalizationDropsUnknownIdsAndAppendsNewTiles(){
        List<String> normalized=TileLayoutStore.normalizeOrder(
                TileLayoutStore.Area.HOME,
                List.of("home_news","removed_tile","home_weather")
        );
        assertEquals(List.of(
                "home_news",
                "home_weather",
                "home_river_summary",
                "home_river_charts",
                "home_events"
        ),normalized);
    }

    @Test public void cartRemainsPinnedAndCannotBeHidden(){
        List<String> normalized=TileLayoutStore.normalizeOrder(
                TileLayoutStore.Area.CASH,
                List.of("cash_twint","cash_cart","cash_drinks")
        );
        assertEquals("cash_cart",normalized.get(0));

        Set<String> hidden=TileLayoutStore.sanitizeHidden(
                TileLayoutStore.Area.CASH,
                new LinkedHashSet<>(List.of("cash_cart","cash_twint","unknown"))
        );
        assertFalse(hidden.contains("cash_cart"));
        assertTrue(hidden.contains("cash_twint"));
        assertEquals(1,hidden.size());
    }

    @Test public void movingTilesDoesNotCrossPinnedCart(){
        List<String> initial=TileLayoutStore.normalizeOrder(TileLayoutStore.Area.CASH,List.of());
        List<String> movedUp=TileLayoutStore.moveOrder(TileLayoutStore.Area.CASH,initial,"cash_drinks",-1);
        assertEquals(initial,movedUp);

        List<String> movedDown=TileLayoutStore.moveOrder(TileLayoutStore.Area.CASH,initial,"cash_drinks",1);
        assertEquals("cash_food",movedDown.get(1));
        assertEquals("cash_drinks",movedDown.get(2));
    }

    @Test public void compactClubTilesRemainKnownAfterCustomOrder(){
        List<String> normalized=TileLayoutStore.normalizeOrder(
                TileLayoutStore.Area.CLUB,
                List.of("club_phone","club_news")
        );
        assertEquals("club_phone",normalized.get(0));
        assertEquals("club_news",normalized.get(1));
        assertEquals(TileLayoutStore.specs(TileLayoutStore.Area.CLUB).size(),normalized.size());
    }

    @Test public void clubCatalogAddsPublicDiscoveryAndSocialTilesToOlderLayouts(){
        List<String> normalized=TileLayoutStore.normalizeOrder(
                TileLayoutStore.Area.CLUB,
                List.of("club_about","club_news","club_contact")
        );
        assertTrue(normalized.contains("club_join"));
        assertTrue(normalized.contains("club_instagram"));
        assertTrue(normalized.contains("club_facebook"));
        assertEquals(TileLayoutStore.specs(TileLayoutStore.Area.CLUB).size(),normalized.size());
    }
}
