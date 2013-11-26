package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

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
    
    private static final Logger logger = Logger.getLogger(MasterDataResource.class.getName());
    
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("leaderboardgroups")
    public Response getMasterDataByLeaderboardGroups(@QueryParam("names[]") List<String> leaderboardGroupNames,
            @QueryParam("compress") Boolean compress)
            throws IOException {
        long startTime = System.currentTimeMillis();
        if (compress == null) {
            compress = false;
        }
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
                getService().getAllEvents(), getService().getPersistentRegattasForRaceIDs(), getService()
                        .getAllMediaTracks());

        JSONObject masterData = masterSerializer.serialize(requestedLeaderboardGroupNames);
        byte[] uncompressedResult = masterData.toJSONString().getBytes("UTF-8");
        byte[] result;
        if (compress) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(uncompressedResult);
            gzip.close();
            result = out.toByteArray();
        } else {
            result = uncompressedResult;
        }
        ResponseBuilder resp = Response.ok(result,
                "application/json;charset=UTF-8");
        Response builtResponse = resp.build();
        long timeToExport = System.currentTimeMillis() - startTime;
        logger.info(String.format("Took %s ms to export master data.", timeToExport));
        return builtResponse;
    }

}
