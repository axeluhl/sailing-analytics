package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Set;

import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RaceLogCompetitorRegistrationDialog extends AbstractCompetitorRegistrationsDialog {

    private String fleetName;
    private String raceColumnName;
    private CheckBox competitorRegistrationInRaceLogCheckBox;

    public RaceLogCompetitorRegistrationDialog(String boatClass, SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, boolean editable, String leaderboardName, String raceColumnName, String fleetName,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<Set<CompetitorDTO>> callback) {
        super(sailingService, stringMessages, errorReporter, editable, callback, leaderboardName, boatClass);
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
        competitorRegistrationInRaceLogCheckBox = new CheckBox(stringMessages.registerCompetitorsOnRace());
        setupCompetitorRegistationsOnRaceCheckbox();
    }

    @Override
    protected void setRegisteredCompetitors() {
        sailingService.getCompetitorRegistrationsForRace(leaderboardName, raceColumnName, fleetName,
                new AsyncCallback<Collection<CompetitorDTO>>() {
                    @Override
                    public void onSuccess(Collection<CompetitorDTO> registeredCompetitors) {
                        move(allCompetitorsTable, registeredCompetitorsTable, registeredCompetitors);
                    }

                    @Override
                    public void onFailure(Throwable reason) {
                        errorReporter.reportError("Could not load already registered competitors: "
                                + reason.getMessage());
                    }
                });
    }

    @Override
    public void addAdditionalWidgets(FlowPanel mainPanel) {
        mainPanel.add(competitorRegistrationInRaceLogCheckBox);
    }

    private void setupCompetitorRegistationsOnRaceCheckbox() {
        sailingService.areCompetitorRegistrationsEnabledForRace(leaderboardName, raceColumnName, fleetName,
                new AsyncCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isEnabled) {
                        competitorRegistrationInRaceLogCheckBox.setValue(isEnabled);
                        if (isEnabled) {
                            activateRegistrationButtons();
                        } else {
                            deactivateRegistrationButtons(stringMessages.competitorRegistrationsOnRaceDisabled());
                        }
                    }

                    @Override
                    public void onFailure(Throwable reason) {
                        errorReporter.reportError("Could not load already registered competitors: "
                                + reason.getMessage());
                    }
                });
        competitorRegistrationInRaceLogCheckBox.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                String title;
                String message;

                if (competitorRegistrationInRaceLogCheckBox.getValue()) {
                    title = stringMessages.doYouWantToRegisterCompetitorsDirectlyOnTheRace();
                    message = stringMessages.warningDirectCompetitorRegistration();
                } else {
                    title = stringMessages.doYouWantToDisableCompetitorsRegistrationsOnTheRace();
                    message = stringMessages.warningRegattaCompetitorRegistration();
                }

                new DataEntryDialog<Void>(title, message, stringMessages.ok(), stringMessages.cancel(), null, false,
                        new DialogCallback<Void>() {
                            @Override
                            public void ok(Void editedObject) {
                                if (competitorRegistrationInRaceLogCheckBox.getValue()) {
                                    sailingService.enableCompetitorRegistrationsForRace(leaderboardName,
                                            raceColumnName, fleetName, new AsyncCallback<Void>() {
                                                @Override
                                                public void onSuccess(Void isEnabled) {
                                                    competitorRegistrationInRaceLogCheckBox.setValue(true);
                                                    activateRegistrationButtons();
                                                    setRegisteredCompetitors();
                                                }

                                                @Override
                                                public void onFailure(Throwable reason) {
                                                    errorReporter
                                                            .reportError("Could not enable competitor registrations for race: "
                                                                    + reason.getMessage());
                                                }
                                            });

                                } else {
                                    sailingService.disableCompetitorRegistrationsForRace(leaderboardName,
                                            raceColumnName, fleetName, new AsyncCallback<Void>() {
                                                @Override
                                                public void onSuccess(Void isEnabled) {
                                                    competitorRegistrationInRaceLogCheckBox.setValue(false);
                                                    deactivateRegistrationButtons(stringMessages
                                                            .competitorRegistrationsOnRaceDisabled());
                                                    setRegisteredCompetitors();
                                                }

                                                @Override
                                                public void onFailure(Throwable reason) {
                                                    errorReporter
                                                            .reportError("Could not deactivate competitor registrations for race: "
                                                                    + reason.getMessage());
                                                }
                                            });
                                }
                            }

                            @Override
                            public void cancel() {
                            }
                        }) {
                    @Override
                    protected Void getResult() {
                        return null;
                    }
                }.show();
            }
        });
    }

    @Override
    protected void setRegisterableCompetitors() {
        allCompetitorsTable.refreshCompetitorList(leaderboardName, new Callback<Iterable<CompetitorDTO>, Throwable>() {
            @Override
            public void onSuccess(Iterable<CompetitorDTO> result) {
                setRegisteredCompetitors();
            }
            
            @Override
            public void onFailure(Throwable reason) {
            }
        });
    }
}
