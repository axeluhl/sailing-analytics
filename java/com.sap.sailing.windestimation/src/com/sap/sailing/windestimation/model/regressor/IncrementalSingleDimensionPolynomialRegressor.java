package com.sap.sailing.windestimation.model.regressor;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.regression.IncrementalLeastSquares;
import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;

public class IncrementalSingleDimensionPolynomialRegressor<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends AbstractRegressorModel<InstanceType, T>
        implements IncrementallyTrainableRegressorModel<InstanceType, T> {

    private static final long serialVersionUID = 2275631213670766824L;
    private final IncrementalLeastSquares regression;

    public IncrementalSingleDimensionPolynomialRegressor(T contextSpecificModelMetadata, int polynomialOrder,
            boolean withBias) {
        super(contextSpecificModelMetadata);
        if (contextSpecificModelMetadata.getNumberOfInputFeatures() > 1) {
            throw new IllegalArgumentException(
                    "Only contextSpecificModelMetadata.getNumberOfInputFeatures() == 1 is supported");
        }
        this.regression = new IncrementalAnyOrderLeastSquaresImpl(polynomialOrder, withBias);
    }

    @Override
    public void train(double[] x, double y) {
        regression.addData(x[0], y);
    }

    @Override
    public double getValue(double[] x) {
        PolynomialFunction polynomialFunction;
        try {
            polynomialFunction = regression.getOrCreatePolynomialFunction();
        } catch (NotEnoughDataHasBeenAddedException e) {
            throw new RuntimeException(e);
        }
        double value = polynomialFunction.value(x[0]);
        return value;
    }

    public double getValue(double x) {
        PolynomialFunction polynomialFunction;
        try {
            polynomialFunction = regression.getOrCreatePolynomialFunction();
        } catch (NotEnoughDataHasBeenAddedException e) {
            throw new RuntimeException(e);
        }
        double value = polynomialFunction.value(x);
        return value;
    }

    @Override
    public boolean isModelReady() {
        if (super.isModelReady()) {
            try {
                regression.getOrCreatePolynomialFunction();
            } catch (NotEnoughDataHasBeenAddedException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    public String getPolynomAsString() {
        PolynomialFunction polynomialFunction;
        try {
            polynomialFunction = regression.getOrCreatePolynomialFunction();
            return polynomialFunction.toString();
        } catch (NotEnoughDataHasBeenAddedException e) {
            throw new RuntimeException(e);
        }
    }

}
