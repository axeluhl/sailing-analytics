package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardCache;
import com.sap.sailing.domain.leaderboard.LeaderboardCacheManager;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.util.SmartFutureCache;
import com.sap.sailing.util.SmartFutureCache.CacheUpdater;
import com.sap.sailing.util.SmartFutureCache.UpdateInterval;

/**
 * Exports a leaderboard to the JSON format. Uses a {@link SmartFutureCache}.
 * 
 * TODO Need to wire a {@link LeaderboardCacheManager} to this
 * 
 * @author Frank, Axel Uhl
 * 
 */
public class LeaderboardJsonExportServlet extends AbstractJsonHttpServlet implements LeaderboardCache {
    private static final long serialVersionUID = -2460691283231361152L;
    private static final Logger logger = Logger.getLogger(LeaderboardJsonExportServlet.class.getName());
    private static final String PARAM_NAME_LEADERBOARDNAME = "leaderboardName";
    private static final String PARAM_NAME_RESULTSTATE = "resultState";
    private enum ResultStates { Live, Preliminary, Final };
    
    // for backward compatibility the default result state is live
    private final ResultStates DEFAULT_RESULT_STATE = ResultStates.Live;
    
    private static final int MAX_TOTAL_NUMBER_OF_CACHE_ENTRIES = 1000;
    
    private int totalNumberOfCacheEntries;
    
    private int cacheHits;
    private int cacheMisses;
    
    private final SmartFutureCache<Leaderboard, Map<Pair<TimePoint, ResultStates>, JSONObject>, LeaderboardJsonCacheUpdateInterval> cache;
    
    /**
     * Used to observe the leaderboards cached so far and triggering {@link #cache} updates.
     */
    private final LeaderboardCacheManager cacheManager;
    
    private static class LeaderboardJsonCacheUpdateInterval implements UpdateInterval<LeaderboardJsonCacheUpdateInterval> {
        private final Set<Pair<TimePoint, ResultStates>> timePointsAndResultStates;
        
        public LeaderboardJsonCacheUpdateInterval(Set<Pair<TimePoint, ResultStates>> timePointAndResultStates) {
            assert timePointAndResultStates != null;
            this.timePointsAndResultStates = timePointAndResultStates;
        }

        public Set<Pair<TimePoint, ResultStates>> getTimePointsAndResultStates() {
            return timePointsAndResultStates;
        }

        @Override
        public LeaderboardJsonCacheUpdateInterval join(LeaderboardJsonCacheUpdateInterval otherUpdateInterval) {
            Set<Pair<TimePoint, ResultStates>> newTimePointsAndResultStates = new HashSet<>();
            newTimePointsAndResultStates.addAll(getTimePointsAndResultStates());
            return new LeaderboardJsonCacheUpdateInterval(newTimePointsAndResultStates);
        }
        
    }
    
    private class LeaderboardJsonCacheUpdater implements CacheUpdater<Leaderboard, Map<Pair<TimePoint, ResultStates>, JSONObject>, LeaderboardJsonCacheUpdateInterval> {
        @Override
        public Map<Pair<TimePoint, ResultStates>, JSONObject> computeCacheUpdate(Leaderboard key,
                LeaderboardJsonCacheUpdateInterval updateInterval) throws Exception {
            Map<Pair<TimePoint, ResultStates>, JSONObject> result = new HashMap<>();
            for (Pair<TimePoint, ResultStates> timePointAndResultState : updateInterval.getTimePointsAndResultStates()) {
                result.put(timePointAndResultState, computeLeaderboardJson(key, timePointAndResultState));
            }
            return result;
        }

        @Override
        public Map<Pair<TimePoint, ResultStates>, JSONObject> provideNewCacheValue(Leaderboard leaderboard,
                Map<Pair<TimePoint, ResultStates>, JSONObject> oldCacheValue, Map<Pair<TimePoint, ResultStates>, JSONObject> computedCacheUpdate,
                LeaderboardJsonCacheUpdateInterval updateInterval) {
            Map<Pair<TimePoint, ResultStates>, JSONObject> result = new LinkedHashMap<Pair<TimePoint, ResultStates>, JSONObject>() {
                private static final long serialVersionUID = -6197983565575024084L;
                @Override
                protected boolean removeEldestEntry(Entry<Pair<TimePoint, ResultStates>, JSONObject> eldest) {
                    final boolean result;
                    if (totalNumberOfCacheEntries > MAX_TOTAL_NUMBER_OF_CACHE_ENTRIES) {
                        totalNumberOfCacheEntries--;
                        result = true;
                    } else {
                        result = false;
                    }
                    return result;
                }
            };
            if (oldCacheValue != null) {
                result.putAll(oldCacheValue);
                totalNumberOfCacheEntries -= oldCacheValue.size();
            } else {
                // first time we cache something for that leaderboard; ensure we get update triggers:
                cacheManager.add(leaderboard);
            }
            result.putAll(computedCacheUpdate);
            totalNumberOfCacheEntries += result.size();
            return result;
        }
    }
    
    public LeaderboardJsonExportServlet() {
        cache = new SmartFutureCache<>(new LeaderboardJsonCacheUpdater(), "LeaderboardJsonExporServlet SmartFutureCache for "+this);
        cacheManager = new LeaderboardCacheManager(this);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String leaderboardName = req.getParameter(PARAM_NAME_LEADERBOARDNAME);
        if (leaderboardName == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Need to specify a leaderboard name using the "+
                    PARAM_NAME_LEADERBOARDNAME+" parameter");
        } else {
            Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
            if (leaderboard == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Leaderboard "+leaderboardName+" not found");
            } else {
                try {
                    ResultStates resState = resolveRequestedResultState(req.getParameter(PARAM_NAME_RESULTSTATE));
                    Pair<TimePoint, ResultStates> resultStateAndTimePoint = new Pair<>(
                            calculateTimePointForResultState(leaderboard, resState),
                            resolveRequestedResultState(req.getParameter(PARAM_NAME_RESULTSTATE)));
                    JSONObject jsonLeaderboard = getFromCacheOrCompute(leaderboard, resultStateAndTimePoint);
                    setJsonResponseHeader(resp);
                    jsonLeaderboard.writeJSONString(resp.getWriter());
                } catch (NoWindException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            }
        }
    }

    private JSONObject getFromCacheOrCompute(Leaderboard leaderboard,
            Pair<TimePoint, ResultStates> timePointAndResultState) throws NoWindException {
        Map<Pair<TimePoint, ResultStates>, JSONObject> cacheEntry = cache.get(leaderboard, /* waitForLatest */ false);
        if (cacheEntry == null || !cacheEntry.containsKey(timePointAndResultState)) {
            cacheMisses++;
            cache.triggerUpdate(leaderboard, new LeaderboardJsonCacheUpdateInterval(Collections.singleton(timePointAndResultState)));
            // now wait for this entry to be computed
            cacheEntry = cache.get(leaderboard, /* waitForLatest */ true);
        } else {
            cacheHits++;
        }
        logger.finest(this+" cache hits/misses: "+cacheHits+"/"+cacheMisses);
        return cacheEntry.get(timePointAndResultState);
    }

    private JSONObject computeLeaderboardJson(Leaderboard leaderboard,
            Pair<TimePoint, ResultStates> resultStateAndTimePoint) throws NoWindException {
        TimePoint resultTimePoint = resultStateAndTimePoint.getA();
        ResultStates resultState = resultStateAndTimePoint.getB();
        List<Competitor> competitorsFromBestToWorstAccordingToTotalRank = leaderboard
                .getCompetitorsFromBestToWorst(resultTimePoint);
        Map<RaceColumn, List<Competitor>> rankedCompetitorsPerColumn = new HashMap<RaceColumn, List<Competitor>>();
        JSONObject jsonLeaderboard = new JSONObject();
        jsonLeaderboard.put("name", leaderboard.getName());
        
        // for backward compatibility 
        jsonLeaderboard.put("timepoint", resultTimePoint.toString());

        jsonLeaderboard.put("resultTimepoint", resultTimePoint.toString());
        jsonLeaderboard.put("requestTimepoint", resultTimePoint.toString());
        jsonLeaderboard.put("resultState", resultState.name());
        
        SettableScoreCorrection scoreCorrection = leaderboard.getScoreCorrection();
        if (scoreCorrection != null) {
            jsonLeaderboard.put("scoringComment", scoreCorrection.getComment());
            TimePoint lastUpdateTimepoint = scoreCorrection.getTimePointOfLastCorrectionsValidity();
            jsonLeaderboard.put("lastScoringUpdate", lastUpdateTimepoint != null ? lastUpdateTimepoint.asDate().toString(): null);
        } else {
            jsonLeaderboard.put("scoringComment", null);
            jsonLeaderboard.put("lastScoringUpdate", null);
        }
        
        JSONArray jsonColumnNames = new JSONArray();
        jsonLeaderboard.put("columnNames", jsonColumnNames);
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            jsonColumnNames.add(raceColumn.getName());
        }
        JSONArray jsonCompetitorEntries = new JSONArray();
        jsonLeaderboard.put("competitors", jsonCompetitorEntries);
        for (Competitor competitor : competitorsFromBestToWorstAccordingToTotalRank) {
            JSONObject jsonCompetitor = new JSONObject();
            jsonCompetitor.put("name", competitor.getName());
            final String displayName = leaderboard.getDisplayName(competitor);
            jsonCompetitor.put("displayName", displayName==null?competitor.getName():displayName);
            jsonCompetitor.put("id", competitor.getId().toString());
            jsonCompetitor.put("sailID", competitor.getBoat().getSailID());
            Nationality nationality = competitor.getTeam().getNationality();
            jsonCompetitor.put("nationality", nationality != null ? nationality.getThreeLetterIOCAcronym(): null);
            jsonCompetitor.put("countryCode", nationality != null ? nationality.getCountryCode().getTwoLetterISOCode(): null);
            
            // only add results if we have a valid resultTimePoint
            if(resultTimePoint != null) {
                jsonCompetitor.put("rank", competitorsFromBestToWorstAccordingToTotalRank.indexOf(competitor) + 1);
                jsonCompetitor.put("carriedPoints", leaderboard.getCarriedPoints(competitor));
                jsonCompetitor.put("totalPoints", leaderboard.getTotalPoints(competitor, resultTimePoint));
                jsonCompetitorEntries.add(jsonCompetitor);
                JSONObject jsonRaceColumns = new JSONObject();
                jsonCompetitor.put("raceScores", jsonRaceColumns);
                for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                    List<Competitor> rankedCompetitorsForColumn = rankedCompetitorsPerColumn.get(raceColumn);
                    if (rankedCompetitorsForColumn == null) {
                        rankedCompetitorsForColumn = leaderboard.getCompetitorsFromBestToWorst(raceColumn, resultTimePoint);
                        rankedCompetitorsPerColumn.put(raceColumn, rankedCompetitorsForColumn);
                    }
                    JSONObject jsonEntry = new JSONObject();
                    jsonRaceColumns.put(raceColumn.getName(), jsonEntry);
                    final Fleet fleetOfCompetitor = raceColumn.getFleetOfCompetitor(competitor);
                    jsonEntry.put("fleet", fleetOfCompetitor==null?"":fleetOfCompetitor.getName());
                    jsonEntry.put("netPoints", leaderboard.getNetPoints(competitor, raceColumn, resultTimePoint));
                    jsonEntry.put("totalPoints", leaderboard.getTotalPoints(competitor, raceColumn, resultTimePoint));
                    MaxPointsReason maxPointsReason = leaderboard.getMaxPointsReason(competitor, raceColumn, resultTimePoint);
                    jsonEntry.put("maxPointsReason", maxPointsReason != null ? maxPointsReason.toString(): null);
                    jsonEntry.put("rank", rankedCompetitorsForColumn.indexOf(competitor)+1);
                    final TrackedRace trackedRace = raceColumn.getTrackedRace(competitor);
                    if (trackedRace != null) {
                        jsonEntry.put("raceRank", trackedRace.getRank(competitor, resultTimePoint));
                    }
                    jsonEntry.put("isDiscarded", leaderboard.isDiscarded(competitor, raceColumn, resultTimePoint));
                    jsonEntry.put("isCorrected", leaderboard.getScoreCorrection().isScoreCorrected(competitor, raceColumn));
                }
            } else {
                jsonCompetitor.put("rank", 0);
                jsonCompetitor.put("carriedPoints", null);
                jsonCompetitor.put("totalPoints", null);
                jsonCompetitorEntries.add(jsonCompetitor);
                JSONObject jsonRaceColumns = new JSONObject();
                jsonCompetitor.put("raceScores", jsonRaceColumns);
                for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                    JSONObject jsonEntry = new JSONObject();
                    jsonRaceColumns.put(raceColumn.getName(), jsonEntry);
                    final Fleet fleetOfCompetitor = raceColumn.getFleetOfCompetitor(competitor);
                    jsonEntry.put("fleet", fleetOfCompetitor==null?"":fleetOfCompetitor.getName());
                    jsonEntry.put("netPoints", null);
                    jsonEntry.put("totalPoints", null);
                    jsonEntry.put("maxPointsReason", "");
                    jsonEntry.put("rank", 0);
                    jsonEntry.put("isDiscarded", false);
                    jsonEntry.put("isCorrected", false);
                }
            }
        }
        return jsonLeaderboard;
    }

    private ResultStates resolveRequestedResultState(String resultStateParam) {
        ResultStates result = DEFAULT_RESULT_STATE;
        if(resultStateParam != null) {
            for(ResultStates state: ResultStates.values()) {
                if(state.name().equalsIgnoreCase(resultStateParam)) {
                    result = state;
                    break;
                }
            }
        }
        return result;
    }

    private TimePoint calculateTimePointForResultState(Leaderboard leaderboard, ResultStates resultState) {
        TimePoint result = null;
        switch (resultState) {
        case Live:
            result = leaderboard.getTimePointOfLatestModification();
            break;
        case Preliminary:
        case Final:
            if (leaderboard.getScoreCorrection() != null) {
                result = leaderboard.getScoreCorrection().getTimePointOfLastCorrectionsValidity();
                if (result == null) {
                    result = leaderboard.getTimePointOfLatestModification();
                }
            }
            break;
        }
        if (result == null) {
            result = MillisecondsTimePoint.now();
        }
        return result;
    }

    @Override
    public void add(Leaderboard leaderboard) {
        // nothing to do; the triggerUpdate call on the SmartFutureCache in doGet ensures that a record for the leaderboard
        // will be created in the SmartFutureCache
    }

    @Override
    public void invalidate(Leaderboard leaderboard) {
        cache.triggerUpdate(leaderboard, null);
    }
}
