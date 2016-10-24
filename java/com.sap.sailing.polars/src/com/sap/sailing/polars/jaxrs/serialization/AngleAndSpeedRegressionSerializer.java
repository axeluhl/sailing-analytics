package com.sap.sailing.polars.jaxrs.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.polars.mining.AngleAndSpeedRegression;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class AngleAndSpeedRegressionSerializer implements JsonSerializer<AngleAndSpeedRegression> {

    public static final String FIELD_ANGLE_REGRESSION = "angleRegression";
    public static final String FIELD_SPEED_REGRESSION = "speedRegression";
    public static final String FIELD_MAX_WIND_SPEED_IN_KNOTS = "maxWindSpeedInKnots";

    @Override
    public JSONObject serialize(AngleAndSpeedRegression object) {
        JSONObject regressionJSON = new JSONObject();

        regressionJSON.put(FIELD_MAX_WIND_SPEED_IN_KNOTS, object.getMaxWindSpeedInKnots());
        regressionJSON.put(FIELD_SPEED_REGRESSION,
                new IncrementalAnyOrderLeastSquaresImplSerializer().serialize(object.getSpeedRegression()));
        regressionJSON.put(FIELD_ANGLE_REGRESSION,
                new IncrementalAnyOrderLeastSquaresImplSerializer().serialize(object.getAngleRegression()));

        return regressionJSON;
    }

}
