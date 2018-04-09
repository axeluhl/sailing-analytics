package com.sap.sailing.gwt.ui.datamining.presentation;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.ManeuverSettings;
import com.sap.sailing.datamining.shared.ManeuverSettingsImpl;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * Settings dialog for maneuver settings.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSettingsDialogComponent implements SettingsDialogComponent<ManeuverSettings> {

    private ManeuverSettings settings;
    private StringMessages stringMessages;
    private CheckBox mainCurveAnalysisBox;
    private DoubleBox minManeuverDurationInSecondsBox;
    private DoubleBox maxManeuverDurationInSecondsBox;
    private DoubleBox minManeuverEnteringSpeedBox;
    private DoubleBox maxManeuverEnteringSpeedBox;
    private DoubleBox minManeuverExitingSpeedBox;
    private DoubleBox maxManeuverExitingSpeedBox;
    private DoubleBox minManeuverEnteringAbsTWA;
    private DoubleBox maxManeuverEnteringAbsTWA;
    private DoubleBox minManeuverExitingAbsTWA;
    private DoubleBox maxManeuverExitingAbsTWA;
    private DoubleBox minAbsCourseChangeInDegrees;
    private DoubleBox maxAbsCourseChangeInDegrees;
    private DoubleBox minDurationInSecondsFromPrecedingManeuver;
    private DoubleBox maxDurationInSecondsFromPrecedingManeuver;
    private DoubleBox minDurationInSecondsToFollowingManeuver;
    private DoubleBox maxDurationInSecondsToFollowingManeuver;

    public ManeuverSettingsDialogComponent(ManeuverSettings settings) {
        this.settings = settings;
        this.stringMessages = StringMessages.INSTANCE;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();
        Grid grid = new Grid(17, 2);
        grid.setCellPadding(5);
        vp.add(grid);
        setupGrid(grid, dialog);
        return vp;
    }

    private void setupGrid(Grid grid, DataEntryDialog<?> dialog) {
        Label mainCurveAnalysisLabel = dialog.createLabel(stringMessages.mainCurveAnalysis());
        grid.setWidget(0, 0, mainCurveAnalysisLabel);
        mainCurveAnalysisBox = dialog.createCheckbox("");
        mainCurveAnalysisBox.setValue(settings.isMainCurveAnalysis());
        grid.setWidget(0, 1, mainCurveAnalysisBox);

        Label minManeuverDurationLabel = dialog.createLabel(stringMessages.minManeuverDuration());
        grid.setWidget(1, 0, minManeuverDurationLabel);
        minManeuverDurationInSecondsBox = dialog.createDoubleBox(
                settings.getMinManeuverDuration() == null ? null : settings.getMinManeuverDuration().asSeconds(), 10);
        grid.setWidget(1, 1, minManeuverDurationInSecondsBox);

        Label maxManeuverDurationLabel = dialog.createLabel(stringMessages.maxManeuverDuration());
        grid.setWidget(2, 0, maxManeuverDurationLabel);
        maxManeuverDurationInSecondsBox = dialog.createDoubleBox(
                settings.getMaxManeuverDuration() == null ? null : settings.getMaxManeuverDuration().asSeconds(), 10);
        grid.setWidget(2, 1, maxManeuverDurationInSecondsBox);

        Label minManeuverEnteringSpeedLabel = dialog.createLabel(stringMessages.minManeuverEnteringSpeedInKnots());
        grid.setWidget(3, 0, minManeuverEnteringSpeedLabel);
        minManeuverEnteringSpeedBox = dialog.createDoubleBox(settings.getMinManeuverEnteringSpeedInKnots(), 10);
        grid.setWidget(3, 1, minManeuverEnteringSpeedBox);

        Label maxManeuverEnteringSpeedLabel = dialog.createLabel(stringMessages.maxManeuverEnteringSpeedInKnots());
        grid.setWidget(4, 0, maxManeuverEnteringSpeedLabel);
        maxManeuverEnteringSpeedBox = dialog.createDoubleBox(settings.getMaxManeuverEnteringSpeedInKnots(), 10);
        grid.setWidget(4, 1, maxManeuverEnteringSpeedBox);

        Label minManeuverExitingSpeedLabel = dialog.createLabel(stringMessages.minManeuverExitingSpeedInKnots());
        grid.setWidget(5, 0, minManeuverExitingSpeedLabel);
        minManeuverExitingSpeedBox = dialog.createDoubleBox(settings.getMinManeuverExitingSpeedInKnots(), 10);
        grid.setWidget(5, 1, minManeuverExitingSpeedBox);

        Label maxManeuverExitingSpeedLabel = dialog.createLabel(stringMessages.maxManeuverExitingSpeedInKnots());
        grid.setWidget(6, 0, maxManeuverExitingSpeedLabel);
        maxManeuverExitingSpeedBox = dialog.createDoubleBox(settings.getMaxManeuverExitingSpeedInKnots(), 10);
        grid.setWidget(6, 1, maxManeuverExitingSpeedBox);

        Label minManeuverEnteringAbsTWALabel = dialog.createLabel(stringMessages.minManeuverEnteringAbsTWA());
        grid.setWidget(7, 0, minManeuverEnteringAbsTWALabel);
        minManeuverEnteringAbsTWA = dialog.createDoubleBox(settings.getMinManeuverEnteringAbsTWA(), 10);
        grid.setWidget(7, 1, minManeuverEnteringAbsTWA);

        Label maxManeuverEnteringAbsTWALabel = dialog.createLabel(stringMessages.maxManeuverEnteringAbsTWA());
        grid.setWidget(8, 0, maxManeuverEnteringAbsTWALabel);
        maxManeuverEnteringAbsTWA = dialog.createDoubleBox(settings.getMaxManeuverEnteringAbsTWA(), 10);
        grid.setWidget(8, 1, maxManeuverEnteringAbsTWA);

        Label minManeuverExitingAbsTWALabel = dialog.createLabel(stringMessages.minManeuverExitingAbsTWA());
        grid.setWidget(9, 0, minManeuverExitingAbsTWALabel);
        minManeuverExitingAbsTWA = dialog.createDoubleBox(settings.getMinManeuverExitingAbsTWA(), 10);
        grid.setWidget(9, 1, minManeuverExitingAbsTWA);

        Label maxManeuverExitingAbsTWALabel = dialog.createLabel(stringMessages.maxManeuverExitingAbsTWA());
        grid.setWidget(10, 0, maxManeuverExitingAbsTWALabel);
        maxManeuverExitingAbsTWA = dialog.createDoubleBox(settings.getMaxManeuverExitingAbsTWA(), 10);
        grid.setWidget(10, 1, maxManeuverExitingAbsTWA);

        Label minAbsCourseChangeInDegreesLabel = dialog.createLabel(stringMessages.minAbsCourseChangeInDegrees());
        grid.setWidget(11, 0, minAbsCourseChangeInDegreesLabel);
        minAbsCourseChangeInDegrees = dialog.createDoubleBox(settings.getMinAbsCourseChangeInDegrees(), 10);
        grid.setWidget(11, 1, minAbsCourseChangeInDegrees);

        Label maxAbsCourseChangeInDegreesLabel = dialog.createLabel(stringMessages.maxAbsCourseChangeInDegrees());
        grid.setWidget(12, 0, maxAbsCourseChangeInDegreesLabel);
        maxAbsCourseChangeInDegrees = dialog.createDoubleBox(settings.getMaxAbsCourseChangeInDegrees(), 10);
        grid.setWidget(12, 1, maxAbsCourseChangeInDegrees);

        Label minDurationInSecondsFromPrecedingManeuverLabel = dialog
                .createLabel(stringMessages.minDurationInSecondsFromPrecedingManeuver());
        grid.setWidget(13, 0, minDurationInSecondsFromPrecedingManeuverLabel);
        minDurationInSecondsFromPrecedingManeuver = dialog
                .createDoubleBox(settings.getMinDurationFromPrecedingManeuver() == null ? null
                        : settings.getMinDurationFromPrecedingManeuver().asSeconds(), 10);
        grid.setWidget(13, 1, minDurationInSecondsFromPrecedingManeuver);

        Label maxDurationInSecondsFromPrecedingManeuverLabel = dialog
                .createLabel(stringMessages.maxDurationInSecondsFromPrecedingManeuver());
        grid.setWidget(14, 0, maxDurationInSecondsFromPrecedingManeuverLabel);
        maxDurationInSecondsFromPrecedingManeuver = dialog
                .createDoubleBox(settings.getMaxDurationFromPrecedingManeuver() == null ? null
                        : settings.getMaxDurationFromPrecedingManeuver().asSeconds(), 10);
        grid.setWidget(14, 1, maxDurationInSecondsFromPrecedingManeuver);

        Label minDurationInSecondsToFollowingManeuverLabel = dialog
                .createLabel(stringMessages.minDurationInSecondsToFollowingManeuver());
        grid.setWidget(15, 0, minDurationInSecondsToFollowingManeuverLabel);
        minDurationInSecondsToFollowingManeuver = dialog
                .createDoubleBox(settings.getMinDurationToFollowingManeuver() == null ? null
                        : settings.getMinDurationToFollowingManeuver().asSeconds(), 10);
        grid.setWidget(15, 1, minDurationInSecondsToFollowingManeuver);

        Label maxDurationInSecondsToFollowingManeuverLabel = dialog
                .createLabel(stringMessages.maxDurationInSecondsToFollowingManeuver());
        grid.setWidget(16, 0, maxDurationInSecondsToFollowingManeuverLabel);
        maxDurationInSecondsToFollowingManeuver = dialog
                .createDoubleBox(settings.getMaxDurationToFollowingManeuver() == null ? null
                        : settings.getMaxDurationToFollowingManeuver().asSeconds(), 10);
        grid.setWidget(16, 1, maxDurationInSecondsToFollowingManeuver);
    }

    @Override
    public ManeuverSettings getResult() {
        return new ManeuverSettingsImpl(getDurationFromSeconds(minManeuverDurationInSecondsBox.getValue()),
                getDurationFromSeconds(maxManeuverDurationInSecondsBox.getValue()),
                minManeuverEnteringSpeedBox.getValue(), maxManeuverEnteringSpeedBox.getValue(),
                minManeuverExitingSpeedBox.getValue(), maxManeuverExitingSpeedBox.getValue(),
                minManeuverEnteringAbsTWA.getValue(), maxManeuverEnteringAbsTWA.getValue(),
                minManeuverExitingAbsTWA.getValue(), maxManeuverExitingAbsTWA.getValue(),
                mainCurveAnalysisBox.getValue(), minAbsCourseChangeInDegrees.getValue(),
                maxAbsCourseChangeInDegrees.getValue(),
                getDurationFromSeconds(minDurationInSecondsFromPrecedingManeuver.getValue()),
                getDurationFromSeconds(maxDurationInSecondsFromPrecedingManeuver.getValue()),
                getDurationFromSeconds(minDurationInSecondsToFollowingManeuver.getValue()),
                getDurationFromSeconds(maxDurationInSecondsToFollowingManeuver.getValue()));
    }

    private Duration getDurationFromSeconds(Double seconds) {
        return seconds == null ? null : new MillisecondsDurationImpl((long) (seconds * 1000));
    }

    @Override
    public FocusWidget getFocusWidget() {
        return minManeuverDurationInSecondsBox;
    }

    @Override
    public Validator<ManeuverSettings> getValidator() {
        return null;
    }

}
