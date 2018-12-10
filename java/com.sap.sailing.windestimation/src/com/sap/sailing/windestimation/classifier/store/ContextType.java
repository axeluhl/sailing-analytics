package com.sap.sailing.windestimation.classifier.store;

public enum ContextType {
    MANEUVER("maneuver"), TWD_TRANSITION("twdTransition");

    private final String contextName;

    private ContextType(String contextName) {
        this.contextName = contextName;
    }

    public String getContextName() {
        return contextName;
    }
}
