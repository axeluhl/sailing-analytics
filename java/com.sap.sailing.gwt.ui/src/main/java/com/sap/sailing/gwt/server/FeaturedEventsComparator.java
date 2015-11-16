package com.sap.sailing.gwt.server;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.gwt.ui.shared.start.StageEventType;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;

public class FeaturedEventsComparator implements Comparator<Pair<StageEventType, EventHolder>> {

    private final TimePoint now = MillisecondsTimePoint.now();
    private final Random randomizer = new Random();
    private final Map<EventBase, Integer> randomizedEvents = new HashMap<>();
    
    @Override
    public int compare(Pair<StageEventType, EventHolder> eventAndStageType1, Pair<StageEventType, EventHolder> eventAndStageType2) {
        EventBase event1 = eventAndStageType1.getB().event, event2 = eventAndStageType2.getB().event;
        TimeRange event1Range = new TimeRangeImpl(event1.getStartDate(), event1.getEndDate());
        TimeRange event2Range = new TimeRangeImpl(event2.getStartDate(), event2.getEndDate());
        int timeDiffComp = event1Range.timeDifference(now).compareTo(event2Range.timeDifference(now));
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