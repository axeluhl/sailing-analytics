package com.sap.sailing.gwt.ui.polarsheets;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationSettingsImpl;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.client.IntegerBox;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;

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
    private CheckBox useOnlyEstimationForWindSpeedBox;
    private WindSteppingConfiguratorPanel windSteppingBox;

    public PolarSheetGenerationSettingsDialogComponent(PolarSheetGenerationSettings settings, StringMessages stringMessages) {
        this.settings = settings;
        this.stringMessages = stringMessages;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();
        Grid grid = new Grid(11,2);
        grid.setCellPadding(5);
        vp.add(grid);
        setupGrid(grid, dialog);
        return vp;
    }

    private void setupGrid(Grid grid, DataEntryDialog<?> dialog) {
        grid.setWidget(0, 0, new Label("Minimum data size per Graph:"));
        minimumGraphDataSizeBox = dialog.createIntegerBox(settings.getMinimumDataCountPerGraph(), 6);
        grid.setWidget(0, 1, minimumGraphDataSizeBox);
        grid.setWidget(1, 0, new Label("Minimum Data Count Per Angle:"));
        minimumDataCountPerAngleBox = dialog.createIntegerBox(settings.getMinimumDataCountPerAngle(), 6);
        grid.setWidget(1, 1, minimumDataCountPerAngleBox);
        grid.setWidget(2, 0, new Label("Minimum Wind Confidence:"));
        minimumWindConfidenceBox = dialog.createDoubleBox(settings.getMinimumWindConfidence(), 6);
        grid.setWidget(2, 1, minimumWindConfidenceBox);
        grid.setWidget(3, 0, new Label("Minimum Confidence Measure:"));
        minimumConfidenceMeasureBox = dialog.createDoubleBox(settings.getMinimumConfidenceMeasure(), 6);
        grid.setWidget(3, 1, minimumConfidenceMeasureBox);
        grid.setWidget(4, 0, new Label("Use only wind gauge data for wind speed:"));
        useOnlyWindGaugesForWindSpeedBox = dialog.createCheckbox("");
        useOnlyWindGaugesForWindSpeedBox.setValue(settings.useOnlyWindGaugesForWindSpeed());
        grid.setWidget(4, 1, useOnlyWindGaugesForWindSpeedBox);
        grid.setWidget(5, 0, new Label("Use only wind estimation data for wind direction:"));
        useOnlyEstimationForWindSpeedBox = dialog.createCheckbox("");
        useOnlyEstimationForWindSpeedBox.setValue(settings.useOnlyEstimatedForWindDirection());
        grid.setWidget(5, 1, useOnlyEstimationForWindSpeedBox);
        grid.setWidget(6, 0, new Label("Remove outliers (Distance Based):"));
        shouldRemoveOutliersBox = dialog.createCheckbox("");
        shouldRemoveOutliersBox.setValue(settings.shouldRemoveOutliers());
        grid.setWidget(6, 1, shouldRemoveOutliersBox);
        grid.setWidget(7, 0, new Label("Outlier Detection Neighborhood Radius:"));
        outlierRadiusBox = dialog.createDoubleBox(settings.getOutlierDetectionNeighborhoodRadius(), 6);
        grid.setWidget(7, 1, outlierRadiusBox);
        grid.setWidget(8, 0, new Label("Outlier Detection Minimum Neighboorhood Percentage"));
        outlierNeighborhoodPctBox = dialog.createDoubleBox(settings.getOutlierMinimumNeighborhoodPct(), 6);
        grid.setWidget(8, 1, outlierNeighborhoodPctBox);
        grid.setWidget(9, 0, new Label("Number of histogram columns:"));
        numberOfHistogramColumnsBox = dialog.createIntegerBox(settings.getNumberOfHistogramColumns(), 3);
        grid.setWidget(9, 1, numberOfHistogramColumnsBox);
        grid.setWidget(10, 0, new Label("Wind stepping in knots:"));
        windSteppingBox = new WindSteppingConfiguratorPanel(settings.getWindStepping());
        grid.setWidget(10, 1, windSteppingBox);
    }

    @Override
    public PolarSheetGenerationSettings getResult() {
        return new PolarSheetGenerationSettingsImpl(minimumGraphDataSizeBox.getValue(),
                minimumWindConfidenceBox.getValue(), minimumDataCountPerAngleBox.getValue(),
                numberOfHistogramColumnsBox.getValue(), minimumConfidenceMeasureBox.getValue(),
                useOnlyWindGaugesForWindSpeedBox.getValue(), shouldRemoveOutliersBox.getValue(),
                outlierRadiusBox.getValue(), outlierNeighborhoodPctBox.getValue(),
                useOnlyEstimationForWindSpeedBox.getValue(), windSteppingBox.getStepping());
    }

    @Override
    public Validator<PolarSheetGenerationSettings> getValidator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FocusWidget getFocusWidget() {
        return minimumGraphDataSizeBox;
    }

}
