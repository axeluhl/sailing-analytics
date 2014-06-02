package com.sap.sailing.xmlexport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Duration;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.LineDetails;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Util;

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
public class LeaderboardData extends ExportAction {
    
    private static final int MAX_EARLY_START_DIFFERENCE = 10*1000; // 10 seconds

    private final Logger log = Logger.getLogger(LeaderboardData.class.getName());
    
    private static final String VERY_LIGHT_WIND_DESCRIPTION = "Very Light";
    private static final String LIGHT_WIND_DESCRIPTION = "Light";
    private static final String MEDIUM_WIND_DESCRIPTION = "Medium";
    private static final String MEDIUM_STRONG_WIND_DESCRIPTION = "Medium Strong";
    private static final String STRONG_WIND_DESCRIPTION = "Strong";
    private static final String VERY_STRONG_WIND_DESCRIPTION = "Very Strong";
    
    public LeaderboardData(HttpServletRequest req, HttpServletResponse res, RacingEventService service) {
        super(req, res, service);
    }

    private Element createLeaderboardXML(Leaderboard leaderboard, List<Element> competitors, List<Element> races, Util.Pair<Double, Vector<String>> leaderboardConfidenceAndErrorMessages) {
        Element leaderboardElement = new Element("leaderboard");
        addNamedElementWithValue(leaderboardElement, "name", leaderboard.getName());
        addNamedElementWithValue(leaderboardElement, "display_name", leaderboard.getDisplayName());
        addNamedElementWithValue(leaderboardElement, "delay_to_live_in_millis", leaderboard.getDelayToLiveInMillis());
        addNamedElementWithValue(leaderboardElement, "scoring_scheme", leaderboard.getScoringScheme().getType().name());
        addNamedElementWithValue(leaderboardElement, "boat_class", getBoatClassName(leaderboard));
        leaderboardElement.addContent(createTimedXML("last_modification_", leaderboard.getTimePointOfLatestModification()));
        leaderboardElement.addContent(createDataConfidenceXML(leaderboardConfidenceAndErrorMessages));
        leaderboardElement.addContent(competitors);
        leaderboardElement.addContent(races);
        return leaderboardElement;
    }
    
    private List<Element> createTimedXML(String prefix, TimePoint timepoint) {
        List<Element> timedElements = new ArrayList<Element>();
        Calendar timedDate = Calendar.getInstance();
        if (timepoint != null) {
            timedDate.setTime(timepoint.asDate());
            
            timedElements.add(createNamedElementWithValue(prefix+"year", timedDate.get(Calendar.YEAR)));
            timedElements.add(createNamedElementWithValue(prefix+"month", timedDate.get(Calendar.MONTH)));
            timedElements.add(createNamedElementWithValue(prefix+"day", timedDate.get(Calendar.DAY_OF_MONTH)));
            timedElements.add(createNamedElementWithValue(prefix+"hour", timedDate.get(Calendar.HOUR_OF_DAY)));
            timedElements.add(createNamedElementWithValue(prefix+"minute", timedDate.get(Calendar.MINUTE)));
            timedElements.add(createNamedElementWithValue(prefix+"second", timedDate.get(Calendar.SECOND)));
            
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            timedElements.add(createNamedElementWithValue(prefix+"formatted", dateFormatter.format(timedDate.getTime())));
            timedElements.add(createNamedElementWithValue(prefix+"millis_since_epoch", timepoint.asMillis()));
        } else {
            timedElements.add(createNamedElementWithValue(prefix+"year", ""));
            timedElements.add(createNamedElementWithValue(prefix+"month", ""));
            timedElements.add(createNamedElementWithValue(prefix+"day", ""));
            timedElements.add(createNamedElementWithValue(prefix+"hour", ""));
            timedElements.add(createNamedElementWithValue(prefix+"minute", ""));
            timedElements.add(createNamedElementWithValue(prefix+"second", ""));
            timedElements.add(createNamedElementWithValue(prefix+"formatted", ""));
            timedElements.add(createNamedElementWithValue(prefix+"millis_since_epoch", ""));
        }
        return timedElements;
    }
    
    public List<Element> createWindXML(String prefix, SpeedWithConfidence<TimePoint> speedWithConfidence) {
        List<Element> windElements = new ArrayList<Element>();
        if (speedWithConfidence == null) {
            speedWithConfidence = new SpeedWithConfidenceImpl<TimePoint>(new KnotSpeedImpl(0.0), 0, MillisecondsTimePoint.now());
        }
        windElements.add(createNamedElementWithValue(prefix+"speed_in_knots", speedWithConfidence.getObject().getKnots()));
        windElements.add(createNamedElementWithValue(prefix+"speed_in_meters_per_second", speedWithConfidence.getObject().getMetersPerSecond()));
        windElements.add(createNamedElementWithValue(prefix+"speed_in_beaufort", Math.rint(speedWithConfidence.getObject().getBeaufort())));
        windElements.add(createNamedElementWithValue(prefix+"confidence", speedWithConfidence.getConfidence()));
        
        double speedInKnots = speedWithConfidence.getObject().getKnots();
        String windSpeedAsHumanReadableString = "";
        String windSpeedAsInterval = "";
        if (speedInKnots <= 4) {
            windSpeedAsHumanReadableString = VERY_LIGHT_WIND_DESCRIPTION;
            windSpeedAsInterval = "0-4kn";
        } else if (speedInKnots > 4 && speedInKnots <= 8) {
            windSpeedAsHumanReadableString = LIGHT_WIND_DESCRIPTION;
            windSpeedAsInterval = "4-8kn";
        } else if (speedInKnots > 8 && speedInKnots <= 12) {
            windSpeedAsHumanReadableString = MEDIUM_WIND_DESCRIPTION;
            windSpeedAsInterval = "8-12kn";
        } else if (speedInKnots > 12 && speedInKnots <= 16) {
            windSpeedAsHumanReadableString = MEDIUM_STRONG_WIND_DESCRIPTION;
            windSpeedAsInterval = "12-16kn";
        } else if (speedInKnots > 16 && speedInKnots <= 20) {
            windSpeedAsHumanReadableString = STRONG_WIND_DESCRIPTION;
            windSpeedAsInterval = "16-20kn";
        } else if (speedInKnots > 20) {
            windSpeedAsHumanReadableString = VERY_STRONG_WIND_DESCRIPTION;
            windSpeedAsInterval = "20-80kn";
        }
        windElements.add(createNamedElementWithValue(prefix+"human_readable", windSpeedAsHumanReadableString));
        windElements.add(createNamedElementWithValue(prefix+"knots_interval", windSpeedAsInterval));
        return windElements;
    }
    
    private Element createRaceXML(final TrackedRace race, final Fleet fleet, final List<Element> legs, final RaceColumn column, final Leaderboard leaderboard, int sameDayGroupIndex, int raceCounter, Util.Pair<Double, Vector<String>> raceConfidenceAndErrorMessages) throws NoWindException, IOException, ServletException {
        Element raceElement = new Element("race");
        addNamedElementWithValue(raceElement, "name", cleanRaceName(race.getRace().getName()));
        addNamedElementWithValue(raceElement, "race_index_in_leaderboard", raceCounter);
        addNamedElementWithValue(raceElement, "fleet_name", fleet.getName());
        addNamedElementWithValue(raceElement, "same_day_index", sameDayGroupIndex);
        addNamedElementWithValue(raceElement, "is_live", race.isLive(MillisecondsTimePoint.now()) ? "true" : "false");
        
        addNamedElementWithValue(raceElement, "start_type", race.getTrackedLeg(race.getRace().getCourse().getFirstLeg()).getLegType(race.getStartOfRace()).name());
        addNamedElementWithValue(raceElement, "delay_to_live_in_millis", race.getDelayToLiveInMillis());
        addNamedElementWithValue(raceElement, "multiplication_factor", column.getExplicitFactor() == null ? 1.0d : column.getExplicitFactor());
        
        addNamedElementWithValue(raceElement, "timepoint_of_last_event_as_millis", handleValue(race.getTimePointOfLastEvent()));
        addNamedElementWithValue(raceElement, "timepoint_of_newest_event_as_millis", handleValue(race.getTimePointOfNewestEvent()));
        addNamedElementWithValue(raceElement, "timepoint_of_oldest_event_as_millis", handleValue(race.getTimePointOfOldestEvent()));
        
        if (race.isLive(MillisecondsTimePoint.now())) {
            // we can't tell meaningful values about live races
            raceElement.addContent(createDataConfidenceXML(raceConfidenceAndErrorMessages));
            raceElement.addContent(legs);
            return raceElement;
        }
        
        raceElement.addContent(createTimedXML("start_time_", race.getStartOfRace()));
        raceElement.addContent(createTimedXML("end_time_", race.getEndOfRace()));
        addNamedElementWithValue(raceElement, "start_of_tracking_time_as_millis", handleValue(race.getStartOfTracking()));
        addNamedElementWithValue(raceElement, "end_of_tracking_time_as_millis", handleValue(race.getEndOfTracking()));
        
        LineDetails start = race.getStartLine(race.getStartOfTracking());
        addNamedElementWithValue(raceElement, "start_line_length_in_meters", start.getLength().getMeters());
        addNamedElementWithValue(raceElement, "start_advantage_in_meters", start.getAdvantage().getMeters());
        addNamedElementWithValue(raceElement, "advantageous_side_while_approaching_start_line", start.getAdvantageousSideWhileApproachingLine().name());

        addNamedElementWithValue(raceElement, "course_length_in_meters", race.getCourseLength().getMeters());
        raceElement.addContent(createWindXML("wind_", race.getAverageWindSpeedWithConfidence(/*resolutionInMillis*/ 5*60*1000)));
        
        final List<Competitor> allCompetitors = race.getCompetitorsFromBestToWorst(/*timePoint*/ race.getEndOfRace());
        addNamedElementWithValue(raceElement, "race_participant_count", allCompetitors.size());
        
        boolean isDiscarded = leaderboard.isDiscarded(allCompetitors.get(0), column, race.getEndOfRace());
        addNamedElementWithValue(raceElement, "is_discarded_by_looking_at_first_competitor", isDiscarded == true ? "true" : "false");
        
        // sort competitors according to their distance to the starboard side of the start line
        List<Competitor> allCompetitorsSortedByDistanceToStarboardSide = new ArrayList<Competitor>(allCompetitors.size());
        allCompetitorsSortedByDistanceToStarboardSide.addAll(allCompetitors);
        Collections.sort(allCompetitorsSortedByDistanceToStarboardSide, new Comparator<Competitor>() {
            @Override
            public int compare(Competitor o1, Competitor o2) {
                Distance o1Distance = race.getDistanceFromStarboardSideOfStartLineWhenPassingStart(o1);
                Distance o2Distance = race.getDistanceFromStarboardSideOfStartLineWhenPassingStart(o2);
                if (o1Distance != null && o2Distance != null) {
                        return o1Distance.compareTo(o2Distance);
                } else if (o1Distance == null && o2Distance == null) {
                    return 0;
                } else {
                    // one of the distances is null - we can't really tell
                    // which competitor is better in terms of sorting but we can try
                    // by giving the competitor that has a null value a very big distance
                    if (o1Distance == null) {
                        return new MeterDistance(100000).compareTo(o2Distance);
                    } else {
                        return o1Distance.compareTo(new MeterDistance(100000));
                    }
                }
            }
        });
        
        Map<Competitor, Integer> competitorToDistanceRank = new HashMap<Competitor, Integer>();
        int raceRankSorted = 0;
        for (Competitor competitorSorted : allCompetitorsSortedByDistanceToStarboardSide) {
            raceRankSorted += 1;
            competitorToDistanceRank.put(competitorSorted, raceRankSorted);
        }
        
        // it can happen that there are competitors that did not race but got points
        int additionalCompetitorCount = 0;
        for (Competitor competitorInLeaderboard : leaderboard.getAllCompetitors()) {
            if (!allCompetitors.contains(competitorInLeaderboard)) {
                // found a competitor that is available in leaderboard but not in that race
                // check if he has an overwritten score for that race
                MaxPointsReason mpr = leaderboard.getScoreCorrection().getMaxPointsReason(competitorInLeaderboard, column, race.getEndOfRace());
                if (mpr != null && !mpr.equals(MaxPointsReason.NONE)) {
                    // add this competitor to the list to have him evaluated
                    Element competitorElement = createCompetitorXML(competitorInLeaderboard, leaderboard, /*shortVersion*/ true, null);
                    Element competitorRaceDataElement = new Element("competitor_race_data");
                    MaxPointsReason maxPointsReason = leaderboard.getMaxPointsReason(competitorInLeaderboard, column, race.getEndOfRace());
                    addNamedElementWithValue(competitorRaceDataElement, "max_points_reason", maxPointsReason.toString()); 
                    boolean isDiscardedForCompetitor = leaderboard.isDiscarded(competitorInLeaderboard, column, race.getEndOfRace());
                    addNamedElementWithValue(competitorRaceDataElement, "is_discarded", isDiscardedForCompetitor == true ? "true" : "false");
                    Double finalRaceScore = leaderboard.getTotalPoints(competitorInLeaderboard, column, race.getEndOfRace());
                    addNamedElementWithValue(competitorRaceDataElement, "final_race_score", finalRaceScore);
                    competitorElement.addContent(competitorRaceDataElement);
                    raceElement.addContent(competitorElement);
                    raceConfidenceAndErrorMessages.getB().add("Competitor " + competitorInLeaderboard.getName() + " has no valid data for this race!");
                    additionalCompetitorCount++;
                }
            }
        }
        addNamedElementWithValue(raceElement, "race_score_participant_count", allCompetitors.size()+additionalCompetitorCount);
        
        int raceRank = 0;
        for (Competitor competitor : allCompetitors) {
            Element competitorElement = createCompetitorXML(competitor, leaderboard, /*shortVersion*/ true, null);
            Element competitorRaceDataElement = new Element("competitor_race_data");
            MaxPointsReason maxPointsReason = leaderboard.getMaxPointsReason(competitor, column, race.getEndOfRace());
            addNamedElementWithValue(competitorRaceDataElement, "max_points_reason", maxPointsReason.toString()); 
            boolean isDiscardedForCompetitor = leaderboard.isDiscarded(competitor, column, race.getEndOfRace());
            addNamedElementWithValue(competitorRaceDataElement, "is_discarded", isDiscardedForCompetitor == true ? "true" : "false");
            Double finalRaceScore = leaderboard.getTotalPoints(competitor, column, race.getEndOfRace());
            addNamedElementWithValue(competitorRaceDataElement, "final_race_score", finalRaceScore);
            if (maxPointsReason.equals(MaxPointsReason.DNS) || race.getMarkPassings(competitor).isEmpty()) {
                // we do not want to include competitors that did not start the race
                competitorElement.addContent(competitorRaceDataElement);
                raceElement.addContent(competitorElement);
                raceConfidenceAndErrorMessages.getB().add("Competitor " + competitor.getName() + " has no valid data for this race!");
                continue;
            }

            List<Maneuver> maneuvers = getManeuvers(race, competitor, /*waitForLatest*/ false);
            addNamedElementWithValue(competitorRaceDataElement, "number_of_maneuvers", maneuvers != null ? maneuvers.size() : 0);
            addNamedElementWithValue(competitorRaceDataElement, "number_of_tacks", getNumberOfTacks(maneuvers));
            addNamedElementWithValue(competitorRaceDataElement, "number_of_jibes", getNumberOfJibes(maneuvers));
            addNamedElementWithValue(competitorRaceDataElement, "number_of_penalty_circles", getNumberOfPenaltyCircles(maneuvers));

            GPSFixTrack<Competitor, GPSFixMoving> gpsFixesForCompetitor = race.getTrack(competitor);
            if (gpsFixesForCompetitor != null) {
                Duration averageIntervall = gpsFixesForCompetitor.getAverageIntervalBetweenFixes();
                if (averageIntervall != null) {
                    addNamedElementWithValue(competitorRaceDataElement, "average_interval_between_fixes_outliers_removed_as_millis", averageIntervall.asMillis());
                }
                Duration averageIntervallRaw = gpsFixesForCompetitor.getAverageIntervalBetweenRawFixes();
                if (averageIntervallRaw != null) {
                    addNamedElementWithValue(competitorRaceDataElement, "average_interval_between_fixes_raw_as_millis", averageIntervallRaw.asMillis());
                }
            }
                
            int[] timePointsInSecondsBeforeStart = new int[]{0, 5, 10, 20, 30};
            for (int i : timePointsInSecondsBeforeStart) {
                TimePoint beforeRaceStartTime = race.getStartOfRace().minus(i*1000);
                addNamedElementWithValue(competitorRaceDataElement, "distance_to_start_line_"+i+"seconds_before_start_in_meters", race.getDistanceToStartLine(competitor, beforeRaceStartTime).getMeters());
                addNamedElementWithValue(competitorRaceDataElement, "speed_"+i+"seconds_before_start_of_race_in_knots", race.getTrack(competitor).getEstimatedSpeed(beforeRaceStartTime).getKnots());
                addNamedElementWithValue(competitorRaceDataElement, "distance_from_starboard_side_of_start_line_"+i+"seconds_before_start_in_meters", race.getDistanceFromStarboardSideOfStartLine(competitor, beforeRaceStartTime).getMeters());
                
                Iterator<Mark> marksForStartLine = race.getStartLine(beforeRaceStartTime).getWaypoint().getControlPoint().getMarks().iterator();
                Mark first = marksForStartLine.next();
                Mark second = null;
                if (marksForStartLine.hasNext()) {
                    second = marksForStartLine.next();
                    Position firstMarkPosition = race.getOrCreateTrack(first).getEstimatedPosition(beforeRaceStartTime, /*extrapolate*/ false);
                    Position secondMarkPosition = race.getOrCreateTrack(second).getEstimatedPosition(beforeRaceStartTime, /*extrapolate*/ false);
                    Position competitorPosition = race.getTrack(competitor).getEstimatedPosition(beforeRaceStartTime, /*extrapolate*/ false);
                    if (firstMarkPosition != null && secondMarkPosition != null) {
                        Position projectedCompetitorPositionOntoStartLine = getOrthogonalProjectionOntoLine(
                            competitorPosition,
                            firstMarkPosition,
                            secondMarkPosition
                            );
                        // now compute the distance from starboard mark
                        Mark starboardMark = race.getStartLine(beforeRaceStartTime).getStarboardMarkWhileApproachingLine();
                        Mark portMark = null;
                        if (starboardMark.equals(first)) {
                            portMark = second;
                        } else {
                            portMark = first;
                        }
                        Position portMarkPosition = race.getOrCreateTrack(portMark).getEstimatedPosition(beforeRaceStartTime, /*extrapolate*/ false);
                        Position starboardMarkPosition = race.getOrCreateTrack(starboardMark).getEstimatedPosition(beforeRaceStartTime, /*extrapolate*/ false);
                        Distance projectedDistance = projectedCompetitorPositionOntoStartLine.getDistance(starboardMarkPosition);
                        Bearing bearingToStarboardMarkFromCompetitorPosition = competitorPosition.getBearingGreatCircle(starboardMarkPosition);
                        Bearing bearingFromPortToStarboardMark = portMarkPosition.getBearingGreatCircle(starboardMarkPosition);
                        Bearing bearingFromProjectedCompetitorPositionToStarboardMark = projectedCompetitorPositionOntoStartLine.getBearingGreatCircle(starboardMarkPosition);
                        addNamedElementWithValue(competitorRaceDataElement, "bearing_fom_port_mark_to_starboard_mark"+i+"seconds_before_start_in_degrees", bearingFromPortToStarboardMark.getDegrees());
                        addNamedElementWithValue(competitorRaceDataElement, "bearing_fom_projected_position_to_starboard_mark"+i+"seconds_before_start_in_degrees", bearingFromProjectedCompetitorPositionToStarboardMark.getDegrees());
                        addNamedElementWithValue(competitorRaceDataElement, "bearing_to_starboard_mark_"+i+"seconds_before_start_in_degrees", bearingToStarboardMarkFromCompetitorPosition.getDegrees());
                        if (Math.rint(bearingFromPortToStarboardMark.getDegrees()) == Math.rint(bearingFromProjectedCompetitorPositionToStarboardMark.getDegrees())) {
                            addNamedElementWithValue(competitorRaceDataElement, "competitor_in_startline_box_"+i+"seconds_before_start", "true");
                            addNamedElementWithValue(competitorRaceDataElement, "projected_starboard_mark_distance_"+i+"seconds_before_start_in_meters", projectedDistance.getMeters());
                        } else {
                            addNamedElementWithValue(competitorRaceDataElement, "competitor_in_startline_box_"+i+"seconds_before_start", "false");
                            addNamedElementWithValue(competitorRaceDataElement, "projected_starboard_mark_distance_"+i+"seconds_before_start_in_meters", 0-projectedDistance.getMeters());
                        }
                        addNamedElementWithValue(competitorRaceDataElement, "projected_position_onto_startline_"+i+"seconds_before_start", projectedCompetitorPositionOntoStartLine.toString());
                    }
                }
            }
            Tack startTack = race.getTack(competitor, race.getStartOfRace());
            addNamedElementWithValue(competitorRaceDataElement, "start_tack", startTack != null ? startTack.name() : "UNKNOWN");
            addNamedElementWithValue(competitorRaceDataElement, "starboard_mark_name", race.getStartLine(race.getStartOfRace()).getStarboardMarkWhileApproachingLine().getName());
            addNamedElementWithValue(competitorRaceDataElement, "distance_to_start_line_on_race_start_in_meters", race.getDistanceToStartLine(competitor, race.getStartOfRace()).getMeters());
            addNamedElementWithValue(competitorRaceDataElement, "speed_on_start_signal_of_race_in_knots", race.getTrack(competitor).getEstimatedSpeed(race.getStartOfRace()).getKnots());
            addNamedElementWithValue(competitorRaceDataElement, "distance_from_starboard_side_of_start_line_when_passing_start_in_meters", race.getDistanceFromStarboardSideOfStartLineWhenPassingStart(competitor).getMeters());
            addNamedElementWithValue(competitorRaceDataElement, "rank_based_on_distance_from_starboard_side_of_start_line", competitorToDistanceRank.get(competitor));
            addNamedElementWithValue(competitorRaceDataElement, "speed_when_crossing_start_line_in_knots", race.getSpeedWhenCrossingStartLine(competitor).getKnots());
            addNamedElementWithValue(competitorRaceDataElement, "maximum_race_speed_over_ground_in_knots", getMaximumSpeedOverGround(competitor, race).getKnots());
            Distance distanceTraveledInThisRace = race.getDistanceTraveled(competitor, race.getEndOfRace());
            if (distanceTraveledInThisRace == null) {
                raceConfidenceAndErrorMessages.getB().add("Competitor " + competitor.getName() + " has no valid distance traveled for this race!");
            }
            addNamedElementWithValue(competitorRaceDataElement, "distance_traveled_in_meters", distanceTraveledInThisRace == null ? 0.0 : distanceTraveledInThisRace.getMeters());
            addNamedElementWithValue(competitorRaceDataElement, "distance_traveled_including_non_finished_legs_in_meters", getDistanceTraveled(race, competitor, race.getEndOfRace(), /*alsoReturnDistanceIfCompetitorHasNotFinishedRace*/ true).getMeters());
            Speed averageSpeedOverGround = getAverageSpeedOverGround(race, competitor, race.getEndOfRace(), true);
            addNamedElementWithValue(competitorRaceDataElement, "average_speed_over_ground_in_knots", averageSpeedOverGround == null ? 0.0 : averageSpeedOverGround.getKnots());
            addNamedElementWithValue(competitorRaceDataElement, "final_race_rank", ++raceRank);
            TrackedLegOfCompetitor trackedLegOfCompetitor = race.getTrackedLeg(competitor, race.getRace().getCourse().getFirstLeg());
            if (trackedLegOfCompetitor != null && trackedLegOfCompetitor.getFinishTime() != null) {
                addNamedElementWithValue(competitorRaceDataElement, "rank_at_end_of_first_leg", trackedLegOfCompetitor.getRank(trackedLegOfCompetitor.getFinishTime()));
            } else {
                raceConfidenceAndErrorMessages.getB().add("It seems that competitor " + competitor.getName() + " has not finished first leg!");
                addNamedElementWithValue(competitorRaceDataElement, "rank_at_end_of_first_leg", 0);
            }
            Distance averageCrossTrackError = race.getAverageAbsoluteCrossTrackError(competitor, race.getStartOfRace(), race.getEndOfRace(), /*upwindOnly*/ false, /*waitForLatestAnalysis*/ false);
            addNamedElementWithValue(competitorRaceDataElement, "average_cross_track_error_in_meters", averageCrossTrackError != null ? averageCrossTrackError.getMeters() : -1.0);
            Distance averageSignedCrossTrackError = race.getAverageSignedCrossTrackError(competitor, race.getStartOfRace(), race.getEndOfRace(), /*upwindOnly*/ false, /*waitForLatestAnalysis*/ false);
            addNamedElementWithValue(competitorRaceDataElement, "average_signed_cross_track_error_in_meters", averageSignedCrossTrackError != null ? averageSignedCrossTrackError.getMeters() : -1.0);
            competitorElement.addContent(competitorRaceDataElement);
            raceElement.addContent(competitorElement);
        }

        raceElement.addContent(createDataConfidenceXML(raceConfidenceAndErrorMessages));
        raceElement.addContent(legs);
        return raceElement;
    }
    
    private Element createCompetitorXML(Competitor competitor, Leaderboard leaderboard, boolean shortVersion, Util.Pair<Double, Vector<String>> competitorConfidenceAndErrorMessages) throws NoWindException, IOException, ServletException {
        TimePoint timeSpent = MillisecondsTimePoint.now();
        Element competitorElement = new Element("competitor");
        addNamedElementWithValue(competitorElement, "uuid", competitor.getId().toString());
        addNamedElementWithValue(competitorElement, "name", competitor.getName());
        
        if (shortVersion)
            return competitorElement;
        
        if (competitor.getBoat() != null) {
            addNamedElementWithValue(competitorElement, "sail_id", cleanSailId(competitor.getBoat().getSailID(), competitor));
            addNamedElementWithValue(competitorElement, "boat_class", competitor.getBoat().getBoatClass().getName());
            addNamedElementWithValue(competitorElement, "boat_name", competitor.getBoat().getName());
        } else {
            addNamedElementWithValue(competitorElement, "sail_id", "");
            addNamedElementWithValue(competitorElement, "boat_class", "");
            addNamedElementWithValue(competitorElement, "boat_name", "");
        }
        
        if (competitor.getTeam() != null && competitor.getTeam().getNationality() != null) {
            addNamedElementWithValue(competitorElement, "nationality_name", competitor.getTeam().getNationality().getName());
            addNamedElementWithValue(competitorElement, "nationality_ioc", competitor.getTeam().getNationality().getThreeLetterIOCAcronym());
        } else {
            addNamedElementWithValue(competitorElement, "nationality_name", "");
            addNamedElementWithValue(competitorElement, "nationality_ioc", "");
        }
        
        if (leaderboard.getTimePointOfLatestModification() != null) {
            TimePoint timePointOfLatestModification = leaderboard.getTimePointOfLatestModification();
            Duration totalTimeSailed = leaderboard.getTotalTimeSailed(competitor, timePointOfLatestModification);
            addNamedElementWithValue(competitorElement, "total_time_sailed_in_milliseconds", totalTimeSailed.asMillis());
            addNamedElementWithValue(competitorElement, "total_time_sailed_including_non_finished_races_in_milliseconds", getTotalTimeSailedInMilliseconds(competitor, timePointOfLatestModification, true));
            Distance totalDistanceSailed = leaderboard.getTotalDistanceTraveled(competitor, timePointOfLatestModification);
            addNamedElementWithValue(competitorElement, "total_distance_sailed_in_meters", totalDistanceSailed != null ? totalDistanceSailed.getMeters() : 0);
            addNamedElementWithValue(competitorElement, "total_distance_sailed_including_non_finished_races_in_meters", getTotalDistanceTraveled(leaderboard, competitor, timePointOfLatestModification).getMeters());
            if (totalDistanceSailed == null) {
                competitorConfidenceAndErrorMessages.getB().add("Competitor has not finished all races in this leaderboard! His distance sailed is not comparable to others!");
            }
            addNamedElementWithValue(competitorElement, "maximum_speed_over_ground_in_knots", leaderboard.getMaximumSpeedOverGround(competitor, timePointOfLatestModification).getB().getKnots());
            Speed averageSpeed = leaderboard.getAverageSpeedOverGround(competitor, timePointOfLatestModification);
            addNamedElementWithValue(competitorElement, "average_speed_over_ground_from_start_mark_passing_in_knots", averageSpeed != null ? averageSpeed.getKnots() : 0);
            if (averageSpeed == null) {
                competitorConfidenceAndErrorMessages.getB().add("Competitor has not finished all races in this leaderboard! His average speed over ground is not comparable to others!");
            }
            Speed averageSpeedOverGroundIncludingNonCompletedRaces = getAverageSpeedOverGround(leaderboard, competitor, timePointOfLatestModification, true);
            addNamedElementWithValue(competitorElement, "average_speed_over_ground_including_non_finished_races_in_knots", averageSpeedOverGroundIncludingNonCompletedRaces == null ? 0 : averageSpeedOverGroundIncludingNonCompletedRaces.getKnots());
            
            addNamedElementWithValue(competitorElement, "overall_rank", leaderboard.getTotalRankOfCompetitor(competitor, timePointOfLatestModification));
            addNamedElementWithValue(competitorElement, "overall_score", leaderboard.getTotalPoints(competitor, timePointOfLatestModification));
        } else {
            TimePoint now = MillisecondsTimePoint.now();
            addNamedElementWithValue(competitorElement, "total_time_sailed_in_milliseconds", leaderboard.getTotalTimeSailed(competitor, now).asMillis());
            addNamedElementWithValue(competitorElement, "total_time_sailed_including_non_finished_races_in_milliseconds", getTotalTimeSailedInMilliseconds(competitor, now, true));
            Distance totalDistanceSailed = leaderboard.getTotalDistanceTraveled(competitor, now);
            addNamedElementWithValue(competitorElement, "total_distance_sailed_in_meters", totalDistanceSailed != null ? totalDistanceSailed.getMeters() : 0);
            if (totalDistanceSailed == null) {
                competitorConfidenceAndErrorMessages.getB().add("Competitor has not finished all races in this leaderboard! His distance sailed is not comparable to others!");
            }
            addNamedElementWithValue(competitorElement, "total_distance_sailed_including_non_finished_races_in_meters", getTotalDistanceTraveled(leaderboard, competitor, now).getMeters());
            addNamedElementWithValue(competitorElement, "maximum_speed_over_ground_in_knots", leaderboard.getMaximumSpeedOverGround(competitor, now).getB().getKnots());
            Speed averageSpeed = leaderboard.getAverageSpeedOverGround(competitor, now);
            addNamedElementWithValue(competitorElement, "average_speed_over_ground_from_start_mark_passing_in_knots", averageSpeed != null ? averageSpeed.getKnots() : 0);
            if (averageSpeed == null) {
                competitorConfidenceAndErrorMessages.getB().add("Competitor has not finished all races in this leaderboard! His average speed over ground is not comparable to others!");
            }
            Speed averageSpeedOverGroundIncludingNonCompletedRaces = getAverageSpeedOverGround(leaderboard, competitor, now, true);
            addNamedElementWithValue(competitorElement, "average_speed_over_ground_including_non_finished_races_in_knots", averageSpeedOverGroundIncludingNonCompletedRaces == null ? 0 : averageSpeedOverGroundIncludingNonCompletedRaces.getKnots());
            
            addNamedElementWithValue(competitorElement, "overall_rank", leaderboard.getTotalRankOfCompetitor(competitor, now));
            addNamedElementWithValue(competitorElement, "overall_score", leaderboard.getTotalPoints(competitor, now));
        }
        competitorElement.addContent(createDataConfidenceXML(competitorConfidenceAndErrorMessages));
        TimePoint elapsedTime = MillisecondsTimePoint.now().minus(timeSpent.asMillis());
        addNamedElementWithValue(competitorElement, "generation_time_in_milliseconds", elapsedTime.asMillis());
        return competitorElement;
    }
    
    private Element createLegXML(TrackedLeg trackedLeg, Leaderboard leaderboard, int legCounter, Util.Pair<Double, Vector<String>> raceConfidenceAndErrorMessages, Util.Pair<Double, Vector<String>> legConfidenceAndErrorMessages) throws NoWindException, IOException, ServletException {
        TimePoint timeSpent = MillisecondsTimePoint.now();
        Leg leg = trackedLeg.getLeg();
        Element legElement = new Element("leg");
        legElement.addContent(createDataConfidenceXML(legConfidenceAndErrorMessages));
        if (trackedLeg.getTrackedRace().isLive(MillisecondsTimePoint.now())) {
            return legElement;
        }
        addNamedElementWithValue(legElement, "position", legCounter);
        addNamedElementWithValue(legElement, "mark_from", leg.getFrom().getName());
        addNamedElementWithValue(legElement, "mark_to", leg.getTo().getName());
        addNamedElementWithValue(legElement, "leg_type", trackedLeg.getLegType(trackedLeg.getTrackedRace().getStartOfRace()).name());
        addNamedElementWithValue(legElement, "great_circle_distance_at_start_of_race_in_meters", trackedLeg.getGreatCircleDistance(trackedLeg.getTrackedRace().getStartOfRace()).getMeters());
        addNamedElementWithValue(legElement, "great_circle_distance_at_end_of_race_in_meters", trackedLeg.getGreatCircleDistance(trackedLeg.getTrackedRace().getEndOfRace()).getMeters());
        
        for (Competitor competitor : trackedLeg.getTrackedRace().getCompetitorsFromBestToWorst(/*timePoint*/ trackedLeg.getTrackedRace().getEndOfRace())) {
            Element competitorElement = createCompetitorXML(competitor, leaderboard, /*shortVersion*/ true, null);
            Element competitorLegDataElement = new Element("competitor_leg_data");
            TrackedLegOfCompetitor competitorLeg = trackedLeg.getTrackedLeg(competitor);
            
            /* if there is no start time then ignore all data of this leg */
            if (competitorLeg == null || competitorLeg.getStartTime() == null || competitorLeg.getFinishTime() == null || competitorLeg.getTrackedLeg().getTrackedRace().getMarkPassings(competitor).isEmpty()) {
                competitorElement.addContent(competitorLegDataElement);
                legElement.addContent(competitorElement);
                continue;
            }
            
            TimePoint legFinishTime = competitorLeg.getFinishTime();
            competitorLegDataElement.addContent(createTimedXML("leg_started_time_", competitorLeg.getStartTime()));
            Util.Pair<GPSFixMoving, Speed> maximumSpeed = competitorLeg.getMaximumSpeedOverGround(legFinishTime);
            addNamedElementWithValue(competitorLegDataElement, "maximum_speed_over_ground_in_knots", maximumSpeed != null ? maximumSpeed.getB().getKnots() : -1);
            if (maximumSpeed == null) {
                legConfidenceAndErrorMessages.getB().add("Competitor "+ competitor.getName() +" has not finished this leg! His maximum speed for this leg is not comparable to others!");
            }
            Speed averageVelocityMadeGood = competitorLeg.getAverageVelocityMadeGood(legFinishTime);
            addNamedElementWithValue(competitorLegDataElement, "average_velocity_made_good_in_knots", averageVelocityMadeGood != null ? averageVelocityMadeGood.getKnots() : 0);
            addNamedElementWithValue(competitorLegDataElement, "leg_finished_time_as_millis", handleValue(legFinishTime));
            addNamedElementWithValue(competitorLegDataElement, "total_race_time_elapsed_as_millis", handleValue(legFinishTime)-handleValue(trackedLeg.getTrackedRace().getStartOfRace()));
            addNamedElementWithValue(competitorLegDataElement, "time_spend_in_this_leg_as_millis", competitorLeg.getTime(legFinishTime).asMillis());
            addNamedElementWithValue(competitorLegDataElement, "gap_to_leader_at_finish_in_seconds", competitorLeg.getGapToLeaderInSeconds(legFinishTime));
            Distance windwardDistanceToOverallLeader = competitorLeg.getWindwardDistanceToOverallLeader(legFinishTime);
            addNamedElementWithValue(competitorLegDataElement, "windward_distance_to_overall_leader_that_has_finished_this_leg_in_meters", windwardDistanceToOverallLeader != null ? windwardDistanceToOverallLeader.getMeters() : 0);
            addNamedElementWithValue(competitorLegDataElement, "distance_traveled_in_meters", competitorLeg.getDistanceTraveled(legFinishTime).getMeters());
            addNamedElementWithValue(competitorLegDataElement, "average_speed_over_ground_in_knots", competitorLeg.getAverageSpeedOverGround(legFinishTime).getKnots());

            addNamedElementWithValue(competitorLegDataElement, "number_of_jibes", competitorLeg.getNumberOfJibes(legFinishTime));
            addNamedElementWithValue(competitorLegDataElement, "number_of_tacks", competitorLeg.getNumberOfTacks(legFinishTime));
            addNamedElementWithValue(competitorLegDataElement, "number_of_penalty_circles", competitorLeg.getNumberOfPenaltyCircles(legFinishTime));
            
            addNamedElementWithValue(competitorLegDataElement, "leg_rank_at_leg_start", competitorLeg.getRank(competitorLeg.getStartTime()));
            addNamedElementWithValue(competitorLegDataElement, "leg_rank_at_leg_finish", competitorLeg.getRank(legFinishTime));
            
            if (legCounter>1) {
                addNamedElementWithValue(competitorLegDataElement, "rank_gain_for_this_leg_between_start_and_finish", competitorLeg.getRank(competitorLeg.getStartTime())-competitorLeg.getRank(legFinishTime));
            } else {
                addNamedElementWithValue(competitorLegDataElement, "rank_gain_for_this_leg_between_start_and_finish", 0);
            }
            
            List<Maneuver> maneuvers = competitorLeg.getManeuvers(legFinishTime, /*waitForLatest*/false);
            addNamedElementWithValue(competitorLegDataElement, "maneuver_count", maneuvers.size());
            Element maneuversElement = new Element("maneuvers");
            int maneuverCounter = 0;
            for (Maneuver man : maneuvers) {
                maneuverCounter += 1;
                Element maneuverInformation = new Element("maneuver");
                addNamedElementWithValue(maneuverInformation, "type", man.getType().toString());
                addNamedElementWithValue(maneuverInformation, "position", maneuverCounter);
                addNamedElementWithValue(maneuverInformation, "direction_change_in_degrees", man.getDirectionChangeInDegrees());
                Distance maneuverLoss = man.getManeuverLoss();
                addNamedElementWithValue(maneuverInformation, "loss_in_meters", maneuverLoss != null ? maneuverLoss.getMeters() : 0.0);
                addNamedElementWithValue(maneuverInformation, "speed_before_in_knots", man.getSpeedWithBearingBefore().getKnots());
                addNamedElementWithValue(maneuverInformation, "speed_after_in_knots", man.getSpeedWithBearingAfter().getKnots());
                maneuversElement.addContent(maneuverInformation);
            }
            competitorLegDataElement.addContent(maneuversElement);
            
            Distance averageCrossTrackError = competitorLeg.getAverageAbsoluteCrossTrackError(legFinishTime, /*waitForLatestAnalysis*/ false);
            addNamedElementWithValue(competitorLegDataElement, "average_cross_track_error_in_meters", averageCrossTrackError != null ? averageCrossTrackError.getMeters() : -1);
            Distance averageSignedCrossTrackError = competitorLeg.getAverageSignedCrossTrackError(legFinishTime, /*waitForLatestAnalysis*/ false);
            addNamedElementWithValue(competitorLegDataElement, "average_signed_cross_track_error_in_meters", averageSignedCrossTrackError != null ? averageSignedCrossTrackError.getMeters() : -1);
            
            competitorElement.addContent(competitorLegDataElement);
            legElement.addContent(competitorElement);
        }
        TimePoint elapsedTime = MillisecondsTimePoint.now().minus(timeSpent.asMillis());
        addNamedElementWithValue(legElement, "generation_time_in_milliseconds", elapsedTime.asMillis());
        return legElement;
    }
    
    private Element createDataConfidenceXML(Util.Pair<Double, Vector<String>> confidenceAndMessages) {
        Element confidenceDataElement = new Element("confidence");
        addNamedElementWithValue(confidenceDataElement, "simple_confidence_value", confidenceAndMessages.getA().doubleValue());
        Element messagesElement = new Element("messages");
        for (String message : confidenceAndMessages.getB()) {
            addNamedElementWithValue(messagesElement, "message", message);
        }
        confidenceDataElement.addContent(messagesElement);
        return confidenceDataElement;
    }
    
    private Util.Pair<Double, Vector<String>> checkData(TrackedLeg leg) throws Exception {
        double simpleConfidence = 1.0; Vector<String> messages = new Vector<String>();
        TimePoint now = MillisecondsTimePoint.now();
        for (TrackedLegOfCompetitor competitorLeg : leg.getTrackedLegsOfCompetitors()) {
            if (competitorLeg == null) {
                messages.add("Found one leg for a competitor that is null!");
                simpleConfidence = 0.0;
                continue;
            }
            if (competitorLeg.getCompetitor() == null) {
                messages.add("Found a leg where the associated competitor is just null!");
                simpleConfidence = 0.0;
                continue;
            }
            if (competitorLeg.getTrackedLeg().getTrackedRace().getMarkPassings(competitorLeg.getCompetitor()).isEmpty()) {
                messages.add("Competitor "+ competitorLeg.getCompetitor().getName() + " has no mark passings for the complete race!");
                simpleConfidence -= 0.02;
            }
            if (!competitorLeg.hasFinishedLeg(now) || competitorLeg.getFinishTime() == null) {
                messages.add("Competitor " + competitorLeg.getCompetitor().getName() + " has not finished this leg! Leg data won't be available!");
                simpleConfidence -= 0.08;
            }
            if (!competitorLeg.hasStartedLeg(now) || competitorLeg.getStartTime() == null) {
                messages.add("Competitor " + competitorLeg.getCompetitor().getName() + " has not started this leg! Leg data won't be available!");
                simpleConfidence -= 0.08;
            }
            if (competitorLeg.getMaximumSpeedOverGround(now) == null) {
                messages.add("Competitor " + competitorLeg.getCompetitor().getName() + " has no maximum speed for this leg!");
                simpleConfidence -= 0.05;
            }
            if (competitorLeg.getAverageAbsoluteCrossTrackError(now, /*waitForLatestAnalysis*/ false) == null) {
                messages.add("Competitor " + competitorLeg.getCompetitor().getName() + " has no average cross track error for this leg!");
                simpleConfidence -= 0.02;
            }
            if (competitorLeg.getAverageSignedCrossTrackError(now, /*waitForLatestAnalysis*/ false) == null) {
                messages.add("Competitor " + competitorLeg.getCompetitor().getName() + " has no average signed cross track error for this leg!");
                simpleConfidence -= 0.02;
            }
            if (competitorLeg.getStartTime() != null && competitorLeg.getStartTime().before(leg.getTrackedRace().getStartOfRace())) {
                if ((leg.getTrackedRace().getStartOfRace().asMillis()-competitorLeg.getStartTime().asMillis())>=MAX_EARLY_START_DIFFERENCE) {
                    messages.add("Competitor " + competitorLeg.getCompetitor().getName() + " has a start time of " + competitorLeg.getStartTime() + " that is 10 seconds or more before start of race time (" + leg.getTrackedRace().getStartOfRace() + ").");
                    simpleConfidence -= 0.01;
                }
            }
        }
        return new Util.Pair<Double, Vector<String>>(simpleConfidence, messages);
    }
    
    private Util.Pair<Double, Vector<String>> checkData(TrackedRace race) throws Exception {
        double simpleConfidence = 1.0; Vector<String> messages = new Vector<String>();
        TimePoint dateAtYear2000 = new MillisecondsTimePoint(946681200000l);
        if (race.getStartOfRace() == null || race.getStartOfRace().before(dateAtYear2000)) {
            messages.add("Start time of race " + race.getRaceIdentifier().getRaceName() + " is either null or not valid!");
            simpleConfidence -= 1;
        }
        if (race.getEndOfRace() == null || race.getEndOfRace().before(dateAtYear2000)) {
            messages.add("End time of race " + race.getRaceIdentifier().getRaceName() + " is either null or not valid!");
            simpleConfidence -= 1;
        }
        if (race.getStartOfTracking() == null || race.getStartOfTracking().before(dateAtYear2000)) {
            messages.add("Start tracking time of race " + race.getRaceIdentifier().getRaceName() + " is either null or not valid!");
            simpleConfidence -= 0.2;
        }
        if (race.getEndOfTracking() == null || race.getEndOfTracking().before(dateAtYear2000)) {
            messages.add("End tracking time of race " + race.getRaceIdentifier().getRaceName() + " is either null or not valid!");
            simpleConfidence -= 0.2;
        }
        if (race.isLive(MillisecondsTimePoint.now())) {
            messages.add("This race is live - data for this race will not be available until the race has been finished!");
        }
        return new Util.Pair<Double, Vector<String>>(simpleConfidence, messages);
    }
    
    private Util.Pair<Double, Vector<String>> checkData(Competitor competitor) throws Exception {
        double simpleConfidence = 1.0; Vector<String> messages = new Vector<String>();
        if (competitor.getName() == null || competitor.getName().equals("")) {
            messages.add("Competitor " + competitor.getId() + " has no name!");
            simpleConfidence -= 1;
        }
        if (competitor.getBoat() == null || competitor.getBoat().getSailID() == null || competitor.getBoat().getSailID().equals("")) {
            messages.add("Competitor " + competitor.getId() + " has no sail id that can be used!");
            simpleConfidence -= 1;
        }
        if (competitor.getTeam() == null || competitor.getTeam().getNationality() == null) {
            messages.add("Either team for "+ competitor.getId() + " is null or nationality could not be determined");
            simpleConfidence -= 0.2;
        }
        return new Util.Pair<Double, Vector<String>>(simpleConfidence, messages);
    }
    
    private Util.Pair<Double, Vector<String>> checkData(Leaderboard leaderboard) throws Exception {
        double simpleConfidence = 1.0; Vector<String> messages = new Vector<String>();
        int raceColumnCount = 0; int attachedRaceToColumnCount = 0;
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            if (raceColumn.hasTrackedRaces()) {
                attachedRaceToColumnCount++;
            }
            raceColumnCount++;
        }
        if (raceColumnCount == 0) {
            messages.add("Could not find any race columns or none of the race columns has tracked races attached!");
            simpleConfidence -= 1.0;
        }
        if (attachedRaceToColumnCount < raceColumnCount) {
            messages.add("Not all race columns contain attached races! Data being aggregated over all races could be wrong!");
            simpleConfidence -= 0.3;
        }
        int competitorCount = 0;
        for (Iterator<Competitor> iterator = leaderboard.getAllCompetitors().iterator(); iterator.hasNext();) {
            iterator.next();
            competitorCount++;
        }
        if (competitorCount == 0) {
            messages.add("Could not find any competitors in this leaderboard!");
            simpleConfidence -= 1.0;
        }
        TimePoint dateAtYear2000 = new MillisecondsTimePoint(946681200000l);
        if (leaderboard.getTimePointOfLatestModification() != null && leaderboard.getTimePointOfLatestModification().before(dateAtYear2000)) {
            messages.add("The time point of the last modification for this leaderboard is very old.");
            simpleConfidence -= 0.4;
        }
        return new Util.Pair<Double, Vector<String>>(simpleConfidence, messages);
    }
    
    private int getSameDayGroupIndex(TrackedRace currentRace, TrackedRace raceBefore) {
        if (raceBefore != null && !raceBefore.equals(currentRace)) {
            if (currentRace != null && currentRace.getStartOfRace() != null && raceBefore.getStartOfRace() != null) {
                Calendar timedDateForCurrentRace = Calendar.getInstance();
                timedDateForCurrentRace.setTime(currentRace.getStartOfRace().asDate());
                String formattedCurrent = new SimpleDateFormat("dd.MM.yyyy").format(timedDateForCurrentRace.getTime());
                
                Calendar timedDateForRaceBefore = Calendar.getInstance();
                timedDateForRaceBefore.setTime(raceBefore.getStartOfRace().asDate());
                String formattedBefore = new SimpleDateFormat("dd.MM.yyyy").format(timedDateForRaceBefore.getTime());
                
                if (formattedCurrent.equals(formattedBefore)) {
                    return 0;
                } 
            }
        } else {
            return 0;
        }
        return 1;
    }
    
    public void perform() throws Exception {
        final Leaderboard leaderboard = getLeaderboard();
        TimePoint timeSpent = MillisecondsTimePoint.now();
        log.info("Starting XML export of " + leaderboard.getName());
        Util.Pair<Double, Vector<String>> leaderboardConfidenceAndErrorMessages = checkData(leaderboard);
        
        final List<Element> racesElements = new ArrayList<Element>();
        final List<Element> competitorElements = new ArrayList<Element>();
        
        for (Competitor competitor : leaderboard.getAllCompetitors()) {
            Util.Pair<Double, Vector<String>> competitorConfidenceAndErrorMessages = checkData(competitor);
            competitorElements.add(createCompetitorXML(competitor, leaderboard, /*shortVersion*/ false, competitorConfidenceAndErrorMessages));
        }
        
        TrackedRace raceBefore = null; int sameDayGroupIndex = 0; int raceCounter = 0;
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                if (trackedRace != null && trackedRace.hasGPSData()) {
                    sameDayGroupIndex += getSameDayGroupIndex(raceColumn.getTrackedRace(fleet), raceBefore);
                    TimePoint timeSpentForRace = MillisecondsTimePoint.now();
                    Util.Pair<Double, Vector<String>> raceConfidenceAndErrorMessages = checkData(trackedRace);
                    final List<Element> legs = new ArrayList<Element>();
                    int legCounter = 0;
                    for (TrackedLeg leg : trackedRace.getTrackedLegs()) {
                        Util.Pair<Double, Vector<String>> legConfidenceAndErrorMessages = checkData(leg);
                        legs.add(createLegXML(leg, leaderboard, ++legCounter, raceConfidenceAndErrorMessages, legConfidenceAndErrorMessages));
                    }
                    Element raceElement = createRaceXML(trackedRace, fleet, legs, raceColumn, leaderboard, sameDayGroupIndex, ++raceCounter, raceConfidenceAndErrorMessages);
                    TimePoint elapsedTimeForRace = MillisecondsTimePoint.now().minus(timeSpentForRace.asMillis());
                    addNamedElementWithValue(raceElement, "generation_time_in_milliseconds", elapsedTimeForRace.asMillis());
                    racesElements.add(raceElement);
                    log.info("Exported complete race " + trackedRace.getRace().getName() + " in " + elapsedTimeForRace.asMillis() + " milliseconds!");
                    raceBefore = trackedRace;
                }
            }
        }
        
        log.info("Finished XML export of leaderboard " + leaderboard.getName() + " in " + MillisecondsTimePoint.now().minus(timeSpent.asMillis()).asMillis() + " milliseconds");
        sendDocument(createLeaderboardXML(leaderboard, competitorElements, racesElements, leaderboardConfidenceAndErrorMessages), leaderboard.getName() + ".xml");
    }
    

}
