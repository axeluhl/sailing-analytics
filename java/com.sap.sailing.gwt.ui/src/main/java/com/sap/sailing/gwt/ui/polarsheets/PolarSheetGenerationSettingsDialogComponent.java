package com.sap.sailing.gwt.ui.polarsheets;

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

    public PolarSheetGenerationSettingsDialogComponent(PolarSheetGenerationSettings settings, StringMessages stringMessages) {
        this.settings = settings;
        this.stringMessages = stringMessages;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();
        Grid grid = new Grid(5,2);
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
        grid.setWidget(4, 0, new Label("Number of histogram columns:"));
        numberOfHistogramColumnsBox = dialog.createIntegerBox(settings.getNumberOfHistogramColumns(), 3);
        grid.setWidget(4, 1, numberOfHistogramColumnsBox);
    }

    @Override
    public PolarSheetGenerationSettings getResult() {
        return new PolarSheetGenerationSettingsImpl(minimumGraphDataSizeBox.getValue(),
                minimumWindConfidenceBox.getValue(), minimumDataCountPerAngleBox.getValue(),
                numberOfHistogramColumnsBox.getValue(), minimumConfidenceMeasureBox.getValue());
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
