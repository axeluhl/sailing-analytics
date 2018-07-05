package com.sap.sailing.server.gateway.jaxrs.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.domain.common.security.Permission.Mode;
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
        SecurityUtils.getSubject().checkPermission(Permission.TRACKED_RACE.getStringPermission(Mode.UPDATE));

        Object requestBody = JSONValue.parseWithException(json);
        JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
        JSONArray windDatas = (JSONArray) requestObject.get("windData");

        JSONArray regattaNamesAndRaceNames = (JSONArray) requestObject.get("regattaNamesAndRaceNames");

        WindSourceType windSourceType = WindSourceType.valueOf((String) requestObject.get("windSourceType"));
        JSONArray answer = new JSONArray();
        String windSourceId = (String) requestObject.get("windSourceId");
        for (Object regattaNameAndRaceName : regattaNamesAndRaceNames) {
            final JSONObject regattaNameAndRaceNameObject = Helpers.toJSONObjectSafe(regattaNameAndRaceName);
            String regattaName = (String) regattaNameAndRaceNameObject.get("regattaName");
            String raceName = (String) regattaNameAndRaceNameObject.get("raceName");
            RegattaNameAndRaceName identifier = new RegattaNameAndRaceName(regattaName, raceName);
            JSONObject answerForRace = new JSONObject();
            answerForRace.put("regattaNameAndRaceName", regattaNameAndRaceName);
            if (windSourceType == WindSourceType.EXPEDITION || windSourceType == WindSourceType.WEB) {
                DynamicTrackedRace trackedRace = getService().getTrackedRace(identifier);
                WindSource windsource = new WindSourceWithAdditionalID(windSourceType, windSourceId);

                if (trackedRace != null) {
                    JSONArray subAnswer = new JSONArray();
                    for (int i = 0; i < windDatas.size(); i++) {
                        JSONObject windData = Helpers.toJSONObjectSafe(windDatas.get(i));
                        Wind data = deserializer.deserialize(windData);
                        boolean success = trackedRace.recordWind(data, windsource);
                        subAnswer.add(i, success);
                    }
                    answerForRace.put("answer", subAnswer);
                } else {
                    answerForRace.put("answer", "Could not resolve traced race");
                }
            } else {
                answerForRace.put("answer", "Only Windsourcetypes expedition or web are allowed");
            }
            answer.add(answerForRace);
        }
        return Response.ok(answer.toJSONString()).build();
    }
}