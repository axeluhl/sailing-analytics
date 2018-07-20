package com.sap.sailing.gwt.ui.simulator.streamlets;

public class SchmittTrigger {
    private double percentage;

    public SchmittTrigger() {}
    public SchmittTrigger(double percentageTriggerThreeshold) {
        this.percentage = percentageTriggerThreeshold;
    }
    
    public boolean isGreaterThanThreeshold(double input,double threesholdValue) {
        if (input * (1 + percentage/100) > threesholdValue) {
            return true;
        }
        else {
            return false;
        }
    }
    public boolean isLesserThanThreeshold(double input,double threesholdValue) {
        if (input * (1 - percentage/100) < threesholdValue) {
            return true;
        }
        else {
            return false;
        }
    }
    public boolean isValueChangeTriggered(double input, double threesholdValue) {
        if (isGreaterThanThreeshold(input, threesholdValue) || isLesserThanThreeshold(input, threesholdValue)) {
            return true;
        }
        else {
            return false;
        }
    }
    public double getTriggerPercentage() {
        return percentage;
    }
}
