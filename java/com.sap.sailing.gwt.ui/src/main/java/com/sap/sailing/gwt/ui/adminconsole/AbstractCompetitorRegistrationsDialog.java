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
import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.gwt.ui.adminconsole.CompetitorImportProviderSelectionDialog.MatchImportedCompetitorsDialogFactory;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.controls.busyindicator.BusyDisplay;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class AbstractCompetitorRegistrationsDialog extends DataEntryDialog<Set<CompetitorDTO>> implements BusyDisplay {
    protected CompetitorTableWrapper<RefreshableMultiSelectionModel<CompetitorDTO>> allCompetitorsTable;
    protected CompetitorTableWrapper<RefreshableMultiSelectionModel<CompetitorDTO>> registeredCompetitorsTable;
    protected final boolean filterByLeaderBoardInitially = false;
    final StringMessages stringMessages;
    protected final SailingServiceAsync sailingService;
    final ErrorReporter errorReporter;
    private boolean editable;
    private Button registerBtn;
    private Button unregisterBtn;
    private Button addCompetitorButton;
    private CheckBox showOnlyCompetitorsOfLogCheckBox;

    private String boatClass;
    
    protected String leaderboardName;
    private final BusyIndicator busyIndicator;
    private final ImportCompetitorCallback importCompetitorCallback;

    /**
     * @param boatClass
     *            The <code>boatClass</code> parameter describes the default shown boat class for new competitors. The
     *            <code>boatClass</code> parameter is <code>null</code>, if you want to edit a competitor or there is no
     *            boat class for the new competitor.
     */
    protected AbstractCompetitorRegistrationsDialog(final SailingServiceAsync sailingService,
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
        this.busyIndicator = new SimpleBusyIndicator();
        this.importCompetitorCallback = new RaceOrRegattaImportCompetitorCallback(this, sailingService, errorReporter, stringMessages);
    }

    @Override
    protected Widget getAdditionalWidget() {
        final FlowPanel mainPanel = new FlowPanel();
        final HorizontalPanel buttonPanel = new HorizontalPanel();
        addCompetitorButton = new Button(stringMessages.add(stringMessages.competitor()));
        addCompetitorButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                registeredCompetitorsTable.openEditCompetitorDialog(new CompetitorDTOImpl(), boatClass);
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
        final Button competitorImportButton = new Button(stringMessages.importCompetitors());
        competitorImportButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                sailingService.getCompetitorProviderNames(new AsyncCallback<Iterable<String>>() {
                    @Override
                    public void onSuccess(Iterable<String> providerNames) {
                        MatchImportedCompetitorsDialogFactory matchCompetitorsDialogFactory = getMatchCompetitorsDialogFactory(
                                sailingService, stringMessages, errorReporter);
                        CompetitorImportProviderSelectionDialog dialog = new CompetitorImportProviderSelectionDialog(
                                matchCompetitorsDialogFactory, AbstractCompetitorRegistrationsDialog.this, providerNames, sailingService,
                                stringMessages, errorReporter);
                        dialog.show();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter
                                .reportError(stringMessages.errorLoadingCompetitorImportProviders(caught.getMessage()));
                    }
                });
            }
        });
        final HorizontalPanel competitorRegistrationPanel = new HorizontalPanel();
        final CaptionPanel allCompetitorsPanel = new CaptionPanel(stringMessages.competitorPool());
        final CaptionPanel registeredCompetitorsPanel = new CaptionPanel(stringMessages.registeredCompetitors());
        allCompetitorsTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */
                true, /* enablePager */true);
        registeredCompetitorsTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */
                true, /* enablePager */false);
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
        buttonPanel.add(inviteCompetitorsButton);
        buttonPanel.add(competitorImportButton);
        buttonPanel.add(busyIndicator);
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
    
    private MatchImportedCompetitorsDialogFactory getMatchCompetitorsDialogFactory(
            final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter) {
        return new MatchImportedCompetitorsDialogFactory() {
            @Override
            public MatchImportedCompetitorsDialog createMatchImportedCompetitorsDialog(
                    final Iterable<CompetitorDescriptor> competitorDescriptors,
                    final Iterable<CompetitorDTO> competitors) {
                return new MatchImportedCompetitorsDialog(competitorDescriptors, competitors, stringMessages,
                        sailingService, errorReporter, importCompetitorCallback);
            }
        };
    }

    @Override
    public void setBusy(boolean isBusy) {
        busyIndicator.setBusy(isBusy);
    }

    public abstract void addAdditionalWidgets(FlowPanel mainPanel);

    protected void move(CompetitorTableWrapper<?> from, CompetitorTableWrapper<?> to, Iterable<CompetitorDTO> toMove) {
        from.getFilterField().removeAll(toMove);
        to.getFilterField().addAll(toMove);
    }
    
    protected void addImportedCompetitorsToRegisteredCompetitorsTableAndRemoveFromAllCompetitorsTable(Iterable<CompetitorDTO> competitorsImported) {
        allCompetitorsTable.getFilterField().removeAll(competitorsImported);
        registeredCompetitorsTable.getFilterField().addAll(competitorsImported);
    }

    private void moveSelected(CompetitorTableWrapper<RefreshableMultiSelectionModel<CompetitorDTO>> from,
            CompetitorTableWrapper<RefreshableMultiSelectionModel<CompetitorDTO>> to) {
        move(from, to, from.getSelectionModel().getSelectedSet());
        validate();
    }

    protected boolean showOnlyCompetitorsOfLog(){
        return showOnlyCompetitorsOfLogCheckBox.getValue();
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
        addCompetitorButton.setEnabled(false);
        registerBtn.setTitle(tooltip);
        unregisterBtn.setTitle(tooltip);
        addCompetitorButton.setTitle(tooltip);
        validate();
    }
    
    public void activateRegistrationButtons(){
        registerBtn.setEnabled(true);
        unregisterBtn.setEnabled(true);
        addCompetitorButton.setEnabled(true);
        registerBtn.setTitle("");
        unregisterBtn.setTitle("");
        addCompetitorButton.setTitle("");
        validate();
    }

    @Override
    protected Set<CompetitorDTO> getResult() {
        final Set<CompetitorDTO> registeredCompetitors = new HashSet<>();
        Util.addAll(registeredCompetitorsTable.getAllCompetitors(), registeredCompetitors);
        return registeredCompetitors;
    }
}
