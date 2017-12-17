package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.LegEntryDTO;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.InvalidDateException;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

@Path("/v2/leaderboards")
public class LeaderboardsResourceV2 extends AbstractLeaderboardsResource {
    private static final Logger logger = Logger.getLogger(LeaderboardsResourceV2.class.getName());

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{name}")
    public Response getLeaderboard(@PathParam("name") String leaderboardName,
            @DefaultValue("Live") @QueryParam("resultState") ResultStates resultState,
            @QueryParam("columnNames") final List<String> raceColumnNames,
            @QueryParam("regattaDetails") final List<String> regattaDetails,            
            @QueryParam("raceDetails") final List<String> raceDetails,            
            @QueryParam("time") String time, @QueryParam("timeasmillis") Long timeasmillis,
            @QueryParam("maxCompetitorsCount") Integer maxCompetitorsCount) {
        Response response;

        TimePoint requestTimePoint = MillisecondsTimePoint.now();     
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            response = Response.status(Status.NOT_FOUND)
                    .entity("Could not find a leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        } else {
            try {
                TimePoint resultTimePoint;
                try {
                    resultTimePoint = parseTimePoint(time, timeasmillis, calculateTimePointForResultState(leaderboard, resultState));
                } catch (InvalidDateException e1) {
                    return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Could not parse the time.")
                            .type(MediaType.TEXT_PLAIN).build();
                }
                JSONObject jsonLeaderboard;
                if (resultTimePoint != null) {
                    Util.Triple<TimePoint, ResultStates, Integer> resultStateAndTimePoint = new Util.Triple<>(
                            resultTimePoint, resultState, maxCompetitorsCount);
                    jsonLeaderboard = getLeaderboardJson(leaderboard, resultStateAndTimePoint, raceColumnNames,
                            regattaDetails, raceDetails);
                } else {
                    jsonLeaderboard = createEmptyLeaderboardJson(leaderboard, resultState, requestTimePoint,
                            maxCompetitorsCount);
                }

                StringWriter sw = new StringWriter();
                jsonLeaderboard.writeJSONString(sw);

                String json = sw.getBuffer().toString();
                response = Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
            } catch (NoWindException | InterruptedException | ExecutionException | IOException e) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage())
                        .type(MediaType.TEXT_PLAIN).build();
            }
        }

        return response;
    }

    private JSONObject getLeaderboardJson(Leaderboard leaderboard,
            Util.Triple<TimePoint, ResultStates, Integer> timePointAndResultStateAndMaxCompetitorsCount,
            List<String> raceColumnNames, List<String> regattaDetailNames,            
            List<String> raceDetailNames)
            throws NoWindException, InterruptedException, ExecutionException {

        List<String> raceColumnsToShow = calculateRaceColumnsToShow(raceColumnNames, leaderboard.getRaceColumns());
        List<DetailType> raceDetailsToShow = calculateRaceDetailTypesToShow(raceDetailNames);

        LeaderboardDTO leaderboardDTO = leaderboard.getLeaderboardDTO(
                timePointAndResultStateAndMaxCompetitorsCount.getA(), raceColumnsToShow, /* addOverallDetails */
                false, getService(), getService().getBaseDomainFactory(),
                /* fillTotalPointsUncorrected */false);

        TimePoint resultTimePoint = timePointAndResultStateAndMaxCompetitorsCount.getA();
        ResultStates resultState = timePointAndResultStateAndMaxCompetitorsCount.getB();
        Integer maxCompetitorsCount = timePointAndResultStateAndMaxCompetitorsCount.getC();       
      
        JSONObject jsonLeaderboard = new JSONObject();
              
        writeCommonLeaderboardData(jsonLeaderboard, leaderboardDTO, resultState, resultTimePoint, maxCompetitorsCount);
     
        JSONArray jsonCompetitorEntries = new JSONArray();
        jsonLeaderboard.put("competitors", jsonCompetitorEntries);
        int counter = 1;
        for (CompetitorDTO competitor : leaderboardDTO.competitors) {
            LeaderboardRowDTO leaderboardRowDTO = leaderboardDTO.rows.get(competitor);

            if (maxCompetitorsCount != null && counter > maxCompetitorsCount) {
                break;
            }
            JSONObject jsonCompetitor = new JSONObject();
            jsonCompetitor.put("name", competitor.getName());
            final String displayName = leaderboardDTO.getDisplayName(competitor);
            jsonCompetitor.put("displayName", displayName == null ? competitor.getName() : displayName);
            jsonCompetitor.put("id", competitor.getIdAsString());
            jsonCompetitor.put("sailID", competitor.getSailID());
            jsonCompetitor.put("nationality", competitor.getThreeLetterIocCountryCode());
            jsonCompetitor.put("countryCode", competitor.getTwoLetterIsoCountryCode());

            jsonCompetitor.put("rank", counter);
            jsonCompetitor.put("carriedPoints", leaderboardRowDTO.carriedPoints);
            jsonCompetitor.put("netPoints", leaderboardRowDTO.netPoints);
            
            jsonCompetitor.put("overallRank", leaderboardDTO.getTotalRank(competitor));
            jsonCompetitorEntries.add(jsonCompetitor);

            JSONObject jsonRaceColumns = new JSONObject();
            jsonCompetitor.put("columns", jsonRaceColumns);
            for (String raceColumnName : raceColumnsToShow) {
                JSONObject jsonEntry = new JSONObject();
                jsonRaceColumns.put(raceColumnName, jsonEntry);
                LeaderboardEntryDTO leaderboardEntry = leaderboardRowDTO.fieldsByRaceColumnName.get(raceColumnName);

                final FleetDTO fleetOfCompetitor = leaderboardEntry.fleet;
                jsonEntry.put("fleet", fleetOfCompetitor == null ? "" : fleetOfCompetitor.getName());
                
                jsonEntry.put("totalPoints", leaderboardEntry.totalPoints);
                jsonEntry.put("uncorrectedTotalPoints", leaderboardEntry.totalPoints);
                jsonEntry.put("netPoints", leaderboardEntry.netPoints);
                MaxPointsReason maxPointsReason = leaderboardEntry.reasonForMaxPoints;
                jsonEntry.put("maxPointsReason", maxPointsReason != null ? maxPointsReason.toString() : null);
                jsonEntry.put("isDiscarded", leaderboardEntry.discarded);
                jsonEntry.put("isCorrected", leaderboardEntry.hasScoreCorrection());

                jsonEntry.put("rank", leaderboardEntry.trackedRank > 0 ? leaderboardEntry.trackedRank : null);
                jsonEntry.put("trackedRank", null);

                boolean finished = false;
                LegEntryDTO detailsOfLastLeg = getDetailsOfLastLeg(leaderboardEntry);
                if (detailsOfLastLeg != null) {
                    finished = detailsOfLastLeg.finished;
                }
                jsonEntry.put("finished", finished);
                
                if (!raceDetailsToShow.isEmpty() && leaderboardEntry.race != null) {
                    JSONObject jsonRaceDetails = new JSONObject();
                    jsonEntry.put("data", jsonRaceDetails);
                    for (DetailType type: raceDetailsToShow) {
                        Pair<String, Object> valueForRaceDetailType = getValueForRaceDetailType(type, leaderboardEntry);
                        if (valueForRaceDetailType.getB() != null) {
                            jsonRaceDetails.put(valueForRaceDetailType.getA(),  valueForRaceDetailType.getB());
                        }
                    }                    
                }
            }
            counter++;
        }
        return jsonLeaderboard;
    }

    private List<DetailType> calculateRaceDetailTypesToShow(List<String> raceDetailTypesNames) {
        List<DetailType> result = new ArrayList<>();
        if (raceDetailTypesNames.size() == 1 && raceDetailTypesNames.get(0).equals("ALL")) {
            result = Arrays.asList(getAvailableRaceDetailColumnTypes());
        } else {
            Map<String, DetailType> typeMap = new HashMap<>();
            for (DetailType detailType: getAvailableRaceDetailColumnTypes()) {
                typeMap.put(detailType.name(), detailType);
            }
            for (String raceDetailTypeName: raceDetailTypesNames) {
                if (typeMap.containsKey(raceDetailTypeName)) {
                    result.add(typeMap.get(raceDetailTypeName));
                }
            }
        }
        return result;
    }

    private DetailType[] getAvailableRaceDetailColumnTypes() {
        return new DetailType[] { DetailType.RACE_GAP_TO_LEADER_IN_SECONDS,
                DetailType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS,
                DetailType.RACE_DISTANCE_TRAVELED,
                DetailType.RACE_DISTANCE_TRAVELED_INCLUDING_GATE_START,
                DetailType.RACE_TIME_TRAVELED,
                DetailType.RACE_CALCULATED_TIME_TRAVELED,
                DetailType.RACE_CALCULATED_TIME_AT_ESTIMATED_ARRIVAL_AT_COMPETITOR_FARTHEST_AHEAD,
                DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS,
                DetailType.RACE_CURRENT_RIDE_HEIGHT_IN_METERS,
                DetailType.RACE_CURRENT_DISTANCE_FOILED_IN_METERS,
                DetailType.RACE_CURRENT_DURATION_FOILED_IN_SECONDS,
                DetailType.RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD_IN_METERS, 
                DetailType.NUMBER_OF_MANEUVERS,
                DetailType.CURRENT_LEG,
                DetailType.RACE_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS,
                DetailType.RACE_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS,
                DetailType.RACE_RATIO_BETWEEN_TIME_SINCE_LAST_POSITION_FIX_AND_AVERAGE_SAMPLING_INTERVAL };
    }
    
    private Pair<String, Object> getValueForRaceDetailType(DetailType type, LeaderboardEntryDTO entry) {
        String name;
        Object value = null;
        int currentLegNumber = entry.getOneBasedCurrentLegNumber();
        switch (type) {
            case RACE_GAP_TO_LEADER_IN_SECONDS:
                name = "gapToLeader-s";
                if (entry.gapToLeaderInOwnTime != null) {
                    value = entry.gapToLeaderInOwnTime.asSeconds();
                }
                break;
            case RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD_IN_METERS:
                name = "gapToLeader-m";
                if (entry.windwardDistanceToCompetitorFarthestAheadInMeters != null) {
                    value = roundDouble(entry.windwardDistanceToCompetitorFarthestAheadInMeters, 2);                
                }
                break;
            case RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS:
                name = "averageSpeedOverGround-kts";
                Distance distance = entry.getDistanceTraveled();
                Duration timeSailed = entry.getTimeSailed();
                if (distance != null && timeSailed != null) {
                    value = roundDouble(distance.inTime(timeSailed).getKnots(), 2);
                }
                break;
            case RACE_DISTANCE_TRAVELED:
                name = "distanceTraveled-m";
                Distance distanceTraveled = entry.getDistanceTraveled();
                if (distanceTraveled != null) {
                    value = roundDouble(distanceTraveled.getMeters(), 2);
                }
                break;
            case RACE_DISTANCE_TRAVELED_INCLUDING_GATE_START:
                name = "distanceTraveledConsideringGateStart-m";
                if (entry.legDetails != null && currentLegNumber > 0) {
                    LegEntryDTO legEntryDTO = entry.legDetails.get(currentLegNumber-1);
                    if (legEntryDTO != null) {
                        value = roundDouble(legEntryDTO.distanceTraveledIncludingGateStartInMeters, 2);
                    }
                }
                break;
            case RACE_TIME_TRAVELED:
                name = "timeTraveled-s";
                Duration timeTraveled = entry.getTimeSailed();
                if (timeTraveled != null) {
                    value = timeTraveled.asSeconds();
                }
                break;
            case RACE_CALCULATED_TIME_TRAVELED:
                name = "";
                break;
            case RACE_CALCULATED_TIME_AT_ESTIMATED_ARRIVAL_AT_COMPETITOR_FARTHEST_AHEAD:
                name = "";
                break;
            case RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
                name = "currentSpeedOverGround-kts";
                if (entry.legDetails != null && currentLegNumber > 0) {
                    LegEntryDTO legEntryDTO = entry.legDetails.get(currentLegNumber-1);
                    if (legEntryDTO != null) {
                        value = roundDouble(legEntryDTO.currentSpeedOverGroundInKnots, 2);
                    }
                }
                break;
            case RACE_CURRENT_RIDE_HEIGHT_IN_METERS:
                name = "";
                break;
            case RACE_CURRENT_DISTANCE_FOILED_IN_METERS:
                name = "";
                break;
            case RACE_CURRENT_DURATION_FOILED_IN_SECONDS:
                name = "";
                break;
            case NUMBER_OF_MANEUVERS:
                name = "numberOfManeuvers";
                Double numberOfManeuvers = null;
                Map<ManeuverType, Double> tacksJibesAndPenalties = getTotalNumberOfTacksJibesAndPenaltyCircles(entry);
                for (Double maneuverCount : tacksJibesAndPenalties.values()) {
                    if (maneuverCount != null) {
                        if (numberOfManeuvers == null) {
                            numberOfManeuvers = maneuverCount;
                        } else {
                            numberOfManeuvers += maneuverCount;
                        }
                    }
                }
                value = numberOfManeuvers;
                break;
            case CURRENT_LEG:
                name = "currentLeg";
                if (currentLegNumber > 0) {
                    value = currentLegNumber;
                }
                break;
            case RACE_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS:
                name = "";
                break;
            case RACE_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS:
                name = "";
                break;
            case RACE_RATIO_BETWEEN_TIME_SINCE_LAST_POSITION_FIX_AND_AVERAGE_SAMPLING_INTERVAL:
                name = "";
                break;
            default:
                name = "gapToLeader-s";
                if (entry.gapToLeaderInOwnTime != null) {
                    value = entry.gapToLeaderInOwnTime.asSeconds();
                }
                break;
        }
        return new Pair<String, Object>(name, value);
    }

    private LegEntryDTO getDetailsOfLastLeg(LeaderboardEntryDTO entry) {
        LegEntryDTO lastLegDetail = null;
        if (entry != null && entry.legDetails != null) {
            int lastLegIndex = entry.legDetails.size() - 1;
            if (lastLegIndex >= 0) {
                lastLegDetail = entry.legDetails.get(lastLegIndex);
            }
        }
        return lastLegDetail;
    }
    
    private Map<ManeuverType, Double> getTotalNumberOfTacksJibesAndPenaltyCircles(LeaderboardEntryDTO entry) {
        Map<ManeuverType, Double> totalNumberOfManeuvers = new HashMap<ManeuverType, Double>();
        for (ManeuverType maneuverType : new ManeuverType[] { ManeuverType.TACK, ManeuverType.JIBE, ManeuverType.PENALTY_CIRCLE }) {
            totalNumberOfManeuvers.put(maneuverType, 0.0);
        }
        if (entry.legDetails != null) {
            for (LegEntryDTO legDetail : entry.legDetails) {
                if (legDetail != null) {
                    for (ManeuverType maneuverType : new ManeuverType[] { ManeuverType.TACK, ManeuverType.JIBE, ManeuverType.PENALTY_CIRCLE }) {
                        if (legDetail.numberOfManeuvers != null && legDetail.numberOfManeuvers.get(maneuverType) != null) {
                            totalNumberOfManeuvers.put(maneuverType,
                                    totalNumberOfManeuvers.get(maneuverType) + (double) legDetail.numberOfManeuvers.get(maneuverType));
                        }
                    }
                }
            }
        }
        return totalNumberOfManeuvers;
    }

    private List<String> calculateRaceColumnsToShow(List<String> raceColumnNames, Iterable<RaceColumn> raceColumnsOfLeaderboard) {
        // Calculates the race columns to retrieve data for
        List<String> allRaceColumns = new ArrayList<>();
        for (RaceColumn raceColumn : raceColumnsOfLeaderboard) {
            allRaceColumns.add(raceColumn.getName());
        }        
        List<String> raceColumnsToShow = new ArrayList<>(allRaceColumns);
        if (!raceColumnNames.isEmpty()) {
            allRaceColumns.removeAll(raceColumnNames);
            raceColumnsToShow.removeAll(allRaceColumns);
        }
        return raceColumnsToShow;
    }

}
