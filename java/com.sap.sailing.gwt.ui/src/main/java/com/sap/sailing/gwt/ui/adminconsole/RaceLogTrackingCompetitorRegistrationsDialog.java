package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
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
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class RaceLogTrackingCompetitorRegistrationsDialog extends AbstractSaveDialog {
    protected CompetitorTableWrapper<MultiSelectionModel<CompetitorDTO>> allCompetitorsTable;
    protected CompetitorTableWrapper<MultiSelectionModel<CompetitorDTO>> registeredCompetitorsTable;
    protected final boolean filterByLeaderBoardInitially = false;
    protected final CompetitorRegistrationHandler competitorRegistrationsHandler;
    private String leaderboardName;

    static interface CompetitorRegistrationHandler {
        void getRegisteredCompetitors(Callback<Collection<CompetitorDTO>, Throwable> callback);

        void setRegisteredCompetitors(Set<CompetitorDTO> competitors);
    }

    public RaceLogTrackingCompetitorRegistrationsDialog(final SailingServiceAsync sailingService,
            final StringMessages stringMessages, final ErrorReporter errorReporter, boolean editable, String leaderboardName,
            CompetitorRegistrationHandler competitorRegistrationHandler) {
        super(sailingService, stringMessages, errorReporter, editable);
        this.competitorRegistrationsHandler = competitorRegistrationHandler;
        this.leaderboardName = leaderboardName;
        
        setupUi();
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
        
        Button editCompetitorButton = new Button(stringMessages.edit(stringMessages.competitor()));
        editCompetitorButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openEditCompetitorDialog();
            }
        });
        
        final Button inviteCompetitorsButton = new Button(stringMessages.inviteSelectedCompetitors());
        inviteCompetitorsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Collection<CompetitorDTO> competitorsToInvite = null;
                if (registeredCompetitorsTable.getSelectionModel().getSelectedSet().size() > 0){
                   competitorsToInvite = registeredCompetitorsTable.getSelectionModel().getSelectedSet();
                } else {
                    Collection<CompetitorDTO> competitors = registeredCompetitorsTable.dataProvider.getList();
                    competitorsToInvite = new ArrayList<CompetitorDTO>(competitors);
                }
                
                boolean emailProvidedForAll = isEmailProvidedForAll(competitorsToInvite);

                if (emailProvidedForAll) {
                    openChooseEventDialogAndSendMails(competitorsToInvite);
                } else {
                    Window.alert(stringMessages.notAllCompetitorsProvideEmail());
                }
            }

            private boolean isEmailProvidedForAll(Iterable<CompetitorDTO> allCompetitors) {
                for (CompetitorDTO competitor : allCompetitors) {
                    if (!competitor.hasEmail()) {
                        return false;
                    }
                }

                return true;
            }
        });
        
        buttonPanel.add(addCompetitorButton);
        buttonPanel.add(editCompetitorButton);
        buttonPanel.add(inviteCompetitorsButton);

        super.addButtons(buttonPanel);
    }
    
    private void openChooseEventDialogAndSendMails(final Collection<CompetitorDTO> competitors) {
        new SelectEventAndHostnameDialog(sailingService, stringMessages, errorReporter, leaderboardName, new DialogCallback<Pair<EventDTO, String>>() {

            @Override
            public void ok(Pair<EventDTO, String> result) {
                sailingService.inviteCompetitorsForTrackingViaEmail(result.getB(), result.getA(), leaderboardName,
                        competitors, LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                Window.alert(stringMessages.sendingMailsFailed() + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Void result) {
                                Window.alert(stringMessages.sendingMailsSuccessful());
                            }
                        });
            }

            @Override
            public void cancel() {
                
            }
        }).show();
    }

    private void move(CompetitorTableWrapper<?> from, CompetitorTableWrapper<?> to, Collection<CompetitorDTO> toMove) {
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

    @Override
    protected void addMainContent(Panel mainPanel) {
        super.addMainContent(mainPanel);

        HorizontalPanel panel = new HorizontalPanel();
        mainPanel.add(panel);
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
        panel.add(registeredCompetitorsPanel);
        panel.add(movePanel);
        panel.setCellVerticalAlignment(movePanel, HasVerticalAlignment.ALIGN_MIDDLE);
        panel.add(allCompetitorsPanel);
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
                }).show();
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
            }).show();
        }
    }

    protected void refreshCompetitors() {
        registeredCompetitorsTable.getDataProvider().getList().clear();

        allCompetitorsTable.refreshCompetitorList(null, new Callback<Iterable<CompetitorDTO>, Throwable>() {
            @Override
            public void onSuccess(Iterable<CompetitorDTO> result) {
                center();

                competitorRegistrationsHandler
                        .getRegisteredCompetitors(new Callback<Collection<CompetitorDTO>, Throwable>() {
                            @Override
                            public void onSuccess(Collection<CompetitorDTO> result) {
                                // add competitors that are already registered
                                move(allCompetitorsTable, registeredCompetitorsTable, result);
                            }

                            @Override
                            public void onFailure(Throwable reason) {
                                errorReporter.reportError("Could not load already registered competitors: "
                                        + reason.getMessage());
                            }
                        });
            }

            @Override
            public void onFailure(Throwable reason) {
            }
        });
    }

    @Override
    protected void save() {
        final Set<CompetitorDTO> registeredCompetitors = new HashSet<>();
        Util.addAll(registeredCompetitorsTable.getAllCompetitors(), registeredCompetitors);
        competitorRegistrationsHandler.setRegisteredCompetitors(registeredCompetitors);
        hide();
    }
}
