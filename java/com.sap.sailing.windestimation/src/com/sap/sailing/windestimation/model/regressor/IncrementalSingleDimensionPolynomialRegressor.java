package com.sap.sailing.windestimation.model.regressor;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.regression.IncrementalLeastSquares;
import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.exception.ModelOperationException;

/**
 * Regression model which supports regressions of arbitrary polynomial order in an incremental manner. It uses
 * {@link IncrementalLeastSquares} under the hood.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of input instances for this model. The purpose of the input instance is to supply the model with
 *            feature vector x, so that the model can generate prediction y.
 * @param <MC>
 *            The type of model context associated with this model.
 */
public class IncrementalSingleDimensionPolynomialRegressor<InstanceType, MC extends ModelContext<InstanceType>> extends
        AbstractRegressorModel<InstanceType, MC> implements IncrementallyTrainableRegressorModel<InstanceType, MC> {

    private static final long serialVersionUID = 2275631213670766824L;
    private final IncrementalLeastSquares regression;

    public IncrementalSingleDimensionPolynomialRegressor(MC modelContext, int polynomialOrder, boolean withBias) {
        super(modelContext);
        if (modelContext.getNumberOfInputFeatures() > 1) {
            throw new IllegalArgumentException("Only modelContext.getNumberOfInputFeatures() == 1 is supported");
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
            throw new ModelOperationException(e);
        }
        double value = polynomialFunction.value(x[0]);
        return value;
    }

    public double getValue(double x) {
        PolynomialFunction polynomialFunction;
        try {
            polynomialFunction = regression.getOrCreatePolynomialFunction();
        } catch (NotEnoughDataHasBeenAddedException e) {
            throw new ModelOperationException(e);
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

    /**
     * Gets a user friendly string representation of the regression polynomial learned from the training data.
     */
    public String getPolynomAsString() {
        PolynomialFunction polynomialFunction;
        try {
            polynomialFunction = regression.getOrCreatePolynomialFunction();
            return polynomialFunction.toString();
        } catch (NotEnoughDataHasBeenAddedException e) {
            throw new ModelOperationException(e);
        }
    }

}
