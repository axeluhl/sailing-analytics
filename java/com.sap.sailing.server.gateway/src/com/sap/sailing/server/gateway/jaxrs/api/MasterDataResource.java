package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.TopLevelMasterDataSerializer;

@Path("/v1/masterdata")
public class MasterDataResource extends AbstractSailingServerResource {
    
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("leaderboardgroups")
    public Response getMasterDataByLeaderboardGroups(@QueryParam("names[]") List<String> leaderboardGroupNames) {
        Map<String, LeaderboardGroup> leaderboardGroups = getService().getLeaderboardGroups();
        Set<String> requestedLeaderboardGroupNames = new HashSet<String>();

        if (leaderboardGroupNames.size() > 0) {
            for (String name : leaderboardGroupNames) {
                requestedLeaderboardGroupNames.add(name);
            }
        } else {
            // No range supplied. Export all for now
            requestedLeaderboardGroupNames.addAll(leaderboardGroups.keySet());
        }

        TopLevelMasterDataSerializer masterSerializer = new TopLevelMasterDataSerializer(leaderboardGroups,
                getService().getAllEvents(), getService().getPersistentRegattasForRaceIDs(), getService().getAllMediaTracks());

        JSONObject masterData = masterSerializer.serialize(requestedLeaderboardGroupNames);
        ResponseBuilder resp = Response.ok(masterData.toJSONString().getBytes(), "application/json;charset=UTF-8");
        return resp.build();
    }

}
