package com.sap.sailing.server.gateway.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.EventBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.LeaderboardGroupBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.LeaderboardSearchResultJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.VenueJsonSerializer;
import com.sap.sse.common.Util;
import com.sap.sse.common.search.KeywordQuery;
import com.sap.sse.common.search.Result;

@Path("/v1/search")
public class SearchResource extends AbstractSailingServerResource {
    private final JsonSerializer<LeaderboardSearchResult> serializer;
    
    public SearchResource() {
        final LeaderboardGroupBaseJsonSerializer leaderboardGroupSerializer = new LeaderboardGroupBaseJsonSerializer();
        serializer = new LeaderboardSearchResultJsonSerializer(new EventBaseJsonSerializer(new VenueJsonSerializer(
                new CourseAreaJsonSerializer()), leaderboardGroupSerializer), leaderboardGroupSerializer);
    }
    
    private Result<LeaderboardSearchResult> search(KeywordQuery query) {
        return getService().search(query);
    }
    
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response search(@QueryParam("q") String keywords) {
        KeywordQuery query = new KeywordQuery(Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(keywords));
        Iterable<LeaderboardSearchResult> searchResults = search(query).getHits();
        JSONArray jsonSearchResults = new JSONArray();
        for (LeaderboardSearchResult searchResult : searchResults) {
            if(searchResult.getLeaderboard() == null || getSecurityService().hasCurrentUserReadPermission(searchResult.getLeaderboard())) {
                if(searchResult.getRegatta() == null || getSecurityService().hasCurrentUserReadPermission(searchResult.getRegatta())) {
                    if (searchResult.getLeaderboard() == null || checkAll(searchResult.getLeaderboardGroups())) {
                        jsonSearchResults.add(serializer.serialize(searchResult));
                    }
                }
            }
            
        }
        return Response.ok(jsonSearchResults.toJSONString()).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

    private boolean checkAll(Iterable<LeaderboardGroup> leaderboardGroups) {
        boolean result = true;
        for (LeaderboardGroup leaderboardGroup : leaderboardGroups) {
            if (!getSecurityService().hasCurrentUserReadPermission(leaderboardGroup)) {
                result = false;
            }
        }
        return result;
    }
}
