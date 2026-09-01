package ch.pfvr.internapp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TrainingMatcherTest {
    @Test
    public void recognisesTrainingAndPreparationEvents() {
        assertTrue(TrainingMatcher.isRelevant("Start Wintertraining", "", 19, false));
        assertTrue(TrainingMatcher.isRelevant("Schiffe zum Kraftwerk stellen", "", 18, false));
        assertTrue(TrainingMatcher.isRelevant("Hindernisse auswassern", "", 18, false));
        assertTrue(TrainingMatcher.isExplicitTraining("Sektionstraining (Zusatztraining)", "", 10, false));
        assertFalse(TrainingMatcher.isExplicitTraining("Schiffe reinigen", "", 18, false));
    }

    @Test
    public void rejectsUnrelatedClubEvents() {
        assertFalse(TrainingMatcher.isRelevant("Pensionärentreffen", "", 13, false));
        assertFalse(TrainingMatcher.isRelevant("Vorstandssitzung", "", 19, false));
    }

    @Test
    public void recognisesCancellationSignals() {
        assertTrue(TrainingMatcher.isCancelled("CANCELLED", "Training", ""));
        assertTrue(TrainingMatcher.isCancelled("", "Training fällt aus", ""));
        assertFalse(TrainingMatcher.isCancelled("CONFIRMED", "Training", ""));
    }
}
