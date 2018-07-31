package com.sap.sailing.windestimation.data.deserializer;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.DetailedBoatClassJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorTrackWithEstimationDataJsonSerializer;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sse.common.Distance;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class CompetitorTrackWithEstimationDataJsonDeserializer<T>
        implements JsonDeserializer<CompetitorTrackWithEstimationData<T>> {

    private final JSONParser jsonParser = new JSONParser();
    private final DetailedBoatClassJsonDeserializer boatClassJsonDeserializer;
    private final JsonDeserializer<T> competitorTrackElementsJsonDeserializer;

    public CompetitorTrackWithEstimationDataJsonDeserializer(
            DetailedBoatClassJsonDeserializer boatClassJsonDeserializer,
            JsonDeserializer<T> competitorTrackElementsJsonDeserializer) {
        this.boatClassJsonDeserializer = boatClassJsonDeserializer;
        this.competitorTrackElementsJsonDeserializer = competitorTrackElementsJsonDeserializer;
    }

    @Override
    public CompetitorTrackWithEstimationData<T> deserialize(JSONObject jsonObject) throws JsonDeserializationException {
        String competitorName = (String) jsonObject
                .get(CompetitorTrackWithEstimationDataJsonSerializer.COMPETITOR_NAME);
        Double avgIntervalBetweenFixesInSeconds = (Double) jsonObject
                .get(CompetitorTrackWithEstimationDataJsonSerializer.AVG_INTERVAL_BETWEEN_FIXES_IN_SECONDS);
        Object boatClassObj = jsonObject.get(CompetitorTrackWithEstimationDataJsonSerializer.BOAT_CLASS);
        BoatClass boatClass;
        try {
            boatClass = boatClassJsonDeserializer.deserialize(getJSONObject(boatClassObj.toString()));
        } catch (ParseException e) {
            throw new JsonDeserializationException(e);
        }
        Double distanceTravelledInMeters = (Double) jsonObject
                .get(CompetitorTrackWithEstimationDataJsonSerializer.DISTANCE_TRAVELLED_IN_METERS);
        Long startUnixTime = (Long) jsonObject.get(CompetitorTrackWithEstimationDataJsonSerializer.START_TIME_POINT);
        Long endUnixTime = (Long) jsonObject.get(CompetitorTrackWithEstimationDataJsonSerializer.END_TIME_POINT);
        Long fixedCountForPolars = (Long) jsonObject
                .get(CompetitorTrackWithEstimationDataJsonSerializer.FIXES_COUNT_FOR_POLARS);
        Long markPassingsCount = (Long) jsonObject
                .get(CompetitorTrackWithEstimationDataJsonSerializer.MARK_PASSINGS_COUNT);
        Long waypointsCount = (Long) jsonObject.get(CompetitorTrackWithEstimationDataJsonSerializer.WAYPOINTS_COUNT);

        JSONArray elementsJson = (JSONArray) jsonObject.get(CompetitorTrackWithEstimationDataJsonSerializer.ELEMENTS);
        List<T> completeManeuverCurves = new ArrayList<>(elementsJson.size());
        for (Object maneuverCurveObj : elementsJson) {
            T elements;
            try {
                elements = competitorTrackElementsJsonDeserializer
                        .deserialize(getJSONObject(maneuverCurveObj.toString()));
            } catch (ParseException e) {
                throw new JsonDeserializationException(e);
            }
            completeManeuverCurves.add(elements);
        }
        CompetitorTrackWithEstimationData<T> competitorTrackWithEstimationData = new CompetitorTrackWithEstimationData<>(
                competitorName, boatClass, completeManeuverCurves, avgIntervalBetweenFixesInSeconds,
                distanceTravelledInMeters == null ? Distance.NULL : new MeterDistance(distanceTravelledInMeters),
                startUnixTime == null ? null : new MillisecondsTimePoint(startUnixTime),
                endUnixTime == null ? null : new MillisecondsTimePoint(endUnixTime), fixedCountForPolars,
                markPassingsCount.intValue(), waypointsCount.intValue());
        return competitorTrackWithEstimationData;
    }

    private JSONObject getJSONObject(String json) throws ParseException {
        return (JSONObject) jsonParser.parse(json);
    }

}
