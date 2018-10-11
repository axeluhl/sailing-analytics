package com.sap.sailing.gwt.ui.raceboard;

import java.util.UUID;
import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.ChartZoomChangedEvent;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChart;
import com.sap.sailing.gwt.ui.client.shared.race.TrackedRaceCreationResultDialog;
import com.sap.sailing.gwt.ui.shared.SliceRacePreperationDTO;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;

/**
 * Encapsulates the UI to slice a race from an existing race.
 * The slice functionality is added as an extra action to a given {@link MultiCompetitorRaceChart}.
 */
public class SliceRaceHandler {
    interface Resources extends ClientBundle {
        @Source("com/sap/sailing/gwt/ui/client/images/close_time_range.png")
        ImageResource sliceButton();
        
        @Source("SliceRaceHandler.gss")
        Styles style();
    }
    
    interface Styles extends CssResource {
        String sliceButtonBackgroundImage();
    }
    
    private final Styles styles = GWT.<Resources>create(Resources.class).style();
    
    private final Button sliceButtonUi;

    private TimeRange visibleRange;

    private final String leaderboardGroupName;

    private final String leaderboardName;

    private final UUID eventId;

    private final SailingServiceAsync sailingService;

    private final UserService userService;

    private final ErrorReporter errorReporter;

    private final RegattaAndRaceIdentifier selectedRaceIdentifier;
    
    private boolean canSlice = false;

    /**
     * Registers this handler as a zoom event handler on the {@code competitorRaceChart}.
     */
    public SliceRaceHandler(SailingServiceAsync sailingService, UserService userService, final ErrorReporter errorReporter,
            MultiCompetitorRaceChart competitorRaceChart, RegattaAndRaceIdentifier selectedRaceIdentifier,
            final String leaderboardGroupName, String leaderboardName, UUID eventId) {
        this.sailingService = sailingService;
        this.userService = userService;
        this.errorReporter = errorReporter;
        this.selectedRaceIdentifier = selectedRaceIdentifier;
        this.leaderboardGroupName = leaderboardGroupName;
        this.leaderboardName = leaderboardName;
        this.eventId = eventId;
        styles.ensureInjected();
        sliceButtonUi = new Button();
        sliceButtonUi.setStyleName(styles.sliceButtonBackgroundImage());
        sliceButtonUi.setTitle(StringMessages.INSTANCE.sliceRace());
        competitorRaceChart.addToolbarButton(sliceButtonUi);
        sliceButtonUi.setVisible(false);
        competitorRaceChart.addChartZoomChangedHandler(this::checkIfMaySliceSelectedRegattaAndRace);
        competitorRaceChart.addChartZoomResetHandler(e -> {
            visibleRange = null;
            updateVisibility();
        });
        sliceButtonUi.addClickHandler((e) -> doSlice());
        sailingService.canSliceRace(selectedRaceIdentifier, new AsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                canSlice = Boolean.TRUE.equals(result);
                updateVisibility();
            }

            @Override
            public void onFailure(Throwable caught) {
                canSlice = false;
                updateVisibility();
            }
        });
        final UserStatusEventHandler userStatusEventHandler = (user, preAuthenticated) -> {
            updateVisibility();
        };
        sliceButtonUi.addAttachHandler(e -> {
            if (e.isAttached()) {
                userService.addUserStatusEventHandler(userStatusEventHandler);
            } else {
                userService.removeUserStatusEventHandler(userStatusEventHandler);
            }
        });
    }
    
    private void updateVisibility() {
        sliceButtonUi.setVisible(canSlice && visibleRange != null && allowsEditing());
    }
    
    private boolean allowsEditing() {
        return userService.hasPermission(SecuredDomainType.REGATTA.getStringPermissionForObjects(DefaultActions.UPDATE, selectedRaceIdentifier.getRegattaName()))
                && userService.hasPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
    }

    private void doSlice() {
        if (visibleRange != null) {
            sailingService.prepareForSlicingOfRace(selectedRaceIdentifier,
                    new AsyncCallback<SliceRacePreperationDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(StringMessages.INSTANCE.errorWhilePreparingToSliceARace());
                        }

                        @Override
                        public void onSuccess(SliceRacePreperationDTO sliceRacePreperatioData) {
                            new SlicedRaceNameDialog(sliceRacePreperatioData, slicedRaceName -> {
                                sailingService.sliceRace(selectedRaceIdentifier, slicedRaceName, visibleRange.from(),
                                        visibleRange.to(), new AsyncCallback<RegattaAndRaceIdentifier>() {

                                            @Override
                                            public void onFailure(Throwable caught) {
                                                errorReporter.reportError(
                                                        StringMessages.INSTANCE.errorWhileSlicingARace(),
                                                        caught.getMessage());
                                            }

                                            @Override
                                            public void onSuccess(RegattaAndRaceIdentifier result) {
                                                new TrackedRaceCreationResultDialog(StringMessages.INSTANCE.sliceRace(),
                                                        StringMessages.INSTANCE.slicingARaceWasSuccessful(), eventId,
                                                        result.getRegattaName(), result.getRaceName(), leaderboardName,
                                                        leaderboardGroupName).show();
                                            }
                                        });
                            }).show();
                        }
                    });
        }
    }

    private void checkIfMaySliceSelectedRegattaAndRace(final ChartZoomChangedEvent e) {
        visibleRange = new TimeRangeImpl(new MillisecondsTimePoint(e.getRangeStart()),
                new MillisecondsTimePoint(e.getRangeEnd()));
        updateVisibility();
    }

    private static class SlicedRaceNameDialog extends DataEntryDialog<String> {

        private final TextBox raceNameInput;

        public SlicedRaceNameDialog(final SliceRacePreperationDTO sliceRacePreperatioData,
                final Consumer<String> okCallback) {
            super(StringMessages.INSTANCE.sliceRace(), StringMessages.INSTANCE.enterNameForSlicedRace(),
                    StringMessages.INSTANCE.ok(), StringMessages.INSTANCE.cancel(), new Validator<String>() {
                        @Override
                        public String getErrorMessage(String valueToValidate) {
                            if (valueToValidate == null || valueToValidate.isEmpty()) {
                                return StringMessages.INSTANCE.raceNameIsRequired();
                            }
                            if (sliceRacePreperatioData.getAlreadyUsedNames().contains(valueToValidate)) {
                                return StringMessages.INSTANCE.raceNameIsAlreadyUsed();
                            }
                            return null;
                        }
                    }, new DialogCallback<String>() {
                        @Override
                        public void ok(String editedObject) {
                            okCallback.accept(editedObject);
                        }

                        @Override
                        public void cancel() {
                        }
                    });
            raceNameInput = createTextBox(sliceRacePreperatioData.getProposedRaceName());
            raceNameInput.addAttachHandler(e -> {
                if (e.isAttached()) {
                    Scheduler.get().scheduleDeferred(() -> raceNameInput.setFocus(true));
                }
            });
        }

        @Override
        protected Widget getAdditionalWidget() {
            return raceNameInput;
        }

        @Override
        protected String getResult() {
            return raceNameInput.getValue().trim();
        }
    }
}
