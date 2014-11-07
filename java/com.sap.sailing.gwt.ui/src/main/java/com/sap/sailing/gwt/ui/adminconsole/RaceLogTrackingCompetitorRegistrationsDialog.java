package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.MultiSelectionModel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RaceLogTrackingCompetitorRegistrationsDialog extends RaceLogTrackingDialog {
    private CompetitorTableWrapper<MultiSelectionModel<CompetitorDTO>> allCompetitorsTable;
    private CompetitorTableWrapper<MultiSelectionModel<CompetitorDTO>> registeredCompetitorsTable;
    private final boolean filterByLeaderBoardInitially = false;
    private final Callback<Boolean, Throwable> competitorsRegistered;

    public RaceLogTrackingCompetitorRegistrationsDialog(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter, final String leaderboardName, final String raceColumnName, final String fleetName, boolean editable,
            Callback<Boolean, Throwable> competitorsRegistered) {
        super(sailingService, stringMessages, errorReporter, leaderboardName, raceColumnName, fleetName, editable);
        this.competitorsRegistered = competitorsRegistered;
        
        refreshCompetitors();
    }

    @Override
    protected void addButtons(Panel buttonPanel) {
        Button addCompetitorButton = new Button(stringMessages.add(stringMessages.competitor()));
        addCompetitorButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openAddCompetitorDialog();
            }
        });
        buttonPanel.add(addCompetitorButton);

        super.addButtons(buttonPanel);
    }
    
    private void move(CompetitorTableWrapper<?> from, CompetitorTableWrapper<?> to, Collection<CompetitorDTO> toMove) {
        if (toMove.isEmpty()) {
            return;
        }
        List<CompetitorDTO> newFromList = new ArrayList<>();
        Util.addAll(from.getFilterField().getAll(), newFromList);
        newFromList.removeAll(toMove);
        from.getFilterField().updateAll(newFromList);
        List<CompetitorDTO> newToList = new ArrayList<>();
        Util.addAll(to.getFilterField().getAll(), newToList);
        newToList.addAll(toMove);
        to.getFilterField().updateAll(newToList);
    }
    
    private void moveSelected(CompetitorTableWrapper<MultiSelectionModel<CompetitorDTO>> from,
            CompetitorTableWrapper<MultiSelectionModel<CompetitorDTO>> to) {
        move(from, to, from.getSelectionModel().getSelectedSet());
    }

    @Override
    protected void addMainContent(Panel mainPanel) {
        super.addMainContent(mainPanel);
        
        HorizontalPanel panel = new HorizontalPanel();
        mainPanel.add(panel);
        CaptionPanel allCompetitorsPanel = new CaptionPanel(stringMessages.competitorPool());
        CaptionPanel registeredCompetitorsPanel = new CaptionPanel(stringMessages.registeredCompetitors());
        allCompetitorsTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter,
                new MultiSelectionModel<CompetitorDTO>(), true);
        registeredCompetitorsTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter,
                new MultiSelectionModel<CompetitorDTO>(), true);
        allCompetitorsPanel.add(allCompetitorsTable);
        registeredCompetitorsPanel.add(registeredCompetitorsTable);
        VerticalPanel movePanel = new VerticalPanel();
        Button registerBtn = new Button("<");
        Button unregisterBtn = new Button(">");
        registerBtn.setEnabled(editable);
        unregisterBtn.setEnabled(editable);
        movePanel.add(registerBtn);
        movePanel.add(unregisterBtn);
        registerBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveSelected(allCompetitorsTable, registeredCompetitorsTable);
            }
        });
        unregisterBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveSelected(registeredCompetitorsTable, allCompetitorsTable);
            }
        });
        panel.add(registeredCompetitorsPanel);
        panel.add(movePanel);
        panel.setCellVerticalAlignment(movePanel, HasVerticalAlignment.ALIGN_MIDDLE);
        panel.add(allCompetitorsPanel);
    }

    @Override
    protected void save() {
        final Set<CompetitorDTO> registeredCompetitors = new HashSet<>();
        Util.addAll(registeredCompetitorsTable.getAllCompetitors(), registeredCompetitors);
        sailingService.setCompetitorRegistrations(leaderboardName, raceColumnName, fleetName,
                registeredCompetitors, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                hide();
                competitorsRegistered.onSuccess(! registeredCompetitors.isEmpty());
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not save competitor registrations: " + caught.getMessage());
                competitorsRegistered.onFailure(caught);
            }
        });
    }

    private void openAddCompetitorDialog() {
        new CompetitorEditDialog(stringMessages, new CompetitorDTOImpl(), new DataEntryDialog.DialogCallback<CompetitorDTO>() {
            @Override
            public void ok(CompetitorDTO competitor) {
                sailingService.addOrUpdateCompetitor(competitor, new AsyncCallback<CompetitorDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to add competitor: "+caught.getMessage());
                    }

                    @Override
                    public void onSuccess(CompetitorDTO updatedCompetitor) {
                        allCompetitorsTable.getFilterField().add(updatedCompetitor);
                    }
                });
            }

            @Override
            public void cancel() {
            }
        }).show();
    }
    
    private void refreshCompetitors() {
        registeredCompetitorsTable.getDataProvider().getList().clear();
        
        allCompetitorsTable.refreshCompetitorList(filterByLeaderBoardInitially ? leaderboardName : null,
                true, new Callback<Iterable<CompetitorDTO>, Throwable>() {
            @Override
            public void onSuccess(Iterable<CompetitorDTO> result) {
                center();

                //add competitors that are already registered
                sailingService.getCompetitorRegistrations(leaderboardName, raceColumnName, fleetName, new AsyncCallback<Collection<CompetitorDTO>>() {
                    @Override
                    public void onSuccess(Collection<CompetitorDTO> result) {
                        move(allCompetitorsTable, registeredCompetitorsTable, result);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Could not load already registered competitors: " + caught.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Throwable reason) {}
        });
    }
}
