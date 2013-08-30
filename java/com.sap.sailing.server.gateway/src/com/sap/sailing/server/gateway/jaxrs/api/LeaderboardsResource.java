package com.sap.sailing.server.gateway.impl.rs;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.MetaLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardCache;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.HttpRequestUtils;
import com.sap.sailing.server.gateway.ParseHttpParameterException;
import com.sap.sailing.server.gateway.impl.LeaderboardJsonGetServlet.ResultStates;
import com.sap.sailing.util.SmartFutureCache;

@Path("/v1/leaderboards")
public class LeaderboardsResource extends AbstractSailingServerResource {
    
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getLeaderboards() {
        JSONArray jsonLeaderboards = new JSONArray();
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        for (String leaderboardName : leaderboards.keySet()) {
            if(!leaderboardName.equals(LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME))
                jsonLeaderboards.add(leaderboardName);
        }

        byte[] json = jsonLeaderboards.toJSONString().getBytes();
        return Response.ok(json).build();
    }
   
    /**
     * Uses a {@link SmartFutureCache}.
     */
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{name}")
    public Response getLeaderboard(@PathParam("name") String leaderboardName,
            @DefaultValue("Live") @QueryParam("resultState") LeaderboardJsonCache.ResultStates resultState,
            @DefaultValue("true") @QueryParam("useCache") boolean useCache,
            @DefaultValue("1000") @QueryParam("maxCompetitorsCount") int maxCompetitorsCount) {
        Response response;

        TimePoint requestTimePoint = MillisecondsTimePoint.now();
            Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
            if (leaderboard == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Leaderboard "+leaderboardName+" not found");
            } else {
                try {
                    TimePoint resultTimePoint = calculateTimePointForResultState(leaderboard, resultState);
                    StringBuffer jsonLeaderboardAsString;
                    if (resultTimePoint != null) {
                        Triple<TimePoint, ResultStates, Integer> resultStateAndTimePointAndMaxCompetitorsCount =
                                new Triple<>(resultTimePoint, resultState, maxCompetitorsCount);
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
                        final String requestTimePointAsJson = "\"requestTimepoint\": \""+requestTimePoint.toString()+"\", ";
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

        
        byte[] json = jsonLeaderboardGroup.toJSONString().getBytes();
        response = Response.ok(json).build();

        return response;
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

}
 