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
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.PublicReadableActions;

public class RaceEntriesJsonSerializer implements JsonSerializer<RaceDefinition> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_COMPETITORS = "competitors";

    private final SecurityService securityService;

    public RaceEntriesJsonSerializer(final SecurityService securityService) {
        this.securityService = securityService;
    }

    public JSONObject serialize(RaceDefinition race) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, race.getName());
        JSONArray competitorsJson = new JSONArray();
        for (Entry<Competitor, Boat> competitorAndBoatEntry: race.getCompetitorsAndTheirBoats().entrySet()) {
            Competitor competitor = competitorAndBoatEntry.getKey();
            DynamicBoat boat = (DynamicBoat) competitorAndBoatEntry.getValue();
            securityService.checkCurrentUserExplicitPermissions(competitor, PublicReadableActions.READ_PUBLIC);
            securityService.checkCurrentUserExplicitPermissions(boat, PublicReadableActions.READ_PUBLIC);
            final CompetitorAndBoatJsonSerializer competitorAndBoatJsonSerializer = CompetitorAndBoatJsonSerializer.create(
                    /* serialize non-public competitor fields: */
                    securityService.hasCurrentUserExplicitPermissions(competitor, DefaultActions.READ));
            competitorsJson.add(competitorAndBoatJsonSerializer.serialize(new Pair<>(competitor, boat)));
        }
        result.put(FIELD_COMPETITORS, competitorsJson);
        return result;
    }
}
