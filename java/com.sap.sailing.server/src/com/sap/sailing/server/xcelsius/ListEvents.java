package com.sap.sailing.server.xcelsius;

import java.net.URLEncoder;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

public class ListEvents extends Action {
    public ListEvents(HttpServletRequest req, HttpServletResponse res, RacingEventService service, int maxRows) {
        super(req, res, service, maxRows);
    }

    public void perform() throws Exception {
        final Document table = getTable("data");
        final HashMap<String, Event> events = getEvents();
        Calendar calendar = new GregorianCalendar();
        for (final String eventName : events.keySet()) {
            final Event event = events.get(eventName);
            final HashMap<String, RaceDefinition> races = getRaces(event);
            for (final String raceName : races.keySet()) {
                RaceDefinition race = races.get(raceName);
                final TrackedRace trackedRace = getTrackedRace(event, race);
                addRow();
                calendar.setTime(trackedRace.getStart().asDate());
                addColumn(""+calendar.get(Calendar.YEAR));
                addColumn(eventName);
                addColumn(race.getBoatClass().getName());
                addColumn(raceName);
                addColumn(URLEncoder.encode(eventName, "UTF-8"));
                addColumn(URLEncoder.encode(raceName, "UTF-8"));
                addColumn("SomeTime");
            }
        }
        say(table);
    }
}
