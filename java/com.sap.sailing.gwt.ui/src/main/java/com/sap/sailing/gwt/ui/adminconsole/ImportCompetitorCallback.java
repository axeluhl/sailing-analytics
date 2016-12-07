package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

/**
 * Ñallback is responsible for saving competitors in store and registering them if necessary. Logic for registering
 * should be implemented in child's classes.
 * 
 * @author Alexander_Tatarinovich
 *
 */
public class ImportCompetitorCallback implements DialogCallback<Set<CompetitorDTO>> {
    protected final SailingServiceAsync sailingService;
    protected final ErrorReporter errorReporter;
    protected final StringMessages stringMessages;

    public ImportCompetitorCallback(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
    }

    @Override
    public void cancel() {
    }

    @Override
    public void ok(final Set<CompetitorDTO> competitorsForRegistering) {
        List<CompetitorDTO> competitorsForSaving = prepareCompetitorsForSaving(competitorsForRegistering);
        registerCompetitorsAfterSaving(competitorsForSaving, competitorsForRegistering);
    }

    protected List<CompetitorDTO> prepareCompetitorsForSaving(Set<CompetitorDTO> competitors) {
        List<CompetitorDTO> competitorsForSaving = new ArrayList<>();
        for (CompetitorDTO competitor : competitors) {
            if (competitor.getIdAsString() == null) {
                competitorsForSaving.add(competitor);
            }
        }
        return competitorsForSaving;
    }

    private void registerCompetitorsAfterSaving(final List<CompetitorDTO> competitorsForSaving,
            final Set<CompetitorDTO> competitorsForRegistration) {
        sailingService.addCompetitors(competitorsForSaving, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(Void result) {
                registerCompetitors(competitorsForRegistration);
            }
        });
    }

    protected void registerCompetitors(Set<CompetitorDTO> competitorDTOs) {
        // Don't register by default
    }
}
