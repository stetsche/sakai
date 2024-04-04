package org.sakaiproject.sitestats.impl;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.beanutils.BeanUtils;
import org.sakaiproject.sitestats.api.presence.Presence;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PresenceConsolidation {


    private static final Comparator<PresenceRecord> PRESENCE_RECORDS_ORDER = Comparator.comparing(Presence::getBegin);

    private List<PresenceRecord> records;


    public PresenceConsolidation() {
        records = new ArrayList<>();
    }


    public boolean add(@NonNull Presence presence) {
        if (!presence.isComplete()) {
            throw new IllegalArgumentException("Can not add incomplete record to consolidation");
        }

        if (records.contains(presence)) {
            // Record is already present in list, leave unchanged
            return false;
        }

        PresenceRecord firstOverlappingRecord = null;
        PresenceRecord lastOverlappingRecord = null;

        // Assign first and last overlapping records
        for (PresenceRecord currentRecord : records) {
            if (currentRecord.overlapsWith(presence)) {
                if (firstOverlappingRecord == null) {
                    firstOverlappingRecord = currentRecord;
                }

                lastOverlappingRecord = currentRecord;
            } else if (lastOverlappingRecord != null) {
                break;
            }
        }

        // If we have no intersections, just add the new record
        if (firstOverlappingRecord == null && lastOverlappingRecord == null) {
            records.add(presenceToRecord(presence));
            return true;
        }

        // If there is only one intersection, extend the existing record
        if (firstOverlappingRecord != null && firstOverlappingRecord == lastOverlappingRecord) {
            Presence onlyIntersectingRecord = firstOverlappingRecord;

            if (presence.isWithin(onlyIntersectingRecord)) {
                return false;
            }

            Instant addedRecordBegin = presence.getBegin();
            Instant addedRecordEnd = presence.getEnd();

            if (addedRecordBegin.isBefore(onlyIntersectingRecord.getBegin())) {
                onlyIntersectingRecord.setBegin(addedRecordBegin);
            }

            if (addedRecordEnd.isAfter(onlyIntersectingRecord.getEnd())) {
                onlyIntersectingRecord.setEnd(addedRecordEnd);
            }

            return true;
        }

        // If there are multiple intersections merge all from fist begin to last end
        if (firstOverlappingRecord != null && firstOverlappingRecord != lastOverlappingRecord) {
            int firstIndex = records.indexOf(firstOverlappingRecord);
            int lastIndex = records.indexOf(lastOverlappingRecord);

            for (int i = firstIndex + 1; i < lastIndex; i++) {
                records.remove(i);
            }

            PresenceRecord mergedRecord = merge(presence, firstOverlappingRecord, lastOverlappingRecord);
            records.set(firstIndex, mergedRecord);
            return true;
        }

        throw new IllegalStateException("Unknown condition, record should have been added");
    }

    /**
     * Adds a presence record to the consolidation
     *
     * @param records the presence record to be added
     * @return true if the record consolidation has changed by adding the new records
     */
    public boolean addAll(Collection<Presence> records) {
        boolean changed = false;

        for (Presence presenceRecordImpl : records) {
            if (add(presenceRecordImpl)) {
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Calculates the duration of the consolidated presence records
     *
     * @return Total duration of consolidated records
     */
    public Duration getDuration() {
        return records.stream()
                .map(Presence::getDuration)
                .reduce(Duration.ZERO, (subtotalDuration, duration) -> subtotalDuration.plus(duration));
    }

    public Map<Instant, PresenceConsolidation> mapByDay() {
        Map<Instant, PresenceConsolidation> recordsByDay = new HashMap<>();
        Function<Instant, PresenceConsolidation> consolidationFacrory = (day) -> new PresenceConsolidation();

        // Split cross day records
        for (int i = 0; i < records.size(); i++) {
            PresenceRecord record = records.get(i);

            if (record.isCrossDay()) {
                Instant begin = record.getBegin();
                Instant end = record.getEnd();
                Instant beginDay = toDay(begin);
                Instant endDay = toDay(end);

                for (Instant day = beginDay;
                        day.isBefore(end);
                        day = day.plus(1, ChronoUnit.DAYS)) {
                    
                    // If the slice is on begin or end day, use the actual begin/end
                    Instant sliceBegin = day.equals(beginDay) ? begin : day;
                    Instant sliceEnd = day.equals(endDay) ? end : day.plus(1, ChronoUnit.DAYS);

                    PresenceRecord presenceSlice = PresenceRecord.builder()
                            .begin(sliceBegin)
                            .end(sliceEnd)
                            .build();

                    // Get or create presence consolidation for day and add record
                    recordsByDay.computeIfAbsent(day, consolidationFacrory).add(presenceSlice);
                }
            } else {
                recordsByDay.computeIfAbsent(record.getDay(), consolidationFacrory).add(record);
            }
        }

        return recordsByDay;
    }

    /**
     * Merges a presence record with one or more records that it overlaps with
     *
     * @param mergeRecord the presence record to merge
     * @param baseRecords the existing presence records must be in order of {@link #PRESENCES_ORDER}
     * @return a merged presence record
     */
    private PresenceRecord merge(@NonNull Presence mergeRecord, Presence... baseRecords) {
        // base records:       b-----------x      x----------x        x----------e
        // merge record:               b-----------------------------------------------e
        // merge:              b-------------------------------------------------------e

        Instant firstBegin = baseRecords[0].getBegin();
        Instant lastEnd = baseRecords[baseRecords.length - 1].getEnd();
    
        Instant mergedBegin = mergeRecord.getBegin();
        Instant mergedEnd = mergeRecord.getEnd();
    
        if (mergedBegin.isAfter(firstBegin)) {
            mergedBegin = firstBegin;
        }
    
        if (mergedEnd.isBefore(lastEnd)) {
            mergedEnd = lastEnd;
        }
    
        return PresenceRecord.builder()
                .begin(mergedBegin)
                .end(mergedEnd)
                .build();
    }

    /**
     * Prints a representation of the internal presence records for debugging purposes,
     * using default scale
     *
     * @return String representation of presence consolidation
     */
    public String print() {
        return print(0.5);
    }

    /**
     * Prints a representation of the internal presence records for debugging purposes
     *
     * @param scale Number of characters that represent one minute
     * @return String representation of presence consolidation
     */
    public String print(@NonNull Double scale) {
        char beginChar = 'b';
        char endChar = 'e';
        char durationChar = '-';
        char spaceChar = ' ';
        StringBuilder sb = new StringBuilder();

        Instant previousEnd = null;
        for (PresenceRecord record : records) {
            if (previousEnd != null) {
                long space = (long) Math.floor(Duration.between(previousEnd, record.getBegin()).toMinutes() * scale);
                for (int i = 0; i < space; i++) {
                    sb.append(spaceChar);
                }
            }
            previousEnd = record.getEnd();

            sb.append(beginChar);

            long len = (long) Math.floor(record.getDuration().toMinutes() * scale);
            len = len > 2 ? len - 2 : len;
            for (long i = 0; i < len ; i++) {
                sb.append(durationChar);
            }

            sb.append(endChar);
        }

        return sb.toString();
    }

    private void sort() {
        Collections.sort(records, PRESENCE_RECORDS_ORDER);
    }

    /**
     * Converts {@link org.sakaiproject.sitestats.api.presence.Presence}
     * to       {@link org.sakaiproject.sitestats.impl.PresenceRecord}
     *
     * @param presence The presence to convert
     * @return The presence object if it's already a record else a new record instance
     */
    private PresenceRecord presenceToRecord(@NonNull Presence presence) {
        if (presence instanceof PresenceRecord) {
            return (PresenceRecord) presence;
        }

        PresenceRecord record = new PresenceRecord();

        try {
            BeanUtils.copyProperties(record, presence);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Could not copy properties from {} to {}, did the entities change?",
                    Presence.class.getName(), PresenceRecord.class.getName());
        }

        return record;
    }

    private static Instant toDay(@NonNull Instant instant) {
        return instant.truncatedTo(ChronoUnit.DAYS);
    }
}
