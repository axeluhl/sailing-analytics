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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class AbstractCompetitorRegistrationsDialog extends DataEntryDialog<Set<CompetitorDTO>> {
    protected CompetitorTableWrapper<MultiSelectionModel<CompetitorDTO>> allCompetitorsTable;
    protected CompetitorTableWrapper<MultiSelectionModel<CompetitorDTO>> registeredCompetitorsTable;
    protected final boolean filterByLeaderBoardInitially = false;
    private final StringMessages stringMessages;
    protected final SailingServiceAsync sailingService;
    final ErrorReporter errorReporter;
    private boolean editable;
    private String boatClass;

    public AbstractCompetitorRegistrationsDialog(final SailingServiceAsync sailingService,
            final StringMessages stringMessages, final ErrorReporter errorReporter, boolean editable,
            DialogCallback<Set<CompetitorDTO>> callback, String boatClass) {
        super(stringMessages.registerCompetitors(), /*messsage*/ null, stringMessages.save(), stringMessages.cancel(), /*validator*/ null, callback);
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter  = errorReporter;
        this.editable = editable;
        this.boatClass = boatClass;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        FlowPanel mainPanel = new FlowPanel();
        
        Button addCompetitorButton = new Button(stringMessages.add(stringMessages.competitor()));
        addCompetitorButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openAddCompetitorDialog();
            }
        });
        
        Button editCompetitorButton = new Button(stringMessages.edit(stringMessages.competitor()));
        editCompetitorButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openEditCompetitorDialog();
            }
        });

        HorizontalPanel competitorRegistrationPanel = new HorizontalPanel();
        CaptionPanel allCompetitorsPanel = new CaptionPanel(stringMessages.competitorPool());
        CaptionPanel registeredCompetitorsPanel = new CaptionPanel(stringMessages.registeredCompetitors());
        allCompetitorsTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */ true, /* enablePager */ true);
        registeredCompetitorsTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */ true, /* enablePager */ true);
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
        competitorRegistrationPanel.add(registeredCompetitorsPanel);
        competitorRegistrationPanel.add(movePanel);
        competitorRegistrationPanel.setCellVerticalAlignment(movePanel, HasVerticalAlignment.ALIGN_MIDDLE);
        competitorRegistrationPanel.add(allCompetitorsPanel);
        
        refreshCompetitors();
        
        mainPanel.add(addCompetitorButton);
        mainPanel.add(editCompetitorButton);
        mainPanel.add(competitorRegistrationPanel);

        return mainPanel;
    }
    
    void move(CompetitorTableWrapper<?> from, CompetitorTableWrapper<?> to, Collection<CompetitorDTO> toMove) {
        if (!toMove.isEmpty()) {
            List<CompetitorDTO> newFromList = new ArrayList<>();
            Util.addAll(from.getFilterField().getAll(), newFromList);
            newFromList.removeAll(toMove);
            from.getFilterField().updateAll(newFromList);
            List<CompetitorDTO> newToList = new ArrayList<>();
            Util.addAll(to.getFilterField().getAll(), newToList);
            newToList.addAll(toMove);
            to.getFilterField().updateAll(newToList);
        }
    }

    private void moveSelected(CompetitorTableWrapper<MultiSelectionModel<CompetitorDTO>> from,
            CompetitorTableWrapper<MultiSelectionModel<CompetitorDTO>> to) {
        move(from, to, from.getSelectionModel().getSelectedSet());
    }


    private void openAddCompetitorDialog() {
      
        new CompetitorEditDialog(stringMessages, new CompetitorDTOImpl(),
                new DataEntryDialog.DialogCallback<CompetitorDTO>() {
                    @Override
                    public void ok(CompetitorDTO competitor) {
                        sailingService.addOrUpdateCompetitor(competitor, new AsyncCallback<CompetitorDTO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error trying to add competitor: " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(CompetitorDTO updatedCompetitor) {
                                registeredCompetitorsTable.getFilterField().add(updatedCompetitor);
                            }
                        });
                    }

                    @Override
                    public void cancel() {
                    }
                },boatClass).show();
    }
    
    private void openEditCompetitorDialog() {
        //get currently selected competitor
        if (registeredCompetitorsTable.getSelectionModel().getSelectedSet().size() != 1){
            // show some warning
        } else {
            final CompetitorDTO competitorToEdit = registeredCompetitorsTable.getSelectionModel().getSelectedSet().iterator().next();
            new CompetitorEditDialog(stringMessages, competitorToEdit,
                    new DataEntryDialog.DialogCallback<CompetitorDTO>() {
                @Override
                public void ok(CompetitorDTO competitor) {
                    sailingService.addOrUpdateCompetitor(competitor, new AsyncCallback<CompetitorDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Error trying to add competitor: " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(CompetitorDTO updatedCompetitor) {
                            int editedCompetitorIndex = registeredCompetitorsTable.getDataProvider().getList().indexOf(competitorToEdit);
                            registeredCompetitorsTable.getDataProvider().getList().remove(competitorToEdit);
                            registeredCompetitorsTable.getDataProvider().getList().add(editedCompetitorIndex, updatedCompetitor);
                            registeredCompetitorsTable.getDataProvider().refresh();
                        }
                    });
                }

                @Override
                public void cancel() {
                }
            },null).show();
        }
    }

    protected void refreshCompetitors() {
        registeredCompetitorsTable.getDataProvider().getList().clear();
        allCompetitorsTable.refreshCompetitorList(null, new Callback<Iterable<CompetitorDTO>, Throwable>() {
            @Override
            public void onSuccess(Iterable<CompetitorDTO> result) {
                setRegisteredCompetitors();
            }
            
            @Override
            public void onFailure(Throwable reason) {
            }
        });
    }
    
    protected abstract void setRegisteredCompetitors();

    @Override
    protected Set<CompetitorDTO> getResult() {
        final Set<CompetitorDTO> registeredCompetitors = new HashSet<>();
        Util.addAll(registeredCompetitorsTable.getAllCompetitors(), registeredCompetitors);
        return registeredCompetitors;
    }
}
