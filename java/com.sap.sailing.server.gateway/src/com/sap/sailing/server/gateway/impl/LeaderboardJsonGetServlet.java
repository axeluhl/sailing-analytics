package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
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
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardCacheManager;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardCache;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.HttpRequestUtils;
import com.sap.sailing.server.gateway.ParseHttpParameterException;
import com.sap.sailing.util.SmartFutureCache;
import com.sap.sailing.util.SmartFutureCache.CacheUpdater;
import com.sap.sailing.util.SmartFutureCache.UpdateInterval;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Exports a leaderboard to the JSON format. Uses a {@link SmartFutureCache}.
 * @author Frank, Axel Uhl
 * 
 */
public class LeaderboardJsonGetServlet extends AbstractJsonHttpServlet implements LeaderboardCache {
    private static final long serialVersionUID = -2460691283231361152L;
    private static final Logger logger = Logger.getLogger(LeaderboardJsonGetServlet.class.getName());
    private static final String PARAM_NAME_LEADERBOARDNAME = "leaderboardName";
    private static final String PARAM_NAME_RESULTSTATE = "resultState";
    private static final String PARAM_NAME_USE_CACHE = "useCache";
    private static final String PARAM_NAME_MAX_COMPETITORS_COUNT = "maxCompetitorsCount";
    
    public static enum ResultStates { Live, Preliminary, Final };
    
    // for backward compatibility the default result state is live
    private final ResultStates DEFAULT_RESULT_STATE = ResultStates.Live;
    private static SimpleDateFormat TIMEPOINT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    private static final int MAX_TOTAL_NUMBER_OF_CACHE_ENTRIES = 1000;
    
    private int totalNumberOfCacheEntries;
    
    private int cacheHits;
    private int cacheMisses;
    
    /**
     * The cache values are linked hash maps keeping the rendered JSON objects as String per time point/result state pair. The cache uses
     * a <em>linked</em> hash map because this way old values can be evicted easily, keeping the cache below a maximum size.
     * Also, typing the {@link SmartFutureCache} with {@link LinkedHashMap} instead of only {@link Map} ensures that also when
     * computing cache updates, new values are appended "at the end" and therefore tend to survive longer than any value appended
     * earlier.
     */
    private final SmartFutureCache<Leaderboard, LinkedHashMap<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>, StringBuffer>, LeaderboardJsonCacheUpdateInterval> cache;
    
    /**
     * Used to observe the leaderboards cached so far and triggering {@link #cache} updates.
     */
    private final LeaderboardCacheManager cacheManager;
    
    private static class LeaderboardJsonCacheUpdateInterval implements UpdateInterval<LeaderboardJsonCacheUpdateInterval> {
        private final LinkedHashSet<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>> timePointsAndResultStates;
        
        public LeaderboardJsonCacheUpdateInterval(Iterable<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>> timePointsAndResultStates) {
            assert timePointsAndResultStates != null;
            this.timePointsAndResultStates = new LinkedHashSet<>();
            Util.addAll(timePointsAndResultStates, this.timePointsAndResultStates);
        }

        public LinkedHashSet<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>> getTimePointsAndResultStates() {
            return timePointsAndResultStates;
        }

        /**
         * Appends the <code>otherUpdateInterval</code> to this interval and returns the new interval. Using a
         * {@link LinkedHashSet} internally, the ordering of time point and result state pairs is preserved, and
         * in conjunction with the cache eviction strategy, the newest additions are the most likely to survive in
         * the cache.
         */
        @Override
        public LeaderboardJsonCacheUpdateInterval join(LeaderboardJsonCacheUpdateInterval otherUpdateInterval) {
            LinkedHashSet<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>> newTimePointsAndResultStates = new LinkedHashSet<>();
            newTimePointsAndResultStates.addAll(getTimePointsAndResultStates());
            newTimePointsAndResultStates.addAll(otherUpdateInterval.getTimePointsAndResultStates());
            return new LeaderboardJsonCacheUpdateInterval(newTimePointsAndResultStates);
        }
        
    }
    
    private class LeaderboardJsonCacheUpdater
            implements
            CacheUpdater<Leaderboard, LinkedHashMap<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>, StringBuffer>, LeaderboardJsonCacheUpdateInterval> {
        @Override
        public LinkedHashMap<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>, StringBuffer> computeCacheUpdate(Leaderboard key,
                LeaderboardJsonCacheUpdateInterval updateInterval) throws Exception {
            final LinkedHashMap<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>, StringBuffer> result;
            if (updateInterval == null) {
                result = null;
            } else {
                // no removeEldestEntry override required here; map is merged into another LinkedHashMap created in provideNewCacheValue
                result = new LinkedHashMap<>();
                for (com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer> timePointAndResultState : updateInterval.getTimePointsAndResultStates()) {
                    StringWriter sw = new StringWriter();
                    computeLeaderboardJson(key, timePointAndResultState).writeJSONString(sw);
                    result.put(timePointAndResultState, sw.getBuffer());
                }
            }
            return result;
        }

        @Override
        public LinkedHashMap<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>, StringBuffer> provideNewCacheValue(Leaderboard leaderboard,
                LinkedHashMap<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>, StringBuffer> oldCacheValue,
                LinkedHashMap<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>, StringBuffer> computedCacheUpdate,
                LeaderboardJsonCacheUpdateInterval updateInterval) {
            final LinkedHashMap<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>, StringBuffer> result;
            if (computedCacheUpdate == null) {
                result = null;
                if (oldCacheValue != null) {
                    totalNumberOfCacheEntries -= oldCacheValue.size();
                }
            } else {
                // Note: don't use access-based ordering as it turns the get(...) call into a "write" access
                result = new LinkedHashMap<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>, StringBuffer>(16, 0.75f) {
                    private static final long serialVersionUID = -6197983565575024084L;
                    @Override
                    protected boolean removeEldestEntry(Entry<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>, StringBuffer> eldest) {
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
                }
                // ensure we get update triggers; note that oldCacheValue!=null doesn't mean that there are currently listeners active;
                // the listeners could have been removed, and the SmartFutureCache will keep old values around as there is no explicit
                // cache eviction for a SmartFutureCache.
                cacheManager.add(leaderboard);
                result.putAll(computedCacheUpdate);
                totalNumberOfCacheEntries += result.size();
            }
            return result;
        }
    }
    
    public LeaderboardJsonGetServlet() {
        cache = new SmartFutureCache<>(new LeaderboardJsonCacheUpdater(), "LeaderboardJsonExporServlet SmartFutureCache for "+this);
        cacheManager = new LeaderboardCacheManager(this);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        TimePoint requestTimePoint = MillisecondsTimePoint.now();
        String leaderboardName = req.getParameter(PARAM_NAME_LEADERBOARDNAME);
        String useCacheParam = req.getParameter(PARAM_NAME_USE_CACHE);
        Integer maxCompetitorsCount = null;
        try {
            maxCompetitorsCount = HttpRequestUtils.getIntParameter(req, PARAM_NAME_MAX_COMPETITORS_COUNT);
        } catch (ParseHttpParameterException e1) {
        }
        boolean useCache = true; // default
        if (useCacheParam != null && "false".equalsIgnoreCase(useCacheParam)) {
            useCache = false;
        }
        if (leaderboardName == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Need to specify a leaderboard name using the "+
                    PARAM_NAME_LEADERBOARDNAME+" parameter");
        } else {
            Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
            if (leaderboard == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Leaderboard "+leaderboardName+" not found");
            } else {
                try {
                    ResultStates resultState = resolveRequestedResultState(req.getParameter(PARAM_NAME_RESULTSTATE));
                    TimePoint resultTimePoint = calculateTimePointForResultState(leaderboard, resultState);
                    StringBuffer jsonLeaderboardAsString;
                    if (resultTimePoint != null) {
                        com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer> resultStateAndTimePointAndMaxCompetitorsCount =
                                new com.sap.sse.common.Util.Triple<>(resultTimePoint, resultState, maxCompetitorsCount);
                        if (useCache) {
                            jsonLeaderboardAsString = getLeaderboardJsonFromCacheOrCompute(leaderboard, resultStateAndTimePointAndMaxCompetitorsCount);
                        } else {
                            StringWriter sw = new StringWriter();
                            computeLeaderboardJson(leaderboard, resultStateAndTimePointAndMaxCompetitorsCount).writeJSONString(sw);
                            jsonLeaderboardAsString = sw.getBuffer();
                        }
                    } else {
                        StringWriter sw = new StringWriter();
                        createEmptyLeaderboardJson(leaderboard, resultState, requestTimePoint, maxCompetitorsCount).writeJSONString(sw);
                        jsonLeaderboardAsString = sw.getBuffer();
                    }
                    setJsonResponseHeader(resp);
                    synchronized (jsonLeaderboardAsString) {
                        int indexOfFirstOpeningBrace = jsonLeaderboardAsString.indexOf("{");
                        final String requestTimePointAsJson = "\"requestTimepoint\": \"" + formatTimePoint(requestTimePoint) + "\", ";
                        if (indexOfFirstOpeningBrace >= 0) {
                            jsonLeaderboardAsString.insert(indexOfFirstOpeningBrace+1, requestTimePointAsJson);
                        }
                        resp.getWriter().write(jsonLeaderboardAsString.toString());
                        if (indexOfFirstOpeningBrace >= 0) {
                            jsonLeaderboardAsString.delete(indexOfFirstOpeningBrace+1, indexOfFirstOpeningBrace+1+requestTimePointAsJson.length());
                        }
                    }
                } catch (NoWindException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            }
        }
    }

    private StringBuffer getLeaderboardJsonFromCacheOrCompute(Leaderboard leaderboard,
            com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer> timePointAndResultStateAndMaxCompetitorsCount) throws NoWindException {
        final StringBuffer result;
        Map<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>, StringBuffer> cacheEntry = cache.get(leaderboard, /* waitForLatest */ false);
        if (cacheEntry == null || !cacheEntry.containsKey(timePointAndResultStateAndMaxCompetitorsCount)) {
            cacheMisses++;
            LinkedHashSet<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>> timePointsAndResultStatesAndMaxCompetitorsCount = new LinkedHashSet<>();
            timePointsAndResultStatesAndMaxCompetitorsCount.add(timePointAndResultStateAndMaxCompetitorsCount);
            cache.triggerUpdate(leaderboard, new LeaderboardJsonCacheUpdateInterval(timePointsAndResultStatesAndMaxCompetitorsCount));
            // now wait for this entry to be computed
            cacheEntry = cache.get(leaderboard, /* waitForLatest */ true);
            StringBuffer jsonObjectAsString = cacheEntry.get(timePointAndResultStateAndMaxCompetitorsCount);
            result = jsonObjectAsString;
        } else {
            cacheHits++;
            result = cacheEntry.get(timePointAndResultStateAndMaxCompetitorsCount);
        }
        logger.finest(this+" cache hits/misses: "+cacheHits+"/"+cacheMisses);
        return result;
    }

    private JSONObject computeLeaderboardJson(Leaderboard leaderboard,
            com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer> timePointAndResultStateAndMaxCompetitorsCount) throws NoWindException {
        TimePoint resultTimePoint = timePointAndResultStateAndMaxCompetitorsCount.getA();
        ResultStates resultState = timePointAndResultStateAndMaxCompetitorsCount.getB();
        Integer maxCompetitorsCount = timePointAndResultStateAndMaxCompetitorsCount.getC();
        List<Competitor> competitorsOrderedByTotalRank = leaderboard.getCompetitorsFromBestToWorst(resultTimePoint);
        Map<RaceColumn, List<Competitor>> rankedCompetitorsPerColumn = new HashMap<RaceColumn, List<Competitor>>();
        JSONObject jsonLeaderboard = new JSONObject();
        
        writeCommonLeaderboardData(jsonLeaderboard, leaderboard, resultState, resultTimePoint, maxCompetitorsCount);

        JSONArray jsonCompetitorEntries = new JSONArray();
        jsonLeaderboard.put("competitors", jsonCompetitorEntries);
        int counter = 1;
        for (Competitor competitor : competitorsOrderedByTotalRank) {
            if(maxCompetitorsCount != null && counter > maxCompetitorsCount) {
                break;
            }
            JSONObject jsonCompetitor = new JSONObject();
            jsonCompetitor.put("name", competitor.getName());
            final String displayName = leaderboard.getDisplayName(competitor);
            jsonCompetitor.put("displayName", displayName==null?competitor.getName():displayName);
            jsonCompetitor.put("id", competitor.getId().toString());
            jsonCompetitor.put("sailID", competitor.getBoat().getSailID());
            Nationality nationality = competitor.getTeam().getNationality();
            jsonCompetitor.put("nationality", nationality != null ? nationality.getThreeLetterIOCAcronym(): null);
            jsonCompetitor.put("countryCode", nationality != null ? (nationality.getCountryCode() != null ? nationality.getCountryCode().getTwoLetterISOCode(): null) : null);
            
            jsonCompetitor.put("rank", competitorsOrderedByTotalRank.indexOf(competitor) + 1);
            jsonCompetitor.put("carriedPoints", leaderboard.getCarriedPoints(competitor));
            jsonCompetitor.put("totalPoints", leaderboard.getTotalPoints(competitor, resultTimePoint));
            jsonCompetitorEntries.add(jsonCompetitor);
            JSONObject jsonRaceColumns = new JSONObject();
            jsonCompetitor.put("raceScores", jsonRaceColumns);
            final Set<RaceColumn> discardedRaceColumns = leaderboard.getResultDiscardingRule().getDiscardedRaceColumns(competitor, leaderboard,
                    leaderboard.getRaceColumns(), resultTimePoint);
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
                jsonEntry.put("uncorrectedNetPoints", leaderboard.getTrackedRank(competitor, raceColumn, resultTimePoint));
                jsonEntry.put("totalPoints", leaderboard.getTotalPoints(competitor, raceColumn, resultTimePoint, discardedRaceColumns));
                MaxPointsReason maxPointsReason = leaderboard.getMaxPointsReason(competitor, raceColumn, resultTimePoint);
                jsonEntry.put("maxPointsReason", maxPointsReason != null ? maxPointsReason.toString(): null);
                jsonEntry.put("rank", rankedCompetitorsForColumn.indexOf(competitor)+1);
                final TrackedRace trackedRace = raceColumn.getTrackedRace(competitor);
                if (trackedRace != null) {
                    jsonEntry.put("raceRank", trackedRace.getRank(competitor, resultTimePoint));
                }
                jsonEntry.put("isDiscarded", leaderboard.isDiscarded(competitor, raceColumn, resultTimePoint));
                jsonEntry.put("isCorrected", leaderboard.getScoreCorrection().isScoreCorrected(competitor, raceColumn, resultTimePoint));
            }
            counter++;
        }
        return jsonLeaderboard;
    }

    private String formatTimePoint(TimePoint timepoint) {
        return timepoint == null ? null : TIMEPOINT_FORMATTER.format(timepoint.asDate());
    }
    
    private JSONObject createEmptyLeaderboardJson(Leaderboard leaderboard,
            ResultStates resultState, TimePoint requestTimePoint, Integer maxCompetitorsCount) throws NoWindException {
        JSONObject jsonLeaderboard = new JSONObject();
        
        writeCommonLeaderboardData(jsonLeaderboard, leaderboard, resultState, null, maxCompetitorsCount);

        JSONArray jsonCompetitorEntries = new JSONArray();
        jsonLeaderboard.put("competitors", jsonCompetitorEntries);
        for (Competitor competitor : leaderboard.getCompetitors()) {
            JSONObject jsonCompetitor = new JSONObject();
            jsonCompetitor.put("name", competitor.getName());
            final String displayName = leaderboard.getDisplayName(competitor);
            jsonCompetitor.put("displayName", displayName==null?competitor.getName():displayName);
            jsonCompetitor.put("id", competitor.getId().toString());
            jsonCompetitor.put("sailID", competitor.getBoat().getSailID());
            Nationality nationality = competitor.getTeam().getNationality();
            jsonCompetitor.put("nationality", nationality != null ? nationality.getThreeLetterIOCAcronym(): null);
            jsonCompetitor.put("countryCode", nationality != null ? (nationality.getCountryCode() != null ? nationality.getCountryCode().getTwoLetterISOCode(): null) : null);
            
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
        return jsonLeaderboard;
    }

    private void writeCommonLeaderboardData(JSONObject jsonLeaderboard, Leaderboard leaderboard, ResultStates resultState, 
            TimePoint resultTimePoint, Integer maxCompetitorsCount) {
        jsonLeaderboard.put("name", leaderboard.getName());
        
        // for backward compatibility 
        jsonLeaderboard.put("timepoint", formatTimePoint(resultTimePoint));

        jsonLeaderboard.put("resultTimepoint", formatTimePoint(resultTimePoint));
        jsonLeaderboard.put("resultState", resultState.name());
        jsonLeaderboard.put("maxCompetitorsCount", maxCompetitorsCount);
        
        SettableScoreCorrection scoreCorrection = leaderboard.getScoreCorrection();
        if (scoreCorrection != null) {
            jsonLeaderboard.put("scoringComment", scoreCorrection.getComment());
            TimePoint lastUpdateTimepoint = scoreCorrection.getTimePointOfLastCorrectionsValidity();
            jsonLeaderboard.put("lastScoringUpdate", formatTimePoint(lastUpdateTimepoint));
        } else {
            jsonLeaderboard.put("scoringComment", null);
            jsonLeaderboard.put("lastScoringUpdate", null);
        }
        
        JSONArray jsonColumnNames = new JSONArray();
        jsonLeaderboard.put("columnNames", jsonColumnNames);
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            jsonColumnNames.add(raceColumn.getName());
        }
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
            if (result == null) {
                result = MillisecondsTimePoint.now();
            }
            break;
        case Preliminary:
        case Final:
            if (leaderboard.getScoreCorrection() != null && leaderboard.getScoreCorrection().getTimePointOfLastCorrectionsValidity() != null) {
                result = leaderboard.getScoreCorrection().getTimePointOfLastCorrectionsValidity();
                // As we don't have implemented bug 1246 (Define a clear result state for races and leaderboards) so far
                // we need to make sure that the timpoint for the final state is not determined in the middle of a running race,
                // because this would deliver not only final results but also some "mixed-in" live results.
                // Therefore, if there is a race that hasn't finished yet and whose first start mark passing is before
                // the current result, move result to before the start mark passing.
                for (TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
                    TimePoint endOfRace = trackedRace.getEndOfRace();
                    if (endOfRace == null) {
                        Waypoint firstWaypoint = trackedRace.getRace().getCourse().getFirstWaypoint();
                        if (firstWaypoint != null) {
                            Iterable<MarkPassing> markPassingsForFirstWaypoint = trackedRace.getMarkPassingsInOrder(firstWaypoint);
                            if (markPassingsForFirstWaypoint != null) {
                                trackedRace.lockForRead(markPassingsForFirstWaypoint);
                                try {
                                    Iterator<MarkPassing> i = markPassingsForFirstWaypoint.iterator();
                                    if (i.hasNext()) {
                                        TimePoint earliestMarkPassingTimePoint = i.next().getTimePoint();
                                        if (result == null || earliestMarkPassingTimePoint.before(result)) {
                                            result = earliestMarkPassingTimePoint.minus(1);
                                        }
                                    }
                                } finally {
                                    trackedRace.unlockAfterRead(markPassingsForFirstWaypoint);
                                }
                            }
                        }
                    }
                }
            }
            break;
        }
        return result;
    }

    @Override
    public void add(Leaderboard leaderboard) {
        // nothing to do; the triggerUpdate call on the SmartFutureCache in doGet ensures that a record for the leaderboard
        // will be created in the SmartFutureCache
    }

    /**
     * Invalidation triggers the recalculation for all time point / result state pairs for which the cache currently
     * holds values for <code>leaderboard</code>. It preserves the order in which they were inserted, so values requested
     * later (not necessarily having a later time point!) will be re-calculated and re-added later and therefore have a higher
     * chance of surviving cache eviction. Still, the cache won't grow beyond its limits because it uses a {@link LinkedHashMap}
     * with total size (aggregated size of all these maps)-based eviction.<p>
     * 
     * At the same time, existing cache entries won't simply be removed by the invalidation but remain in place for clients
     * asking with <code>waitForLatest==false</code> and will only be replaced once the re-calculation has completed. It will be
     * the cache size set by {@link #MAX_TOTAL_NUMBER_OF_CACHE_ENTRIES} that roughly decides how long a recalculation can last.
     * The higher this number, the more entries a map for a single leaderboard may have which all will be re-calculated when
     * this method is called.
     */
    @Override
    public void invalidate(Leaderboard leaderboard) {
        final LinkedHashMap<com.sap.sse.common.Util.Triple<TimePoint, ResultStates, Integer>, StringBuffer> currentCachedValueForLeaderboard =
                cache.get(leaderboard, /* waitForLatest */ false);
        cache.triggerUpdate(leaderboard, new LeaderboardJsonCacheUpdateInterval(currentCachedValueForLeaderboard.keySet()));
    }
}
