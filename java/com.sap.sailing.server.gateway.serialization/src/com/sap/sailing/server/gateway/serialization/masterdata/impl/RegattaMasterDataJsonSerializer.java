package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RegattaMasterDataJsonSerializer implements JsonSerializer<Regatta> {

    public static final String FIELD_ID = "id";
    public static final String FIELD_ID_TYPE = "idType";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_BASE_NAME = "baseName";
    public static final String FIELD_REGATTAS = "regattas";
    public static final String FIELD_BOAT_CLASS_NAME = "boatClass";
    public static final String FIELD_DEFAULT_COURSE_AREA_ID = "defaultCourseAreaId";
    public static final String FIELD_SCORING_SCHEME_TYPE = "scoringSchemeType";
    public static final String FIELD_SERIES = "series";
    public static final String FIELD_RACE_COLUMNS = "raceColumns";
    public static final String FIELD_IS_MEDAL = "isMedal";
    public static final String FIELD_FLEETS = "fleets";
    public static final String FIELD_IS_PERSISTENT = "isPersistent";
    public static final String FIELD_REGATTA_NAME = "regattaName";
    public static final String FIELD_RESULT_DISCARDING_RULE = "resultDiscardingRule";
    public static final String FIELD_INDICES = "indices";
    public static final String FIELD_REGATTA_RACE_IDS = "regattaRaceIds";

    private final JsonSerializer<Fleet> fleetSerializer;
    private final JsonSerializer<RaceColumn> raceColumnSerializer;
    private final ConcurrentHashMap<String, Regatta> regattaForRaceIdStrings;

    public RegattaMasterDataJsonSerializer(JsonSerializer<Fleet> fleetSerializer,
            JsonSerializer<RaceColumn> raceColumnSerializer, ConcurrentHashMap<String, Regatta> regattaForRaceIdStrings) {
        this.fleetSerializer = fleetSerializer;
        this.raceColumnSerializer = raceColumnSerializer;
        this.regattaForRaceIdStrings = regattaForRaceIdStrings;
    }

    @Override
    public JSONObject serialize(Regatta regatta) {
        return createJsonForRegatta(regatta);
    }

    private JSONObject createJsonForRegatta(Regatta regatta) {
        JSONObject result = new JSONObject();
        // Special treatment for UUIDs. They are represented as String because JSON doesn't have a way to represent them
        // otherwise. However, other, e.g., numeric, types used to encode a serializable ID must be preserved according
        // to JSON semantics.
        // Also see the corresponding case distinction in the deserialized which first tries to parse a string as a UUID
        // becore returning the ID as is.
        result.put(FIELD_ID_TYPE, regatta.getId().getClass().getName());
        Serializable regattaId = regatta.getId() instanceof UUID ? regatta.getId().toString() : regatta.getId();
        result.put(FIELD_ID, regattaId);
        result.put(FIELD_BASE_NAME, regatta.getBaseName());
        result.put(FIELD_BOAT_CLASS_NAME, regatta.getBoatClass().getName());
        CourseArea defaultCourseArea = regatta.getDefaultCourseArea();
        if (defaultCourseArea != null) {
            result.put(FIELD_DEFAULT_COURSE_AREA_ID, defaultCourseArea.getId().toString());
        } else {
            result.put(FIELD_DEFAULT_COURSE_AREA_ID, null);
        }
        result.put(FIELD_SCORING_SCHEME_TYPE, regatta.getScoringScheme().getType().toString());
        result.put(FIELD_SERIES, createJsonArrayForSeries(regatta.getSeries()));
        result.put(FIELD_IS_PERSISTENT, regatta.isPersistent());
        result.put(FIELD_REGATTA_NAME, ((RegattaName) regatta.getRegattaIdentifier()).getRegattaName());
        result.put(FIELD_REGATTA_RACE_IDS, createJsonArrayForRaceIdStrings(regatta));
        return result;
    }

    private JSONArray createJsonArrayForRaceIdStrings(Regatta regatta) {
        JSONArray result = new JSONArray();
        for (Entry<String, Regatta> entry: regattaForRaceIdStrings.entrySet()) {
            if (entry.getValue() == regatta) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private JSONArray createJsonArrayForSeries(Iterable<? extends Series> series) {
        JSONArray array = new JSONArray();
        for (Series oneSeries : series) {
            array.add(createJsonForSeries(oneSeries));
        }
        return array;
    }

    private JSONObject createJsonForSeries(Series series) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, series.getName());
        result.put(FIELD_IS_MEDAL, series.isMedal());
        result.put(FIELD_FLEETS, createJsonArrayForFleets(series.getFleets()));
        result.put(FIELD_RACE_COLUMNS, createJsonArrayForRaceColumns(series.getRaceColumns()));
        result.put(FIELD_RESULT_DISCARDING_RULE, createJsonForResultDiscardingRule(series.getResultDiscardingRule()));
        return result;
    }

    private JSONObject createJsonForResultDiscardingRule(ResultDiscardingRule resultDiscardingRule) {
        if (resultDiscardingRule == null) {
            return null;
        }
        JSONObject result = new JSONObject();
        if (resultDiscardingRule instanceof ThresholdBasedResultDiscardingRule) {
            ThresholdBasedResultDiscardingRule rule = (ThresholdBasedResultDiscardingRule) resultDiscardingRule;
            JSONArray indices = new JSONArray();
            int[] rawValues = rule.getDiscardIndexResultsStartingWithHowManyRaces();
            for (int i = 0; i < rawValues.length; i++) {
                indices.add(new Integer(rawValues[i]));
            }
            result.put(FIELD_INDICES, indices);
        }
        return result;
    }

    private JSONArray createJsonArrayForFleets(Iterable<? extends Fleet> fleets) {
        JSONArray array = new JSONArray();
        for (Fleet fleet : fleets) {
            array.add(fleetSerializer.serialize(fleet));
        }
        return array;
    }

    private JSONArray createJsonArrayForRaceColumns(Iterable<? extends RaceColumnInSeries> raceColumns) {
        JSONArray array = new JSONArray();
        for (RaceColumnInSeries raceColumn : raceColumns) {
            array.add(createJsonForRaceColumn(raceColumn));
        }
        return array;
    }

    private JSONObject createJsonForRaceColumn(RaceColumnInSeries raceColumn) {
        return raceColumnSerializer.serialize(raceColumn);
    }

}
