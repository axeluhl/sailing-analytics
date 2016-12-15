package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.domain.common.dto.CompetitorDescriptorDTO;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.gwt.ui.adminconsole.CompetitorDescriptorTableWrapper.CompetitorsToImportToExistingLinking;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * Defines dialog where we can match and choose imported competitors. It mainly consists of two tables: one showing
 * competitor records coming from an source for import, such as an external regatta management system, and another table
 * that---upon selecting a single record in the table with the importable records--- displays potentially matching
 * existing competitors from the server's competitor base. The user can then assemble a set of competitors by first
 * optionally defining mappings from the importable competitors to existing competitors where instead of creating a new
 * competitor by means of import the existing one shall be used. Then, the user can make a selection in the table with
 * the importable competitors, including those for which a mapping to an existing competitor was defined. Pressing OK
 * will produce a set of {@link CompetitorDTO}s where the ones coming from the set of already existing competitors have
 * a non-{@code null} {@link CompetitorDTO#getIdAsString() ID} whereas the ones to import have all <em>but</em> an
 * {@link CompetitorDTO#getIdAsString() ID}.
 * <p>
 * 
 * Use an {@link ImportCompetitorCallback} or subclass thereof as the {@link DialogCallback} passed to the constructor
 * for conveniently saving imported competitors to the store using
 * {@link SailingServiceAsync#addCompetitors(Iterable, com.google.gwt.user.client.rpc.AsyncCallback)}. Define a subclass
 * thereof and override {@link ImportCompetitorCallback#registerCompetitors(Set)} to determine what should happen with
 * the set of competitors the user has assembled through this dialog.
 * 
 * @author Alexander Tatarinovich
 *
 */
public class MatchImportedCompetitorsDialog extends DataEntryDialog<Set<CompetitorDTO>> {

    private CompetitorDescriptorTableWrapper<RefreshableMultiSelectionModel<CompetitorDescriptorDTO>> importedCompetitorsTable;
    private CompetitorTableWrapper<RefreshableSingleSelectionModel<CompetitorDTO>> existingCompetitorsTable;

    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    private final Iterable<CompetitorDescriptorDTO> competitorDescriptorDTOs;
    private final CompetitorImportMatcher competitorImportMatcher;

    private final Map<CompetitorDescriptorDTO, CompetitorDTO> existingCompetitorsByImported = new HashMap<>();

    public MatchImportedCompetitorsDialog(final Iterable<CompetitorDescriptorDTO> competitorDescriptorDTOs,
            final Iterable<CompetitorDTO> existingCompetitor, StringMessages stringMessages,
            SailingServiceAsync sailingService, ErrorReporter errorReporter,
            DialogCallback<Set<CompetitorDTO>> callback) {
        super(stringMessages.importCompetitors(), stringMessages.chooseWhichCompetitorsShouldBeImported(),
                stringMessages.ok(), stringMessages.cancel(), null, callback);
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.competitorDescriptorDTOs = competitorDescriptorDTOs;
        competitorImportMatcher = new CompetitorImportMatcher(existingCompetitor);
    }

    protected void refreshInImportTable(CompetitorDescriptorDTO competitor) {
        final List<CompetitorDescriptorDTO> importedCompetitors = importedCompetitorsTable.getDataProvider().getList();
        importedCompetitors.set(importedCompetitors.indexOf(competitor), competitor); // force refresh of line item in table, showing new linkage state
    }

    @Override
    protected Widget getAdditionalWidget() {
        existingCompetitorsTable = new CompetitorTableWrapper<>(sailingService, stringMessages,
                errorReporter, /* multiSelection */
                false, /* enablePager */true);
        final CompetitorsToImportToExistingLinking linker = new CompetitorsToImportToExistingLinking() {
            @Override
            public void unlinkCompetitor(CompetitorDescriptorDTO competitor) {
                final CompetitorDTO existingCompetitor = existingCompetitorsByImported.remove(competitor);
                if (existingCompetitor != null) {
                    existingCompetitorsTable.getSelectionModel().setSelected(existingCompetitor, false);
                    refreshInImportTable(competitor);
                }
            }

            @Override
            public CompetitorDTO getExistingCompetitorToUseInsteadOf(CompetitorDescriptorDTO competitor) {
                return existingCompetitorsByImported.get(competitor);
            }
        };
        importedCompetitorsTable = new CompetitorDescriptorTableWrapper<>(competitorImportMatcher, sailingService,
                stringMessages, errorReporter, /* multiSelection */ true, /* enablePager */false, /* unlinkCallback */ linker);
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
                if (competitorDescriptors.size() == 1) {
                    final CompetitorDTO selectedCompetitor = existingCompetitorSelectionModel.getSelectedObject();
                    final CompetitorDescriptorDTO competitorFromImport = competitorDescriptors.iterator().next();
                    final boolean changed;
                    if (selectedCompetitor == null) {
                        changed = existingCompetitorsByImported.remove(competitorFromImport) != null;
                    } else {
                        changed = !Util.equalsWithNull(
                                existingCompetitorsByImported.put(competitorFromImport, selectedCompetitor), selectedCompetitor);
                    }
                    if (changed) {
                        refreshInImportTable(competitorFromImport);
                    }
                }
            }
        };
    }

    private Handler getHandlerForImportedCompetitorsModel(
            final RefreshableMultiSelectionModel<CompetitorDescriptorDTO> importedCompetitorSelectionModel) {
        return new Handler() {
            private boolean refreshing = false;
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (!refreshing) {
                    Set<CompetitorDescriptorDTO> selectedCompetitorDescriptors = importedCompetitorSelectionModel.getSelectedSet();
                    if (selectedCompetitorDescriptors.size() > 1) {
                        existingCompetitorsTable.getFilterField().removeAll();
                        return;
                    }
                    CompetitorDescriptorDTO selectedCompetitorDescriptor = selectedCompetitorDescriptors.isEmpty() ? null
                            : importedCompetitorSelectionModel.getSelectedSet().iterator().next();
                    refreshing = true;
                    try {
                        existingCompetitorsTable.refreshCompetitorList(
                                competitorImportMatcher.getMatchesCompetitors(selectedCompetitorDescriptor));
                        CompetitorDTO competitor = existingCompetitorsByImported.get(selectedCompetitorDescriptor);
                        if (competitor != null && !existingCompetitorsTable.getSelectionModel().isSelected(competitor)) {
                            existingCompetitorsTable.getSelectionModel().setSelected(competitor, true);
                        }
                    } finally {
                        refreshing = false;
                    }
                }
            }
        };
    }

    @Override
    protected Set<CompetitorDTO> getResult() {
        final Set<CompetitorDTO> competitorsForSave = new HashSet<>();
        for (CompetitorDescriptorDTO competitorDescriptor : importedCompetitorsTable.getSelectionModel().getSelectedSet()) {
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
        BoatDTO defaultBoat = new BoatDTO(null, competitorDescriptor.getSailNumber());
        BoatClassDTO defaultBoatClass = new BoatClassDTO(competitorDescriptor.getBoatClassName() == null
                ? BoatClassDTO.DEFAULT_NAME : competitorDescriptor.getBoatClassName(),
                /* some default hull length; not used if boat class name can be resolved on the server */ new MeterDistance(5));
        return new CompetitorDTOImpl(competitorDescriptor.getName(), null, null,
                competitorDescriptor.getTwoLetterIsoCountryCode(), competitorDescriptor.getThreeLetterIocCountryCode(),
                competitorDescriptor.getCountryName(), null, null, null, defaultBoat, defaultBoatClass, null, null, null);
    }
}
