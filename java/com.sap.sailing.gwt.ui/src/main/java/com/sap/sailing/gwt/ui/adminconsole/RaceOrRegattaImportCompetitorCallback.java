package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public class RaceOrRegattaImportCompetitorCallback extends ImportCompetitorCallback {
    private final CompetitorRegistrationsPanel registrationsDialog;

    public RaceOrRegattaImportCompetitorCallback(CompetitorRegistrationsPanel registrationsDialog, SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        super(sailingService, errorReporter, stringMessages);
        this.registrationsDialog = registrationsDialog;
    }

    @Override
    protected void registerCompetitors(Set<CompetitorDTO> competitors) {
        registrationsDialog.addImportedCompetitorsToRegisteredCompetitorsTableAndRemoveFromAllCompetitorsTable(competitors);
    }
}
