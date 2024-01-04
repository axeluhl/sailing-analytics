package com.sap.sailing.gwt.ui.datamining.presentation;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.TackTypeSegmentsDataMiningSettings;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.dialog.DoubleBox;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * Settings dialog for maneuver settings.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class TackTypeSegmentsDataMiningSettingsDialogComponent implements SettingsDialogComponent<TackTypeSegmentsDataMiningSettings> {

    private TackTypeSegmentsDataMiningSettings settings;
    private StringMessages stringMessages;
    private DoubleBox minimumDurationBetweenAdjacentTackTypeSegmentsInSecondsBox;
    private DoubleBox minimumTackTypeSegmentDurationInSecondsBox;

    public TackTypeSegmentsDataMiningSettingsDialogComponent(TackTypeSegmentsDataMiningSettings settings) {
        this.settings = settings;
        this.stringMessages = StringMessages.INSTANCE;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();
        Grid grid = new Grid(2, 2);
        grid.setCellPadding(5);
        vp.add(grid);
        setupGrid(grid, dialog);
        return vp;
    }

    private void setupGrid(Grid grid, DataEntryDialog<?> dialog) {
        final Label minDurationBetweenTackTypeSegmentsLabel = dialog.createLabel(stringMessages.minimumDurationBetweenAdjacentTackTypeSegmentsInSeconds());
        grid.setWidget(0, 0, minDurationBetweenTackTypeSegmentsLabel);
        minimumDurationBetweenAdjacentTackTypeSegmentsInSecondsBox = dialog.createDoubleBox(
                settings.getMinimumTackTypeSegmentDuration() == null ? null : settings.getMinimumDurationBetweenAdjacentTackTypeSegments().asSeconds(), 10);
        grid.setWidget(0, 1, minimumDurationBetweenAdjacentTackTypeSegmentsInSecondsBox);

        final Label minTackTypeSegmentsDurationLabel = dialog.createLabel(stringMessages.minimumTackTypeSegmentsDurationInSeconds());
        grid.setWidget(1, 0, minTackTypeSegmentsDurationLabel);
        minimumTackTypeSegmentDurationInSecondsBox = dialog.createDoubleBox(
                settings.getMinimumTackTypeSegmentDuration() == null ? null : settings.getMinimumTackTypeSegmentDuration().asSeconds(), 10);
        grid.setWidget(1, 1, minimumTackTypeSegmentDurationInSecondsBox);
    }

    @Override
    public TackTypeSegmentsDataMiningSettings getResult() {
        return new TackTypeSegmentsDataMiningSettings(
                getDurationFromSeconds(minimumDurationBetweenAdjacentTackTypeSegmentsInSecondsBox.getValue()),
                getDurationFromSeconds(minimumTackTypeSegmentDurationInSecondsBox.getValue()));
    }

    private Duration getDurationFromSeconds(Double seconds) {
        return seconds == null ? null : new MillisecondsDurationImpl((long) (seconds * 1000));
    }

    @Override
    public FocusWidget getFocusWidget() {
        return minimumDurationBetweenAdjacentTackTypeSegmentsInSecondsBox;
    }

    @Override
    public Validator<TackTypeSegmentsDataMiningSettings> getValidator() {
        return new Validator<TackTypeSegmentsDataMiningSettings>() {
            @Override
            public String getErrorMessage(TackTypeSegmentsDataMiningSettings valueToValidate) {
                final String result;
                if (valueToValidate.getMinimumDurationBetweenAdjacentTackTypeSegments() != null &&
                        valueToValidate.getMinimumDurationBetweenAdjacentTackTypeSegments().compareTo(Duration.NULL) < 0) {
                    result = stringMessages.errorMinimumDurationBetweenAdjacentTackTypeSegmentsMustNotBeNegative();
                } else if (valueToValidate.getMinimumTackTypeSegmentDuration() != null &&
                        valueToValidate.getMinimumTackTypeSegmentDuration().compareTo(Duration.NULL) < 0) {
                    result = stringMessages.errorMinimumTackTypeSegmentDurationMustNotBeNegative();
                } else {
                    result = null;
                }
                return result;
            }
        };
    }

}
