package com.sap.sailing.windestimation.model.store;

public enum ContextType {
    MANEUVER("Maneuver"), TWD_TRANSITION("TwdTransition");

    private final String contextName;

    private ContextType(String contextName) {
        this.contextName = contextName;
    }

    public String getContextName() {
        return contextName;
    }
}
