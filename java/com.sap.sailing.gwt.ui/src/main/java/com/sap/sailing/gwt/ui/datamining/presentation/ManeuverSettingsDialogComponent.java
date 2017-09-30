package com.sap.sailing.gwt.ui.datamining.presentation;

import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.ManeuverSettings;
import com.sap.sailing.datamining.shared.ManeuverSettingsImpl;
import com.sap.sailing.gwt.ui.client.StringMessages;
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
    private DoubleBox minManeuverDurationBox;
    private DoubleBox maxManeuverDurationBox;
    private DoubleBox minManeuverEnteringSpeedBox;
    private DoubleBox maxManeuverEnteringSpeedBox;
    private DoubleBox minManeuverExitingSpeedBox;
    private DoubleBox maxManeuverExitingSpeedBox;

    public ManeuverSettingsDialogComponent(ManeuverSettings settings) {
        this.settings = settings;
        this.stringMessages = StringMessages.INSTANCE;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();
        Grid grid = new Grid(6, 2);
        grid.setCellPadding(5);
        vp.add(grid);
        setupGrid(grid, dialog);
        return vp;
    }

    private void setupGrid(Grid grid, DataEntryDialog<?> dialog) {
        Label minManeuverDurationLabel = dialog.createLabel(stringMessages.minManeuverDuration());
        grid.setWidget(0, 0, minManeuverDurationLabel);
        minManeuverDurationBox = dialog.createDoubleBox(settings.getMinManeuverDuration(), 10);
        grid.setWidget(0, 1, minManeuverDurationBox);

        Label maxManeuverDurationLabel = dialog.createLabel(stringMessages.maxManeuverDuration());
        grid.setWidget(1, 0, maxManeuverDurationLabel);
        maxManeuverDurationBox = dialog.createDoubleBox(settings.getMaxManeuverDuration(), 10);
        grid.setWidget(1, 1, maxManeuverDurationBox);

        Label minManeuverEnteringSpeedLabel = dialog.createLabel(stringMessages.minManeuverEnteringSpeedInKnots());
        grid.setWidget(2, 0, minManeuverEnteringSpeedLabel);
        minManeuverEnteringSpeedBox = dialog.createDoubleBox(settings.getMinManeuverEnteringSpeedInKnots(), 10);
        grid.setWidget(2, 1, minManeuverEnteringSpeedBox);

        Label maxManeuverEnteringSpeedLabel = dialog.createLabel(stringMessages.maxManeuverEnteringSpeedInKnots());
        grid.setWidget(3, 0, maxManeuverEnteringSpeedLabel);
        maxManeuverEnteringSpeedBox = dialog.createDoubleBox(settings.getMaxManeuverEnteringSpeedInKnots(), 10);
        grid.setWidget(3, 1, maxManeuverEnteringSpeedBox);

        Label minManeuverExitingSpeedLabel = dialog.createLabel(stringMessages.minManeuverExitingSpeedInKnots());
        grid.setWidget(4, 0, minManeuverExitingSpeedLabel);
        minManeuverExitingSpeedBox = dialog.createDoubleBox(settings.getMinManeuverExitingSpeedInKnots(), 10);
        grid.setWidget(4, 1, minManeuverExitingSpeedBox);

        Label maxManeuverExitingSpeedLabel = dialog.createLabel(stringMessages.maxManeuverExitingSpeedInKnots());
        grid.setWidget(5, 0, maxManeuverExitingSpeedLabel);
        maxManeuverExitingSpeedBox = dialog.createDoubleBox(settings.getMaxManeuverExitingSpeedInKnots(), 10);
        grid.setWidget(5, 1, maxManeuverExitingSpeedBox);
    }

    @Override
    public ManeuverSettings getResult() {
        return new ManeuverSettingsImpl(minManeuverDurationBox.getValue(), maxManeuverDurationBox.getValue(),
                minManeuverEnteringSpeedBox.getValue(), maxManeuverEnteringSpeedBox.getValue(),
                minManeuverExitingSpeedBox.getValue(), maxManeuverExitingSpeedBox.getValue());
    }

    @Override
    public FocusWidget getFocusWidget() {
        return minManeuverDurationBox;
    }

    @Override
    public Validator<ManeuverSettings> getValidator() {
        return null;
    }

}
