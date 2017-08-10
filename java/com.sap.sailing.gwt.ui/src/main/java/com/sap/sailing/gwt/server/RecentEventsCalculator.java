package com.sap.sailing.gwt.server;

import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.gwt.server.HomeServiceUtil.EventVisitor;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RecentEventsCalculator implements EventVisitor {
    private final TimePoint now = MillisecondsTimePoint.now();
    private final SortedSet<EventHolder> recentEventsOfLast12Month = new TreeSet<>(new Comparator<EventHolder>() {
        @Override
        public int compare(EventHolder o1, EventHolder o2) {
            final long diff = o2.event.getEndDate().asMillis() - o1.event.getEndDate().asMillis();
            return diff > 0l ? 1 : diff < 0l ? -1 : 0;
        }
    });

    @Override
    public void visit(EventBase event, boolean onRemoteServer, URL baseURL) {
        final EventHolder holder = new EventHolder(event, onRemoteServer, baseURL);
        final TimePoint endDate = event.getEndDate();
        if (endDate != null && endDate.before(now) && endDate.after(now.minus(Duration.ONE_YEAR))) {
            recentEventsOfLast12Month.add(holder);
        }
    }

    public Collection<EventHolder> getRecentEventsOfLast12Month() {
        return recentEventsOfLast12Month;
    }
}
