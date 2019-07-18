package com.sap.sailing.windestimation.model.classifier.twdtransition;

/**
 * @author Vladislav Chumak (D069712)
 * @see TwdTransitionClassifiersCache
 */
public class TwdTransitionClassificationResult {

    private final double transitionCorrectProbability;
    private final double transitionIncorrectProbability;

    public TwdTransitionClassificationResult(double transitionCorrectProbability,
            double transitionIncorrectProbability) {
        this.transitionCorrectProbability = transitionCorrectProbability;
        this.transitionIncorrectProbability = transitionIncorrectProbability;
    }

    public double getTransitionCorrectProbability() {
        return transitionCorrectProbability;
    }

    public double getTransitionIncorrectProbability() {
        return transitionIncorrectProbability;
    }

}
