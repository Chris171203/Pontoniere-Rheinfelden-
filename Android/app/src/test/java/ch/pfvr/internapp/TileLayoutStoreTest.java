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
                "home_weather_3day",
                "home_river_summary",
                "home_river_charts",
                "home_events"
        ),normalized);
    }

    @Test public void oldHomeLayoutPlacesNewThreeDayWeatherDirectlyAfterTrainingWeather(){
        List<String> normalized=TileLayoutStore.normalizeOrder(
                TileLayoutStore.Area.HOME,
                List.of("home_weather","home_river_summary","home_events","home_news")
        );
        assertEquals("home_weather",normalized.get(0));
        assertEquals("home_weather_3day",normalized.get(1));
        assertEquals("home_river_summary",normalized.get(2));
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

    @Test public void clubLinksRemainKnownAfterCustomOrder(){
        List<String> normalized=TileLayoutStore.normalizeOrder(
                TileLayoutStore.Area.CLUB,
                List.of("club_contact","club_news")
        );
        assertEquals("club_contact",normalized.get(0));
        assertEquals("club_news",normalized.get(1));
        assertEquals(TileLayoutStore.specs(TileLayoutStore.Area.CLUB).size(),normalized.size());
    }

    @Test public void previousDefaultClubLayoutBecomesCompactDiscoveryOrder(){
        var order=TileLayoutStore.normalizeOrder(TileLayoutStore.Area.CLUB,List.of("club_about","club_news","club_program","club_youth","club_board","club_history","club_contact"));
        assertEquals(List.of("club_sport","club_youth","club_board","club_history"),order.subList(0,4));
        assertFalse(order.contains("club_program"));
        assertTrue(TileLayoutStore.sanitizeHidden(TileLayoutStore.Area.CLUB,Set.of("club_program")).isEmpty());
        for(var spec:TileLayoutStore.specs(TileLayoutStore.Area.CLUB))assertEquals(TileLayoutStore.Width.WIDE,spec.width);
    }

    @Test public void clubCatalogDropsRetiredTilesAndPreservesRemainingLayout(){
        List<String> normalized=TileLayoutStore.normalizeOrder(
                TileLayoutStore.Area.CLUB,
                List.of("club_about","club_join","club_instagram","club_facebook","club_phone","club_email","club_depot","club_news","club_contact")
        );
        assertFalse(normalized.contains("club_join"));
        assertFalse(normalized.contains("club_instagram"));
        assertFalse(normalized.contains("club_facebook"));
        assertFalse(normalized.contains("club_phone"));
        assertFalse(normalized.contains("club_email"));
        assertFalse(normalized.contains("club_depot"));
        assertEquals(List.of("club_about","club_news","club_contact"),normalized.subList(0,3));
        assertTrue(TileLayoutStore.sanitizeHidden(TileLayoutStore.Area.CLUB,
                Set.of("club_phone","club_email","club_depot")).isEmpty());
        assertEquals(TileLayoutStore.specs(TileLayoutStore.Area.CLUB).size(),normalized.size());
    }
}
