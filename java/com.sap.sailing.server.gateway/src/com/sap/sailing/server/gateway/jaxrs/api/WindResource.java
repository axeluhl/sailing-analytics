package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.deserialization.impl.PositionJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.WindJsonDeserializer;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;

@Path("/v1/wind")
public class WindResource extends AbstractSailingServerResource {
    
    private static class RaceIdentifierAndTrackedRace {
        private final RegattaAndRaceIdentifier raceIdentifier;
        private final DynamicTrackedRace trackedRace;
        public RaceIdentifierAndTrackedRace(RegattaAndRaceIdentifier raceIdentifier, DynamicTrackedRace trackedRace) {
            super();
            this.raceIdentifier = raceIdentifier;
            this.trackedRace = trackedRace;
        }
        public RegattaAndRaceIdentifier getRaceIdentifier() {
            return raceIdentifier;
        }
        public DynamicTrackedRace getTrackedRace() {
            return trackedRace;
        }
    }
    
    private final JsonDeserializer<Wind> deserializer = new WindJsonDeserializer(new PositionJsonDeserializer());
    private final static String REGATTA_NAME_FIELD = "regattaName";
    private final static String RACE_NAME_FIELD = "raceName";

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("putWind")
    public Response putWind(String json) throws ParseException, JsonDeserializationException {
        Object requestBody = JSONValue.parseWithException(json);
        JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
        JSONArray windDatas = (JSONArray) requestObject.get("windData");
        JSONArray regattaNamesAndRaceNames = (JSONArray) requestObject.get("regattaNamesAndRaceNames");
        WindSourceType windSourceType = WindSourceType.valueOf((String) requestObject.get("windSourceType"));
        JSONArray answer = new JSONArray();
        String windSourceId = (String) requestObject.get("windSourceId");
        for (final RaceIdentifierAndTrackedRace raceIdentifierAndTrackedRace : getRaces(regattaNamesAndRaceNames)) {
            JSONObject answerForRace = new JSONObject();
            answerForRace.put("regattaNameAndRaceName", serialize(raceIdentifierAndTrackedRace.getRaceIdentifier()));
            if (windSourceType == WindSourceType.EXPEDITION || windSourceType == WindSourceType.WEB) {
                DynamicTrackedRace trackedRace = raceIdentifierAndTrackedRace.getTrackedRace();
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
    
    private JSONObject serialize(RegattaAndRaceIdentifier raceIdentifier) {
        final JSONObject result = new JSONObject();
        result.put(REGATTA_NAME_FIELD, raceIdentifier.getRegattaName());
        result.put(RACE_NAME_FIELD, raceIdentifier.getRaceName());
        return result;
    }

    private Iterable<RaceIdentifierAndTrackedRace> getRaces(final JSONArray regattaNamesAndRaceNames) throws JsonDeserializationException {
        final List<RaceIdentifierAndTrackedRace> result = new ArrayList<>();
        if (regattaNamesAndRaceNames != null) {
            for (Object regattaNameAndRaceName : regattaNamesAndRaceNames) {
                final JSONObject regattaNameAndRaceNameObject = Helpers.toJSONObjectSafe(regattaNameAndRaceName);
                String regattaName = (String) regattaNameAndRaceNameObject.get(REGATTA_NAME_FIELD);
                String raceName = (String) regattaNameAndRaceNameObject.get(RACE_NAME_FIELD);
                RegattaNameAndRaceName identifier = new RegattaNameAndRaceName(regattaName, raceName);
                // add wind only to those races the subject is permitted to update
                if (getSecurityService().hasCurrentUserUpdatePermission(identifier)) {
                    result.add(new RaceIdentifierAndTrackedRace(identifier, getService().getTrackedRace(identifier)));
                }
            }
        } else {
            for (final Regatta regatta : getService().getAllRegattas()) {
                final DynamicTrackedRegatta trackedRegatta = getService().getTrackedRegatta(regatta);
                for (final DynamicTrackedRace trackedRace : trackedRegatta.getTrackedRaces()) {
                    if (getSecurityService().hasCurrentUserUpdatePermission(trackedRace.getRaceIdentifier())) {
                        result.add(new RaceIdentifierAndTrackedRace(trackedRace.getRaceIdentifier(), trackedRace));
                    }
                }
            }
        }
        return result;
    }
}