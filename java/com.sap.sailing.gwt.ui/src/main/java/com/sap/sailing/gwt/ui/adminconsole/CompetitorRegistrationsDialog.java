package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class CompetitorRegistrationsDialog extends DialogBox {
    private final CompetitorTableWrapper competitorTable;
    
    public CompetitorRegistrationsDialog(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter, final String leaderboardName,
            final String raceColumnName, final String fleetName, boolean editable) {
        
        boolean filterByLeaderBoardInitially = false;
        
        VerticalPanel mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        
        HorizontalPanel buttonPanel = new HorizontalPanel();
        final Button toggleOnlyLeaderboard = new Button(filterByLeaderBoardInitially ? stringMessages.removeLeaderboardFilter(): 
            stringMessages.filterByLeaderboard());
        toggleOnlyLeaderboard.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                boolean filterByLeaderboard = toggleOnlyLeaderboard.getText().equals(stringMessages.filterByLeaderboard());
                competitorTable.refreshCompetitorList(filterByLeaderboard ? leaderboardName : null);
                toggleOnlyLeaderboard.setText(filterByLeaderboard ? stringMessages.removeLeaderboardFilter() :
                    stringMessages.filterByLeaderboard());
            }
        });
        final Button toggleSelection = new Button(stringMessages.selectAll());
        toggleSelection.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                boolean selectAll = toggleSelection.getText().equals(stringMessages.selectAll());
                for (CompetitorDTO competitor : competitorTable.getAllCompetitors()) {
                    competitorTable.getSelectionModel().setSelected(competitor, selectAll);
                }
                toggleSelection.setText(selectAll ? stringMessages.deselectAll() : stringMessages.selectAll());
            }
            
        });
        Button cancel = new Button(stringMessages.cancel());
        cancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        Button save = new Button(stringMessages.save());
        save.setEnabled(editable);
        save.setTitle(stringMessages.canOnlyBeEditedBeforeStartingTracking());
        save.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                sailingService.setCompetitorRegistrations(leaderboardName, raceColumnName, fleetName,
                        competitorTable.getSelectionModel().getSelectedSet(), new AsyncCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                hide();
                            }
                            
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Could not save competitor registrations: " + caught.getMessage());
                            }
                        });
            }
        });
        buttonPanel.add(toggleOnlyLeaderboard);
        buttonPanel.add(toggleSelection);
        buttonPanel.add(cancel);
        buttonPanel.add(save);
        mainPanel.add(buttonPanel);
        
        competitorTable = new CompetitorTableWrapper(sailingService, stringMessages, errorReporter);
        mainPanel.add(competitorTable);
                
        competitorTable.refreshCompetitorList(filterByLeaderBoardInitially ? leaderboardName : null,
                true, new Callback<Iterable<CompetitorDTO>, Throwable>() {
            @Override
            public void onSuccess(Iterable<CompetitorDTO> result) {
                center();
                
                //preselect competitors that are already registered
                sailingService.getCompetitorRegistrations(leaderboardName, raceColumnName, fleetName, new AsyncCallback<Iterable<CompetitorDTO>>() {
                    @Override
                    public void onSuccess(Iterable<CompetitorDTO> result) {
                        Set<String> registeredIds = new HashSet<String>();
                        for (CompetitorDTO c : result) {
                            registeredIds.add(c.getIdAsString());
                        }
                        for (CompetitorDTO c : competitorTable.getAllCompetitors()) {
                            competitorTable.getSelectionModel().setSelected(c, registeredIds.contains(c.getIdAsString()));
                        }
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
