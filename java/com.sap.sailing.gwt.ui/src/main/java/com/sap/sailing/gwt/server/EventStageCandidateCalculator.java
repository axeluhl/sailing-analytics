package com.sap.sailing.gwt.server;

import java.net.URL;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.gwt.home.communication.start.StageEventType;
import com.sap.sailing.gwt.server.HomeServiceUtil.EventVisitor;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class EventStageCandidateCalculator implements EventVisitor {
    public static final int MAX_STAGE_EVENTS = 5;
    
    private final TimePoint now = MillisecondsTimePoint.now();
    private final SortedSet<Pair<StageEventType, EventHolder>> featuredEvents = new TreeSet<>(new FeaturedEventsComparator());

    @Override
    public void visit(EventBase event, boolean onRemoteServer, URL baseURL) {
        final TimePoint startDate = event.getStartDate(), endDate = event.getEndDate();
        if (startDate != null) {
            final EventHolder holder = new EventHolder(event, onRemoteServer, baseURL);
            if (now.after(startDate) && (endDate == null || now.before(endDate))) {
                featuredEvents.add(new Pair<StageEventType, EventHolder>(StageEventType.RUNNING, holder));
            } else if (startDate.after(now) && startDate.before(now.plus(Duration.ONE_WEEK.times(4)))) {
                // This ensures that no events appear on the stage that do not have their leaderboards configured
                // as the event page only shows a placeholder message in this stage
                if (HomeServiceUtil.hasRegattaData(event)) {
                    featuredEvents.add(new Pair<StageEventType, EventHolder>(StageEventType.UPCOMING_SOON, holder));
                }
            } else if (endDate != null && endDate.before(now) && endDate.after(now.minus(Duration.ONE_YEAR))) {
                featuredEvents.add(new Pair<StageEventType, EventHolder>(StageEventType.POPULAR, holder));
            }
        }
    }

    public Collection<Pair<StageEventType, EventHolder>> getFeaturedEvents() {
        return featuredEvents;
    }
}
