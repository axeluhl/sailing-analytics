package com.sap.sailing.windestimation.model;

import com.sap.sailing.windestimation.data.persistence.maneuver.PersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionStdRegressorTrainer;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DurationBasedTwdTransitionStdRegressorTrainer;
import com.sap.sailing.windestimation.model.store.FileSystemModelStoreImpl;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStoreImpl;
import com.sap.sailing.windestimation.util.LoggingUtil;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SimpleModelsTrainingPart2 {

    public static void main(String[] args) throws Exception {
        final ModelStore modelStore;
        final PersistenceManager<?> dbProvider = new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(AggregatedSingleDimensionType.DURATION);
        if (args.length > 2) {
            modelStore = new FileSystemModelStoreImpl(args[2]);
        } else {
            modelStore = new MongoDbModelStoreImpl(dbProvider.getDb());
        }
        DurationBasedTwdTransitionStdRegressorTrainer.train(modelStore);
        DistanceBasedTwdTransitionStdRegressorTrainer.train(modelStore);
        Thread.sleep(1000);
        ExportedModelsGenerator.main(new String[0]);
        LoggingUtil.logInfo("Model training finished. You can upload the generated file to a server instance.");
    }

}
