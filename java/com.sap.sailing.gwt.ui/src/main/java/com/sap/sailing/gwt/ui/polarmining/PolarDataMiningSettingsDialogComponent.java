package com.sap.sailing.gwt.ui.polarmining;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.polarsheets.WindSpeedSteppingConfiguratorPanel;
import com.sap.sailing.polars.datamining.shared.PolarDataMiningSettings;
import com.sap.sailing.polars.datamining.shared.PolarDataMiningSettingsImpl;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class PolarDataMiningSettingsDialogComponent implements SettingsDialogComponent<PolarDataMiningSettings> {

    private PolarDataMiningSettings settings;
    private StringMessages stringMessages;
    private IntegerBox minimumGraphDataSizeBox;
    private DoubleBox minimumWindConfidenceBox;
    private IntegerBox minimumDataCountPerAngleBox;
    private IntegerBox numberOfHistogramColumnsBox;
    private CheckBox useOnlyWindGaugesForWindSpeedBox;
    private CheckBox useOnlyEstimationForWindDirectionBox;
    private WindSpeedSteppingConfiguratorPanel windSteppingBox;
    private DoubleBox windSteppingMaxDistanceBox;

    public PolarDataMiningSettingsDialogComponent(PolarDataMiningSettings settings) {
        this.settings = settings;
        this.stringMessages = StringMessages.INSTANCE;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();
        Grid grid = new Grid(14, 2);
        grid.setCellPadding(5);
        vp.add(grid);
        setupGrid(grid, dialog);
        return vp;
    }

    private void setupGrid(Grid grid, DataEntryDialog<?> dialog) {
        Label minimumGraphDataSizeLabel = new Label(stringMessages.polarSheetMinimumDataSizePerGraph() + ":");
        minimumGraphDataSizeLabel.setTitle(stringMessages.polarSheetMinimumDataSizePerGraphTooltip());
        grid.setWidget(0, 0, minimumGraphDataSizeLabel);
        minimumGraphDataSizeBox = dialog.createIntegerBox(settings.getMinimumDataCountPerGraph(), 6);
        grid.setWidget(0, 1, minimumGraphDataSizeBox);
        Label minimumDataSizePerAngleLabel = new Label(stringMessages.polarSheetMinimumDataSizePerAngle() + ":");
        minimumDataSizePerAngleLabel.setTitle(stringMessages.polarSheetMinimumDataSizePerAngleTooltip());
        grid.setWidget(1, 0, minimumDataSizePerAngleLabel);
        minimumDataCountPerAngleBox = dialog.createIntegerBox(settings.getMinimumDataCountPerAngle(), 6);
        grid.setWidget(1, 1, minimumDataCountPerAngleBox);
        Label minimumWindConfidenceLabel = new Label(stringMessages.polarSheetMinimumWindConfidence() + ":");
        minimumWindConfidenceLabel.setTitle(stringMessages.polarSheetMinimumWindConfidenceTooltip());
        grid.setWidget(2, 0, minimumWindConfidenceLabel);
        minimumWindConfidenceBox = dialog.createDoubleBox(settings.getMinimumWindConfidence(), 6);
        grid.setWidget(2, 1, minimumWindConfidenceBox);
        Label useOnlyWindGaugesForWindSpeedLabel = new Label(stringMessages.polarSheetUseOnlyWindGaugeData() + ":");
        useOnlyWindGaugesForWindSpeedLabel.setTitle(stringMessages.polarSheetUseOnlyWindGaugeDataTooltip());
        grid.setWidget(4, 0, useOnlyWindGaugesForWindSpeedLabel);
        useOnlyWindGaugesForWindSpeedBox = dialog.createCheckbox("");
        useOnlyWindGaugesForWindSpeedBox.setValue(settings.useOnlyWindGaugesForWindSpeed());
        grid.setWidget(4, 1, useOnlyWindGaugesForWindSpeedBox);
        Label useOnlyEstimationForWindDirectionLabel = new Label(stringMessages.polarSheetUseOnlyEstimationData() + ":");
        useOnlyEstimationForWindDirectionLabel.setTitle(stringMessages.polarSheetUseOnlyEstimationDataTooltip());
        grid.setWidget(6, 0, useOnlyEstimationForWindDirectionLabel);
        useOnlyEstimationForWindDirectionBox = dialog.createCheckbox("");
        useOnlyEstimationForWindDirectionBox.setValue(settings.useOnlyEstimatedForWindDirection());
        grid.setWidget(6, 1, useOnlyEstimationForWindDirectionBox);
        grid.setWidget(10, 0, new Label(stringMessages.polarSheetNumberOfHistogramColumns() + ":"));
        numberOfHistogramColumnsBox = dialog.createIntegerBox(settings.getNumberOfHistogramColumns(), 3);
        grid.setWidget(10, 1, numberOfHistogramColumnsBox);
        grid.setWidget(11, 0, new Label(stringMessages.polarSheetWindSteppingInKnots() + ":"));
        windSteppingBox = new WindSpeedSteppingConfiguratorPanel(settings.getWindSpeedStepping());
        grid.setWidget(11, 1, windSteppingBox);
        Label windSteppingMaxDistanceLabel = new Label(stringMessages.polarSheetWindSteppingMaxDistance() + ":");
        windSteppingMaxDistanceLabel.setTitle(stringMessages.polarSheetWindSteppingMaxDistanceTooltip());
        grid.setWidget(12, 0, windSteppingMaxDistanceLabel);
        windSteppingMaxDistanceBox = dialog.createDoubleBox(settings.getWindSpeedStepping().getMaxDistance(), 6);
        grid.setWidget(12, 1, windSteppingMaxDistanceBox);
        grid.setWidget(13, 0, new Label(stringMessages.pleaseSeeToolTips()));
    }

    @Override
    public PolarDataMiningSettings getResult() {
        return new PolarDataMiningSettingsImpl(minimumGraphDataSizeBox.getValue(), minimumWindConfidenceBox.getValue(),
                minimumDataCountPerAngleBox.getValue(), numberOfHistogramColumnsBox.getValue(),
                useOnlyWindGaugesForWindSpeedBox.getValue(), useOnlyEstimationForWindDirectionBox.getValue(),
                windSteppingBox.getStepping(windSteppingMaxDistanceBox.getValue()));
    }

    @Override
    public FocusWidget getFocusWidget() {
        return minimumGraphDataSizeBox;
    }

    @Override
    public Validator<PolarDataMiningSettings> getValidator() {
        return new PolarDataMiningSettingsValidator(stringMessages);
    }

}
