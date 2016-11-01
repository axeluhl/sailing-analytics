package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.adminconsole.CompetitorImportProviderSelectionDialog.ApplyImportedCompetitorsDialogFactory;

public class CompetitorImportDialogResult {
    private final String providerName;
    private final String eventName;
    private final String regattaName;
    private final ApplyImportedCompetitorsDialogFactory applyImportedCompetitorsDialogFactory;

    public CompetitorImportDialogResult(String providerName, String eventName, String regattaName,
            ApplyImportedCompetitorsDialogFactory applyImportedCompetitorsDialogFactory) {
        super();
        this.providerName = providerName;
        this.eventName = eventName;
        this.regattaName = regattaName;
        this.applyImportedCompetitorsDialogFactory = applyImportedCompetitorsDialogFactory;
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

    public ApplyImportedCompetitorsDialogFactory getApplyImportedCompetitorsDialogFactory() {
        return applyImportedCompetitorsDialogFactory;
    }
}
