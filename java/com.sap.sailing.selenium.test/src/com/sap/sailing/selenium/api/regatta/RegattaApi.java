package com.sap.sailing.selenium.api.regatta;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class RegattaApi {

    private static final String REGATTAS = "/api/v1/regattas";
    private static final String LIST_REGATTA_RACES = "/races";
    private static final String COMPETITOR_CREATE_AND_ADD_WITH_BOAT = "/competitors/createandadd";
    private static final String ADD_RACE_COLUMN_URL = "/addracecolumns";

    public Regatta getRegatta(ApiContext ctx, String regattaName) {
        return new Regatta(ctx.get(REGATTAS + "/" + regattaName));
    }

    public RegattaRaces getRegattaRaces(ApiContext ctx, String regattaName) {
        return new RegattaRaces(ctx.get(REGATTAS + "/" + regattaName + LIST_REGATTA_RACES));
    }

    public Competitor createAndAddCompetitor(ApiContext ctx, String regattaName, String boatclass,
            String competitorEmail, String competitorName, String nationalityIOC, UUID deviceUuid) {
        return createAndAddCompetitor(ctx, regattaName, boatclass, competitorEmail, competitorName, nationalityIOC,
                Optional.empty(), Optional.ofNullable(deviceUuid));
    }

    public Competitor createAndAddCompetitor(ApiContext ctx, String regattaName, String boatclass,
            String competitorEmail, String competitorName, String nationalityIOC) {
        return createAndAddCompetitor(ctx, regattaName, boatclass, competitorEmail, competitorName, nationalityIOC,
                Optional.empty(), Optional.empty());
    }

    public Competitor createAndAddCompetitorWithSecret(ApiContext ctx, String regattaName, String boatclass,
            String competitorEmail, String competitorName, String nationalityIOC, String secret, UUID deviceUuid) {
        return createAndAddCompetitor(ctx, regattaName, boatclass, competitorEmail, competitorName, nationalityIOC,
                Optional.of(secret), Optional.of(deviceUuid));
    }

    public Competitor createAndAddCompetitorWithSecret(ApiContext ctx, String regattaName, String boatclass,
            String competitorEmail, String competitorName, String nationalityIOC, String secret) {
        return createAndAddCompetitor(ctx, regattaName, boatclass, competitorEmail, competitorName, nationalityIOC,
                Optional.ofNullable(secret), Optional.empty());
    }

    private Competitor createAndAddCompetitor(ApiContext ctx, String regattaName, String boatclass,
            String competitorEmail, String competitorName, String nationalityIOC, Optional<String> secret,
            Optional<UUID> deviceUuid) {
        final String url = REGATTAS + "/" + regattaName + COMPETITOR_CREATE_AND_ADD_WITH_BOAT;
        final Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("boatclass", boatclass);
        queryParams.put("competitorEmail", competitorEmail);
        queryParams.put("competitorName", competitorName);
        queryParams.put("nationalityIOC", nationalityIOC);
        queryParams.put("secret", secret.orElse(null));
        deviceUuid.ifPresent(c -> queryParams.put("deviceUuid", c.toString()));
        final JSONObject competitorJson = ctx.post(url, queryParams);
        return new Competitor(competitorJson);
    }

    public RaceColumn[] addRaceColumn(ApiContext ctx, String regattaName, String prefix, Integer numberOfRaces) {
        final String url = REGATTAS + "/" + regattaName + ADD_RACE_COLUMN_URL;
        final Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("prefix", prefix);
        queryParams.put("numberOfRaces", numberOfRaces != null ? numberOfRaces.toString() : null);
        final JSONArray json = ctx.post(url, queryParams);
        final RaceColumn[] result = new RaceColumn[json.size()];
        for (int i = 0; i < json.size(); i++) {
            result[i] = new RaceColumn((JSONObject) (json.get(i)));
        }
        return result;
    }

}
