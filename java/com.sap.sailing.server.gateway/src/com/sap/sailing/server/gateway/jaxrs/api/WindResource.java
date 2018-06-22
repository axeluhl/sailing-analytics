package com.sap.sailing.server.gateway.jaxrs.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.deserialization.impl.PositionJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.WindJsonDeserializer;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;

@Path("/v1/wind")
public class WindResource extends AbstractSailingServerResource {
    private final JsonDeserializer<Wind> deserializer = new WindJsonDeserializer(new PositionJsonDeserializer());

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("putWind")
    public Response putWind(String json) throws ParseException, JsonDeserializationException {
        Object requestBody = JSONValue.parseWithException(json);
        JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
        JSONArray windDatas = (JSONArray) requestObject.get("windData");

        String regattaName = (String) requestObject.get("regattaName");
        String raceName = (String) requestObject.get("raceName");

        WindSourceType windSourceType = WindSourceType.valueOf((String) (String) requestObject.get("windSourceType"));
        String windSourceId = (String) requestObject.get("windSourceId");
        DynamicTrackedRace trackedRace = getService().getTrackedRace(new RegattaNameAndRaceName(regattaName, raceName));
        WindSource windsource = new WindSourceWithAdditionalID(windSourceType, windSourceId);

        JSONArray answer = new JSONArray();
        if (trackedRace != null) {
            for (int i = 0; i < windDatas.size(); i++) {
                JSONObject windData = Helpers.toJSONObjectSafe(windDatas.get(i));
                Wind data = deserializer.deserialize(windData);
                boolean success = trackedRace.recordWind(data, windsource);
                answer.add(i, success);
            }
        } else {
            return Response.ok("{\"error\":\"Could not resolve race\"}").build();
        }
        return Response.ok(answer.toJSONString()).build();
    }
}