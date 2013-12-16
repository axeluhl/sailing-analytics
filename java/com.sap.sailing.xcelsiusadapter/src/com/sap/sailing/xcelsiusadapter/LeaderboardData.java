package com.sap.sailing.xcelsiusadapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

/**
 * Exports all data from a leaderboard into XML format. Format is as follows:
 * 
 * <pre>
 * <leaderboard>
 *      <name>XY</name>
 *      <competitor>
 *              <uuid>1234</uuid>
 *              <sail_id></sail_id>
 *      </competitor>
 *      
 *      <race>
 *              <name>R1</name>
 *              <competitor>
 *                      <uuid>1234</uuid>
 *                      <race_final_rank>1</race_final_rank>
 *                      <race_final_score>10</race_final_score>
 *              </competitor>
 *              <leg>
 *                      <count>1</count>
 *                      <competitor>
 *                              <uuid>1234</uuid>
 *                              <number_of_jibes>123</number_of_jibes>
 *                      </competitor>
 *              </leg>
 *      </race>
 * </leaderboard>
 * </pre>
 * 
 * @author Simon Marcel Pamies
 */
public class LeaderboardData {
    
    private static final String VERY_LIGHT_WIND_DESCRIPTION = "Very Light";
    private static final String LIGHT_WIND_DESCRIPTION = "Light";
    private static final String MEDIUM_WIND_DESCRIPTION = "Medium";
    private static final String STRONG_WIND_DESCRIPTION = "Strong";
    private static final String VERY_STRONG_WIND_DESCRIPTION = "Very Strong";
    
    private final HttpServletRequest req;
    private final HttpServletResponse res;
    private final RacingEventService service;
    
    public LeaderboardData(HttpServletRequest req, HttpServletResponse res, RacingEventService service) {
        this.req = req;
        this.res = res;
        this.service = service;
    }
    
    private String getAttribute(String name) {
        return this.req.getParameter(name);
    }

    private RacingEventService getService() {
        return service;
    }

    private Leaderboard getLeaderboard() throws IOException, ServletException {
        final String leaderboardName = getAttribute("leaderboard");
        if (leaderboardName == null) {
            throw new ServletException("Use the leaderboard= parameter to specify the leaderboard");
        }
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName); 
        if (leaderboard == null) {
            throw new ServletException("Leaderboard " + leaderboardName + " not found.");
        }
        return leaderboard;
    }
    
    private long handleValue(TimePoint timepoint) {
        if (timepoint != null) {
            return timepoint.asMillis();
        }
        return 0;
    }
    
    private Map<Competitor, Map<Waypoint, Integer>> getRankAtWaypoint(TrackedRace trackedRace) {
        Map<Competitor, Map<Waypoint, Integer>> result = new HashMap<>();
        Iterable<Waypoint> waypoints = trackedRace.getRace().getCourse().getWaypoints();
        for (Waypoint waypoint : waypoints) {
            Iterable<MarkPassing> markPassingsInOrder = trackedRace.getMarkPassingsInOrder(waypoint);
            int rank = 1;
            for (MarkPassing markPassing : markPassingsInOrder) {
                Map<Waypoint, Integer> map = result.get(markPassing.getCompetitor());
                if (map == null) {
                    map = new HashMap<>();
                    result.put(markPassing.getCompetitor(), map);
                }
                map.put(waypoint, rank++);
            }
        }
        return result;
    }


    private String getBoatClassName(final Leaderboard leaderboard) {
        String result = null;
        if (leaderboard instanceof RegattaLeaderboard) { 
            result = ((RegattaLeaderboard) leaderboard).getRegatta().getBoatClass().getName();
        } else {
            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                for (Fleet fleet : raceColumn.getFleets()) {
                    TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                    if (trackedRace != null) {
                        result = trackedRace.getRace().getBoatClass().getName();
                    }
                }
            }
        }
        return result;
    }

    private void addNamedElementWithValue(Element parent, String newChildName, Integer i) {
        if (i == null) {
            addNamedElementWithValue(parent, newChildName, "0");
        } else {
            addNamedElementWithValue(parent, newChildName, i.toString());
        }

    }

    private void addNamedElementWithValue(Element parent, String newChildName, Double dbl) {
        if (dbl == null) {
            addNamedElementWithValue(parent, newChildName, "0");
        } else {
            addNamedElementWithValue(parent, newChildName, dbl.toString());
        }

    }

    private void addNamedElementWithValue(Element parent, String newChildName, Long l) {
        if (l == null) {
            addNamedElementWithValue(parent, newChildName, "0");
        } else {
            addNamedElementWithValue(parent, newChildName, l.toString());
        }

    }

    private Element addNamedElement(Document doc, String newChildName) {
        final Element newChild = new Element(newChildName);
        doc.addContent(newChild);
        return newChild;
    }

    private Element addNamedElementWithValue(Element parent, String newChildName, String value) {
        final Element newChild = new Element(newChildName);
        newChild.addContent(value);
        parent.addContent(newChild);
        return newChild;
    }

    private Element addNamedElement(Element parent, String newChildName) {
        final Element newChild = new Element(newChildName);
        parent.addContent(newChild);
        return newChild;
    }
    
    private Element createNamedElementWithValue(String elementName, String value) {
        final Element element = new Element(elementName);
        element.addContent(value);
        return element;
    }

    private Element createNamedElementWithValue(String elementName, int value) {
        final Element element = new Element(elementName);
        element.addContent(String.valueOf(value));
        return element;
    }

    private Element createNamedElementWithValue(String elementName, double value) {
        final Element element = new Element(elementName);
        element.addContent(String.valueOf(value));
        return element;
    }
    
    private Element createLeaderboardXML(Leaderboard leaderboard, List<Element> competitors, List<Element> races) {
        Element leaderboardElement = new Element("leaderboard");
        addNamedElementWithValue(leaderboardElement, "name", leaderboard.getName());
        addNamedElementWithValue(leaderboardElement, "display_name", leaderboard.getDisplayName());
        addNamedElementWithValue(leaderboardElement, "delay_to_live_in_millis", leaderboard.getDelayToLiveInMillis());
        addNamedElementWithValue(leaderboardElement, "scoring_scheme", leaderboard.getScoringScheme().getType().name());
        addNamedElementWithValue(leaderboardElement, "boat_class", getBoatClassName(leaderboard));
        leaderboardElement.addContent(competitors);
        leaderboardElement.addContent(races);
        return leaderboardElement;
    }
    
    private List<Element> createTimedXML(String prefix, TimePoint timepoint) {
        List<Element> timedElements = new ArrayList<Element>();
        Calendar timedDate = Calendar.getInstance();
        timedDate.setTime(timepoint.asDate());
        timedElements.add(createNamedElementWithValue(prefix+"year", timedDate.get(Calendar.YEAR)));
        timedElements.add(createNamedElementWithValue(prefix+"month", timedDate.get(Calendar.MONTH)));
        timedElements.add(createNamedElementWithValue(prefix+"day", timedDate.get(Calendar.DAY_OF_MONTH)));
        timedElements.add(createNamedElementWithValue(prefix+"hour", timedDate.get(Calendar.HOUR_OF_DAY)));
        timedElements.add(createNamedElementWithValue(prefix+"minute", timedDate.get(Calendar.MINUTE)));
        timedElements.add(createNamedElementWithValue(prefix+"second", timedDate.get(Calendar.SECOND)));
        // TODO: Format date to a special format with leading zeros
        return timedElements;
    }
    
    public List<Element> createWindXML(String prefix, Pair<Speed, Double> windInformation) {
        List<Element> windElements = new ArrayList<Element>();
        if (windInformation.getA() == null) {
            windInformation = new Pair<Speed, Double>(new KnotSpeedImpl(0.0), 0.0);
        }
        windElements.add(createNamedElementWithValue(prefix+"speed_in_knots", windInformation.getA().getKnots()));
        windElements.add(createNamedElementWithValue(prefix+"speed_in_meters_per_second", windInformation.getA().getMetersPerSecond()));
        windElements.add(createNamedElementWithValue(prefix+"confidence", windInformation.getB()));
        double speedInKnots = windInformation.getA().getKnots();
        String windSpeedAsHumanReadableString = "";
        if (speedInKnots <= 4) {
            windSpeedAsHumanReadableString = VERY_LIGHT_WIND_DESCRIPTION;
        } else if (speedInKnots > 4 && speedInKnots <= 8) {
            windSpeedAsHumanReadableString = LIGHT_WIND_DESCRIPTION;
        } else if (speedInKnots > 8 && speedInKnots <= 14) {
            windSpeedAsHumanReadableString = MEDIUM_WIND_DESCRIPTION;
        } else if (speedInKnots > 14 && speedInKnots <= 20) {
            windSpeedAsHumanReadableString = STRONG_WIND_DESCRIPTION;
        } else if (speedInKnots > 20) {
            windSpeedAsHumanReadableString = VERY_STRONG_WIND_DESCRIPTION;
        }
        windElements.add(createNamedElementWithValue(prefix+"human_readable", windSpeedAsHumanReadableString));
        return windElements;
    }
    
    private Element createRaceXML(TrackedRace race, Fleet fleet, List<Element> legs) throws NoWindException {
        // TODO: Plausibility checks (race has start time, ...)
        Element raceElement = new Element("race");
        addNamedElementWithValue(raceElement, "name", race.getRace().getName());
        addNamedElementWithValue(raceElement, "fleet_name", fleet.getName());
        addNamedElementWithValue(raceElement, "start_time_millis", handleValue(race.getStartOfRace()));
        addNamedElementWithValue(raceElement, "start_of_tracking_time_millis", handleValue(race.getStartOfTracking()));
        addNamedElementWithValue(raceElement, "end_time_millis", handleValue(race.getEndOfRace()));
        addNamedElementWithValue(raceElement, "end_of_tracking_time_millis", handleValue(race.getEndOfTracking()));
        raceElement.addContent(createTimedXML("start_time_", race.getStartOfRace()));
        raceElement.addContent(createTimedXML("end_time_", race.getEndOfRace()));
        raceElement.addContent(createWindXML("wind_", race.getAverageWindSpeedWithConfidence(/*resolutionInMinutes*/ 5)));
        
        for (Competitor competitor : race.getCompetitorsFromBestToWorst(null)) {
            Element competitorElement = createCompetitorXML(competitor, /*shortVersion*/ true);
            // TODO: race_final_rank, race_final_score, ...
            raceElement.addContent(competitorElement);
        }
        
        raceElement.addContent(legs);
        return raceElement;
    }
    
    private Element createCompetitorXML(Competitor competitor, boolean shortVersion) {
        Element competitorElement = new Element("competitor");
        addNamedElementWithValue(competitorElement, "uuid", competitor.getId().toString());
        if (shortVersion)
            return competitorElement;
        
        addNamedElementWithValue(competitorElement, "name", competitor.getName());
        addNamedElementWithValue(competitorElement, "nationality_name", competitor.getTeam().getNationality().getName());
        addNamedElementWithValue(competitorElement, "nationality_ioc", competitor.getTeam().getNationality().getThreeLetterIOCAcronym());
        addNamedElementWithValue(competitorElement, "sail_id", competitor.getBoat().getSailID());
        addNamedElementWithValue(competitorElement, "boat_name", competitor.getBoat().getName());
        addNamedElementWithValue(competitorElement, "boat_class", competitor.getBoat().getBoatClass().getName());
        
        // TODO: average_speed, max_speed, distance sailed
        return competitorElement;
    }
    
    private Element createLegXML(TrackedLeg leg) {
        Element legElement = new Element("leg");
        return legElement;
    }
    
    public void perform() throws ServletException, IOException, NoWindException {
        final Leaderboard leaderboard = getLeaderboard();
        
        final List<Element> racesElements = new ArrayList<Element>();
        final List<Element> competitorElements = new ArrayList<Element>();
        
        for (Competitor competitor : leaderboard.getAllCompetitors()) {
            competitorElements.add(createCompetitorXML(competitor, /*shortVersion*/ false));
        }
        
        for (RaceColumn r : leaderboard.getRaceColumns()) {
            for (Fleet fleet : r.getFleets()) {
                TrackedRace trackedRace = r.getTrackedRace(fleet);
                if (trackedRace != null && trackedRace.hasGPSData()) {
                    final List<Element> legs = new ArrayList<Element>();
                    for (TrackedLeg leg : trackedRace.getTrackedLegs()) {
                        legs.add(createLegXML(leg));
                    }
                    racesElements.add(createRaceXML(trackedRace, fleet, legs));
                }
            }
        }
        
        Element leaderboardElement = createLeaderboardXML(leaderboard, competitorElements, racesElements);
    }
    

}
