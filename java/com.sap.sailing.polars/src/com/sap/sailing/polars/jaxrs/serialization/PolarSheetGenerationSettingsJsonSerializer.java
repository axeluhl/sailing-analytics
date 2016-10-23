package com.sap.sailing.polars.jaxrs.serialization;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.impl.WindSpeedSteppingWithMaxDistance;
import com.sap.sailing.polars.jaxrs.deserialization.PolarSheetGenerationSettingsJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class PolarSheetGenerationSettingsJsonSerializer implements JsonSerializer<PolarSheetGenerationSettings> {

    public JSONObject serialize(PolarSheetGenerationSettings settings) {
        JSONObject jsonSettings = new JSONObject();

        jsonSettings.put(PolarSheetGenerationSettingsJsonDeserializer.FIELD_MINIMUM_DATA_COUNT_PER_GRAPH,
                settings.getMinimumDataCountPerGraph());
        jsonSettings.put(PolarSheetGenerationSettingsJsonDeserializer.FIELD_MINIMUM_WIND_CONFIDENCE,
                settings.getMinimumWindConfidence());
        jsonSettings.put(PolarSheetGenerationSettingsJsonDeserializer.FIELD_MINIMUM_DATA_COUNT_PER_ANGLE,
                settings.getMinimumDataCountPerAngle());
        jsonSettings.put(PolarSheetGenerationSettingsJsonDeserializer.FIELD_NUMBER_OF_HISTOGRAM_COLUMNS,
                settings.getNumberOfHistogramColumns());
        jsonSettings.put(PolarSheetGenerationSettingsJsonDeserializer.FIELD_MINIMUM_CONFIDENCE_MEASURE,
                settings.getMinimumConfidenceMeasure());
        jsonSettings.put(PolarSheetGenerationSettingsJsonDeserializer.FIELD_USE_ONLY_WIND_GAUGES_FOR_WIND_SPEED,
                settings.useOnlyWindGaugesForWindSpeed());
        jsonSettings.put(PolarSheetGenerationSettingsJsonDeserializer.FIELD_SHOULD_REMOVE_OUTLIERS,
                settings.shouldRemoveOutliers());
        jsonSettings.put(PolarSheetGenerationSettingsJsonDeserializer.FIELD_OUTLIER_DETECTION_NEIGHBOORHOOD_RADIUS,
                settings.getOutlierDetectionNeighborhoodRadius());
        jsonSettings.put(PolarSheetGenerationSettingsJsonDeserializer.FIELD_OUTLIER_MINIMUM_NEIGHBOORHOOD_PCT,
                settings.getOutlierMinimumNeighborhoodPct());
        jsonSettings.put(PolarSheetGenerationSettingsJsonDeserializer.FIELD_USE_ONLY_ESTIMATION_FOR_WIND_DIRECTION,
                settings.useOnlyEstimatedForWindDirection());

        WindSpeedSteppingWithMaxDistance windStepping = settings.getWindSpeedStepping();
        JSONObject jsonWindStepping = new JSONObject();
        JSONArray jsonLevelsArray = new JSONArray();
        for (double level : windStepping.getRawStepping()) {
            jsonLevelsArray.add(level);
        }
        jsonWindStepping.put(PolarSheetGenerationSettingsJsonDeserializer.FIELD_LEVELS, jsonLevelsArray);
        jsonWindStepping.put(PolarSheetGenerationSettingsJsonDeserializer.FIELD_MAX_DISTANCE,
                windStepping.getMaxDistance());
        jsonSettings.put(PolarSheetGenerationSettingsJsonDeserializer.FIELD_WIND_STEPPING, jsonWindStepping);

        jsonSettings.put(PolarSheetGenerationSettingsJsonDeserializer.FIELD_SPLIT_BY_WIND_GAUGES,
                settings.splitByWindgauges());
        jsonSettings.put(PolarSheetGenerationSettingsJsonDeserializer.FIELD_PCT_OF_LEADING_COMPETITORS_TO_INCLUDE,
                settings.getPctOfLeadingCompetitorsToInclude());

        return jsonSettings;
    }

}
