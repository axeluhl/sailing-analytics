package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.LegEntryDTO;
import com.sap.sailing.domain.common.sharding.ShardingType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.sharding.ShardingContext;
import com.sap.sse.InvalidDateException;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

@Path("/v2/leaderboards")
public class LeaderboardsResourceV2 extends AbstractLeaderboardsResource {
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{name}")
    public Response getLeaderboard(@PathParam("name") String leaderboardName,
            @DefaultValue("Live") @QueryParam("resultState") ResultStates resultState,
            @QueryParam("columnNames") final List<String> raceColumnNames,
            @QueryParam("raceDetails") final List<String> raceDetails,
            @QueryParam("time") String time, @QueryParam("timeasmillis") Long timeasmillis,
            @QueryParam("maxCompetitorsCount") Integer maxCompetitorsCount) {
        ShardingContext.setShardingConstraint(ShardingType.LEADERBOARDNAME, leaderboardName);
        
        try {
            Response response;
            Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
            if (leaderboard == null) {
                response = Response.status(Status.NOT_FOUND)
                        .entity("Could not find a leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
                        .type(MediaType.TEXT_PLAIN).build();
            } else {
                try {
                    TimePoint timePoint;
                    try {
                        timePoint = parseTimePoint(time, timeasmillis, calculateTimePointForResultState(leaderboard, resultState));
                    } catch (InvalidDateException e1) {
                        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Could not parse the time.")
                                .type(MediaType.TEXT_PLAIN).build();
                    }
                    JSONObject jsonLeaderboard;
                    if (timePoint != null || resultState == ResultStates.Live) {
                        jsonLeaderboard = getLeaderboardJson(leaderboard, timePoint, resultState, maxCompetitorsCount, raceColumnNames, raceDetails);
                    } else {
                        jsonLeaderboard = createEmptyLeaderboardJson(leaderboard, resultState, maxCompetitorsCount);
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
        } finally {
            ShardingContext.clearShardingConstraint(ShardingType.LEADERBOARDNAME);
        }
    }

    @Override
    protected JSONObject getLeaderboardJson(Leaderboard leaderboard,
            TimePoint resultTimePoint, ResultStates resultState, Integer maxCompetitorsCount,
            List<String> raceColumnNames, List<String> raceDetailNames)
            throws NoWindException, InterruptedException, ExecutionException {
        List<String> raceColumnsToShow = calculateRaceColumnsToShow(raceColumnNames, leaderboard.getRaceColumns());
        List<DetailType> raceDetailsToShow = calculateRaceDetailTypesToShow(raceDetailNames);
        LeaderboardDTO leaderboardDTO = leaderboard.getLeaderboardDTO(
                resultTimePoint, raceColumnsToShow, /* addOverallDetails */
                false, getService(), getService().getBaseDomainFactory(),
                /* fillTotalPointsUncorrected */false);
        JSONObject jsonLeaderboard = new JSONObject();
        writeCommonLeaderboardData(jsonLeaderboard, leaderboard, resultState, leaderboardDTO.getTimePoint(), maxCompetitorsCount);
        Map<String, Map<String, Map<CompetitorDTO, Integer>>> competitorRanksPerRaceColumnsAndFleets = new HashMap<>();
        for (String raceColumnName : raceColumnsToShow) {
            List<CompetitorDTO> competitorsFromBestToWorst = leaderboardDTO.getCompetitorsFromBestToWorst(raceColumnName);
            Map<String, Map<CompetitorDTO, Integer>> competitorsOrderedByFleets = new HashMap<>();
            for (CompetitorDTO competitor: competitorsFromBestToWorst) {
                LeaderboardRowDTO row = leaderboardDTO.rows.get(competitor);
                LeaderboardEntryDTO leaderboardEntry = row.fieldsByRaceColumnName.get(raceColumnName);                
                FleetDTO fleetOfCompetitor = leaderboardEntry.fleet;
                if (fleetOfCompetitor != null && fleetOfCompetitor.getName() != null) {
                    Map<CompetitorDTO, Integer> competitorsOfFleet = competitorsOrderedByFleets.get(fleetOfCompetitor.getName());
                    if (competitorsOfFleet == null) {
                        competitorsOfFleet = new HashMap<>();
                        competitorsOrderedByFleets.put(fleetOfCompetitor.getName(), competitorsOfFleet);
                    }
                    competitorsOfFleet.put(competitor, competitorsOfFleet.size() + 1);
                }
            }
            competitorRanksPerRaceColumnsAndFleets.put(raceColumnName, competitorsOrderedByFleets);
        }
        JSONArray jsonCompetitorEntries = new JSONArray();
        jsonLeaderboard.put("competitors", jsonCompetitorEntries);
        jsonLeaderboard.put("ShardingLeaderboardName", ShardingType.LEADERBOARDNAME.encodeIfNeeded(leaderboard.getName()));
        int competitorCounter = 1;
        // Remark: leaderboardDTO.competitors are ordered by total rank
        for (CompetitorDTO competitor : leaderboardDTO.competitors) {
            LeaderboardRowDTO leaderboardRowDTO = leaderboardDTO.rows.get(competitor);
            if (maxCompetitorsCount != null && competitorCounter > maxCompetitorsCount) {
                break;
            }
            JSONObject jsonCompetitor = new JSONObject();
            writeCompetitorBaseData(jsonCompetitor, competitor, leaderboardDTO);
            BoatDTO rowBoatDTO = leaderboardRowDTO.boat;
            if (rowBoatDTO != null) {
                JSONObject jsonBoat = new JSONObject();
                writeBoatData(jsonBoat, rowBoatDTO);
                jsonCompetitor.put("boat", jsonBoat);
            }
            jsonCompetitor.put("rank", competitorCounter);
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
                BoatDTO entryBoatDTO = leaderboardEntry.boat;
                if (entryBoatDTO != null) {
                    JSONObject jsonBoat = new JSONObject();
                    writeBoatData(jsonBoat, entryBoatDTO);
                    jsonEntry.put("boat", jsonBoat);
                }
                final FleetDTO fleetOfCompetitor = leaderboardEntry.fleet;
                jsonEntry.put("fleet", fleetOfCompetitor == null ? "" : fleetOfCompetitor.getName());
                jsonEntry.put("totalPoints", leaderboardEntry.totalPoints);
                jsonEntry.put("uncorrectedTotalPoints", leaderboardEntry.totalPoints);
                jsonEntry.put("netPoints", leaderboardEntry.netPoints);
                MaxPointsReason maxPointsReason = leaderboardEntry.reasonForMaxPoints;
                jsonEntry.put("maxPointsReason", maxPointsReason != null ? maxPointsReason.toString() : null);
                jsonEntry.put("isDiscarded", leaderboardEntry.discarded);
                jsonEntry.put("isCorrected", leaderboardEntry.hasScoreCorrection());
                // if we have no fleet information there is no way to know in which fleet the competitor was racing
                Integer rank = null;
                if (fleetOfCompetitor != null && fleetOfCompetitor.getName() != null) {
                    Map<String, Map<CompetitorDTO, Integer>> rcMap = competitorRanksPerRaceColumnsAndFleets.get(raceColumnName);
                    if (rcMap != null && !rcMap.isEmpty()) {
                        Map<CompetitorDTO, Integer> rankMap = rcMap.get(fleetOfCompetitor.getName());
                        if (rankMap != null && !rankMap.isEmpty()) {
                            rank = rankMap.get(competitor);
                        }
                    }
                }
                jsonEntry.put("rank", rank);
                LegEntryDTO detailsOfLastAvailableLeg =  getDetailsOfLastAvailableLeg(leaderboardEntry);
                jsonEntry.put("trackedRank", detailsOfLastAvailableLeg != null ? detailsOfLastAvailableLeg.rank : null);
                boolean finished = false;
                LegEntryDTO detailsOfLastCourseLeg = getDetailsOfLastCourseLeg(leaderboardEntry);
                if (detailsOfLastCourseLeg != null) {
                    finished = detailsOfLastCourseLeg.finished;
                }
                jsonEntry.put("finished", finished);
                if (!raceDetailsToShow.isEmpty() && leaderboardEntry.race != null) {
                    LegEntryDTO currentLegEntry = null;
                    int currentLegNumber = leaderboardEntry.getOneBasedCurrentLegNumber();
                    if (leaderboardEntry.legDetails != null && currentLegNumber > 0 && currentLegNumber <= leaderboardEntry.legDetails.size()) {
                        currentLegEntry = leaderboardEntry.legDetails.get(currentLegNumber-1);
                        if (currentLegEntry != null) {
                            jsonEntry.put("trackedRank", currentLegEntry.rank);
                        }
                    }
                    JSONObject jsonRaceDetails = new JSONObject();
                    jsonEntry.put("data", jsonRaceDetails);
                    for (DetailType type : raceDetailsToShow) {
                        Pair<String, Object> valueForRaceDetailType = getValueForRaceDetailType(type, leaderboardEntry, currentLegEntry);
                        if (valueForRaceDetailType != null && valueForRaceDetailType.getA() != null && valueForRaceDetailType.getB() != null) {
                            jsonRaceDetails.put(valueForRaceDetailType.getA(),  valueForRaceDetailType.getB());
                        }
                    }                    
                }
            }
            competitorCounter++;
        }
        return jsonLeaderboard;
    }

    private List<DetailType> calculateRaceDetailTypesToShow(List<String> raceDetailTypesNames) {
        List<DetailType> result = new ArrayList<>();
        if (raceDetailTypesNames.size() == 0) {
            result = Arrays.asList(getDefaultRaceDetailColumnTypes());
        } else if (raceDetailTypesNames.size() == 1 && raceDetailTypesNames.get(0).equals("ALL")) {
            result = Arrays.asList(getAvailableRaceDetailColumnTypes());
        } else {
            Map<String, DetailType> typeMap = new HashMap<>();
            for (DetailType detailType : getAvailableRaceDetailColumnTypes()) {
                typeMap.put(detailType.name(), detailType);
            }
            for (String raceDetailTypeName : raceDetailTypesNames) {
                if (typeMap.containsKey(raceDetailTypeName)) {
                    result.add(typeMap.get(raceDetailTypeName));
                }
            }
        }
        return result;
    }

    private DetailType[] getDefaultRaceDetailColumnTypes() {
        return new DetailType[] { DetailType.RACE_GAP_TO_LEADER_IN_SECONDS,
                DetailType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS,
                DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS,
                DetailType.RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD_IN_METERS, 
                DetailType.RACE_CURRENT_LEG };
    }

    private DetailType[] getAvailableRaceDetailColumnTypes() {
        return new DetailType[] { DetailType.RACE_GAP_TO_LEADER_IN_SECONDS,
                DetailType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS,
                DetailType.RACE_DISTANCE_TRAVELED,
                DetailType.RACE_TIME_TRAVELED,
                DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS,
                DetailType.RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD_IN_METERS, 
                DetailType.NUMBER_OF_MANEUVERS,
                DetailType.RACE_CURRENT_LEG };
    }

    private Pair<String, Object> getValueForRaceDetailType(DetailType type, LeaderboardEntryDTO entry, LegEntryDTO currentLegEntry) {
        String name;
        Object value = null;
        Pair<String, Object> result = null;
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
            case RACE_TIME_TRAVELED:
                name = "timeTraveled-s";
                Duration timeTraveled = entry.getTimeSailed();
                if (timeTraveled != null) {
                    value = timeTraveled.asSeconds();
                }
                break;
            case RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
                name = "currentSpeedOverGround-kts";
                if (currentLegEntry != null) {
                    value = roundDouble(currentLegEntry.currentSpeedOverGroundInKnots, 2);
                }
                break;
            case NUMBER_OF_MANEUVERS:
                name = "numberOfManeuvers";
                Integer numberOfManeuvers = null;
                Map<ManeuverType, Integer> tacksJibesAndPenalties = getTotalNumberOfTacksJibesAndPenaltyCircles(entry);
                for (Integer maneuverCount : tacksJibesAndPenalties.values()) {
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
            case RACE_CURRENT_LEG:
                name = "currentLeg";
                int currentLegNumber = entry.getOneBasedCurrentLegNumber();
                if (currentLegNumber > 0) {
                    value = currentLegNumber;
                }
                break;
            default:
                name = null;
                break;
        }
        if (name != null && value != null) {
            result = new Pair<String, Object>(name, value);
        }
        return result;
    }

    private LegEntryDTO getDetailsOfLastCourseLeg(LeaderboardEntryDTO entry) {
        LegEntryDTO lastLegDetail = null;
        if (entry != null && entry.legDetails != null) {
            int lastLegIndex = entry.legDetails.size() - 1;
            if (lastLegIndex >= 0) {
                lastLegDetail = entry.legDetails.get(lastLegIndex);
            }
        }
        return lastLegDetail;
    }

    private LegEntryDTO getDetailsOfLastAvailableLeg(LeaderboardEntryDTO entry) {
        LegEntryDTO lastAvailableLegDetail = null;
        if (entry != null && entry.legDetails != null) {
            for (int i = entry.legDetails.size() - 1; i >= 0; i--) {
                lastAvailableLegDetail = entry.legDetails.get(i);
                if (lastAvailableLegDetail != null) {
                    break;
                }
            }
        }
        return lastAvailableLegDetail;
    }

    private Map<ManeuverType, Integer> getTotalNumberOfTacksJibesAndPenaltyCircles(LeaderboardEntryDTO entry) {
        Map<ManeuverType, Integer> totalNumberOfManeuvers = new HashMap<>();
        for (ManeuverType maneuverType : new ManeuverType[] { ManeuverType.TACK, ManeuverType.JIBE, ManeuverType.PENALTY_CIRCLE }) {
            totalNumberOfManeuvers.put(maneuverType, 0);
        }
        if (entry.legDetails != null) {
            for (LegEntryDTO legDetail : entry.legDetails) {
                if (legDetail != null) {
                    for (ManeuverType maneuverType : new ManeuverType[] { ManeuverType.TACK, ManeuverType.JIBE, ManeuverType.PENALTY_CIRCLE }) {
                        if (legDetail.numberOfManeuvers != null && legDetail.numberOfManeuvers.get(maneuverType) != null) {
                            totalNumberOfManeuvers.put(maneuverType,
                                    totalNumberOfManeuvers.get(maneuverType) + legDetail.numberOfManeuvers.get(maneuverType));
                        }
                    }
                }
            }
        }
        return totalNumberOfManeuvers;
    }

    /**
     * If {@code raceColumnNames} is empty or {@code null}, return the names of all {@link raceColumnsOfLeaderboard}; otherwise
     * return those race column names from {@code raceColumnsOfLeaderboard} that are also in {@code raceColumnNames}.
     */
    private List<String> calculateRaceColumnsToShow(List<String> raceColumnNames, Iterable<RaceColumn> raceColumnsOfLeaderboard) {
        final Set<String> raceColumnNamesAsSet = new HashSet<>();
        if (raceColumnNames != null) {
            raceColumnNamesAsSet.addAll(raceColumnNames);
        }
        // Calculates the race columns to retrieve data for
        final List<String> raceColumnsToShow = new ArrayList<>();
        for (final RaceColumn raceColumn : raceColumnsOfLeaderboard) {
            if (raceColumnNamesAsSet.isEmpty() || raceColumnNamesAsSet.contains(raceColumn.getName())) {
                raceColumnsToShow.add(raceColumn.getName());
            }
        }        
        return raceColumnsToShow;
    }

}
