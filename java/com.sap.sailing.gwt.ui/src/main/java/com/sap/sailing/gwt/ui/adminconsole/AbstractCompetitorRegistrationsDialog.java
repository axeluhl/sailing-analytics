package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class AbstractCompetitorRegistrationsDialog extends DataEntryDialog<Set<CompetitorDTO>> {
    protected CompetitorTableWrapper<RefreshableMultiSelectionModel<CompetitorDTO>> allCompetitorsTable;
    protected CompetitorTableWrapper<RefreshableMultiSelectionModel<CompetitorDTO>> registeredCompetitorsTable;
    protected final boolean filterByLeaderBoardInitially = false;
    final StringMessages stringMessages;
    protected final SailingServiceAsync sailingService;
    final ErrorReporter errorReporter;
    private boolean editable;
    private Button registerBtn;
    private Button unregisterBtn;
    private CheckBox showOnlyCompetitorsOfLogCheckBox;

    private String boatClass;
    
    protected String leaderboardName;

    /**
     * @param boatClass
     *            The <code>boatClass</code> parameter describes the default shown boat class for new competitors. The
     *            <code>boatClass</code> parameter is <code>null</code>, if you want to edit a competitor or there is no
     *            boat class for the new competitor.
     */
    public AbstractCompetitorRegistrationsDialog(final SailingServiceAsync sailingService,
            final StringMessages stringMessages, final ErrorReporter errorReporter, boolean editable,
            DialogCallback<Set<CompetitorDTO>> callback, String leaderboardName, String boatClass,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator<Set<CompetitorDTO>> validator) {
        super(stringMessages.registerCompetitors(), /* messsage */null, stringMessages.save(), stringMessages.cancel(), validator, callback);
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.editable = editable;
        this.leaderboardName = leaderboardName;
        this.boatClass = boatClass;
    }

    @Override
    protected Widget getAdditionalWidget() {
        FlowPanel mainPanel = new FlowPanel();
        HorizontalPanel buttonPanel = new HorizontalPanel();
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
                Set<CompetitorDTO> competitors = registeredCompetitorsTable.getSelectionModel().getSelectedSet();
                CompetitorInvitationHelper helper = new CompetitorInvitationHelper(sailingService, stringMessages,
                        errorReporter);
                helper.inviteCompetitors(competitors, leaderboardName);
            }
        });
        HorizontalPanel competitorRegistrationPanel = new HorizontalPanel();
        CaptionPanel allCompetitorsPanel = new CaptionPanel(stringMessages.competitorPool());
        CaptionPanel registeredCompetitorsPanel = new CaptionPanel(stringMessages.registeredCompetitors());
        allCompetitorsTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */
                true, /* enablePager */true);
        registeredCompetitorsTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */
                true, /* enablePager */true);
        allCompetitorsPanel.add(allCompetitorsTable);
        registeredCompetitorsPanel.add(registeredCompetitorsTable);
        VerticalPanel movePanel = new VerticalPanel();
        registerBtn = new Button("<");
        unregisterBtn = new Button(">");
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
        buttonPanel.add(addCompetitorButton);
        buttonPanel.add(editCompetitorButton);
        buttonPanel.add(inviteCompetitorsButton);
        mainPanel.add(buttonPanel);
        showOnlyCompetitorsOfLogCheckBox = new CheckBox(stringMessages.showOnlyCompetitorsOfLog());
        showOnlyCompetitorsOfLogCheckBox.setValue(false);
        showOnlyCompetitorsOfLogCheckBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (Window.confirm(stringMessages.confirmLosingCompetitorEditsWhenTogglingLogBasedView())) {
                    refreshCompetitors();
                } else {
                    showOnlyCompetitorsOfLogCheckBox.setValue(!showOnlyCompetitorsOfLogCheckBox.getValue());
                }
            }
        });
        mainPanel.add(showOnlyCompetitorsOfLogCheckBox);
        addAdditionalWidgets(mainPanel);
        mainPanel.add(competitorRegistrationPanel);
        refreshCompetitors();
        return mainPanel;
    }

    public abstract void addAdditionalWidgets(FlowPanel mainPanel);

    void move(CompetitorTableWrapper<?> from, CompetitorTableWrapper<?> to, Iterable<CompetitorDTO> toMove) {
        from.getFilterField().removeAll(toMove);
        to.getFilterField().addAll(toMove);
    }

    private void moveSelected(CompetitorTableWrapper<RefreshableMultiSelectionModel<CompetitorDTO>> from,
            CompetitorTableWrapper<RefreshableMultiSelectionModel<CompetitorDTO>> to) {
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
                }, boatClass).show();
    }
    
    protected boolean showOnlyCompetitorsOfLog(){
        return showOnlyCompetitorsOfLogCheckBox.getValue();
    }

    private void openEditCompetitorDialog() {
        // get currently selected competitor
        if (registeredCompetitorsTable.getSelectionModel().getSelectedSet().size() != 1) {
            // show some warning
        } else {
            final CompetitorDTO competitorToEdit = registeredCompetitorsTable.getSelectionModel().getSelectedSet()
                    .iterator().next();
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
                                    int editedCompetitorIndex = registeredCompetitorsTable.getDataProvider().getList()
                                            .indexOf(competitorToEdit);
                                    registeredCompetitorsTable.getDataProvider().getList().remove(competitorToEdit);
                                    registeredCompetitorsTable.getDataProvider().getList()
                                            .add(editedCompetitorIndex, updatedCompetitor);
                                    registeredCompetitorsTable.getDataProvider().refresh();
                                }
                            });
                        }

                        @Override
                        public void cancel() {
                        }
                    }, /* boatClass */null).show();
        }
    }

    protected void refreshCompetitors() {
        registeredCompetitorsTable.getFilterField().removeAll();
        allCompetitorsTable.getFilterField().removeAll();
        setRegisterableCompetitorsAndRegisteredCompetitors();
    }

    protected abstract void setRegisteredCompetitors();
    
    private void setRegisterableCompetitorsAndRegisteredCompetitors() {
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

    public void deactivateRegistrationButtons(String tooltip){
        registerBtn.setEnabled(false);
        unregisterBtn.setEnabled(false);
        registerBtn.setTitle(tooltip);
        unregisterBtn.setTitle(tooltip);
        validate();
    }
    
    public void activateRegistrationButtons(){
        registerBtn.setEnabled(true);
        unregisterBtn.setEnabled(true);
        registerBtn.setTitle("");
        unregisterBtn.setTitle("");
        validate();
    }

    @Override
    protected Set<CompetitorDTO> getResult() {
        final Set<CompetitorDTO> registeredCompetitors = new HashSet<>();
        Util.addAll(registeredCompetitorsTable.getAllCompetitors(), registeredCompetitors);
        return registeredCompetitors;
    }

}
