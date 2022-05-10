package com.sap.sailing.gwt.server;

import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.gwt.server.HomeServiceUtil.EventVisitor;

public class RecentEventsCalculator implements EventVisitor {
    private final SortedSet<EventHolder> eventsNewestFirst = new TreeSet<>(new Comparator<EventHolder>() {
        @Override
        public int compare(EventHolder o1, EventHolder o2) {
            final long diff = o2.event.getEndDate().asMillis() - o1.event.getEndDate().asMillis();
            return diff > 0l ? 1 : diff < 0l ? -1 : 0;
        }
    });

    @Override
    public void visit(EventBase event, boolean onRemoteServer, URL baseURL) {
        final EventHolder holder = new EventHolder(event, onRemoteServer, baseURL);
        eventsNewestFirst.add(holder);
    }

    public Collection<EventHolder> getEventsNewestFirst() {
        return eventsNewestFirst;
    }
}
