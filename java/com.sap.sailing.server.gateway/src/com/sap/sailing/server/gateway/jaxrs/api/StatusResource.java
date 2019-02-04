package com.sap.sailing.server.gateway.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.replication.ReplicationService;
import com.sap.sse.replication.ReplicationStatus;

@Path("/v1/status")
public class StatusResource extends AbstractSailingServerResource {
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getBoatClasses() {
        final JSONObject result = new JSONObject();
        final RacingEventService service = getService();
        result.put("numberofracestorestore", service.getNumberOfTrackedRacesToRestore());
        result.put("numberofracesrestored", service.getNumberOfTrackedRacesRestored());
        final ReplicationService replicationService = getReplicationService();
        final ReplicationStatus replicationStatus = replicationService == null ? null : replicationService.getStatus();
        if (replicationStatus != null) {
            result.put("replication", replicationStatus.toJSONObject());
        }
        result.put("available", service.getNumberOfTrackedRacesRestored() >= service.getNumberOfTrackedRacesToRestore() &&
                (replicationStatus == null || replicationStatus.isAvailable()));
        return Response.ok(result.toJSONString()).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }
}