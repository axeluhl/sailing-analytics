package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RegattaJsonSerializer implements JsonSerializer<Regatta> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_BOATCLASS = "boatclass";
    public static final String FIELD_START_DATE = "startDate";
    public static final String FIELD_END_DATE = "endDate";
    public static final String FIELD_SCORINGSYSTEM = "scoringSystem";
    public static final String FIELD_SERIES = "series";
    public static final String FIELD_COMPETITORS = "competitors";
    public static final String FIELD_BOATS = "boats";
    public static final String FIELD_TRACKED_RACES = "trackedRaces";
    public static final String FIELD_COURSE_AREA_ID = "courseAreaId";
    public static final String FIELD_CAN_BOATS_OF_COMPETITORS_CHANGE_PER_RACE = "canBoatsOfCompetitorsChangePerRace";
    public static final String FIELD_COMPETITOR_REGISTRATION_TYPE = "competitorRegistrationType";

    private final JsonSerializer<Series> seriesSerializer;
    private final JsonSerializer<Competitor> competitorSerializer;
    private final JsonSerializer<Boat> boatSerializer;

    public RegattaJsonSerializer() {
        this(null, null, null);
    }

    public RegattaJsonSerializer(JsonSerializer<Series> seriesSerializer,
            JsonSerializer<Competitor> competitorSerializer, JsonSerializer<Boat> boatSerializer) {
        this.seriesSerializer = seriesSerializer;
        this.competitorSerializer = competitorSerializer;
        this.boatSerializer = boatSerializer;
    }

    public JSONObject serialize(Regatta regatta) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, regatta.getName());
        result.put(FIELD_START_DATE, regatta.getStartDate() != null ? regatta.getStartDate().asMillis() : null);
        result.put(FIELD_END_DATE, regatta.getEndDate() != null ? regatta.getEndDate().asMillis() : null);
        result.put(FIELD_SCORINGSYSTEM, regatta.getScoringScheme().getType().name());
        result.put(FIELD_BOATCLASS, regatta.getBoatClass() != null ? regatta.getBoatClass().getName() : null);
        result.put(FIELD_COURSE_AREA_ID,
                regatta.getDefaultCourseArea() != null ? regatta.getDefaultCourseArea().getId().toString() : null);
        result.put(FIELD_CAN_BOATS_OF_COMPETITORS_CHANGE_PER_RACE, regatta.canBoatsOfCompetitorsChangePerRace());
        result.put(FIELD_COMPETITOR_REGISTRATION_TYPE, regatta.getCompetitorRegistrationType().name());
        if (seriesSerializer != null) {
            JSONArray seriesJson = new JSONArray();
            for (Series series : regatta.getSeries()) {
                seriesJson.add(seriesSerializer.serialize(series));
            }
            result.put(FIELD_SERIES, seriesJson);
        }
        if (competitorSerializer != null) {
            JSONArray competitorsJson = new JSONArray();
            for (Competitor competitor : regatta.getAllCompetitors()) {
                competitorsJson.add(competitorSerializer.serialize(competitor));
            }
            result.put(FIELD_COMPETITORS, competitorsJson);
        }
        if (regatta.canBoatsOfCompetitorsChangePerRace() && boatSerializer != null) {
            JSONArray boatsJson = new JSONArray();
            for (Boat boat : regatta.getAllBoats()) {
                boatsJson.add(boatSerializer.serialize(boat));
            }
            result.put(FIELD_BOATS, boatsJson);
        }
        return result;
    }
}
