package com.sap.sailing.server.xcelsius;

import java.net.URLEncoder;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.TrackedLeg;
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
                List<Leg> legs = race.getCourse().getLegs();
                TrackedLeg lastTrackedLeg = trackedRace.getTrackedLeg(legs.get(legs.size()-1));
                LinkedHashMap<Competitor, Integer> finalRanks = lastTrackedLeg.getRanks(trackedRace.getTimePointOfNewestEvent());
                Iterator<Map.Entry<Competitor, Integer>> entryIter = finalRanks.entrySet().iterator();
                for (int i=0; i<3; i++) {
                    String nationality = "";
                    String sailID = "";
                    String competitorName = "";
                    if (entryIter.hasNext()) {
                        Map.Entry<Competitor, Integer> next = entryIter.next();
                        nationality = next.getKey().getTeam().getNationality().getThreeLetterIOCAcronym();
                        sailID = next.getKey().getBoat().getSailID();
                        competitorName = next.getKey().getName();
                    }
                    addColumn(nationality);
                    addColumn(sailID);
                    addColumn(competitorName);
                }
                Iterator<Waypoint> waypointsIter= race.getCourse().getWaypoints().iterator();
                Position startPos = trackedRace.getApproximatePosition(waypointsIter.next(),
                        trackedRace.getStart());
                Position secondMarkPos = trackedRace.getApproximatePosition(waypointsIter.next(),
                        trackedRace.getStart());
                addColumn(""+startPos.getBearingGreatCircle(secondMarkPos).getDegrees());
                addColumn(""+startPos.getLatDeg());
                addColumn(""+startPos.getLngDeg());
            }
        }
        say(table);
    }
}
