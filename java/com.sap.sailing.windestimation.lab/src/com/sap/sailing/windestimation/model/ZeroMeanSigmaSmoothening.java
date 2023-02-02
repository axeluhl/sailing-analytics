package com.sap.sailing.windestimation.model;

import java.net.UnknownHostException;

import org.json.simple.parser.ParseException;

import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;
import com.sap.sse.shared.json.JsonDeserializationException;

public class ZeroMeanSigmaSmoothening {
    public static void main(String[] args) throws JsonDeserializationException, UnknownHostException, ParseException {
        SimpleModelsTrainingPart1.enforceMonotonicZeroMeanSigmaGrowth(AggregatedSingleDimensionType.DISTANCE);
        SimpleModelsTrainingPart1.enforceMonotonicZeroMeanSigmaGrowth(AggregatedSingleDimensionType.DURATION);
    }
}
