package com.sap.sailing.windestimation.model.regressor;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.regression.IncrementalLeastSquares;
import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;

public class PolynomialRegression<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>> extends
        AbstractRegressionModel<InstanceType, T> implements IncrementallyTrainableRegressorModel<InstanceType, T> {

    private static final long serialVersionUID = 2275631213670766824L;
    private final IncrementalLeastSquares[] regressions;

    public PolynomialRegression(T contextSpecificModelMetadata, int[] polynomialOrder, boolean[] withBias) {
        super(contextSpecificModelMetadata);
        int numberOfInputFeatures = getContextSpecificModelMetadata().getNumberOfInputFeatures();
        this.regressions = new IncrementalAnyOrderLeastSquaresImpl[numberOfInputFeatures];
        for (int i = 0; i < regressions.length; i++) {
            regressions[i] = new IncrementalAnyOrderLeastSquaresImpl(polynomialOrder[i], withBias[i]);
        }
    }

    @Override
    public void train(double[] x, double y) {
        for (int i = 0; i < regressions.length; i++) {
            IncrementalLeastSquares regression = regressions[i];
            double input = x[i];
            regression.addData(input, y);
        }
    }

    @Override
    public double getValue(double[] x) {
        double valueSum = 0;
        for (int i = 0; i < x.length; i++) {
            double input = x[i];
            PolynomialFunction polynomialFunction;
            try {
                polynomialFunction = regressions[i].getOrCreatePolynomialFunction();
            } catch (NotEnoughDataHasBeenAddedException e) {
                throw new RuntimeException(e);
            }
            double value = polynomialFunction.value(input);
            valueSum += value;
        }
        return valueSum;
    }

    @Override
    public boolean isModelReady() {
        if (super.isModelReady()) {
            for (int i = 0; i < regressions.length; i++) {
                try {
                    regressions[i].getOrCreatePolynomialFunction();
                } catch (NotEnoughDataHasBeenAddedException e) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
