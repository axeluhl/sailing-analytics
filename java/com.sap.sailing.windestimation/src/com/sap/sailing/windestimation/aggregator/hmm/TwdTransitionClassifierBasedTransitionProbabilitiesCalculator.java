package com.sap.sailing.windestimation.aggregator.hmm;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.classifier.twdtransition.TwdTransitionClassificationResult;
import com.sap.sailing.windestimation.model.classifier.twdtransition.TwdTransitionClassifiersCache;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class TwdTransitionClassifierBasedTransitionProbabilitiesCalculator
        extends SimpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator {

    private final TwdTransitionClassifiersCache twdTransitionClassifiersCache;

    public TwdTransitionClassifierBasedTransitionProbabilitiesCalculator(
            TwdTransitionClassifiersCache twdTransitionClassifiersCache,
            boolean propagateIntersectedWindRangeOfHeadupAndBearAway) {
        super(propagateIntersectedWindRangeOfHeadupAndBearAway);
        this.twdTransitionClassifiersCache = twdTransitionClassifiersCache;
    }

    @Override
    protected double getPenaltyFactorForTransition(TwdTransition twdTransition) {
        TwdTransitionClassificationResult classificationResult = twdTransitionClassifiersCache
                .classifyInstance(twdTransition);
        return classificationResult.getTransitionCorrectProbability();
    }

}
