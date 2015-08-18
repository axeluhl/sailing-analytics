package com.sap.sailing.gwt.server;

import java.util.Comparator;

import com.sap.sailing.gwt.ui.shared.start.StageEventType;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;

public class FeaturedEventsComparator implements Comparator<Pair<StageEventType, EventHolder>> {
    public static final Comparator<Pair<StageEventType, EventHolder>> INSTANCE = new FeaturedEventsComparator();
    
    @Override
    public int compare(Pair<StageEventType, EventHolder> eventAndStageType1, Pair<StageEventType, EventHolder> eventAndStageType2) {
        TimePoint now = MillisecondsTimePoint.now();
        TimeRange event1Range = new TimeRangeImpl(eventAndStageType1.getB().event.getStartDate(),
                eventAndStageType1.getB().event.getEndDate());
        TimeRange event2Range = new TimeRangeImpl(eventAndStageType2.getB().event.getStartDate(),
                eventAndStageType2.getB().event.getEndDate());
        return event1Range.timeDifference(now).compareTo(event2Range.timeDifference(now));
    }
}