package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

/**
 * Ñallback is responsible for saving competitors in store and registering them if necessary.
 * Logic for registering should be implemented in child's classes.
 * @author Alexander_Tatarinovich
 *
 */
public class ImportCompetitorCallback implements DialogCallback<Set<CompetitorDTO>> {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    public ImportCompetitorCallback(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
    }

    @Override
    public void ok(Set<CompetitorDTO> competitorDTOs) {
        saveCompetitors(competitorDTOs);
    }

    @Override
    public void cancel() {
    }

    private void saveCompetitors(Set<CompetitorDTO> competitorDTOs) {
        // TODO: implement logic for saving competitors
    }

    protected void registerCompetitors(Set<CompetitorDTO> competitorDTOs) {

    }
}
