package ch.pfvr.internapp;

import java.time.LocalDate;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static ch.pfvr.internapp.TrainingSeason.Season.*;

public class TrainingSeasonTest {
    private LocalDate day(String value) { return LocalDate.parse(value); }
    private TrainingSeason.Anchor anchor(String date, String title) {
        return new TrainingSeason.Anchor(day(date),title,false);
    }
    @Test public void multipleAutumnTriggersButNoSummerTransportOrUnrelatedCleaning() {
        for(String title:List.of("Schiffe verladen","Schiffe reinigen","Abfahren","Saisonabschluss","Einwintern")) {
            assertEquals(PAUSE,TrainingSeason.on(day("2026-09-28"),List.of(anchor("2026-09-26",title))));
            assertEquals(SUMMER,TrainingSeason.on(day("2026-09-28"),List.of(anchor("2026-07-26",title))));
        }
        assertFalse(TrainingSeason.summerEnd("Depot reinigen",day("2026-09-26")));
        assertEquals(SUMMER,TrainingSeason.on(day("2026-09-25"),List.of(anchor("2026-09-26","Schiffe verladen"))));
    }
    @Test public void winterNeedsStartAndDoesNotLeakIntoNextSeason() {
        var anchors=List.of(anchor("2026-09-26","Schiffe reinigen"),anchor("2026-10-22","Start Wintertraining"));
        assertEquals(PAUSE,TrainingSeason.on(day("2026-10-01"),anchors));
        assertEquals(WINTER,TrainingSeason.on(day("2026-10-22"),anchors));
        assertEquals(WINTER,TrainingSeason.on(day("2027-01-07"),anchors));
        assertEquals(SUMMER,TrainingSeason.on(day("2027-04-01"),anchors));
        assertEquals(SUMMER,TrainingSeason.on(day("2027-09-28"),anchors));
        assertEquals(PAUSE,TrainingSeason.on(day("2027-10-01"),anchors));
        assertEquals(PAUSE,TrainingSeason.on(day("2026-10-29"),List.of(anchor("2026-10-22","Wintertraining"))));
        assertEquals(PAUSE,TrainingSeason.on(day("2026-10-29"),List.of(new TrainingSeason.Anchor(day("2026-10-22"),"Start Wintertraining",true))));
    }
}
