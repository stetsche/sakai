package org.sakaiproject.sitestats.api.presence;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;

public interface Presence {


    public static final Comparator<Presence> BY_BEGIN_ASC = Comparator.comparing(Presence::getBegin, Comparator.nullsLast(Comparator.naturalOrder()));
    public static final Comparator<Presence> BY_END_ASC = Comparator.comparing(Presence::getEnd, Comparator.nullsLast(Comparator.naturalOrder()));


    public Instant getBegin();

    public void setBegin(Instant begin);

    public Instant getEnd();

    public void setEnd(Instant end);

    public boolean isEnding();

    public boolean isBeginning();

    public boolean isComplete();

    public boolean isCrossDay();

    public boolean isWithin(Presence other);

    public boolean overlapsWith(Presence other);

    public Duration getDuration();

    public Instant getDay();
}
