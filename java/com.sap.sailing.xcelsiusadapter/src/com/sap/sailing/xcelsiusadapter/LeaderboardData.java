package com.sap.sailing.xcelsiusadapter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.LineLengthAndAdvantage;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
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
public class LeaderboardData extends ExportAction {
    
    private static final String VERY_LIGHT_WIND_DESCRIPTION = "Very Light";
    private static final String LIGHT_WIND_DESCRIPTION = "Light";
    private static final String MEDIUM_WIND_DESCRIPTION = "Medium";
    private static final String STRONG_WIND_DESCRIPTION = "Strong";
    private static final String VERY_STRONG_WIND_DESCRIPTION = "Very Strong";
    
    public LeaderboardData(HttpServletRequest req, HttpServletResponse res, RacingEventService service) {
        super(req, res, service);
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
        
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.mm.yyyy HH:MM:SS");
        timedElements.add(createNamedElementWithValue(prefix+"second", dateFormatter.format(timedDate)));
        
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
    
    private Element createRaceXML(TrackedRace race, Fleet fleet, List<Element> legs, RaceColumn column, Leaderboard leaderboard, int sameDayGroupIndex) throws NoWindException {
        // TODO: Plausibility checks (race has start time, ...)
        Element raceElement = new Element("race");
        addNamedElementWithValue(raceElement, "name", cleanRaceName(race.getRace().getName()));
        addNamedElementWithValue(raceElement, "fleet_name", fleet.getName());
        
        addNamedElementWithValue(raceElement, "delay_to_live_in_millis", race.getDelayToLiveInMillis());
        
        addNamedElementWithValue(raceElement, "timepoint_of_last_event_as_millis", handleValue(race.getTimePointOfLastEvent()));
        addNamedElementWithValue(raceElement, "timepoint_of_newest_event_as_millis", handleValue(race.getTimePointOfNewestEvent()));
        addNamedElementWithValue(raceElement, "timepoint_of_oldest_event_as_millis", handleValue(race.getTimePointOfOldestEvent()));
        
        addNamedElementWithValue(raceElement, "start_time_as_millis", handleValue(race.getStartOfRace()));
        raceElement.addContent(createTimedXML("start_time_", race.getStartOfRace()));
        addNamedElementWithValue(raceElement, "start_of_tracking_time_as_millis", handleValue(race.getStartOfTracking()));
        addNamedElementWithValue(raceElement, "end_time_as_millis", handleValue(race.getEndOfRace()));
        raceElement.addContent(createTimedXML("end_time_", race.getEndOfRace()));
        addNamedElementWithValue(raceElement, "end_of_tracking_time_as_millis", handleValue(race.getEndOfTracking()));
        addNamedElementWithValue(raceElement, "same_day_index", sameDayGroupIndex);
        
        addNamedElementWithValue(raceElement, "course_length_in_meters", race.getCourseLength().getMeters());
        
        raceElement.addContent(createWindXML("wind_", race.getAverageWindSpeedWithConfidence(/*resolutionInMinutes*/ 5)));
        
        final List<Competitor> allCompetitors = race.getCompetitorsFromBestToWorst(/*timePoint*/ race.getEndOfRace());
        addNamedElementWithValue(raceElement, "race_participants_count", allCompetitors.size());
        
        int raceRank = 0;
        for (Competitor competitor : allCompetitors) {
            Element competitorElement = createCompetitorXML(competitor, leaderboard, /*shortVersion*/ true);
            LineLengthAndAdvantage start = race.getStartLine(race.getStartOfTracking());
            addNamedElementWithValue(raceElement, "distance_to_start_line_on_race_start_in_meters", race.getDistanceToStartLine(competitor, race.getStartOfRace()).getMeters());
            addNamedElementWithValue(raceElement, "start_advantage_in_meters", start.getAdvantage().getMeters());
            addNamedElementWithValue(raceElement, "race_start_speed_in_knots", race.getTrack(competitor).getEstimatedSpeed(race.getStartOfRace()).getKnots());
            addNamedElementWithValue(raceElement, "advantageous_side_while_approaching_start_line", start.getAdvantageousSideWhileApproachingLine().name());
            addNamedElementWithValue(raceElement, "average_cross_track_error_in_meters", race.getAverageCrossTrackError(competitor, race.getEndOfRace(), /*waitForLatestAnalysis*/false).getMeters());
            addNamedElementWithValue(raceElement, "distance_traveled_in_meters", race.getDistanceTraveled(competitor, race.getEndOfRace()).getMeters());
            addNamedElementWithValue(raceElement, "final_race_rank", ++raceRank);
            addNamedElementWithValue(raceElement, "final_race_score", leaderboard.getScoringScheme().getScoreForRank(column, competitor, raceRank, new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return allCompetitors.size();
                }
            }));
            addNamedElementWithValue(competitorElement, "max_points_reason", leaderboard.getMaxPointsReason(competitor, column, race.getEndOfRace()).toString());
            raceElement.addContent(competitorElement);
        }
        
        raceElement.addContent(legs);
        return raceElement;
    }
    
    private Element createCompetitorXML(Competitor competitor, Leaderboard leaderboard, boolean shortVersion) throws NoWindException {
        Element competitorElement = new Element("competitor");
        addNamedElementWithValue(competitorElement, "uuid", competitor.getId().toString());
        if (shortVersion)
            return competitorElement;
        
        addNamedElementWithValue(competitorElement, "name", competitor.getName());
        addNamedElementWithValue(competitorElement, "nationality_name", competitor.getTeam().getNationality().getName());
        addNamedElementWithValue(competitorElement, "nationality_ioc", competitor.getTeam().getNationality().getThreeLetterIOCAcronym());
        addNamedElementWithValue(competitorElement, "sail_id", cleanSailId(competitor.getBoat().getSailID(), competitor));
        addNamedElementWithValue(competitorElement, "boat_name", competitor.getBoat().getName());
        addNamedElementWithValue(competitorElement, "boat_class", competitor.getBoat().getBoatClass().getName());
        
        addNamedElementWithValue(competitorElement, "total_time_sailed_in_milliseconds", leaderboard.getTotalTimeSailedInMilliseconds(competitor, leaderboard.getTimePointOfLatestModification()));
        addNamedElementWithValue(competitorElement, "total_distance_sailed_in_meters", leaderboard.getTotalDistanceTraveled(competitor, leaderboard.getTimePointOfLatestModification()).getMeters());
        addNamedElementWithValue(competitorElement, "maximum_speed_over_ground_in_knots", leaderboard.getMaximumSpeedOverGround(competitor, leaderboard.getTimePointOfLatestModification()).getB().getKnots());
        
        addNamedElementWithValue(competitorElement, "overall_rank", leaderboard.getTotalRankOfCompetitor(competitor, leaderboard.getTimePointOfLatestModification()));
        addNamedElementWithValue(competitorElement, "overall_score", leaderboard.getTotalPoints(competitor, leaderboard.getTimePointOfLatestModification()));
        
        // TODO: average_speed, max_speed, distance sailed
        return competitorElement;
    }
    
    private Element createLegXML(TrackedLeg trackedLeg, Leaderboard leaderboard, int legCounter) throws NoWindException {
        Leg leg = trackedLeg.getLeg();
        Element legElement = new Element("leg");
        addNamedElementWithValue(legElement, "position", legCounter);
        addNamedElementWithValue(legElement, "mark_from", leg.getFrom().getName());
        addNamedElementWithValue(legElement, "mark_to", leg.getTo().getName());
        addNamedElementWithValue(legElement, "leg_type", trackedLeg.getLegType(trackedLeg.getTrackedRace().getStartOfRace()).name());
        
        int raceRank = 0;
        for (Competitor competitor : trackedLeg.getTrackedRace().getCompetitorsFromBestToWorst(/*timePoint*/ trackedLeg.getTrackedRace().getEndOfRace())) {
            Element competitorElement = createCompetitorXML(competitor, leaderboard, /*shortVersion*/ true);
            TrackedLegOfCompetitor competitorLeg = trackedLeg.getTrackedLeg(competitor);
            TimePoint legFinishTime = competitorLeg.getFinishTime();
            // TODO: Plausability checks here
            addNamedElementWithValue(competitorElement, "leg_finished_time_as_millis", handleValue(legFinishTime));
            addNamedElementWithValue(competitorElement, "leg_started_time_as_millis", handleValue(competitorLeg.getStartTime()));
            addNamedElementWithValue(competitorElement, "total_race_time_elapsed_as_millis", handleValue(legFinishTime)-handleValue(trackedLeg.getTrackedRace().getStartOfRace()));
            addNamedElementWithValue(competitorElement, "time_spend_in_this_leg_as_millis", competitorLeg.getTimeInMilliSeconds(legFinishTime));
            addNamedElementWithValue(competitorElement, "leg_rank_at_finish_time", competitorLeg.getRank(legFinishTime));
            addNamedElementWithValue(competitorElement, "rank_gain_for_this_leg", competitorLeg.getRank(competitorLeg.getStartTime())-competitorLeg.getRank(legFinishTime));
            addNamedElementWithValue(competitorElement, "final_race_rank", ++raceRank);
            addNamedElementWithValue(competitorElement, "gap_to_leader_at_finish_in_seconds", competitorLeg.getGapToLeaderInSeconds(legFinishTime));
            addNamedElementWithValue(competitorElement, "windward_distance_to_overall_leader_in_meters", competitorLeg.getWindwardDistanceToOverallLeader(legFinishTime).getMeters());
            addNamedElementWithValue(competitorElement, "average_speed_over_ground_in_knots", competitorLeg.getAverageSpeedOverGround(legFinishTime).getKnots());
            addNamedElementWithValue(competitorElement, "maximum_speed_over_ground_in_knots", competitorLeg.getMaximumSpeedOverGround(legFinishTime).getB().getKnots());
            addNamedElementWithValue(competitorElement, "average_velocity_made_good_in_knots", competitorLeg.getAverageVelocityMadeGood(legFinishTime).getKnots());
            addNamedElementWithValue(competitorElement, "distance_traveled_in_meters", competitorLeg.getDistanceTraveled(legFinishTime).getMeters());
            addNamedElementWithValue(competitorElement, "number_of_jibes", competitorLeg.getNumberOfJibes(legFinishTime));
            addNamedElementWithValue(competitorElement, "number_of_tacks", competitorLeg.getNumberOfTacks(legFinishTime));
            addNamedElementWithValue(competitorElement, "maneuver_count", competitorLeg.getManeuvers(legFinishTime, /*waitForLatest*/false).size());
            addNamedElementWithValue(competitorElement, "number_of_penalty_circles", competitorLeg.getNumberOfPenaltyCircles(legFinishTime));
            addNamedElementWithValue(competitorElement, "average_cross_track_error_in_meters", competitorLeg.getAverageCrossTrackError(legFinishTime, /*waitForLatestAnalysis*/ false).getMeters());
            
            legElement.addContent(competitorElement);
        }
        
        return legElement;
    }
    
    private int getSameDayGroupIndex(TrackedRace currentRace, TrackedRace raceBefore) {
        if (!raceBefore.equals(currentRace)) {
            Calendar timedDateForCurrentRace = Calendar.getInstance();
            timedDateForCurrentRace.setTime(currentRace.getStartOfRace().asDate());
            Calendar timedDateForRaceBefore = Calendar.getInstance();
            timedDateForRaceBefore.setTime(raceBefore.getStartOfRace().asDate());
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.mm.yyyy");
            if (dateFormatter.format(timedDateForCurrentRace).equals(dateFormatter.format(timedDateForRaceBefore))) {
                return 0;
            } else {
                return 1;
            }
        }
        return 0;
    }
    
    public void perform() throws ServletException, IOException, NoWindException {
        final Leaderboard leaderboard = getLeaderboard();
        
        final List<Element> racesElements = new ArrayList<Element>();
        final List<Element> competitorElements = new ArrayList<Element>();
        
        for (Competitor competitor : leaderboard.getAllCompetitors()) {
            competitorElements.add(createCompetitorXML(competitor, leaderboard, /*shortVersion*/ false));
        }
        
        TrackedRace raceBefore = null; int sameDayGroupIndex = 0;
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                sameDayGroupIndex += getSameDayGroupIndex(raceColumn.getTrackedRace(fleet), raceBefore);
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                if (trackedRace != null && trackedRace.hasGPSData()) {
                    final List<Element> legs = new ArrayList<Element>();
                    int legCounter = 0;
                    for (TrackedLeg leg : trackedRace.getTrackedLegs()) {
                        legs.add(createLegXML(leg, leaderboard, ++legCounter));
                    }
                    racesElements.add(createRaceXML(trackedRace, fleet, legs, raceColumn, leaderboard, sameDayGroupIndex));
                    raceBefore = trackedRace;
                }
            }
        }
        
        Element leaderboardElement = createLeaderboardXML(leaderboard, competitorElements, racesElements);
    }
    

}
