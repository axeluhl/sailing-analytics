package com.sap.sailing.windestimation.model;

import com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionStdRegressorTrainer;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DurationBasedTwdTransitionStdRegressorTrainer;
import com.sap.sailing.windestimation.util.LoggingUtil;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SimpleModelsTrainingPart2 {

    public static void main(String[] args) throws Exception {
        DurationBasedTwdTransitionStdRegressorTrainer.main(args);
        DistanceBasedTwdTransitionStdRegressorTrainer.main(args);
        Thread.sleep(1000);
        ExportedModelsGenerator.main(args);
        LoggingUtil.logInfo("Model training finished. You can upload the generated file to a server instance.");
    }

}
