package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.sitestats.impl.PresenceRecord;

public class PresenceRecordTest {


    private Instant base;
    private PresenceRecord beginningRecord;
    private PresenceRecord endingRecord;
    private PresenceRecord oneHourRecord;
    private PresenceRecord emptyRecord;


    @Before
    public void setup() {
        base = Instant.now();
        beginningRecord = PresenceRecord.builder().begin(base).end(null).build();
        endingRecord = PresenceRecord.builder().begin(null).end(base).build();
        oneHourRecord = PresenceRecord.builder().begin(base).end(base.plus(1, ChronoUnit.HOURS)).build();
        emptyRecord = PresenceRecord.builder().begin(null).end(null).build();
    }

    @Test
    public void testIsEnding() {
        assertTrue(endingRecord.isEnding());
        assertFalse(oneHourRecord.isEnding());
        assertFalse(beginningRecord.isEnding());
        assertFalse(emptyRecord.isEnding());
    }

    @Test
    public void testIsBeginning() {
        assertTrue(beginningRecord.isBeginning());
        assertFalse(oneHourRecord.isBeginning());
        assertFalse(emptyRecord.isBeginning());
        assertFalse(endingRecord.isBeginning());
    }

    @Test
    public void testIsComplete() {
        assertTrue(oneHourRecord.isComplete());
        assertFalse(emptyRecord.isComplete());
        assertFalse(beginningRecord.isComplete());
        assertFalse(endingRecord.isComplete());
    }

    @Test
    public void testDuration() {
        assertEquals(oneHourRecord.getDuration(), Duration.ofHours(1));
        assertEquals(emptyRecord.getDuration(), Duration.ZERO);
        assertEquals(beginningRecord.getDuration(), Duration.ZERO);
        assertEquals(endingRecord.getDuration(), Duration.ZERO);
    }
}