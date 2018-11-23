package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

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
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTOImpl;
import com.sap.sailing.domain.common.dto.CompetitorWithToolTipDTO;
import com.sap.sailing.gwt.ui.adminconsole.CompetitorImportProviderSelectionDialog.MatchImportedCompetitorsDialogFactory;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.controls.busyindicator.BusyDisplay;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.security.ui.client.UserService;

/**
 * Shows two competitor tables next to each other; the table on the left is that of the "registered" competitors, the table
 * on the right is the "pool" of all competitors with those already "registered" removed. Between the tables are buttons
 * to move competitors left and right, thereby assigning them to the "registered" set or moving them back to the pool.
 * The {@link #getResult result} consists of all {@link CompetitorDTO competitors} in the table of "registered" competitors.<p>
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class CompetitorRegistrationsPanel extends FlowPanel implements BusyDisplay {
    private final CompetitorTableWrapper<RefreshableMultiSelectionModel<CompetitorDTO>> allCompetitorsTable;
    private final CompetitorTableWrapper<RefreshableMultiSelectionModel<CompetitorDTO>> registeredCompetitorsTable;
    private final ErrorReporter errorReporter;
    private Button registerBtn;
    private Button unregisterBtn;
    private Button addCompetitorWithBoatButton;
    private Button addCompetitorButton;
    private CheckBox showOnlyCompetitorsOfLogCheckBox;
    private final String leaderboardName;
    private final BusyIndicator busyIndicator;
    private final ImportCompetitorCallback importCompetitorCallback;
    private final Runnable validator;
    private final Consumer<AsyncCallback<Collection<CompetitorDTO>>> registeredCompetitorsRetriever;
    private final boolean restrictPoolToLeaderboard;

    /**
     * @param boatClass
     *            The <code>boatClass</code> parameter describes the default shown boat class for new competitors. The
     *            <code>boatClass</code> parameter is <code>null</code>, if you want to edit a competitor or there is no
     *            boat class for the new competitor.
     * @param validator
     *            when this panel is used to modify the resulting competitor set, this callback's {@link Runnable#run}
     *            method is invoked if a non-{@code null} value is passed here.
     * @param registeredCompetitorsRetriever
     *            although declared as a consumer, this is a provider of the set of competitors to be shown in the
     *            "registered" competitors table. Technically, it "consumes" a callback to which to pass the competitors
     *            retrieved as the "registered" ones.
     * @param restrictPoolToLeaderboard
     *            whether the pool of "all" competitors is to be restricted to those obtained from the leaderboard, or
     *            to all competitors in the server's competitor store
     * @param additionalWidgetsBeforeTables widgets to be inserted above / before the competitor tables; may be {@code null} or empty
     */
    protected CompetitorRegistrationsPanel(final SailingServiceAsync sailingService, final UserService userService,
            final StringMessages stringMessages, final ErrorReporter errorReporter, boolean editable,
            String leaderboardName, boolean canBoatsOfCompetitorsChangePerRace, String boatClass, Runnable validator,
            Consumer<AsyncCallback<Collection<CompetitorDTO>>> registeredCompetitorsRetriever,
            boolean restrictPoolToLeaderboard, Widget... additionalWidgetsBeforeTables) {
        this.errorReporter = errorReporter;
        this.restrictPoolToLeaderboard = restrictPoolToLeaderboard;
        this.validator = validator;
        this.leaderboardName = leaderboardName;
        this.busyIndicator = new SimpleBusyIndicator();
        this.registeredCompetitorsRetriever = registeredCompetitorsRetriever;
        this.importCompetitorCallback = new RaceOrRegattaImportCompetitorCallback(this, sailingService, errorReporter, stringMessages);
        final HorizontalPanel buttonPanel = new HorizontalPanel();
        addCompetitorButton = new Button(stringMessages.add(stringMessages.competitor()));
        addCompetitorButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                CompetitorWithBoatDTOImpl competitorDTO = new CompetitorWithBoatDTOImpl();
                competitorDTO.setBoat(null);
                registeredCompetitorsTable.openEditCompetitorWithoutBoatDialog(competitorDTO);
            }
        });
        addCompetitorWithBoatButton = new Button(stringMessages.add(stringMessages.competitorWithBoat()));
        addCompetitorWithBoatButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                CompetitorWithBoatDTOImpl competitorDTO = new CompetitorWithBoatDTOImpl();
                registeredCompetitorsTable.openEditCompetitorWithBoatDialog(competitorDTO, boatClass);
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
                                sailingService, userService, stringMessages, errorReporter);
                        CompetitorImportProviderSelectionDialog dialog = new CompetitorImportProviderSelectionDialog(
                                matchCompetitorsDialogFactory, CompetitorRegistrationsPanel.this, providerNames, sailingService,
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
        allCompetitorsTable = new CompetitorTableWrapper<>(sailingService, userService, stringMessages, errorReporter, /* multiSelection */
                true, /* enablePager */true, /* filterCompetitorWithBoat */ canBoatsOfCompetitorsChangePerRace, /* filterCompetitorsWithoutBoat */ !canBoatsOfCompetitorsChangePerRace);
        registeredCompetitorsTable = new CompetitorTableWrapper<>(sailingService, userService, stringMessages, errorReporter, /* multiSelection */
                true, /* enablePager */false,  /* filterCompetitorWithBoat */ false, /* filterCompetitorsWithoutBoat */ false);
        registeredCompetitorsTable.getSelectionModel().addSelectionChangeHandler(event -> validateAndUpdate());
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
        if (canBoatsOfCompetitorsChangePerRace) {
            buttonPanel.add(addCompetitorButton);
        } else {
            buttonPanel.add(addCompetitorWithBoatButton);
        }
        buttonPanel.add(inviteCompetitorsButton);
        buttonPanel.add(competitorImportButton);
        buttonPanel.add(busyIndicator);
        this.add(buttonPanel);
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
        this.add(showOnlyCompetitorsOfLogCheckBox);
        if (additionalWidgetsBeforeTables != null) {
            for (final Widget additionalWidget : additionalWidgetsBeforeTables) {
                add(additionalWidget);
            }
        }
        this.add(competitorRegistrationPanel);
        refreshCompetitors();
    }
    
    private MatchImportedCompetitorsDialogFactory getMatchCompetitorsDialogFactory(
            final SailingServiceAsync sailingService, final UserService userService, final StringMessages stringMessages,
            final ErrorReporter errorReporter) {
        return new MatchImportedCompetitorsDialogFactory() {
            @Override
            public MatchImportedCompetitorsDialog createMatchImportedCompetitorsDialog(
                    final Iterable<CompetitorDescriptor> competitorDescriptors,
                    final Iterable<CompetitorDTO> competitors) {
                return new MatchImportedCompetitorsDialog(competitorDescriptors, competitors, stringMessages,
                        sailingService, userService, errorReporter, importCompetitorCallback);
            }
        };
    }

    @Override
    public void setBusy(boolean isBusy) {
        busyIndicator.setBusy(isBusy);
    }

    protected void move(CompetitorTableWrapper<?> from, CompetitorTableWrapper<?> to, Iterable<CompetitorDTO> toMove) {
        from.getFilterField().removeAll(toMove);
        to.getFilterField().addAll(toMove);
    }
    
    protected void addImportedCompetitorsToRegisteredCompetitorsTableAndRemoveFromAllCompetitorsTable(Iterable<CompetitorDTO> competitorsImported) {
        allCompetitorsTable.getFilterField().removeAll(competitorsImported);
        registeredCompetitorsTable.getFilterField().addAll(competitorsImported);
        validateAndUpdate();
    }

    private void moveSelected(CompetitorTableWrapper<RefreshableMultiSelectionModel<CompetitorDTO>> from,
            CompetitorTableWrapper<RefreshableMultiSelectionModel<CompetitorDTO>> to) {
        move(from, to, from.getSelectionModel().getSelectedSet());
        validateAndUpdate();
    }

    protected boolean showOnlyCompetitorsOfLog(){
        return showOnlyCompetitorsOfLogCheckBox.getValue();
    }

    protected void refreshCompetitors() {
        registeredCompetitorsTable.getFilterField().removeAll();
        allCompetitorsTable.getFilterField().removeAll();
        setRegisterableCompetitorsAndRegisteredCompetitors();
    }

    private void setRegisterableCompetitorsAndRegisteredCompetitors() {
        allCompetitorsTable.refreshCompetitorList(this.restrictPoolToLeaderboard ? leaderboardName : null, new Callback<Iterable<CompetitorDTO>, Throwable>() {
            @Override
            public void onSuccess(Iterable<CompetitorDTO> result) {
                registeredCompetitorsRetriever.accept(new AsyncCallback<Collection<CompetitorDTO>>() {
                    @Override
                    public void onSuccess(Collection<CompetitorDTO> registeredCompetitors) {
                        moveFromPoolToRegistered(registeredCompetitors);
                        validateAndUpdate();
                    }

                    @Override
                    public void onFailure(Throwable reason) {
                        errorReporter.reportError("Could not load already registered competitors: " + reason.getMessage());
                    }
                });
            }
    
            @Override
            public void onFailure(Throwable reason) {
                errorReporter.reportError("Could not load pool of competitors: " + reason.getMessage());
            }
        });
    }

    public void deactivateRegistrationButtons(String tooltip){
        registerBtn.setEnabled(false);
        unregisterBtn.setEnabled(false);
        addCompetitorWithBoatButton.setEnabled(false);
        addCompetitorButton.setEnabled(false);
        registerBtn.setTitle(tooltip);
        unregisterBtn.setTitle(tooltip);
        addCompetitorWithBoatButton.setTitle(tooltip);
        addCompetitorButton.setTitle(tooltip);
        validateAndUpdate();
    }
    
    private void validateAndUpdate() {
        if (validator != null) {
            validator.run();
        }
    }
    
    public void activateRegistrationButtons(){
        registerBtn.setEnabled(true);
        unregisterBtn.setEnabled(true);
        addCompetitorWithBoatButton.setEnabled(true);
        addCompetitorButton.setEnabled(true);
        registerBtn.setTitle("");
        unregisterBtn.setTitle("");
        addCompetitorWithBoatButton.setTitle("");
        addCompetitorButton.setTitle("");
        validateAndUpdate();
    }

    public Set<CompetitorDTO> getResult() {
        final Set<CompetitorDTO> registeredCompetitors = new HashSet<>();
        Util.addAll(registeredCompetitorsTable.getAllCompetitors(), registeredCompetitors);
        return registeredCompetitors;
    }

    public void grayOutCompetitorsFromRegistered(List<CompetitorWithToolTipDTO> competitors) {
        registeredCompetitorsTable.grayOutCompetitors(competitors);
    }

    public void grayOutCompetitorsFromPool(List<CompetitorWithToolTipDTO> competitors) {
        allCompetitorsTable.grayOutCompetitors(competitors);
    }

    public void moveFromPoolToRegistered(Collection<CompetitorDTO> registeredCompetitors) {
        move(allCompetitorsTable, registeredCompetitorsTable, registeredCompetitors);
    }
    
    public Set<CompetitorDTO> getSelectedRegisteredCompetitors() {
        return registeredCompetitorsTable.getSelectionModel().getSelectedSet();
    }
}
