package com.sap.sailing.polars.jaxrs.deserialization;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationSettingsImpl;
import com.sap.sailing.domain.common.impl.WindSpeedSteppingWithMaxDistance;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class PolarSheetGenerationSettingsJsonDeserializer implements JsonDeserializer<PolarSheetGenerationSettings> {

    public static final String FIELD_MAX_DISTANCE = "maxDistance";
    public static final String FIELD_LEVELS = "levels";
    public static final String FIELD_WIND_STEPPING = "windStepping";
    public static final String FIELD_PCT_OF_LEADING_COMPETITORS_TO_INCLUDE = "pctOfLeadingCompetitorsToInclude";
    public static final String FIELD_SPLIT_BY_WIND_GAUGES = "splitByWindGauges";
    public static final String FIELD_USE_ONLY_ESTIMATION_FOR_WIND_DIRECTION = "useOnlyEstimationForWindDirection";
    public static final String FIELD_OUTLIER_MINIMUM_NEIGHBOORHOOD_PCT = "outlierMinimumNeighboorhoodPct";
    public static final String FIELD_OUTLIER_DETECTION_NEIGHBOORHOOD_RADIUS = "outlierDetectionNeighboorhoodRadius";
    public static final String FIELD_SHOULD_REMOVE_OUTLIERS = "shouldRemoveOutliers";
    public static final String FIELD_USE_ONLY_WIND_GAUGES_FOR_WIND_SPEED = "useOnlyWindGaugesForWindSpeed";
    public static final String FIELD_MINIMUM_CONFIDENCE_MEASURE = "minimumConfidenceMeasure";
    public static final String FIELD_NUMBER_OF_HISTOGRAM_COLUMNS = "numberOfHistogramColumns";
    public static final String FIELD_MINIMUM_DATA_COUNT_PER_ANGLE = "minimumDataCountPerAngle";
    public static final String FIELD_MINIMUM_WIND_CONFIDENCE = "minimumWindConfidence";
    public static final String FIELD_MINIMUM_DATA_COUNT_PER_GRAPH = "minimumDataCountPerGraph";

    @Override
    public PolarSheetGenerationSettings deserialize(JSONObject object) throws JsonDeserializationException {
        int minimumDataCountPerGraph = Integer.valueOf(object.get(FIELD_MINIMUM_DATA_COUNT_PER_GRAPH).toString());
        double minimumWindConfidence = (double) object.get(FIELD_MINIMUM_WIND_CONFIDENCE);
        int minimumDataCountPerAngle = Integer.valueOf(object.get(FIELD_MINIMUM_DATA_COUNT_PER_ANGLE).toString());
        int numberOfHistogramColumns = Integer.valueOf(object.get(FIELD_NUMBER_OF_HISTOGRAM_COLUMNS).toString());
        double minimumConfidenceMeasure = (double) object.get(FIELD_MINIMUM_CONFIDENCE_MEASURE);
        boolean useOnlyWindGaugesForWindSpeed = (boolean) object.get(FIELD_USE_ONLY_WIND_GAUGES_FOR_WIND_SPEED);
        boolean shouldRemoveOutliers = (boolean) object.get(FIELD_SHOULD_REMOVE_OUTLIERS);
        double outlierDetectionNeighboorhoodRadius = (double) object.get(FIELD_OUTLIER_DETECTION_NEIGHBOORHOOD_RADIUS);
        double outlierMinimumNeighboorhoodPct = (double) object.get(FIELD_OUTLIER_MINIMUM_NEIGHBOORHOOD_PCT);
        boolean useOnlyEstimationForWindDirection = (boolean) object.get(FIELD_USE_ONLY_ESTIMATION_FOR_WIND_DIRECTION);
        boolean splitByWindGauges = (boolean) object.get(FIELD_SPLIT_BY_WIND_GAUGES);
        double pctOfLeadingCompetitorsToInclude = (double) object.get(FIELD_PCT_OF_LEADING_COMPETITORS_TO_INCLUDE);

        JSONObject windSteppingJSON = (JSONObject) object.get(FIELD_WIND_STEPPING);
        JSONArray levelsJSON = (JSONArray) windSteppingJSON.get(FIELD_LEVELS);
        double[] levels = new double[levelsJSON.size()];
        for (int i = 0; i < levelsJSON.size(); i++) {
            levels[i] = (double) levelsJSON.get(i);
        }
        WindSpeedSteppingWithMaxDistance windStepping = new WindSpeedSteppingWithMaxDistance(levels,
                (double) windSteppingJSON.get(FIELD_MAX_DISTANCE));

        return new PolarSheetGenerationSettingsImpl(minimumDataCountPerGraph, minimumWindConfidence,
                minimumDataCountPerAngle, numberOfHistogramColumns, minimumConfidenceMeasure,
                useOnlyWindGaugesForWindSpeed, shouldRemoveOutliers, outlierDetectionNeighboorhoodRadius,
                outlierMinimumNeighboorhoodPct, useOnlyEstimationForWindDirection, windStepping, splitByWindGauges,
                pctOfLeadingCompetitorsToInclude);
    }

}
