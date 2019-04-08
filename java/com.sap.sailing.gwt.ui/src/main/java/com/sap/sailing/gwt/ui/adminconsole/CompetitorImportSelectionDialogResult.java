package com.sap.sailing.gwt.ui.adminconsole;

public class CompetitorImportSelectionDialogResult {
    private final String providerName;
    private final String eventName;
    private final String regattaName;

    public CompetitorImportSelectionDialogResult(String providerName, String eventName, String regattaName) {
        this.providerName = providerName;
        this.eventName = eventName;
        this.regattaName = regattaName;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getEventName() {
        return eventName;
    }

    public String getRegattaName() {
        return regattaName;
    }
}
