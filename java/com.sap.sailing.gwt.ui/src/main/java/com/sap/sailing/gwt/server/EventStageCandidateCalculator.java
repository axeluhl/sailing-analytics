package com.sap.sailing.gwt.server;

import java.net.URL;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.gwt.server.HomeServiceUtil.EventVisitor;
import com.sap.sailing.gwt.ui.shared.start.StageEventType;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class EventStageCandidateCalculator implements EventVisitor {
    private final TimePoint now = MillisecondsTimePoint.now();
    private final SortedSet<Pair<StageEventType, EventHolder>> featuredEvents = new TreeSet<>(FeaturedEventsComparator.INSTANCE);

    @Override
    public void visit(EventBase event, boolean onRemoteServer, URL baseURL) {
        EventHolder holder = new EventHolder(event, onRemoteServer, baseURL);
        if (now.after(event.getStartDate()) && now.before(event.getEndDate())) {
            featuredEvents.add(new Pair<StageEventType, EventHolder>(StageEventType.RUNNING, holder));
        } else if (event.getStartDate().after(now) &&
                event.getStartDate().before(now.plus(Duration.ONE_WEEK.times(4)))) {
            // This ensures that no events appear on the stage that do not have their leaderboards configured
            // as the event page only shows a placeholder message in this stage
            if(HomeServiceUtil.hasRegattaData(event)) {
                featuredEvents.add(new Pair<StageEventType, EventHolder>(StageEventType.UPCOMING_SOON, holder));
            }
        } else if (event.getEndDate().before(now) &&
                event.getEndDate().after(now.minus(Duration.ONE_YEAR))) {
            featuredEvents.add(new Pair<StageEventType, EventHolder>(StageEventType.POPULAR, holder));
        }
    }

    public Collection<Pair<StageEventType, EventHolder>> getFeaturedEvents() {
        return featuredEvents;
    }
}
