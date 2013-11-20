package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.masterdataimport.RaceColumnMasterData;
import com.sap.sailing.domain.masterdataimport.RegattaMasterData;
import com.sap.sailing.domain.masterdataimport.SeriesMasterData;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.LeaderboardMasterDataJsonSerializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.RegattaMasterDataJsonSerializer;

public class RegattaMasterDataJsonDeserializer implements JsonDeserializer<RegattaMasterData> {
    
    private final JsonDeserializer<Fleet> fleetDeserializer;
    private final JsonDeserializer<RaceColumnMasterData> raceColumnDeserializer;

    public RegattaMasterDataJsonDeserializer(JsonDeserializer<Fleet> fleetDeserializer, JsonDeserializer<RaceColumnMasterData> raceColumnDeserializer) {
        this.fleetDeserializer = fleetDeserializer;
        this.raceColumnDeserializer = raceColumnDeserializer;
    }

    @Override
    public RegattaMasterData deserialize(JSONObject object) throws JsonDeserializationException {
        String id = (String) object.get(RegattaMasterDataJsonSerializer.FIELD_ID);
        String regattaName = (String) object.get(RegattaMasterDataJsonSerializer.FIELD_REGATTA_NAME);
        String baseName = (String) object.get(RegattaMasterDataJsonSerializer.FIELD_BASE_NAME);
        String defaultCourseAreaId = (String) object.get(RegattaMasterDataJsonSerializer.FIELD_DEFAULT_COURSE_AREA_ID);
        String boatClassName = (String) object.get(RegattaMasterDataJsonSerializer.FIELD_BOAT_CLASS_NAME);
        String scoringSchemeType = (String) object.get(RegattaMasterDataJsonSerializer.FIELD_SCORING_SCHEME_TYPE);
        boolean isPersistent = (Boolean) object.get(RegattaMasterDataJsonSerializer.FIELD_IS_PERSISTENT);
        Iterable<SeriesMasterData> series = deserializeSeries((JSONArray) object.get(RegattaMasterDataJsonSerializer.FIELD_SERIES));
        Iterable<String> raceIdsAsStrings = deserializeRaceIds((JSONArray) object.get(RegattaMasterDataJsonSerializer.FIELD_REGATTA_RACE_IDS));
        
        RacingProcedureType procedureType = null;
        String defaultRacingProcedureTypeValue = (String) object.get(RegattaMasterDataJsonSerializer.FIELD_DEFAULT_RACING_PROCEDURE_TYPE);
        if (defaultRacingProcedureTypeValue != null) {
            procedureType = RacingProcedureType.valueOf(defaultRacingProcedureTypeValue);
        }
        CourseDesignerMode designerMode = null;
        String defaultCourseDesignerModeValue = (String) object.get(RegattaMasterDataJsonSerializer.FIELD_DEFAULT_COURSE_DESIGNER_MODE);
        if (defaultCourseDesignerModeValue != null) {
            designerMode = CourseDesignerMode.valueOf(defaultCourseDesignerModeValue);
        }
        
        return new RegattaMasterData(id, baseName, defaultCourseAreaId, boatClassName, scoringSchemeType, 
                series, isPersistent, regattaName, raceIdsAsStrings, procedureType, designerMode);
    }

    private Iterable<String> deserializeRaceIds(JSONArray jsonArray) {
        Set<String> raceIdStrings = new HashSet<String>();
        for (Object obj : jsonArray) {
            String id = (String) obj;
            raceIdStrings.add(id);
        }
        return raceIdStrings;
    }

    private Iterable<SeriesMasterData> deserializeSeries(JSONArray jsonArray) throws JsonDeserializationException {
        List<SeriesMasterData> series = new ArrayList<SeriesMasterData>();
        for (Object seriesObject : jsonArray) {
            JSONObject seriesJson = (JSONObject) seriesObject;
            String name = (String) seriesJson.get(RegattaMasterDataJsonSerializer.FIELD_NAME);
            boolean isMedal = (Boolean) seriesJson.get(RegattaMasterDataJsonSerializer.FIELD_IS_MEDAL);
            Iterable<Fleet> fleets = deserializeFleets((JSONArray) seriesJson.get(RegattaMasterDataJsonSerializer.FIELD_FLEETS));
            Iterable<RaceColumnMasterData> raceColumnNames = deserializeRaceColumnNames((JSONArray) seriesJson.get(RegattaMasterDataJsonSerializer.FIELD_RACE_COLUMNS));
            int[] discardingRule = deserializeResultDesicardingRule((JSONObject) seriesJson.get(RegattaMasterDataJsonSerializer.FIELD_RESULT_DISCARDING_RULE));
            series.add(new SeriesMasterData(name, isMedal, fleets, raceColumnNames, discardingRule));
        }
        
        return series;
    }

    private Iterable<RaceColumnMasterData> deserializeRaceColumnNames(JSONArray jsonArray) throws JsonDeserializationException {
        List<RaceColumnMasterData> raceColumns = new ArrayList<RaceColumnMasterData>();
        for (Object raceColumnObject : jsonArray) {
            JSONObject jsonRaceColumn = (JSONObject) raceColumnObject;
            raceColumns.add(raceColumnDeserializer.deserialize(jsonRaceColumn));
        }
        return raceColumns;
    }

    private Iterable<Fleet> deserializeFleets(JSONArray jsonArray) {
        List<Fleet> fleets = new ArrayList<Fleet>();
        for (Object fleetObject : jsonArray) {
            JSONObject fleetJson = (JSONObject) fleetObject;
            try {
                fleets.add(fleetDeserializer.deserialize(fleetJson));
            } catch (JsonDeserializationException e) {
                // TODO exception handling
                e.printStackTrace();
            }
            
        }
        return fleets;
    }
    
    private int[] deserializeResultDesicardingRule(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        JSONArray indeces = (JSONArray) jsonObject.get(LeaderboardMasterDataJsonSerializer.FIELD_INDICES);
        if (indeces == null) {
            return null;
        }
        int[] result = new int[indeces.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = ((Long) indeces.get(i)).intValue();
        }
        return result;
    }

}
