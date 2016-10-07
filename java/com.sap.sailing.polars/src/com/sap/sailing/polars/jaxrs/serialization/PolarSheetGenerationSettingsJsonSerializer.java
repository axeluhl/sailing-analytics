package com.sap.sailing.polars.jaxrs.serialization;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.impl.WindSpeedSteppingWithMaxDistance;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class PolarSheetGenerationSettingsJsonSerializer implements JsonSerializer<PolarSheetGenerationSettings> {

    public JSONObject serialize(PolarSheetGenerationSettings settings) {
        JSONObject jsonSettings = new JSONObject();

        jsonSettings.put("minimumDataCountPerGraph", settings.getMinimumDataCountPerGraph());
        jsonSettings.put("minimumWindConfidence", settings.getMinimumWindConfidence());
        jsonSettings.put("minimumDataCountPerAngle", settings.getMinimumDataCountPerAngle());
        jsonSettings.put("numberOfHistogramColumns", settings.getNumberOfHistogramColumns());
        jsonSettings.put("minimumConfidenceMeasure", settings.getMinimumConfidenceMeasure());
        jsonSettings.put("useOnlyWindGaugesForWindSpeed", settings.useOnlyWindGaugesForWindSpeed());
        jsonSettings.put("shouldRemoveOutliers", settings.shouldRemoveOutliers());
        jsonSettings.put("outlierDetectionNeighboorhoodRadius", settings.getOutlierDetectionNeighborhoodRadius());
        jsonSettings.put("outlierMinimumNeighboorhoodPct", settings.getOutlierMinimumNeighborhoodPct());
        jsonSettings.put("useOnlyEstimationForWindDirection", settings.useOnlyEstimatedForWindDirection());

        WindSpeedSteppingWithMaxDistance windStepping = settings.getWindSpeedStepping();
        JSONObject jsonWindStepping = new JSONObject();
        JSONArray jsonLevelsArray = new JSONArray();
        for (double level : windStepping.getRawStepping()) {
            jsonLevelsArray.add(level);
        }
        jsonWindStepping.put("levels", jsonLevelsArray);
        jsonWindStepping.put("maxDistance", windStepping.getMaxDistance());
        jsonSettings.put("windStepping", jsonWindStepping);

        jsonSettings.put("splitByWindGauges", settings.splitByWindgauges());
        jsonSettings.put("pctOfLeadingCompetitorsToInclude", settings.getPctOfLeadingCompetitorsToInclude());

        return jsonSettings;
    }

}
