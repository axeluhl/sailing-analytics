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
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.server.RacingEventService;

public class ListEventsAction extends HttpAction {
    public ListEventsAction(HttpServletRequest req, HttpServletResponse res, RacingEventService service, int maxRows) {
        super(req, res, service, maxRows);
    }

    public void perform() throws Exception {
        final Document table = getTable("data");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm zzz");
        final HashMap<String, Regatta> regattas = getRegattas();
        for (final String regattaName : regattas.keySet()) {
            final Regatta regatta = regattas.get(regattaName);
            final HashMap<String, RaceDefinition> races = getRaces(regatta);
            for (final String raceName : races.keySet()) {
                RaceDefinition race = races.get(raceName);
                final TrackedRace trackedRace = getTrackedRace(regatta, race);
                if (trackedRace != null) {
                    addRow();
                    addColumn(race.getBoatClass().getName());
                    addColumn(regattaName);
                    addColumn(raceName);
                    addColumn(trackedRace.getStartOfRace() == null ? " " : dateFormat.format(trackedRace.getStartOfRace().asDate()));
                    addColumn("" + Util.size(race.getCompetitors()));
                    Iterator<Waypoint> waypointsIter = race.getCourse().getWaypoints().iterator();
                    Position startPos = trackedRace
                            .getApproximatePosition(waypointsIter.next(), trackedRace.getStartOfRace());
                    Position secondMarkPos = trackedRace.getApproximatePosition(waypointsIter.next(),
                            trackedRace.getStartOfRace());
                    Wind wind = trackedRace.getWind(startPos, trackedRace.getStartOfRace());
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
                    addColumn(URLEncoder.encode(regattaName, "UTF-8"));
                    addColumn(URLEncoder.encode(raceName, "UTF-8"));
                }
            }
        }
        say(table);
    }
}
