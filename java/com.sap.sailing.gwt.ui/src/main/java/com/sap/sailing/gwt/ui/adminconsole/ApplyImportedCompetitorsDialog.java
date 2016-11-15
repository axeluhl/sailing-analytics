package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.domain.common.dto.CompetitorDescriptorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * Define dialog where we can match and apply imported competitors.
 * 
 * @author Alexander_Tatarinovich
 *
 */
public class ApplyImportedCompetitorsDialog extends DataEntryDialog<Set<CompetitorDTO>> {

    private CompetitorDescriptorTableWrapper<RefreshableMultiSelectionModel<CompetitorDescriptorDTO>> importedCompetitorsTable;
    private CompetitorTableWrapper<RefreshableSingleSelectionModel<CompetitorDTO>> existingCompetitorsTable;

    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    private final Iterable<CompetitorDescriptorDTO> competitorDescriptorDTOs;
    private final CompetitorImportMatcher competitorImportMatcher;

    private final Map<CompetitorDescriptorDTO, CompetitorDTO> existingCompetitorsByImported = new HashMap<>();

    public ApplyImportedCompetitorsDialog(final Iterable<CompetitorDescriptorDTO> competitorDescriptorDTOs,
            final Iterable<CompetitorDTO> existingCompetitor, StringMessages stringMessages,
            SailingServiceAsync sailingService, ErrorReporter errorReporter) {
        super(stringMessages.importCompetitors(), stringMessages.chooseWhichCompetitorsShouldBeImported(),
                stringMessages.ok(), stringMessages.cancel(), null, /* TODO: implement calback */null);
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.competitorDescriptorDTOs = competitorDescriptorDTOs;
        competitorImportMatcher = new CompetitorImportMatcher(existingCompetitor);
    }

    @Override
    protected Widget getAdditionalWidget() {
        existingCompetitorsTable = new CompetitorTableWrapper<>(sailingService, stringMessages,
                errorReporter, /* multiSelection */
                false, /* enablePager */true);
        importedCompetitorsTable = new CompetitorDescriptorTableWrapper<>(competitorImportMatcher, sailingService,
                stringMessages, errorReporter, /* multiSelection */
                true, /* enablePager */false);
        importedCompetitorsTable.refreshCompetitorDescriptorList(competitorDescriptorDTOs);

        final RefreshableMultiSelectionModel<CompetitorDescriptorDTO> importedCompetitorSelectionModel = importedCompetitorsTable
                .getSelectionModel();
        importedCompetitorSelectionModel
                .addSelectionChangeHandler(getHandlerForImportedCompetitorsModel(importedCompetitorSelectionModel));

        final RefreshableSingleSelectionModel<CompetitorDTO> existingCompetitorSelectionModel = existingCompetitorsTable
                .getSelectionModel();
        existingCompetitorSelectionModel.addSelectionChangeHandler(getHandlerForExistingCompetitorsModel(
                importedCompetitorSelectionModel, existingCompetitorSelectionModel));

        final FlowPanel mainPanel = new FlowPanel();
        final HorizontalPanel competitorImportPanel = new HorizontalPanel();
        final CaptionPanel existingCompetitorsPanel = new CaptionPanel(stringMessages.existingCompetitors());
        final CaptionPanel importedCompetitorsPanel = new CaptionPanel(stringMessages.importedCompetitors());

        existingCompetitorsPanel.add(existingCompetitorsTable);
        importedCompetitorsPanel.add(importedCompetitorsTable);

        ScrollPanel scrollCompetitorRegistrationPanel = new ScrollPanel(importedCompetitorsPanel);
        scrollCompetitorRegistrationPanel.setSize("100%", "500px");
        ScrollPanel scrollExistingCompetitorRegistrationPanel = new ScrollPanel(existingCompetitorsPanel);
        scrollCompetitorRegistrationPanel.setSize("100%", "500px");
        competitorImportPanel.add(scrollCompetitorRegistrationPanel);
        competitorImportPanel.add(scrollExistingCompetitorRegistrationPanel);

        mainPanel.add(competitorImportPanel);
        return mainPanel;
    }

    private Handler getHandlerForExistingCompetitorsModel(
            final RefreshableMultiSelectionModel<CompetitorDescriptorDTO> importedCompetitorSelectionModel,
            final RefreshableSingleSelectionModel<CompetitorDTO> existingCompetitorSelectionModel) {
        return new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<CompetitorDescriptorDTO> competitorDescriptors = importedCompetitorSelectionModel.getSelectedSet();
                if (competitorDescriptors.size() > 1) {
                    return;
                }
                CompetitorDTO selectedCompetitor = existingCompetitorSelectionModel.getSelectedObject();
                CompetitorDescriptorDTO selectedCompetitorDescriptor = competitorDescriptors.isEmpty() ? null
                        : importedCompetitorSelectionModel.getSelectedSet().iterator().next();
                if (selectedCompetitorDescriptor != null) {
                    existingCompetitorsByImported.put(selectedCompetitorDescriptor, selectedCompetitor);
                }
            }
        };
    }

    private Handler getHandlerForImportedCompetitorsModel(
            final RefreshableMultiSelectionModel<CompetitorDescriptorDTO> importedCompetitorSelectionModel) {
        return new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<CompetitorDescriptorDTO> selectedCompetitorDescriptors = importedCompetitorSelectionModel
                        .getSelectedSet();
                if (selectedCompetitorDescriptors.size() > 1) {
                    existingCompetitorsTable.getFilterField().removeAll();
                    return;
                }

                CompetitorDescriptorDTO selectedCompetitorDescriptor = selectedCompetitorDescriptors.isEmpty() ? null
                        : importedCompetitorSelectionModel.getSelectedSet().iterator().next();
                existingCompetitorsTable.refreshCompetitorList(
                        competitorImportMatcher.getMatchesCompetitors(selectedCompetitorDescriptor));
                CompetitorDTO competitor = existingCompetitorsByImported.get(selectedCompetitorDescriptor);
                if (competitor != null) {
                    existingCompetitorsTable.getSelectionModel().setSelected(competitor, true);
                }
            }
        };
    }

    private void saveCompetitors(Set<CompetitorDTO> competitors) {
        // TODO: implement logic for saving competitors

        registerCompetitors(competitors);
    }

    protected void registerCompetitors(Set<CompetitorDTO> competitors) {
        // Do nothing by default. Can be overridden in child classes for registration competitor in race or regatta
    }

    @Override
    protected Set<CompetitorDTO> getResult() {
        final Set<CompetitorDTO> competitorsForSave = new HashSet<>();
        for (CompetitorDescriptorDTO competitorDescriptor : importedCompetitorsTable.getSelectionModel()
                .getSelectedSet()) {
            CompetitorDTO existingCompetitor = existingCompetitorsByImported.get(competitorDescriptor);
            if (existingCompetitor != null) {
                competitorsForSave.add(existingCompetitor);
            } else {
                competitorsForSave.add(convertCompetitorDescriptorToCompetitorDTO(competitorDescriptor));
            }
        }
        return competitorsForSave;
    }

    private CompetitorDTO convertCompetitorDescriptorToCompetitorDTO(CompetitorDescriptorDTO competitorDescriptor) {
        BoatDTO boat = new BoatDTO(null, competitorDescriptor.getSailNumber());
        return new CompetitorDTOImpl(competitorDescriptor.getName(), null, null,
                competitorDescriptor.getTwoLetterIsoCountryCode(), competitorDescriptor.getThreeLetterIocCountryCode(),
                competitorDescriptor.getCountryName(), null, null, null, boat, null, null, null, null);
    }
}
