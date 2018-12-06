package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

public class RaceEntriesJsonSerializer implements JsonSerializer<RaceDefinition> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_COMPETITORS = "competitors";

    private final CompetitorAndBoatJsonSerializer competitorAndBoatSerializer;
    private final SecurityService securityService;

    public RaceEntriesJsonSerializer(final SecurityService securityService) {
        this(null, securityService);
    }

    public RaceEntriesJsonSerializer(CompetitorAndBoatJsonSerializer competitorAndBoatSerializer, final SecurityService securityService) {
        this.competitorAndBoatSerializer = competitorAndBoatSerializer;
        this.securityService = securityService;
    }

    public JSONObject serialize(RaceDefinition race) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, race.getName());
        if(competitorAndBoatSerializer != null) {
            JSONArray competitorsJson = new JSONArray();
            for (Entry<Competitor, Boat> competitorAndBoatEntry: race.getCompetitorsAndTheirBoats().entrySet()) {
                Competitor competitor = competitorAndBoatEntry.getKey();
                competitor = (securityService.hasCurrentUserExplictPermissions(competitor, DefaultActions.READ)) ? competitor : null;
                DynamicBoat boat = (DynamicBoat) competitorAndBoatEntry.getValue();
                boat = (securityService.hasCurrentUserExplictPermissions(boat, DefaultActions.READ)) ? boat : null; 
                competitorsJson.add(competitorAndBoatSerializer.serialize(new Pair<>(competitor, boat)));
            }
            result.put(FIELD_COMPETITORS, competitorsJson);
        }
        return result;
    }
}
