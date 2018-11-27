package com.sap.sailing.windestimation.aggregator.advancedhmm;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.polars.windestimation.ManeuverClassification;

public class AdvancedGraphLevel {

    private final AdvancedGraphLevel parent;
    private final List<AdvancedGraphLevel> children = new ArrayList<>();
    private final double distanceToParent;
    private final ManeuverClassification maneuverClassification;

    public AdvancedGraphLevel(ManeuverClassification maneuverClassification) {
        this.maneuverClassification = maneuverClassification;
        parent = null;
        distanceToParent = 0;
    }

    private AdvancedGraphLevel(AdvancedGraphLevel parent, double distanceToParent,
            ManeuverClassification maneuverClassification) {
        this.parent = parent;
        this.distanceToParent = distanceToParent;
        this.maneuverClassification = maneuverClassification;
    }

    public AdvancedGraphLevel addChild(double distanceToParent, ManeuverClassification maneuverClassification) {
        AdvancedGraphLevel child = new AdvancedGraphLevel(this, distanceToParent, maneuverClassification);
        children.add(child);
        return child;
    }

    public AdvancedGraphLevel getParent() {
        return parent;
    }

    public double getDistanceToParent() {
        return distanceToParent;
    }

    public ManeuverClassification getManeuverClassification() {
        return maneuverClassification;
    }

    public List<AdvancedGraphLevel> getChildren() {
        return children;
    }

}
