package com.sap.sailing.polars.jaxrs.deserialization;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.polars.jaxrs.serialization.IncrementalAnyOrderLeastSquaresImplSerializer;
import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class IncrementalAnyOrderLeastSquaresImplDeserializer implements JsonDeserializer<IncrementalAnyOrderLeastSquaresImpl> {

    @Override
    public IncrementalAnyOrderLeastSquaresImpl deserialize(JSONObject object) throws JsonDeserializationException {
        JSONArray matrixOfXSumsJSON = (JSONArray) object.get(IncrementalAnyOrderLeastSquaresImplSerializer.FIELD_MATRIX_OF_X_SUMS);
        double matrixOfXSums[][] = new double[matrixOfXSumsJSON.size()][];
        for (int i = 0; i < matrixOfXSumsJSON.size(); i++) {
            JSONArray rowOfXSumsJSON = (JSONArray) matrixOfXSumsJSON.get(i);
            matrixOfXSums[i] = new double[rowOfXSumsJSON.size()];
            
            for (int j = 0; j < rowOfXSumsJSON.size(); j++) {
                matrixOfXSums[i][j] = (double) rowOfXSumsJSON.get(j);
            }
        }
        
        JSONArray vectorOfXYMultSumsJSON = (JSONArray) object.get(IncrementalAnyOrderLeastSquaresImplSerializer.FIELD_VECTOR_OF_XY_MULT_SUMS);
        double vectorOfXYMultSums[] = new double[vectorOfXYMultSumsJSON.size()];
        for (int i = 0; i < vectorOfXYMultSumsJSON.size(); i++) {
            vectorOfXYMultSums[i] = (double) vectorOfXYMultSumsJSON.get(i);
        }
        
        int polynomialOrder = Integer.valueOf(object.get(IncrementalAnyOrderLeastSquaresImplSerializer.FIELD_POLYNOMIAL_ORDER).toString());
        boolean hasIntercept = (boolean) object.get(IncrementalAnyOrderLeastSquaresImplSerializer.FIELD_HAS_INTERCEPT);
        boolean useSymbollicInversionIfPossible = (boolean) object.get(IncrementalAnyOrderLeastSquaresImplSerializer.FIELD_USE_SYMBOLLIC_INVERSION_IF_POSSIBLE);
        long numberOfPointsAdded = (long) object.get(IncrementalAnyOrderLeastSquaresImplSerializer.FIELD_NUMBER_OF_POINTS_ADDED);
        
        return new IncrementalAnyOrderLeastSquaresImpl(matrixOfXSums, vectorOfXYMultSums, polynomialOrder, hasIntercept, useSymbollicInversionIfPossible, numberOfPointsAdded);
    }

}
