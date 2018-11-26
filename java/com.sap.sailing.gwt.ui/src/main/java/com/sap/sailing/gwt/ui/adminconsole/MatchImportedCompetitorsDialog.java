package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.adminconsole.CompetitorDescriptorTableWrapper.CompetitorsToImportToExistingLinking;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

/**
 * Defines dialog where we can match and choose imported competitors. It mainly consists of two tables: one showing
 * competitor records coming from an source for import, such as an external regatta management system, and another table
 * that---upon selecting a single record in the table with the importable records--- displays potentially matching
 * existing competitors from the server's competitor base. The user can then assemble a set of competitors by first
 * optionally defining mappings from the importable competitors to existing competitors where instead of creating a new
 * competitor by means of import the existing one shall be used. Then, the user can make a selection in the table with
 * the importable competitors, including those for which a mapping to an existing competitor was defined. Pressing OK
 * will produce a set of {@link CompetitorWithBoatDTO}s where the ones coming from the set of already existing competitors have
 * a non-{@code null} {@link CompetitorWithBoatDTO#getIdAsString() ID} whereas the ones to import have all <em>but</em> an
 * {@link CompetitorWithBoatDTO#getIdAsString() ID}.
 * <p>
 * 
 * The result is a {@link Map} whose keys are all competitors selected for import and whose values are {@code null}
 * in case the competitor shall really be imported and hence be created anew, or a valid {@link CompetitorWithBoatDTO} with
 * a valid {@link CompetitorWithBoatDTO#getIdAsString() ID}, representing an already existing competitor that shall be used
 * instead of the key competitor descriptor.<p>
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
public class MatchImportedCompetitorsDialog extends DataEntryDialog<Pair<Map<CompetitorDescriptor, CompetitorDTO>, String>> {

    private CompetitorDescriptorTableWrapper<RefreshableMultiSelectionModel<CompetitorDescriptor>> importedCompetitorsTable;
    private CompetitorTableWrapper<RefreshableSingleSelectionModel<CompetitorDTO>> existingCompetitorsTable;
    
    private TextBox searchTagField;

    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final UserService userService;
    private final ErrorReporter errorReporter;

    private final Iterable<CompetitorDescriptor> CompetitorDescriptors;
    private final CompetitorImportMatcher competitorImportMatcher;

    private final Map<CompetitorDescriptor, CompetitorDTO> existingCompetitorsByImported = new HashMap<>();

    public MatchImportedCompetitorsDialog(final Iterable<CompetitorDescriptor> CompetitorDescriptors,
            final Iterable<CompetitorDTO> existingCompetitor, StringMessages stringMessages,
            SailingServiceAsync sailingService, final UserService userService, ErrorReporter errorReporter,
            DialogCallback<Pair<Map<CompetitorDescriptor, CompetitorDTO>, String>> callback) {
        super(stringMessages.importCompetitors(), stringMessages.chooseWhichCompetitorsShouldBeImported(),
                stringMessages.ok(), stringMessages.cancel(), /* validator */ null, callback);
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.userService = userService;
        this.errorReporter = errorReporter;
        this.CompetitorDescriptors = CompetitorDescriptors;
        competitorImportMatcher = new CompetitorImportMatcher(existingCompetitor);
    }

    protected void refreshInImportTable(CompetitorDescriptor competitor) {
        final List<CompetitorDescriptor> importedCompetitors = importedCompetitorsTable.getDataProvider().getList();
        importedCompetitors.set(importedCompetitors.indexOf(competitor), competitor); // force refresh of line item in table, showing new linkage state
    }

    @Override
    protected Widget getAdditionalWidget() {
        existingCompetitorsTable = new CompetitorTableWrapper<>(sailingService, userService, stringMessages,
                errorReporter, /* multiSelection */
                false, /* enablePager */true, /* filterCompetitorWithBoat */ false, /* filterCompetitorsWithoutBoat */ false);
        final CompetitorsToImportToExistingLinking linker = new CompetitorsToImportToExistingLinking() {
            @Override
            public void unlinkCompetitor(CompetitorDescriptor competitor) {
                final CompetitorDTO existingCompetitor = existingCompetitorsByImported.remove(competitor);
                if (existingCompetitor != null) {
                    existingCompetitorsTable.getSelectionModel().setSelected(existingCompetitor, false);
                    refreshInImportTable(competitor);
                }
            }

            @Override
            public CompetitorDTO getExistingCompetitorToUseInsteadOf(CompetitorDescriptor competitor) {
                return existingCompetitorsByImported.get(competitor);
            }
        };
        importedCompetitorsTable = new CompetitorDescriptorTableWrapper<>(competitorImportMatcher, sailingService,
                stringMessages, errorReporter, /* multiSelection */ true, /* enablePager */false, /* unlinkCallback */ linker);
        importedCompetitorsTable.refreshCompetitorDescriptorList(CompetitorDescriptors);
        final RefreshableMultiSelectionModel<CompetitorDescriptor> importedCompetitorSelectionModel = importedCompetitorsTable.getSelectionModel();
        importedCompetitorSelectionModel.addSelectionChangeHandler(getHandlerForImportedCompetitorsModel(importedCompetitorSelectionModel));
        final RefreshableSingleSelectionModel<CompetitorDTO> existingCompetitorSelectionModel = existingCompetitorsTable.getSelectionModel();
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
        searchTagField = createTextBox("", 40);
        searchTagField.getElement().setPropertyString("placeholder", stringMessages.searchTagForImportedCompetitorPlaceholder());
        final Label searchTagLabel = new Label(stringMessages.searchTag());
        final HorizontalPanel searchTagPanel = new HorizontalPanel();
        searchTagPanel.add(searchTagLabel);
        searchTagPanel.add(searchTagField);
        mainPanel.add(searchTagPanel);
        mainPanel.add(competitorImportPanel);
        return mainPanel;
    }

    private Handler getHandlerForExistingCompetitorsModel(
            final RefreshableMultiSelectionModel<CompetitorDescriptor> importedCompetitorSelectionModel,
            final RefreshableSingleSelectionModel<CompetitorDTO> existingCompetitorSelectionModel) {
        return new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<CompetitorDescriptor> competitorDescriptors = importedCompetitorSelectionModel.getSelectedSet();
                if (competitorDescriptors.size() == 1) {
                    final CompetitorDTO selectedCompetitor = existingCompetitorSelectionModel.getSelectedObject();
                    final CompetitorDescriptor competitorFromImport = competitorDescriptors.iterator().next();
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
            final RefreshableMultiSelectionModel<CompetitorDescriptor> importedCompetitorSelectionModel) {
        return new Handler() {
            private boolean refreshing = false;
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (!refreshing) {
                    Set<CompetitorDescriptor> selectedCompetitorDescriptors = importedCompetitorSelectionModel.getSelectedSet();
                    if (selectedCompetitorDescriptors.size() > 1) {
                        existingCompetitorsTable.getFilterField().removeAll();
                        return;
                    }
                    CompetitorDescriptor selectedCompetitorDescriptor = selectedCompetitorDescriptors.isEmpty() ? null
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
    protected Pair<Map<CompetitorDescriptor, CompetitorDTO>, String> getResult() {
        final Map<CompetitorDescriptor, CompetitorDTO> result = new HashMap<>();
        for (CompetitorDescriptor competitorDescriptor : importedCompetitorsTable.getSelectionModel().getSelectedSet()) {
            result.put(competitorDescriptor, existingCompetitorsByImported.get(competitorDescriptor));
        }
        return new Pair<>(result, searchTagField.getText());
    }
}
