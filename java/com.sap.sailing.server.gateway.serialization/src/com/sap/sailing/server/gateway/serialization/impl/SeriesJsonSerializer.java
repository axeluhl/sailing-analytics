package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class SeriesJsonSerializer implements JsonSerializer<Series> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_FLEETS = "fleets";
    public static final String FIELD_RACES = "races"; // TODO: 'races' is wrong here... it's actually 'raceColumns'
    public static final String FIELD_TRACKED_RACES = "trackedRaces";
    public static final String FIELD_STARTS_WITH_ZERO_SCORE = "startsWithZeroScore";
    public static final String FIELD_HAS_SPLIT_FLEET_CONTIGUOUS_SCORING = "hasSplitFleetContiguousScoring";
    public static final String FIELD_IS_MEDAL_SERIES = "isMedalSeries";
    public static final String FIELD_FLEETS_CAN_RUN_IN_PARALLEL = "fleetsCanRunInParallel";
    public static final String FIELD_MAXIMUM_NUMBER_OF_DISCARDS = "maximumNumberOfDiscards";
    public static final String FIELD_FIRST_COLUMS_IS_NON_DISCARDABLE_CARRY_FORWARD = "firstColumnIsNonDiscardableCarryForward";

    private final JsonSerializer<Fleet> fleetSerializer;

    public SeriesJsonSerializer(JsonSerializer<Fleet> fleetSerializer) {
        this.fleetSerializer = fleetSerializer;
    }

    public JSONObject serialize(Series series) {
        JSONObject result = new JSONObject();
        
        result.put(FIELD_NAME, series.getName());
        result.put(FIELD_IS_MEDAL_SERIES, series.isMedal());
        result.put(FIELD_FLEETS_CAN_RUN_IN_PARALLEL, series.isFleetsCanRunInParallel());
        result.put(FIELD_STARTS_WITH_ZERO_SCORE, series.isStartsWithZeroScore());
        result.put(FIELD_HAS_SPLIT_FLEET_CONTIGUOUS_SCORING, series.hasSplitFleetContiguousScoring());
        result.put(FIELD_MAXIMUM_NUMBER_OF_DISCARDS, series.getMaximumNumberOfDiscards());
        result.put(FIELD_FIRST_COLUMS_IS_NON_DISCARDABLE_CARRY_FORWARD, series.isFirstColumnIsNonDiscardableCarryForward());

        JSONArray fleetsJson = new JSONArray();
        for (Fleet fleet : series.getFleets()) {
            fleetsJson.add(fleetSerializer.serialize(fleet));
        }
        result.put(FIELD_FLEETS, fleetsJson);

        JSONArray racesJson = new JSONArray();
        for (RaceColumn raceColumn : series.getRaceColumns()) {
            racesJson.add(raceColumn.getName());
        }
        result.put(FIELD_RACES, racesJson);

        JSONObject trackedRacesJson = new JSONObject();
        JSONArray trackedFleetsJson = new JSONArray();
        for (Fleet fleet : series.getFleets()) {
            JSONObject trackedFleetJson = new JSONObject();
            trackedFleetJson.put(FIELD_NAME, fleet.getName());
            JSONArray racesPerFleetJson = new JSONArray();
            for (RaceColumn raceColumn : series.getRaceColumns()) {
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                JSONObject raceColumnJson = new JSONObject();
                raceColumnJson.put(FIELD_NAME, raceColumn.getName());
                raceColumnJson.put("isMedalRace" , raceColumn.isMedalRace());
                if (trackedRace != null) {
                    raceColumnJson.put("isLive", trackedRace.isLive(MillisecondsTimePoint.now()));
                    raceColumnJson.put("isTracked", true);
                    raceColumnJson.put("trackedRaceName", trackedRace.getRace().getName());
                    raceColumnJson.put("hasGpsData", trackedRace.hasGPSData());
                    raceColumnJson.put("hasWindData", trackedRace.hasWindData());
                } else {
                    raceColumnJson.put("isLive", false);
                    raceColumnJson.put("isTracked", false);
                    raceColumnJson.put("trackedRaceName", null);
                    raceColumnJson.put("hasGpsData", false);
                    raceColumnJson.put("hasWindData", false);
                }
                racesPerFleetJson.add(raceColumnJson);
            }
            trackedFleetJson.put(FIELD_RACES, racesPerFleetJson);
            trackedFleetsJson.add(trackedFleetJson);
        }
        trackedRacesJson.put(FIELD_FLEETS, trackedFleetsJson);        
        result.put(FIELD_TRACKED_RACES, trackedRacesJson);        
        
        return result;
    }
}
