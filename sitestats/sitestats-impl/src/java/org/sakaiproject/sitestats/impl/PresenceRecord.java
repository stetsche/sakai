package org.sakaiproject.sitestats.impl;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.sakaiproject.sitestats.api.presence.Presence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceRecord implements Presence, Comparable<PresenceRecord> {


    private Instant begin;
    private Instant end;

    @Override
    public int compareTo(PresenceRecord other) {
        return BY_BEGIN_ASC
                .thenComparing(BY_END_ASC)
                .compare(this, other);
    }

    @Override
    public boolean isEnding() {
        return begin == null && end != null;
    }

    @Override
    public boolean isBeginning() {
        return begin != null && end == null;
    }

    @Override
    public boolean isComplete() {
        return begin != null && end != null;
    }

    @Override
    public boolean isCrossDay() {
        if (isComplete()) {
            // Is begin and end on the same day?
            return !Objects.equals(toDay(begin), toDay(end));
        } else if (isBeginning()) {
            // Did the presence begin today?
            return !Objects.equals(toDay(begin), today());
        }

        // Ending or empty presence
        return false;
    }


    @Override
    public Duration getDuration() {
        if (!isComplete()) {
            return Duration.ZERO;
        }

        return Duration.between(begin, end);
    }


    @Override
    public Instant getDay() {
        if (isComplete() || isEnding()) {
            return toDay(end);
        } else if (isBeginning()) {
            return toDay(begin);
        }

        // Empty presence
        return null;
    }

    @Override
    public boolean overlapsWith(@NonNull Presence other) {
        Instant otherBegin = other.getBegin();
        Instant otherEnd = other.getEnd();

        return (begin == null || begin.isBefore(otherEnd)) && (otherBegin == null || otherBegin.isBefore(end));
    }

    @Override
    public boolean isWithin(@NonNull Presence other) {
        if (end == null || begin == null) {
            return false;
        }

        Instant otherBegin = other.getBegin();
        Instant otherEnd = other.getEnd();

        return (otherBegin == null || otherBegin.isBefore(begin)) && (otherEnd == null || otherEnd.isAfter(end));
    }

    private static Instant toDay(@NonNull Instant instant) {
        return instant.truncatedTo(ChronoUnit.DAYS);
    }

    private static Instant today() {
        return toDay(Instant.now());
    }
}
