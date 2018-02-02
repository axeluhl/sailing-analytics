package com.sap.sailing.gwt.ui.raceboard;

import java.util.UUID;
import java.util.function.Consumer;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.ChartCssResources;
import com.sap.sailing.gwt.ui.client.shared.charts.ChartZoomChangedEvent;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChart;
import com.sap.sailing.gwt.ui.client.shared.race.TrackedRaceCreationResultDialog;
import com.sap.sailing.gwt.ui.shared.SliceRacePreperationDTO;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SliceRaceHandler {
    private final Button splitButtonUi;

    private TimeRange visibleRange;

    private final String leaderboardGroupName;

    private final String leaderboardName;

    private final UUID eventId;

    private final SailingServiceAsync sailingService;

    private final ErrorReporter errorReporter;

    private final RegattaAndRaceIdentifier selectedRaceIdentifier;

    public SliceRaceHandler(SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            MultiCompetitorRaceChart competitorRaceChart, RegattaAndRaceIdentifier selectedRaceIdentifier,
            final String leaderboardGroupName, String leaderboardName, UUID eventId) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.selectedRaceIdentifier = selectedRaceIdentifier;
        this.leaderboardGroupName = leaderboardGroupName;
        this.leaderboardName = leaderboardName;
        this.eventId = eventId;
        splitButtonUi = new Button();
        splitButtonUi.setStyleName(ChartCssResources.INSTANCE.css().sliceButtonBackgroundImage());
        splitButtonUi.setTitle(StringMessages.INSTANCE.sliceRace());
        competitorRaceChart.addToolbarButton(splitButtonUi);
        splitButtonUi.setVisible(false);
        competitorRaceChart.addChartZoomChangedHandler(this::checkIfMaySliceSelectedRegattaAndRace);
        competitorRaceChart.addChartZoomResetHandler(e -> splitButtonUi.setVisible(false));
        splitButtonUi.addClickHandler((e) -> doSlice());
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
                                                errorReporter
                                                        .reportError(StringMessages.INSTANCE.errorWhileSlicingARace());
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
        // TODO: we could cache result
        visibleRange = null;
        sailingService.canSliceRace(selectedRaceIdentifier, new AsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                boolean yesWeCan = Boolean.TRUE.equals(result);
                splitButtonUi.setVisible(yesWeCan);
                if (yesWeCan) {
                    visibleRange = new TimeRangeImpl(new MillisecondsTimePoint(e.getRangeStart()),
                            new MillisecondsTimePoint(e.getRangeEnd()));
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                splitButtonUi.setVisible(false);
            }
        });
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
