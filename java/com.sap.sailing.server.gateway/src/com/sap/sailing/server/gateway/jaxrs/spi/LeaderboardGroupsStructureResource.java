package com.sap.sailing.server.gateway.jaxrs.spi;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.TopLevelMasterDataSerializer;

@Path("/v1/leaderboardgroups/structure")
public class LeaderboardGroupsStructureResource extends AbstractSailingServerResource {
    
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getStructureOfLeaderboardGroups(@QueryParam("names") List<String> leaderboardGroupNames) {
        Map<String, LeaderboardGroup> leaderboardGroups = getService().getLeaderboardGroups();
        Set<String> requestedLeaderboardGroupNames = new HashSet<String>();

        if (leaderboardGroupNames != null && !leaderboardGroupNames.isEmpty()) {
            requestedLeaderboardGroupNames.addAll(leaderboardGroupNames);
        } else {
            // No range supplied. Export all for now
            requestedLeaderboardGroupNames.addAll(leaderboardGroups.keySet());
        }

        TopLevelMasterDataSerializer masterSerializer = new TopLevelMasterDataSerializer(leaderboardGroups,
                getService().getAllEvents(), getService().getPersistentRegattasForRaceIDs(), getService().getAllMediaTracks());

        JSONObject masterData = masterSerializer.serialize(requestedLeaderboardGroupNames);
        
        String json = masterData.toJSONString();
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
}
 