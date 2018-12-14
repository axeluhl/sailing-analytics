package com.sap.sailing.windestimation.regression;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.regression.IncrementalLeastSquares;
import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;

public class PolynomialRegression<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends AbstractRegressionModel<InstanceType, T> {

    private static final long serialVersionUID = 2275631213670766824L;
    private PolynomialFunction[] polynomialFunctions;
    private final int[] polynomialOrder;
    private final boolean[] withBias;

    public PolynomialRegression(T contextSpecificModelMetadata, int[] polynomialOrder, boolean[] withBias) {
        super(contextSpecificModelMetadata);
        this.polynomialOrder = polynomialOrder;
        this.withBias = withBias;
    }

    @Override
    public void train(double[][] x, double[] y) {
        int numberOfInputFeatures = getContextSpecificModelMetadata().getNumberOfInputFeatures();
        IncrementalLeastSquares[] regressions = new IncrementalAnyOrderLeastSquaresImpl[numberOfInputFeatures];
        for (int i = 0; i < regressions.length; i++) {
            regressions[i] = new IncrementalAnyOrderLeastSquaresImpl(polynomialOrder[i], withBias[i]);
        }
        for (int i = 0; i < y.length; i++) {
            double target = y[i];
            double[] inputs = x[i];
            for (int j = 0; j < regressions.length; j++) {
                double input = inputs[j];
                IncrementalLeastSquares regression = regressions[j];
                regression.addData(input, target);
            }
        }
        for (int i = 0; i < regressions.length; i++) {
            IncrementalLeastSquares regression = regressions[i];
            try {
                PolynomialFunction polynomialFunction = regression.getOrCreatePolynomialFunction();
                polynomialFunctions[i] = polynomialFunction;
            } catch (NotEnoughDataHasBeenAddedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public double getValue(double[] x) {
        double valueSum = 0;
        for (int i = 0; i < x.length; i++) {
            double input = x[i];
            PolynomialFunction polynomialFunction = polynomialFunctions[i];
            double value = polynomialFunction.value(input);
            valueSum += value;
        }
        return valueSum;
    }

}
