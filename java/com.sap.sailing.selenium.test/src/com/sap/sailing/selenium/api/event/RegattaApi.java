package com.sap.sailing.selenium.api.event;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.JsonWrapper;

public class RegattaApi {

    private static final String REGATTAS = "/api/v1/regattas";
    private static final String LIST_REGATTA_RACES = "/races";
    private static final String COMPETITOR_CREATE_AND_ADD_WITH_BOAT = "/competitors/createandadd";
    private static final String ADD_RACE_COLUMN_URL = "/addracecolumns";

    public Regatta getRegatta(ApiContext ctx, String regattaName) {
        Regatta regatta = new Regatta(ctx.get(REGATTAS + "/" + regattaName));
        return regatta;
    }

    public JSONObject getRegattaRaces(ApiContext ctx, String regattaName) {
        return ctx.get(REGATTAS + "/" + regattaName + LIST_REGATTA_RACES);
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
        String url = REGATTAS + "/" + regattaName + COMPETITOR_CREATE_AND_ADD_WITH_BOAT;
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("boatclass", boatclass);
        queryParams.put("competitorEmail", competitorEmail);
        queryParams.put("competitorName", competitorName);
        queryParams.put("nationalityIOC", nationalityIOC);
        queryParams.put("secret", secret.orElse(null));
        deviceUuid.ifPresent(c -> queryParams.put("deviceUuid", c.toString()));
        JSONObject competitorJson = ctx.post(url, queryParams);
        return new Competitor(competitorJson);
    }

    public RaceColumn[] addRaceColumn(ApiContext ctx, String regattaName, String prefix, Integer numberOfRaces) {
        String url = REGATTAS + "/" + regattaName + ADD_RACE_COLUMN_URL;
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("prefix", prefix);
        queryParams.put("numberOfRaces", numberOfRaces != null ? numberOfRaces.toString() : null);
        JSONArray json = ctx.post(url, queryParams);
        RaceColumn[] result = new RaceColumn[json.size()];
        for (int i = 0; i < json.size(); i++) {
            result[i] = new RaceColumn((JSONObject)(json.get(i)));
        }
        return result;
    }

    public class Regatta extends JsonWrapper {

        public Regatta(JSONObject json) {
            super(json);
        }

        public String getName() {
            return get("name");
        }

        public Date getStartDate() {
            return get("startDate");
        }

        public Date getEndDate() {
            return get("endDate");
        }

        public String getBoatClass() {
            return get("boatclass");
        }

        public String getScoringSystem() {
            return get("scoringSystem");
        }

        public String getCourseAreaId() {
            return get("courseAreaId");
        }

        public Boolean canBoatsOfCompetitorsChangePerRace() {
            return get("canBoatsOfCompetitorsChangePerRace");
        }

        public CompetitorRegistrationType getCompetitorRegistrationType() {
            return CompetitorRegistrationType.valueOf(get("competitorRegistrationType"));
        }
    }

    public class Competitor extends JsonWrapper {

        public Competitor(JSONObject json) {
            super(json);
        }

        public UUID getId() {
            return UUID.fromString(get("id"));
        }

        public String getName() {
            return get("name");
        }

        public String getShortName() {
            return get("shortName");
        }

        public String getNationality() {
            return get("nationality");
        }

        public String getNationalityISO2() {
            return get("nationalityISO2");
        }

        public String getNationalityISO3() {
            return get("nationalityISO3");
        }

        public Team getTeam() {
            return new Team(get("team"));
        }

        public Boat getBoat() {
            return new Boat(get("boat"));
        }
    }

    public class Team extends JsonWrapper {

        public Team(JSONObject json) {
            super(json);
        }

        public String getName() {
            return get("name");
        }

        public String getNationality() {
            return (String) ((JSONObject) get("nationality")).get("IOC");
        }

    }

    public class Boat extends JsonWrapper {

        public Boat(JSONObject json) {
            super(json);
        }

        public UUID getId() {
            return UUID.fromString(get("id"));
        }

        public String getName() {
            return get("name");
        }

        public String getSailId() {
            return get("sailId");
        }

        public String getColor() {
            return get("color");
        }

        public BoatClass getBoatClass() {
            return new BoatClass(get("boatClass"));
        }
    }

    public class BoatClass extends JsonWrapper {

        public BoatClass(JSONObject json) {
            super(json);
        }

        public String getName() {
            return get("name");
        }

        public String getDisplayName() {
            return get("displayName");
        }

        public Boolean getTypciallyStartsUpwind() {
            return get("typicallyStartsUpwind");
        }

        public Double getHullLengthInMeters() {
            return get("hullLengthInMeters");
        }

        public Double getHullBeamInMeters() {
            return get("hullBeamInMeters");
        }
    }

    public class RaceColumn extends JsonWrapper {

        public RaceColumn(JSONObject json) {
            super(json);
        }

        public String getSeriesName() {
            return get("seriesname");
        }

        public String getRaceName() {
            return get("racename");
        }
    }
}
