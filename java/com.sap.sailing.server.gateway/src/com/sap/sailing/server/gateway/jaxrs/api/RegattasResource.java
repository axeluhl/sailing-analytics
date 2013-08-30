package com.sap.sailing.server.gateway.impl.rs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.ColorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.FleetJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RegattaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.SeriesJsonSerializer;

@Path("/v1/regattas")
public class RegattasResource extends AbstractSailingServerResource {
    
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getRegattas() {
        RegattaJsonSerializer regattaJsonSerializer = new RegattaJsonSerializer(); 
        
        JSONArray regattasJson = new JSONArray();
        for (Regatta regatta : getService().getAllRegattas()) {
            regattasJson.add(regattaJsonSerializer.serialize(regatta));
        }
        byte[] json = regattasJson.toJSONString().getBytes();
        return Response.ok(json).build();
    }
    
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{name}")
    public Response getLeaderboardGroup(@PathParam("name") String regattaName) {
        Response response;
        Regatta regatta = getService().getRegatta(new RegattaName(regattaName));
        if (regatta == null) {
            response = Response.status(Status.NOT_FOUND).entity("Could not find a regatta with name '" + regatta + "'.").type(MediaType.TEXT_PLAIN).build();
        } else {
            SeriesJsonSerializer seriesJsonSerializer = new SeriesJsonSerializer(new FleetJsonSerializer(new ColorJsonSerializer()));
            JsonSerializer<Regatta> regattaSerializer = new RegattaJsonSerializer(seriesJsonSerializer, null);
            JSONObject serializedRegatta = regattaSerializer.serialize(regatta);
            
            byte[] json = serializedRegatta.toJSONString().getBytes();
            response = Response.ok(json).build();
        }
        return response;
    }
}
 