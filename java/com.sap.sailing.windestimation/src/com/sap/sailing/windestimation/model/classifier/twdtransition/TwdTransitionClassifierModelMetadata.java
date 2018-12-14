package com.sap.sailing.windestimation.model.classifier.twdtransition;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.store.ContextType;

public class TwdTransitionClassifierModelMetadata extends ContextSpecificModelMetadata<TwdTransition> {

    private static final long serialVersionUID = 819528288811779220L;

    private final BoatClass boatClass;

    public TwdTransitionClassifierModelMetadata(BoatClass boatClass) {
        super(ContextType.TWD_TRANSITION);
        this.boatClass = boatClass;
    }

    @Override
    public double[] getX(TwdTransition instance) {
        double[] x = new double[] { instance.getDistance().getMeters(), instance.getDuration().asSeconds(),
                instance.getTwdChange().getDegrees() };
        return x;
    }

    @Override
    public boolean isContainsAllFeatures(TwdTransition instance) {
        return true;
    }

    @Override
    public int getNumberOfInputFeatures() {
        return 3;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((boatClass == null) ? 0 : boatClass.hashCode());
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
        TwdTransitionClassifierModelMetadata other = (TwdTransitionClassifierModelMetadata) obj;
        if (boatClass == null) {
            if (other.boatClass != null)
                return false;
        } else if (!boatClass.equals(other.boatClass))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TwdTransitionModelMetadata [boatClass=" + boatClass + "]";
    }

    @Override
    public int getNumberOfPossibleTargetValues() {
        return 2;
    }

    @Override
    public String getId() {
        StringBuilder id = new StringBuilder("TwdTransition-");
        if (getBoatClass() == null) {
            id.append("All");
        } else {
            id.append(getBoatClass().getName());
            id.append("_");
            id.append(getBoatClass().typicallyStartsUpwind() ? "startsUpwind" : "startsDownwind");
        }
        return id.toString();
    }

    public BoatClass getBoatClass() {
        return boatClass;
    }

}
