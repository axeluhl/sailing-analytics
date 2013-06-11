package com.sap.sailing.xcelsiusadapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.server.RacingEventService;

public class RegattaDataPerRaceAction extends HttpAction {
    public RegattaDataPerRaceAction(HttpServletRequest req, HttpServletResponse res, RacingEventService service, int maxRows) {
        super(req, res, service, maxRows);
    }
	

    public void perform() throws Exception {


        TimePoint now = MillisecondsTimePoint.now();
        final Leaderboard leaderboard = getLeaderboard(); // Get leaderboard data from request (get value for regatta name from URL parameter regatta)
        // if the regatta does not exist a tag <message> will be returned with a text message from function
        // getLeaderboard().
        if (leaderboard == null) {
            return;
        }
 
        final Document doc = new Document(); // initialize xml document
        final Element regatta_node = addNamedElement(doc, "regatta"); // add root to xml
        addNamedElementWithValue(regatta_node, "name", leaderboard.getName());
        addNamedElementWithValue(regatta_node, "boat_class", getBoatClassName(leaderboard));
        
        
        
        /*
         * Races
         */
//        final HashMap<String, RaceDefinition> races = getRaces(regatta); // get races for the regatta
        final Element races_node = addNamedElement(regatta_node, "races"); // add node that contains all races

        for (RaceColumn r : leaderboard.getRaceColumns()) {
            for (Fleet f : r.getFleets()) {
                TrackedRace trackedRace = r.getTrackedRace(f);
                // skip race if not tracked
                if (trackedRace == null || !trackedRace.hasGPSData()) {
                    continue;
                }
                Map<Competitor, Map<Waypoint, Integer>> rankAtWaypoint = getRankAtWaypoint(trackedRace, now);
                RaceDefinition race = trackedRace.getRace();
                
                final Element race_node = addNamedElement(races_node, "race"); // add race node for the current race
                
                String raceName = race.getName();
                if (raceName.matches("[A-Za-z ]*\\d")) {
                    Pattern regex = Pattern.compile("([A-Za-z ]*)(\\d)");
                    Matcher matcher = regex.matcher(raceName);
                    
                    String raceNameFirstPart = matcher.replaceAll("$1");
                    String raceNumber = matcher.replaceAll("$2");
                    
                    
                    
                    try {
                        if (Integer.parseInt(raceNumber) < 10) {
                            raceNumber = "0" + raceNumber;
                            raceName = raceNameFirstPart + raceNumber;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                }
                
                addNamedElementWithValue(race_node, "name", raceName); // add name node to current race
                
                
                final TimePoint raceStarted = getTimePoint(trackedRace); // get TimePoint for when the race started
                if (raceStarted == null) {
                    continue;
                }

                long minNextLegStart = raceStarted.asMillis(); // variable for keeping track of when the first competitor
                                                               // started the next leg
                TimePoint legStarted = new MillisecondsTimePoint(minNextLegStart); // get TimePoint for when the leg started

                addNamedElementWithValue(race_node, "start_time_ms", raceStarted.asMillis()); // add the starttime to the
                if (trackedRace.getEndOfRace() != null) {
                    addNamedElementWithValue(race_node, "assumed_end_ms", trackedRace.getEndOfRace().asMillis()); // add the assumed enddtime
                } else {
                    addNamedElementWithValue(race_node, "assumed_end_ms", 0); // add the assumed enddtime
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(raceStarted.asDate());
                addNamedElementWithValue(race_node, "start_time_year", cal.get(Calendar.YEAR));
                int month = cal.get(Calendar.MONTH) + 1;
                addNamedElementWithValue(race_node, "start_time_month", month);
                addNamedElementWithValue(race_node, "start_time_day", cal.get(Calendar.DAY_OF_MONTH));
                addNamedElementWithValue(race_node, "start_time_hour", cal.get(Calendar.HOUR_OF_DAY));
                addNamedElementWithValue(race_node, "start_time_minute", cal.get(Calendar.MINUTE));
                addNamedElementWithValue(race_node, "start_time_second", cal.get(Calendar.SECOND));
                addNamedElementWithValue(race_node, "start_time_formatted", (cal.get(Calendar.DAY_OF_MONTH) < 10 ? ("0" + cal.get(Calendar.DAY_OF_MONTH)) : cal.get(Calendar.DAY_OF_MONTH))
                                + "."
                                + (month < 10 ? ("0" + month) : month)
                                + "."
                                + cal.get(Calendar.YEAR)
                                + " - "
                                + (cal.get(Calendar.HOUR_OF_DAY) < 10 ? ("0" + cal.get(Calendar.HOUR_OF_DAY)) : cal
                                        .get(Calendar.HOUR_OF_DAY))
                                + ":"
                                + (cal.get(Calendar.MINUTE) < 10 ? ("0" + cal.get(Calendar.MINUTE)) : cal
                                        .get(Calendar.MINUTE))
                                + ":"
                                + (cal.get(Calendar.SECOND) < 10 ? ("0" + cal.get(Calendar.SECOND)) : cal
                                        .get(Calendar.SECOND)));

                Pair<Double, Double> averageWindSpeedofRace = calculateAverageWindSpeedofRace(trackedRace);
                
                Double wind_speed = 0.0;
//                Double wind_confi = 0.0;
                long wind_beaufort = 0;
                
                if (averageWindSpeedofRace != null) {
                    wind_speed = averageWindSpeedofRace.getA();
                    wind_beaufort = Math.round(averageWindSpeedofRace.getB());
                }
                
                addNamedElementWithValue(race_node, "average_wind_speed", wind_speed);
                addNamedElementWithValue(race_node, "average_wind_speed_beaufort", wind_beaufort);
           
                addNamedElementWithValue(race_node, "course_length_m", getCourseLength(trackedRace) == null ? 0 : getCourseLength(trackedRace).getMeters());
                
                addNamedElementWithValue(race_node, "first_leg_type", trackedRace.getTrackedLeg(trackedRace.getRace().getCourse().getFirstLeg()).getLegType(raceStarted).toString());
                
                
                final Element competitors_node = addNamedElement(race_node, "competitors"); // add node that contains all competitors
                
                
                for (Competitor competitor : trackedRace.getCompetitorsFromBestToWorst(trackedRace.getEndOfTracking())) {
                    final Element competitor_node = addNamedElement(competitors_node, "competitor"); // add competitor node
                    
                    try {
                        addNamedElementWithValue(competitor_node, "name", competitor.getName());
                        addNamedElementWithValue(competitor_node, "nationality", competitor.getTeam()
                                .getNationality().getThreeLetterIOCAcronym());
                        addNamedElementWithValue(competitor_node, "sail_id", competitor.getBoat().getSailID());
                                String sail_id = competitor.getBoat().getSailID();
                                if (sail_id.matches("^[A-Z]{3}\\s[0-9]*")) {                                        
                                    
                                    Pattern regex = Pattern.compile("(^[A-Z]{3})\\s([0-9]*)");
                                    Matcher regexMatcher = regex.matcher(sail_id);
                                    try {
                                        String resultString = regexMatcher.replaceAll("$1$2");
                                        addNamedElementWithValue(competitor_node, "sail_id_formatted", resultString);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    
                                } else if (sail_id.matches("^[A-Z]{3}\\S[0-9]*")) {
                                    addNamedElementWithValue(competitor_node, "sail_id_formatted", sail_id);
                                } else if (sail_id.matches("[0-9]*")){
                                    addNamedElementWithValue(competitor_node, "sail_id_formatted", competitor.getTeam().getNationality().getThreeLetterIOCAcronym() + sail_id);
                        } else {
                                    addNamedElementWithValue(competitor_node, "sail_id_formatted", sail_id);
                        }
    
                        addNamedElementWithValue(competitor_node, "race_final_rank", trackedRace.getRank(competitor, now));
                        addNamedElementWithValue(competitor_node, "race_participants", trackedRace.getCompetitorsFromBestToWorst(trackedRace.getStartOfTracking()).size());
                        addNamedElementWithValue(competitor_node, "race_relative_final_rank", 1.0 - ((trackedRace.getRank(competitor, now) - 1.0)/(trackedRace.getCompetitorsFromBestToWorst(trackedRace.getStartOfTracking()).size() - 1.0)));

                        addNamedElementWithValue(competitor_node, "distance_traveled_m", trackedRace.getDistanceTraveled(competitor, trackedRace.getEndOfTracking()).getMeters());
                        addNamedElementWithValue(competitor_node, "avg_xte_m", trackedRace.getAverageCrossTrackError(competitor, trackedRace.getEndOfTracking(), true).getMeters());
                        
                        TrackedLeg previousLeg = null;
                        ArrayList<Integer> posGLlist = new ArrayList<Integer>();
                        Double race_time = 0.0;
                        int rankAfterFirstLeg = -3;
                        for (final TrackedLeg trackedLeg : trackedRace.getTrackedLegs()) {
                            final Leg leg = trackedLeg.getLeg();
                            TrackedLegOfCompetitor trackedLegOfCompetitor = trackedLeg.getTrackedLeg(competitor); // Get data
                            TimePoint compareLegEnd = new MillisecondsTimePoint(0);
                            // time elapsed / when did the competitor pass the end mark of the leg
                            MarkPassing mp = trackedRace.getMarkPassing(competitor, leg.getTo());
                            if (mp != null) {
                                compareLegEnd = mp.getTimePoint();
                                
                            } else {
                                continue;
                            }
                            
                            if (trackedLegOfCompetitor == null || compareLegEnd == null) {
                                continue;
                            }
                            
                            Double compLegTimeAlt = -1.0;
                            try {
                                compLegTimeAlt = trackedLegOfCompetitor.getTimeInMilliSeconds(compareLegEnd) / 1000.0;
                                race_time += compLegTimeAlt;
                            } catch (Exception e1) {
//                                e1.printStackTrace();
//                                If this happens, the race is probably broken
                            }
                            
                            TimePoint compFinishedLeg = compareLegEnd;

                            // plausibility check
                            // competitor has finished the leg and the leg end time is not the race start time
                            if (trackedLegOfCompetitor.hasFinishedLeg(compFinishedLeg) && compareLegEnd.asMillis() != 0) {
                                // Calculate rank loss/gain
                                int posGL = 0;
                                if (previousLeg != null) {
                                    posGL = rankAtWaypoint.get(competitor).get(trackedLeg.getLeg().getTo()) -
                                            rankAtWaypoint.get(competitor).get(trackedLeg.getLeg().getFrom());
                                }
                                posGLlist.add(posGL * -1);
                            }
                            
                            // get rank after first leg
                            if (trackedLegOfCompetitor.getLeg().equals(trackedRace.getRace().getCourse().getFirstLeg())) {
                                rankAfterFirstLeg = trackedLegOfCompetitor.getRank(now);
                            }
                            
                            // assign the smallest start time for the next leg
                            minNextLegStart = (minNextLegStart > compFinishedLeg.asMillis() ? compFinishedLeg
                                    .asMillis() : minNextLegStart);
                            
                            legStarted = new MillisecondsTimePoint(minNextLegStart);
                            minNextLegStart = Long.MAX_VALUE;
                            previousLeg = trackedLeg;

                        }
                        
                        Double avg_rank_gain = 0.0;
                        int temp = 0;
                        for (int posGLvalue : posGLlist) {
                            temp = temp + posGLvalue;
                        }
                        if (posGLlist.size() != 0) {
                            avg_rank_gain = temp * 1.0 / posGLlist.size();
                        } 

                        addNamedElementWithValue(competitor_node, "avg_rank_gain", avg_rank_gain); // avg rank gain
                        addNamedElementWithValue(competitor_node, "rank_gains", posGLlist.toString()); // Ranks Gained/Lost
                        
                        addNamedElementWithValue(competitor_node, "rank_after_first_leg", rankAfterFirstLeg); // Rank after first leg
                        addNamedElementWithValue(competitor_node, "rank_gain_between_first_and_finish", rankAfterFirstLeg - trackedRace.getRank(competitor, now)); // Rank gain between first leg and finish
                        
                        addNamedElementWithValue(competitor_node, "race_time_s", race_time); 
                        
                        Double time_per_nm = (race_time / getCourseLength(trackedRace).getNauticalMiles());
                        
                        addNamedElementWithValue(competitor_node, "time_per_nm_s", time_per_nm); 
                        
                        // START ANALYSIS
                        // Distance to startline at race start
                        addNamedElementWithValue(competitor_node, "racestart_dist_to_startline_m", trackedRace.getDistanceToStartLine(competitor, trackedRace.getStartOfRace()).getMeters());
                        
                        // Speed at race start
                        addNamedElementWithValue(competitor_node, "racestart_speed_kn", trackedRace.getTrack(competitor).getEstimatedSpeed(trackedRace.getStartOfRace()).getKnots());
                        
                        // measures when competitor is passing the starting mark
                        NavigableSet<MarkPassing> competitorMarkPassings = trackedRace.getMarkPassings(competitor);
                        Speed competitorSpeedWhenPassingStart = null;
                        Tack startTack = null;
                        Distance distanceFromStarboardSideOfStartLineWhenPassingStart = null;
                        trackedRace.lockForRead(competitorMarkPassings);
                        try {
                            if (!competitorMarkPassings.isEmpty()) {
                                TimePoint competitorStartTime = competitorMarkPassings.iterator().next().getTimePoint();
                                competitorSpeedWhenPassingStart = trackedRace.getTrack(competitor).getEstimatedSpeed(
                                        competitorStartTime);
                                startTack = trackedRace.getTack(competitor, competitorStartTime);
                                distanceFromStarboardSideOfStartLineWhenPassingStart = trackedRace.getDistanceFromStarboardSideOfStartLineWhenPassingStart(competitor);
                            }
                        } finally {
                            trackedRace.unlockAfterRead(competitorMarkPassings);
                        }
                        
                        // Speed when passing start
                        addNamedElementWithValue(competitor_node, "racestart_speed_when_passing_start_kn", competitorSpeedWhenPassingStart.getKnots());
                        
                        // start tack
                        addNamedElementWithValue(competitor_node, "racestart_start_tack", startTack.toString());
                        
                        // distance from starboard side of startline when passing start
                        addNamedElementWithValue(competitor_node, "racestart_dist_from_starboard_side_of_startline_when_passing_start_m", distanceFromStarboardSideOfStartLineWhenPassingStart.getMeters());
                        
                        // END OF START ANALYSIS

                    } catch (Exception ex) {
                            //competitor_data_node.removeContent(competitor_node); // if the competitor dataset is not complete, remove it from the list
                                                                             // complete, remove it from the list
                    }
                }
            }
        }
        sendDocument(doc, leaderboard.getName() + ".xml");// output doc to client

    } // function end

    
    private Distance getCourseLengthAtStart(TrackedRace race) {
        return getCourseLengthAt(race, race.getStartOfRace());
    }
    
    private Distance getCourseLengthAt(TrackedRace race, TimePoint timePoint) {
        if (timePoint == null) {
            return null;
        }
        List<Leg> legs = race.getRace().getCourse().getLegs();
        Distance raceDistance = new MeterDistance(0);
        for (Leg leg : legs) {
            Waypoint from = leg.getFrom();
            Waypoint to = leg.getTo();
            Position fromPos = race.getApproximatePosition(from, timePoint);
            Position toPos = race.getApproximatePosition(to, timePoint);
            Distance legDistance = fromPos.getDistance(toPos);
            Distance inMeters = new MeterDistance(legDistance.getMeters());
            raceDistance = raceDistance.add(inMeters);
        }
        return raceDistance;
    }
    
    private Distance getCourseLength(TrackedRace race) {
        List<Leg> legs = race.getRace().getCourse().getLegs();
        Distance raceDistance = new NauticalMileDistance(0);
        for (Leg leg : legs) {
            Waypoint from = leg.getFrom();
            Iterable<MarkPassing> markPassings = race.getMarkPassingsInOrder(from);
            Iterator<MarkPassing> markPassingsIterator = markPassings.iterator();
            if (!markPassingsIterator.hasNext()) {
                return null;
            }
            MarkPassing firstPassing = markPassingsIterator.next();
            TimePoint timePointOfFirstPassing = firstPassing.getTimePoint();
            Waypoint to = leg.getTo();
            Position fromPos = race.getApproximatePosition(from, timePointOfFirstPassing);
            Position toPos = race.getApproximatePosition(to, timePointOfFirstPassing);
            Distance legDistance = fromPos.getDistance(toPos);
            raceDistance = raceDistance.add(legDistance);
        }
        return raceDistance;
    }



    private Map<Competitor, Map<Waypoint, Integer>> getRankAtWaypoint(TrackedRace trackedRace, TimePoint timePoint) {
        Map<Competitor, Map<Waypoint, Integer>> result = new HashMap<>();
        Iterable<Waypoint> waypoints = trackedRace.getRace().getCourse().getWaypoints();
        for (Waypoint waypoint : waypoints) {
            if (waypoint != trackedRace.getRace().getCourse().getFirstWaypoint()) {
                TrackedLeg trackedLeg = trackedRace.getTrackedLegFinishingAt(waypoint);
                LinkedHashMap<Competitor, Integer> ranks = trackedLeg.getRanks(timePoint);
                for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                    Map<Waypoint, Integer> map = result.get(competitor);
                    if (map == null) {
                        map = new HashMap<>();
                        result.put(competitor, map);
                    }
                    map.put(waypoint, trackedLeg.getTrackedLeg(competitor).getRank(timePoint));
                }
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

    private Pair<Double, Double> calculateAverageWindSpeedofRace(TrackedRace trackedRace) {
        Pair<Double, Double> result = null;
        if (trackedRace.getEndOfRace() != null) {
            TimePoint fromTimePoint = trackedRace.getStartOfRace();
            TimePoint toTimePoint = trackedRace.getEndOfRace();
            long resolutionInMilliseconds = 60 * 1000 * 5; // 5 min

            List<WindSource> windSourcesToDeliver = new ArrayList<WindSource>();
            WindSourceImpl windSource = new WindSourceImpl(WindSourceType.COMBINED);
            windSourcesToDeliver.add(windSource);

            double sumWindSpeed = 0.0;
            double sumWindSpeedConfidence = 0.0;
            double sumWindSpeedBeaufort = 0.0;
            int speedCounter = 0;

            int numberOfFixes = (int) ((toTimePoint.asMillis() - fromTimePoint.asMillis()) / resolutionInMilliseconds);
            WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
            TimePoint timePoint = fromTimePoint;
            for (int i = 0; i < numberOfFixes && toTimePoint != null && timePoint.compareTo(toTimePoint) < 0; i++) {
                WindWithConfidence<Pair<Position, TimePoint>> averagedWindWithConfidence = windTrack
                        .getAveragedWindWithConfidence(null, timePoint);
                if (averagedWindWithConfidence != null) {
                    double windSpeedinKnots = averagedWindWithConfidence.getObject().getKnots();
                    double confidence = averagedWindWithConfidence.getConfidence();
                    double windSpeedinBeaufort = averagedWindWithConfidence.getObject().getBeaufort();

                    sumWindSpeed += windSpeedinKnots;
                    sumWindSpeedConfidence += confidence;
                    sumWindSpeedBeaufort += windSpeedinBeaufort;

                    speedCounter++;
                }
                timePoint = new MillisecondsTimePoint(timePoint.asMillis() + resolutionInMilliseconds);
            }
            if (speedCounter > 0) {
                double averageWindSpeed = sumWindSpeed / speedCounter;
                double averageWindSpeedConfidence = sumWindSpeedConfidence / speedCounter;
                double averageWindSpeedBeaufort = sumWindSpeedBeaufort / speedCounter;

                result = new Pair<Double, Double>(averageWindSpeed, averageWindSpeedBeaufort);
            }
        } else {
            result = new Pair<Double, Double>(0.0, 0.0);
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

}
