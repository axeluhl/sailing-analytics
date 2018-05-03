package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

/**
 * Callback is responsible for creation competitors for which no {@link CompetitorWithBoatDTO} that represents an
 * already existing competitor has been provided, and save those newly created competitors to
 * the competitor store using {@link SailingServiceAsync#addCompetitors(Iterable, AsyncCallback)}. The combined
 * set of competitors as represented by the {@link CompetitorWithBoatDTO}s provided as values representing the existing
 * competitors to use, plus those {@link CompetitorWithBoatDTO}s returned from the service representing the newly
 * created competitors (those actually "imported") are then passed to {@link #registerCompetitors(Set)} which
 * in this class does nothing but may be overridden by subclasses.
 * 
 * @author Alexander Tatarinovich
 *
 */
public class ImportCompetitorCallback implements DialogCallback<Pair<Map<CompetitorDescriptor, CompetitorDTO>, String>> {
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
    public void ok(final Pair<Map<CompetitorDescriptor, CompetitorDTO>, String> competitorsForRegisteringAndSearchTag) {
        final Set<CompetitorDTO> existingCompetitorsSelected = new HashSet<>();
        for (final CompetitorDTO existingCompetitor : competitorsForRegisteringAndSearchTag.getA().values()) {
            if (existingCompetitor != null) {
                existingCompetitor.addToSearchTag(competitorsForRegisteringAndSearchTag.getB());
                existingCompetitorsSelected.add(existingCompetitor);
            }
        }
        sailingService.addOrUpdateCompetitors(new ArrayList<>(existingCompetitorsSelected), new MarkedAsyncCallback<>(
                new AsyncCallback<List<CompetitorDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(caught.getMessage());
                    }
                    @Override
                    public void onSuccess(List<CompetitorDTO> result) {
                        // TODO trigger an update of any client-side competitor display
                    }
                }));
        registerCompetitorsAfterSaving(competitorsForRegisteringAndSearchTag.getA().entrySet().stream().filter((e->e.getValue() == null)).map(e->e.getKey()).
                collect(Collectors.toList()),
                competitorsForRegisteringAndSearchTag.getA().values().stream().filter(e->e!=null).collect(Collectors.toList()), competitorsForRegisteringAndSearchTag.getB());
    }

    /**
     * @param competitorsForSaving
     *            the descriptors of those competitors from the import source that the user selected and for which no
     *            existing competitor was mapped
     * @param competitorsForRegistration
     *            the existing competitors to which competitors from the import were mapped; this collection is logically
     *            disjoint from {@code competitorsForSaving} which only contains "net-new" competitors that will be
     *            created with a new ID on the server
     */
    private void registerCompetitorsAfterSaving(final List<CompetitorDescriptor> competitorsForSaving,
            final Iterable<CompetitorDTO> competitorsForRegistration, String searchTag) {
        sailingService.addCompetitors(competitorsForSaving, searchTag, new AsyncCallback<List<CompetitorWithBoatDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }

            @Override
            public void onSuccess(List<CompetitorWithBoatDTO> result) {
                final Set<CompetitorDTO> competitorsToAddWithNewOnesReplacedBySavedOnesWithId = new HashSet<>();
                Util.addAll(competitorsForRegistration, competitorsToAddWithNewOnesReplacedBySavedOnesWithId);
                // add those competitors returned by the server after saving to the competitor store where they received an ID:
                competitorsToAddWithNewOnesReplacedBySavedOnesWithId.addAll(result);
                registerCompetitors(competitorsToAddWithNewOnesReplacedBySavedOnesWithId);
            }
        });
    }

    protected void registerCompetitors(Set<CompetitorDTO> competitorDTOs) {
        // Don't register by default
    }
}
