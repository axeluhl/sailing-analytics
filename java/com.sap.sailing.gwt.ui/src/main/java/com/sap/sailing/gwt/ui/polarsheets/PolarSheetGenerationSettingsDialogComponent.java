package com.sap.sailing.gwt.ui.polarsheets;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationSettingsImpl;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class PolarSheetGenerationSettingsDialogComponent implements SettingsDialogComponent<PolarSheetGenerationSettings> {

    private PolarSheetGenerationSettings settings;
    private StringMessages stringMessages;
    private IntegerBox minimumGraphDataSizeBox;
    private DoubleBox minimumWindConfidenceBox;
    private IntegerBox minimumDataCountPerAngleBox;
    private IntegerBox numberOfHistogramColumnsBox;
    private DoubleBox minimumConfidenceMeasureBox;
    private CheckBox useOnlyWindGaugesForWindSpeedBox;
    private CheckBox shouldRemoveOutliersBox;
    private DoubleBox outlierRadiusBox;
    private DoubleBox outlierNeighborhoodPctBox;
    private CheckBox useOnlyEstimationForWindDirectionBox;
    private WindSpeedSteppingConfiguratorPanel windSteppingBox;
    private DoubleBox windSteppingMaxDistanceBox;
    private CheckBox splitByWindGaugesBox;

    public PolarSheetGenerationSettingsDialogComponent(PolarSheetGenerationSettings settings, StringMessages stringMessages) {
        this.settings = settings;
        this.stringMessages = stringMessages;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();
        Grid grid = new Grid(14,2);
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
        Label minimumConfidenceMeasureLabel = new Label(stringMessages.polarSheetMinimumConfidenceMeasure() + ":");
        minimumConfidenceMeasureLabel.setTitle(stringMessages.polarSheetMinimumConfidenceMeasureTooltip());
        grid.setWidget(3, 0, minimumConfidenceMeasureLabel);
        minimumConfidenceMeasureBox = dialog.createDoubleBox(settings.getMinimumConfidenceMeasure(), 6);
        grid.setWidget(3, 1, minimumConfidenceMeasureBox);
        Label useOnlyWindGaugesForWindSpeedLabel = new Label(stringMessages.polarSheetUseOnlyWindGaugeData() + ":");
        useOnlyWindGaugesForWindSpeedLabel.setTitle(stringMessages.polarSheetUseOnlyWindGaugeDataTooltip());
        grid.setWidget(4, 0, useOnlyWindGaugesForWindSpeedLabel);
        useOnlyWindGaugesForWindSpeedBox = dialog.createCheckbox("");
        useOnlyWindGaugesForWindSpeedBox.setValue(settings.useOnlyWindGaugesForWindSpeed());
        grid.setWidget(4, 1, useOnlyWindGaugesForWindSpeedBox);
        Label splitByWindGaugesLabel = new Label(stringMessages.polarSheetSplitByWindGauges() + ":");
        splitByWindGaugesLabel.setTitle(stringMessages.polarSheetSplitByWindGaugesTooltip());
        grid.setWidget(5, 0, splitByWindGaugesLabel);
        splitByWindGaugesBox = dialog.createCheckbox("");
        splitByWindGaugesBox.setValue(settings.splitByWindgauges());
        grid.setWidget(5, 1, splitByWindGaugesBox);
        Label useOnlyEstimationForWindDirectionLabel = new Label(stringMessages.polarSheetUseOnlyEstimationData() + ":");
        useOnlyEstimationForWindDirectionLabel.setTitle(stringMessages.polarSheetUseOnlyEstimationDataTooltip());
        grid.setWidget(6, 0, useOnlyEstimationForWindDirectionLabel);
        useOnlyEstimationForWindDirectionBox = dialog.createCheckbox("");
        useOnlyEstimationForWindDirectionBox.setValue(settings.useOnlyEstimatedForWindDirection());
        grid.setWidget(6, 1, useOnlyEstimationForWindDirectionBox);
        Label shouldRemoveOutliersLabel = new Label(stringMessages.polarSheetRemoveOutliers() + ":");
        shouldRemoveOutliersLabel.setTitle(stringMessages.polarSheetRemoveOutliersTooltip());
        grid.setWidget(7, 0, shouldRemoveOutliersLabel);
        shouldRemoveOutliersBox = dialog.createCheckbox("");
        shouldRemoveOutliersBox.setValue(settings.shouldRemoveOutliers());
        grid.setWidget(7, 1, shouldRemoveOutliersBox);
        Label outlierRadiusLabel = new Label(stringMessages.polarSheetOutlierDetectionRadius() + ":");
        outlierRadiusLabel.setTitle(stringMessages.polarSheetOutlierDetectionRadiusTooltip());
        grid.setWidget(8, 0, outlierRadiusLabel);
        outlierRadiusBox = dialog.createDoubleBox(settings.getOutlierDetectionNeighborhoodRadius(), 6);
        grid.setWidget(8, 1, outlierRadiusBox);
        Label outlierNeighborhoodPctLabel = new Label(stringMessages.polarSheetOutlierDetectionMinimumPerc() + ":");
        outlierNeighborhoodPctLabel.setTitle(stringMessages.polarSheetOutlierDetectionMinimumPercTooltip());
        grid.setWidget(9, 0, outlierNeighborhoodPctLabel);
        outlierNeighborhoodPctBox = dialog.createDoubleBox(settings.getOutlierMinimumNeighborhoodPct(), 6);
        if (!settings.shouldRemoveOutliers()) {
            outlierNeighborhoodPctBox.setEnabled(false);
            outlierRadiusBox.setEnabled(false);
        }
        grid.setWidget(9, 1, outlierNeighborhoodPctBox);
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
        
        setValueChangeListenerForOnlyWindGaugesForSpeed();
        setValueChangeListenerForOutlierRemoval();
    }

    private void setValueChangeListenerForOutlierRemoval() {
        shouldRemoveOutliersBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                boolean isChecked = event.getValue();
                outlierRadiusBox.setEnabled(isChecked);
                outlierNeighborhoodPctBox.setEnabled(isChecked);
            }
        });

    }

    private void setValueChangeListenerForOnlyWindGaugesForSpeed() {
        useOnlyWindGaugesForWindSpeedBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    splitByWindGaugesBox.setEnabled(true);
                } else {
                    splitByWindGaugesBox.setEnabled(false);
                    splitByWindGaugesBox.setValue(false);
                }
            }
        });
    }

    @Override
    public PolarSheetGenerationSettings getResult() {
        return new PolarSheetGenerationSettingsImpl(minimumGraphDataSizeBox.getValue(),
                minimumWindConfidenceBox.getValue(), minimumDataCountPerAngleBox.getValue(),
                numberOfHistogramColumnsBox.getValue(), minimumConfidenceMeasureBox.getValue(),
                useOnlyWindGaugesForWindSpeedBox.getValue(), shouldRemoveOutliersBox.getValue(),
                outlierRadiusBox.getValue(), outlierNeighborhoodPctBox.getValue(),
                useOnlyEstimationForWindDirectionBox.getValue(), windSteppingBox.getStepping(windSteppingMaxDistanceBox
                        .getValue()), splitByWindGaugesBox.getValue(), 0 /*TODO*/);
    }

    @Override
    public Validator<PolarSheetGenerationSettings> getValidator() {
        return new PolarSheetGenerationSettingsValidator(stringMessages);
    }

    @Override
    public FocusWidget getFocusWidget() {
        return minimumGraphDataSizeBox;
    }

}
