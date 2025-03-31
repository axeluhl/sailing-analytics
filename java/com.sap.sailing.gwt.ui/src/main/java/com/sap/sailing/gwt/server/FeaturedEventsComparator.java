package com.sap.sailing.gwt.server;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.gwt.home.communication.start.StageEventType;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;

public class FeaturedEventsComparator implements Comparator<Pair<StageEventType, EventHolder>> {
    private final static Duration LONG_RUNNING_EVENT_DURATION_THRESHOLD = Duration.ONE_WEEK.times(2);
    private final TimePoint now = MillisecondsTimePoint.now();
    private final Random randomizer = new Random();
    private final Map<EventBase, Integer> randomizedEvents = new HashMap<>();
    
    @Override
    public int compare(Pair<StageEventType, EventHolder> eventAndStageType1, Pair<StageEventType, EventHolder> eventAndStageType2) {
        final EventBase event1 = eventAndStageType1.getB().event, event2 = eventAndStageType2.getB().event;
        final TimeRange event1Range = new TimeRangeImpl(event1.getStartDate(), event1.getEndDate());
        final TimeRange event2Range = new TimeRangeImpl(event2.getStartDate(), event2.getEndDate());
        // long-running events that exceed two weeks are probably a series of some sort, modeled as a single
        // event; we don't want those to dominate the stage in case other interesting but short-running events
        // also qualify for the stage:
        final boolean event1IsLongRunning = event1Range.getDuration().compareTo(LONG_RUNNING_EVENT_DURATION_THRESHOLD) > 0;
        final boolean event2IsLongRunning = event2Range.getDuration().compareTo(LONG_RUNNING_EVENT_DURATION_THRESHOLD) > 0;
        final int timeDiffComp = event1IsLongRunning || event2IsLongRunning ? 0 : event1Range.timeDifference(now).compareTo(event2Range.timeDifference(now));
        return timeDiffComp == 0 ? getRandomValue(event1) - getRandomValue(event2) : timeDiffComp;
    }
    
    private int getRandomValue(EventBase event) {
        Integer randomValue = randomizedEvents.get(event);
        if (randomValue == null) {
            randomizedEvents.put(event, randomValue = randomizer.nextInt());
        }
        return randomValue;
    }
}