package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public class RaceOrRegattaImportCompetitorCallback extends ImportCompetitorCallback {
    private final CompetitorRegistrationsPanel registrationsDialog;

    public RaceOrRegattaImportCompetitorCallback(CompetitorRegistrationsPanel registrationsDialog, SailingServiceWriteAsync sailingServiceWrite, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        super(sailingServiceWrite, errorReporter, stringMessages);
        this.registrationsDialog = registrationsDialog;
    }

    @Override
    protected void registerCompetitors(Set<CompetitorDTO> competitors) {
        registrationsDialog.addImportedCompetitorsToRegisteredCompetitorsTableAndRemoveFromAllCompetitorsTable(competitors);
    }
}
