package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.AbstractRegressorModel;
import com.sap.sailing.windestimation.model.regressor.SingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.store.PersistenceSupport;
import com.sap.sailing.windestimation.model.store.SerializationBasedPersistenceSupport;

public class DistanceAndDurationPolynomialSumBasedTwdTransitionRegressor
        extends AbstractRegressorModel<TwdTransition, TwdTransitionRegressorModelMetadata> {

    private static final long serialVersionUID = 76683324L;
    private final SingleDimensionPolynomialRegressor<TwdTransition, DistanceBasedTwdTransitionRegressorModelMetadata> distanceBasedTwdTransitionRegressor;
    private final SingleDimensionPolynomialRegressor<TwdTransition, DurationBasedTwdTransitionRegressorModelMetadata> durationBasedTwdTransitionRegressor;

    public DistanceAndDurationPolynomialSumBasedTwdTransitionRegressor(
            SingleDimensionPolynomialRegressor<TwdTransition, DistanceBasedTwdTransitionRegressorModelMetadata> distanceBasedTwdTransitionRegressor,
            SingleDimensionPolynomialRegressor<TwdTransition, DurationBasedTwdTransitionRegressorModelMetadata> durationBasedTwdTransitionRegressor) {
        super(new TwdTransitionRegressorModelMetadata());
        this.distanceBasedTwdTransitionRegressor = distanceBasedTwdTransitionRegressor;
        this.durationBasedTwdTransitionRegressor = durationBasedTwdTransitionRegressor;
    }

    @Override
    public PersistenceSupport getPersistenceSupport() {
        return new SerializationBasedPersistenceSupport(this);
    }

    @Override
    public double getValue(double[] x) {
        double distanceValue = getContextSpecificModelMetadata().getDistanceValue(x);
        double durationValue = getContextSpecificModelMetadata().getDurationValue(x);
        double distanceBasedTwdTransition = distanceBasedTwdTransitionRegressor.getValue(distanceValue);
        double durationBasedTwdTransition = durationBasedTwdTransitionRegressor.getValue(durationValue);
        double valuesSum = distanceBasedTwdTransition + durationBasedTwdTransition;
        return valuesSum;
    }

    public SingleDimensionPolynomialRegressor<TwdTransition, DistanceBasedTwdTransitionRegressorModelMetadata> getDistanceBasedTwdTransitionRegressor() {
        return distanceBasedTwdTransitionRegressor;
    }

    public SingleDimensionPolynomialRegressor<TwdTransition, DurationBasedTwdTransitionRegressorModelMetadata> getDurationBasedTwdTransitionRegressor() {
        return durationBasedTwdTransitionRegressor;
    }

    @Override
    public boolean isModelReady() {
        return distanceBasedTwdTransitionRegressor.isModelReady() && durationBasedTwdTransitionRegressor.isModelReady();
    }

    @Override
    public void train(double[][] x, double[] y) {
        throw new UnsupportedOperationException(
                "Distance and duration dimensions must be trained separately. See getDistanceBasedTwdTransitionRegressor() and getDurationBasedTwdTransitionRegressor()");
    }

}
