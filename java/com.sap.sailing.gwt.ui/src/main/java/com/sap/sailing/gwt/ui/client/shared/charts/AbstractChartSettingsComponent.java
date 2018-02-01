package com.sap.sailing.gwt.ui.client.shared.charts;

import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public abstract class AbstractChartSettingsComponent<SettingsType extends ChartSettings> implements SettingsDialogComponent<SettingsType> {
    private final ChartSettings settings;
    protected DoubleBox stepSizeBox;
    protected final StringMessages stringMessages;

    public AbstractChartSettingsComponent(ChartSettings settings, StringMessages stringMessages) {
        this.settings = settings;
        this.stringMessages = stringMessages;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(new Label(stringMessages.stepSizeInSeconds()));
        stepSizeBox = dialog.createDoubleBox(((double) settings.getStepSizeInMillis()) / 1000, 5);
        mainPanel.add(stepSizeBox);
        return mainPanel;
    }

    @Override
    public Validator<SettingsType> getValidator() {
        return new Validator<SettingsType>() {
            @Override
            public String getErrorMessage(SettingsType valueToValidate) {
                String errorMessage = null;
                if (valueToValidate.getStepSizeInMillis() < 1) {
                    errorMessage = stringMessages.stepSizeMustBeGreaterThanNull();
                }
                return errorMessage;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return stepSizeBox;
    }

    public ChartSettings getAbstractResult() {
        Double valueInSeconds = stepSizeBox.getValue();
        Long value = valueInSeconds == null ? 0 : (long) (stepSizeBox.getValue() * 1000);
        return new ChartSettings(value);
    }

    public ChartSettings getSettings() {
        return settings;
    }

}
