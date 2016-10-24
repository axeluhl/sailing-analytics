package com.sap.sailing.polars.jaxrs.serialization;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class IncrementalAnyOrderLeastSquaresImplSerializer implements JsonSerializer<IncrementalAnyOrderLeastSquaresImpl> {

    public static final String FIELD_USE_SYMBOLLIC_INVERSION_IF_POSSIBLE = "useSymbollicInversionIfPossible";
    public static final String FIELD_HAS_INTERCEPT = "hasIntercept";
    public static final String FIELD_POLYNOMIAL_ORDER = "polynomialOrder";
    public static final String FIELD_VECTOR_OF_XY_MULT_SUMS = "vectorOfXYMultSums";
    public static final String FIELD_MATRIX_OF_X_SUMS = "matrixOfXSums";

    @Override
    public JSONObject serialize(IncrementalAnyOrderLeastSquaresImpl object) {
        JSONObject speedRegressionJSON = new JSONObject();
        
        JSONArray matrixOfXSumsJSON = new JSONArray();
        double matrixOfXSums[][] = object.getMatrixOfXSums();
        for (int i = 0; i < matrixOfXSums.length; i++) {
            double rowOfXSum[] = matrixOfXSums[i];
            JSONArray rowOfXSumJSON = new JSONArray();
            for (int j = 0; j < rowOfXSum.length; j++) {
                rowOfXSumJSON.add(rowOfXSum[j]);
            }
            matrixOfXSumsJSON.add(rowOfXSumJSON);
        }
        speedRegressionJSON.put(FIELD_MATRIX_OF_X_SUMS, matrixOfXSumsJSON);
        
        JSONArray vectorOfXYMultSumsJSON = new JSONArray();
        double vectorOfXYMultSums[] = object.getVectorOfXYMultSums();
        for (int i = 0; i < vectorOfXYMultSums.length; i++) {
            vectorOfXYMultSumsJSON.add(vectorOfXYMultSums[i]);
        }
        speedRegressionJSON.put(FIELD_VECTOR_OF_XY_MULT_SUMS, vectorOfXYMultSumsJSON);
        speedRegressionJSON.put(FIELD_POLYNOMIAL_ORDER, object.getPolynomialOrder());
        speedRegressionJSON.put(FIELD_HAS_INTERCEPT, object.isHasIntercept());
        speedRegressionJSON.put(FIELD_USE_SYMBOLLIC_INVERSION_IF_POSSIBLE, object.isUseSymbollicInversionIfPossible());
        
        return speedRegressionJSON;
    }

}
