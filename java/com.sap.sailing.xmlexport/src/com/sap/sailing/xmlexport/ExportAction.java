package com.sap.sailing.xmlexport;

import java.io.IOException;
import java.io.StringWriter;
import java.util.NavigableSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.security.SecurityService;

public abstract class ExportAction {
    private HttpServletRequest req;
    private HttpServletResponse res;

    private RacingEventService service;
    private Leaderboard leaderboard;
    protected final boolean useProvidedLeaderboard;
    private String resultingXMLData;
    protected final SecurityService securityService;
    
    public ExportAction(HttpServletRequest req, HttpServletResponse res, RacingEventService service,
            final SecurityService securityService) {
        this.req = req;
        this.res = res;
        this.service = service;
        this.securityService = securityService;
        this.leaderboard = null;
        this.useProvidedLeaderboard = false;
    }
    
    public ExportAction(Leaderboard leaderboard, SecurityService securityService) {
        this.leaderboard = leaderboard;
        this.useProvidedLeaderboard = true;
        this.securityService = securityService;
    }

    public String getAttribute(String name) {
        return this.req.getParameter(name);
    }

    public RacingEventService getService() {
        return service;
    }
    
    public String getResultXML() {
        return resultingXMLData;
    }

    public Leaderboard getLeaderboard() throws IOException, ServletException {
        if (!useProvidedLeaderboard) {
            final String leaderboardName = getAttribute("name");
            if (leaderboardName == null) {
                throw new ServletException("Use the name= parameter to specify the leaderboard");
            }
            leaderboard = getService().getLeaderboardByName(leaderboardName);
            securityService.checkCurrentUserReadPermission(leaderboard);
            if (leaderboard == null) {
                throw new ServletException("Leaderboard " + leaderboardName + " not found.");
            }
        }
        return leaderboard;
    }
    
    protected String cleanRaceName(String raceName) {
        String newRaceName = raceName;
        if (raceName.matches("[A-Za-z ]*\\d")) {
            Pattern regex = Pattern.compile("([A-Za-z ]*)(\\d)");
            Matcher matcher = regex.matcher(raceName);
            String raceNameFirstPart = matcher.replaceAll("$1");
            String raceNumber = matcher.replaceAll("$2");
            try {
                if (Integer.parseInt(raceNumber) < 10) {
                    raceNumber = "0" + raceNumber;
                    newRaceName = raceNameFirstPart + raceNumber;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return newRaceName;
    }
    
    public Position getOrthogonalProjectionOntoLine(Position positionToProject, Position lineMarkPosition1, Position lineMarkPosition2) {
        Bearing bearing = lineMarkPosition1.getBearingGreatCircle(lineMarkPosition2);
        return positionToProject.projectToLineThrough(lineMarkPosition2, bearing);
    }
    
    public Long getTotalTimeSailedInMilliseconds(final Competitor competitor, final TimePoint timePoint, boolean alsoReturnTimeIfCompetitorHasNotFinishedRace) throws IOException, ServletException {
        Long result = null;
        for (TrackedRace trackedRace : getLeaderboard().getTrackedRaces()) {
            if (Util.contains(trackedRace.getRace().getCompetitors(), competitor)) {
                NavigableSet<MarkPassing> markPassings = trackedRace.getMarkPassings(competitor);
                if (!markPassings.isEmpty()) {
                    TimePoint from = trackedRace.getStartOfRace(); // start counting at race start, not when the competitor passed the line
                    if (from != null && !timePoint.before(from)) { // but only if the race started after timePoint
                        TimePoint to;
                        if (timePoint.after(markPassings.last().getTimePoint())
                                && markPassings.last().getWaypoint() == trackedRace.getRace().getCourse()
                                        .getLastWaypoint()) {
                            // stop counting when competitor finished the race
                            to = markPassings.last().getTimePoint();
                        } else {
                            // in contrary to AbstractSimpleLeaderboardImpl#getTotalTimeSailedInMilliseconds
                            // this method will not return null if a competitor did not finish the race but
                            // will return all data until here
                            if (!alsoReturnTimeIfCompetitorHasNotFinishedRace) {
                                if (trackedRace.getEndOfTracking() != null
                                        && timePoint.after(trackedRace.getEndOfTracking())) {
                                        result = null; // race not finished until end of tracking; no reasonable value can be
                                        // computed for competitor
                                        break;
                                } else {
                                    to = timePoint;
                                }
                            } else {
                                // count until last mark passing - we can not find
                                // any time point later than that because the competitor
                                // could have sailed away from the race course
                                to = markPassings.last().getTimePoint();
                            }
                        }
                        long timeSpent = to.asMillis() - from.asMillis();
                        if (result == null) {
                            result = timeSpent;
                        } else {
                            result += timeSpent;
                        }
                    }
                }
            }
        }
        return result;
    }
    
    public Distance getDistanceTraveled(TrackedRace trackedRace, Competitor competitor, TimePoint timePoint, boolean alsoReturnDistanceIfCompetitorHasNotFinishedRace) {
        NavigableSet<MarkPassing> markPassings = trackedRace.getMarkPassings(competitor);
        if (markPassings.isEmpty()) {
            return null;
        } else {
            TimePoint end = timePoint;
            if (markPassings.last().getWaypoint() == trackedRace.getRace().getCourse().getLastWaypoint()
                    && timePoint.compareTo(markPassings.last().getTimePoint()) > 0) {
                // competitor has finished race; use time point of crossing the finish line
                end = markPassings.last().getTimePoint();
            } else {
                if (markPassings.last().getWaypoint() != trackedRace.getRace().getCourse().getLastWaypoint() &&
                        alsoReturnDistanceIfCompetitorHasNotFinishedRace) {
                    end = markPassings.last().getTimePoint();
                }
            }
            return trackedRace.getTrack(competitor).getDistanceTraveled(markPassings.first().getTimePoint(), end);
        }
    }

    public Distance getTotalDistanceTraveled(Leaderboard leaderboard, Competitor competitor, TimePoint timePoint) {
        Distance result = null;
        for (TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
            if (Util.contains(trackedRace.getRace().getCompetitors(), competitor)) {
                Distance distanceSailedInRace = getDistanceTraveled(trackedRace, competitor, timePoint, true);
                if (distanceSailedInRace != null) {
                    if (result == null) {
                        result = distanceSailedInRace;
                    } else {
                        result = result.add(distanceSailedInRace);
                    }
                }
            }
        }
        return result;
    }
    
    public Triple<GPSFixMoving, Speed, TrackedLegOfCompetitor> getMaximumSpeedOverGround(Competitor competitor, TrackedRace trackedRace) {
        Triple<GPSFixMoving, Speed, TrackedLegOfCompetitor> result = null;
        com.sap.sse.common.Util.Pair<GPSFixMoving, Speed> speedWithGPSFix = null;
        TrackedLegOfCompetitor legOfCompetitorWhereSpeedHasBeenReached = null;
        if (Util.contains(trackedRace.getRace().getCompetitors(), competitor)) {
            NavigableSet<MarkPassing> markPassings = trackedRace.getMarkPassings(competitor);
            if (!markPassings.isEmpty()) {
                TimePoint from = markPassings.first().getTimePoint();
                TimePoint to = trackedRace.getEndOfRace();
                speedWithGPSFix = trackedRace.getTrack(competitor).getMaximumSpeedOverGround(from, to);
            }
            if (speedWithGPSFix != null) {
                legOfCompetitorWhereSpeedHasBeenReached = trackedRace.getTrackedLeg(competitor, speedWithGPSFix.getA().getTimePoint());
                result = new Triple<GPSFixMoving, Speed, TrackedLegOfCompetitor>(speedWithGPSFix.getA(), speedWithGPSFix.getB(), legOfCompetitorWhereSpeedHasBeenReached);
            }
        }
        return result;
    }

    public Speed getAverageSpeedOverGround(Leaderboard leaderboard, Competitor competitor, TimePoint timePoint, boolean alsoIncludeNonFinishedRaces) {
        Speed result = null;
        for (TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
            if (Util.contains(trackedRace.getRace().getCompetitors(), competitor)) {
                NavigableSet<MarkPassing> markPassings = trackedRace.getMarkPassings(competitor);
                if (!markPassings.isEmpty()) {
                    TimePoint from = markPassings.first().getTimePoint();
                    TimePoint to;
                    if (timePoint.after(markPassings.last().getTimePoint()) &&
                            markPassings.last().getWaypoint() == trackedRace.getRace().getCourse().getLastWaypoint()) {
                        // stop counting when competitor finished the race
                        to = markPassings.last().getTimePoint();
                    } else {
                        if (markPassings.last().getWaypoint() != trackedRace.getRace().getCourse().getLastWaypoint() &&
                                timePoint.after(markPassings.last().getTimePoint()) &&
                                !alsoIncludeNonFinishedRaces) {
                            result = null;
                            break;
                        }
                        to = timePoint;
                    }
                    Distance distanceTraveled = trackedRace.getDistanceTraveled(competitor, timePoint);
                    if (distanceTraveled != null) {
                        result = distanceTraveled.inTime(to.asMillis()-from.asMillis());
                    }
                }
            }
        }
        return result;
    }
    
    public Iterable<Maneuver> getManeuvers(TrackedRace trackedRace, Competitor competitor, boolean waitForLatest) throws NoWindException {
        Iterable<Maneuver> maneuvers = trackedRace.getManeuvers(competitor,
                trackedRace.getStartOfRace(), trackedRace.getEndOfRace(), waitForLatest);
        return maneuvers;
    }

    public Integer getNumberOfJibes(Iterable<Maneuver> maneuvers) throws NoWindException {
        int result = 0;
        for (Maneuver maneuver : maneuvers) {
            if (maneuver.getType() == ManeuverType.JIBE) {
                result++;
            }
        }
        return result;
    }

    public Integer getNumberOfPenaltyCircles(Iterable<Maneuver> maneuvers) throws NoWindException {
        int result = 0;
        for (Maneuver maneuver : maneuvers) {
            if (maneuver.getType() == ManeuverType.PENALTY_CIRCLE) {
                result++;
            }
        }
        return result;
    }

    public Integer getNumberOfTacks(Iterable<Maneuver> maneuvers) throws NoWindException {
        int result = 0;
        for (Maneuver maneuver : maneuvers) {
            if (maneuver.getType() == ManeuverType.TACK) {
                result++;
            }
        }
        return result;
    }

    protected String cleanSailId(String sailId, Competitor competitor) {
        if (sailId.matches("^[A-Z]{3}\\s[0-9]*")) {                                        
            Pattern regex = Pattern.compile("(^[A-Z]{3})\\s([0-9]*)");
            Matcher regexMatcher = regex.matcher(sailId);
            try {
                return regexMatcher.replaceAll("$1$2");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (sailId.matches("^[A-Z]{3}\\S[0-9]*")) {
            return sailId;
        } else if (sailId.matches("[0-9]*")){
            Nationality nationality = competitor.getTeam().getNationality();
            return (nationality==null ? "" : nationality.getThreeLetterIOCAcronym() + sailId);
        } 
        return sailId;
    }

    public void sendDocument(Element element, String fileName) {
        final Format format = Format.getPrettyFormat();
        format.setIndent("  ");
        format.setExpandEmptyElements(true);
        format.setEncoding("UTF-8");
        XMLOutputter xmlOutputter = new XMLOutputter(format);
        
        if (!useProvidedLeaderboard) {
            res.setContentType("text/xml");
            res.addHeader("Content-Disposition", "attachment; filename=" + fileName);
            try {
                xmlOutputter.output(element, res.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            StringWriter resultStringWriter = new StringWriter();
            try {
                xmlOutputter.output(element, resultStringWriter);
                resultingXMLData = resultStringWriter.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected long handleValue(TimePoint timepoint) {
        if (timepoint != null) {
            return timepoint.asMillis();
        }
        return 0;
    }
    
    protected String getBoatClassName(final Leaderboard leaderboard) {
        String result = null;
        if (leaderboard instanceof RegattaLeaderboard) { 
            result = ((RegattaLeaderboard) leaderboard).getRegatta().getBoatClass().getName();
        } else {
            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                for (Fleet fleet : raceColumn.getFleets()) {
                    TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                    if (trackedRace != null) {
                        result = trackedRace.getRace().getBoatClass().getName();
                        break;
                    }
                }
            }
        }
        return result;
    }

    protected void addNamedElementWithValue(Element parent, String newChildName, Integer i) {
        if (i == null) {
            addNamedElementWithValue(parent, newChildName, "0");
        } else {
            addNamedElementWithValue(parent, newChildName, i.toString());
        }

    }

    protected void addNamedElementWithValue(Element parent, String newChildName, Double dbl) {
        if (dbl == null) {
            addNamedElementWithValue(parent, newChildName, "0");
        } else {
            addNamedElementWithValue(parent, newChildName, dbl.toString());
        }

    }

    protected void addNamedElementWithValue(Element parent, String newChildName, Long l) {
        if (l == null) {
            addNamedElementWithValue(parent, newChildName, "0");
        } else {
            addNamedElementWithValue(parent, newChildName, l.toString());
        }
    }

    protected Element addNamedElementWithValue(Element parent, String newChildName, String value) {
        final Element newChild = new Element(newChildName);
        newChild.addContent(value);
        parent.addContent(newChild);
        return newChild;
    }

    protected Element createNamedElementWithValue(String elementName, String value) {
        final Element element = new Element(elementName);
        element.addContent(value);
        return element;
    }

    protected Element createNamedElementWithValue(String elementName, int value) {
        final Element element = new Element(elementName);
        element.addContent(String.valueOf(value));
        return element;
    }

    protected Element createNamedElementWithValue(String elementName, double value) {
        final Element element = new Element(elementName);
        element.addContent(String.valueOf(value));
        return element;
    }
    
    protected Element createNamedElementWithValue(String elementName, long value) {
        final Element element = new Element(elementName);
        element.addContent(String.valueOf(value));
        return element;
    }
}
