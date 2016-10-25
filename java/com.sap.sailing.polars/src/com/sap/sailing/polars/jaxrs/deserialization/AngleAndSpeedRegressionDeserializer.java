package com.sap.sailing.polars.jaxrs.deserialization;

import org.json.simple.JSONObject;

import com.sap.sailing.polars.jaxrs.serialization.AngleAndSpeedRegressionSerializer;
import com.sap.sailing.polars.mining.AngleAndSpeedRegression;
import com.sap.sailing.polars.regression.IncrementalLeastSquares;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class AngleAndSpeedRegressionDeserializer implements JsonDeserializer<AngleAndSpeedRegression> {

    private IncrementalAnyOrderLeastSquaresImplDeserializer regressionDeserializer = new IncrementalAnyOrderLeastSquaresImplDeserializer();

    @Override
    public AngleAndSpeedRegression deserialize(JSONObject object) throws JsonDeserializationException {
        double maxWindSpeedInKnots = (double) object
                .get(AngleAndSpeedRegressionSerializer.FIELD_MAX_WIND_SPEED_IN_KNOTS);
        
        IncrementalLeastSquares speedRegression = regressionDeserializer
                .deserialize((JSONObject) object.get(AngleAndSpeedRegressionSerializer.FIELD_SPEED_REGRESSION));
        
        IncrementalLeastSquares angleRegression = regressionDeserializer
                .deserialize((JSONObject) object.get(AngleAndSpeedRegressionSerializer.FIELD_ANGLE_REGRESSION));
        
        return new AngleAndSpeedRegression(maxWindSpeedInKnots, speedRegression, angleRegression);
    }

}
