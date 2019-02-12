package com.sap.sailing.windestimation.model.classifier.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.store.ModelDomainType;

public final class TwdTransitionClassifierModelContext extends ModelContext<TwdTransition> {

    private static final long serialVersionUID = 819528288811779220L;
    private final ManeuverTypeTransition maneuverTypeTransition;

    public TwdTransitionClassifierModelContext(ManeuverTypeTransition maneuverTypeTransition) {
        super(ModelDomainType.TWD_TRANSITION_CLASSIFIER);
        this.maneuverTypeTransition = maneuverTypeTransition;
    }

    @Override
    public double[] getX(TwdTransition instance) {
        double[] x = new double[] { getXAsSingleValue(instance) };
        return x;
    }

    public double getXAsSingleValue(TwdTransition instance) {
        return instance.getTwdChange().getDegrees();
    }

    @Override
    public boolean isContainsAllFeatures(TwdTransition instance) {
        return true;
    }

    @Override
    public int getNumberOfInputFeatures() {
        return 1;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maneuverTypeTransition == null) ? 0 : maneuverTypeTransition.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TwdTransitionClassifierModelContext other = (TwdTransitionClassifierModelContext) obj;
        if (maneuverTypeTransition != other.maneuverTypeTransition)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TwdTransitionClassifierModelContext [maneuverTypeTransition=" + maneuverTypeTransition + "]";
    }

    @Override
    public int getNumberOfPossibleTargetValues() {
        return 2;
    }

    @Override
    public String getId() {
        return "TwdTransitionClassification-" + maneuverTypeTransition.toString();
    }

    public ManeuverTypeTransition getManeuverTypeTransition() {
        return maneuverTypeTransition;
    }

}
