package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.SingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager.SingleDimensionType;
import com.sap.sailing.windestimation.model.regressor.SingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStore;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class TwdTransitionRegressorTrainer {

    private final SingleDimensionBasedTwdTransitionPersistenceManager persistenceManager;
    private final ModelStore regressorModelStore;

    public TwdTransitionRegressorTrainer(SingleDimensionBasedTwdTransitionPersistenceManager persistenceManager,
            ModelStore regressorModelStore) {
        this.persistenceManager = persistenceManager;
        this.regressorModelStore = regressorModelStore;
    }

    public void trainRegressor(
            SingleDimensionPolynomialRegressor<TwdTransition, ? extends SingleDimensionBasedTwdTransitionRegressorModelMetadata> model)
            throws Exception {
        PersistedElementsIterator<SingleDimensionBasedTwdTransition> iterator = persistenceManager.getIterator();
        double[] x = new double[1];
        LoggingUtil.logInfo("Training of " + model.getClass() + " started...");
        while (iterator.hasNext()) {
            SingleDimensionBasedTwdTransition twdTransition = iterator.next();
            x[0] = twdTransition.getDimensionValue();
            model.train(x, twdTransition.getTwdChangeInDegrees());
        }
        LoggingUtil.logInfo("Persisting trained regressor...");
        regressorModelStore.persistState(model);
        LoggingUtil.logInfo("Classifier persisted successfully. Finished!");
    }

    public static void main(String[] args) throws Exception {
        SingleDimensionBasedTwdTransitionPersistenceManager distanceBasedPersistenceManager = new SingleDimensionBasedTwdTransitionPersistenceManager(
                SingleDimensionType.DISTANCE);
        SingleDimensionBasedTwdTransitionPersistenceManager durationBasedPersistenceManager = new SingleDimensionBasedTwdTransitionPersistenceManager(
                SingleDimensionType.DURATION);
        MongoDbModelStore mongoDbModelStore = new MongoDbModelStore(distanceBasedPersistenceManager.getDb());
        TwdTransitionRegressorModelFactory modelFactory = new TwdTransitionRegressorModelFactory();
        TwdTransitionRegressorTrainer trainer = new TwdTransitionRegressorTrainer(distanceBasedPersistenceManager,
                mongoDbModelStore);
        trainer.trainRegressor(modelFactory.getDistanceBasedRegressorFactory().getNewModel());
        trainer = new TwdTransitionRegressorTrainer(durationBasedPersistenceManager, mongoDbModelStore);
        trainer.trainRegressor(modelFactory.getDurationBasedRegressorFactory().getNewModel());
    }

}
