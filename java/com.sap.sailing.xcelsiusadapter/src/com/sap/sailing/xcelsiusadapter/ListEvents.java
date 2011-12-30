package com.sap.sailing.xcelsiusadapter;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import com.sap.sailing.domain.common.Util;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.server.RacingEventService;

public class ListEvents extends Action {
    public ListEvents(HttpServletRequest req, HttpServletResponse res, RacingEventService service, int maxRows) {
        super(req, res, service, maxRows);
    }

    public void perform() throws Exception {
        final Document table = getTable("data");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm zzz");
        final HashMap<String, Event> events = getEvents();
        for (final String eventName : events.keySet()) {
            final Event event = events.get(eventName);
            final HashMap<String, RaceDefinition> races = getRaces(event);
            for (final String raceName : races.keySet()) {
                RaceDefinition race = races.get(raceName);
                final TrackedRace trackedRace = getTrackedRace(event, race);
                if (trackedRace != null) {
                    addRow();
                    addColumn(race.getBoatClass().getName());
                    addColumn(eventName);
                    addColumn(raceName);
                    addColumn(trackedRace.getStart() == null ? " " : dateFormat.format(trackedRace.getStart().asDate()));
                    addColumn("" + Util.size(race.getCompetitors()));
                    Iterator<Waypoint> waypointsIter = race.getCourse().getWaypoints().iterator();
                    Position startPos = trackedRace
                            .getApproximatePosition(waypointsIter.next(), trackedRace.getStart());
                    Position secondMarkPos = trackedRace.getApproximatePosition(waypointsIter.next(),
                            trackedRace.getStart());
                    Wind wind = trackedRace.getWind(startPos, trackedRace.getStart());
                    addColumn("" + wind.getBeaufort());
                    addColumn("" + wind.getFrom().getDegrees());
                    addColumn("0"); // gusts
                    List<Leg> legs = race.getCourse().getLegs();
                    TrackedLeg lastTrackedLeg = trackedRace.getTrackedLeg(legs.get(legs.size() - 1));
                    LinkedHashMap<Competitor, Integer> finalRanks = lastTrackedLeg.getRanks(trackedRace
                            .getTimePointOfNewestEvent());
                    Iterator<Map.Entry<Competitor, Integer>> entryIter = finalRanks.entrySet().iterator();
                    for (int i = 0; i < 3; i++) {
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
                    addColumn("" + startPos.getBearingGreatCircle(secondMarkPos).getDegrees());
                    addColumn("" + startPos.getLatDeg());
                    addColumn("" + startPos.getLngDeg());
                    addColumn(URLEncoder.encode(eventName, "UTF-8"));
                    addColumn(URLEncoder.encode(raceName, "UTF-8"));
                }
            }
        }
        say(table);
    }
}
