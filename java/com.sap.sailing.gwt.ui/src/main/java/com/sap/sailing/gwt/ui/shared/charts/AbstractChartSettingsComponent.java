package com.sap.sailing.gwt.ui.shared.charts;

import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public abstract class AbstractChartSettingsComponent<SettingsType extends ChartSettings> implements SettingsDialogComponent<SettingsType> {

    private DoubleBox stepSizeBox;
    private final ChartSettings settings;
    private final StringMessages stringMessages;

    public AbstractChartSettingsComponent(ChartSettings settings, StringMessages stringMessages) {
        this.settings = settings;
        this.stringMessages = stringMessages;
    }

    @Override
    public VerticalPanel getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel panel = new VerticalPanel();
        panel.add(new Label(getStringMessages().stepSizeInSeconds()));
        stepSizeBox = dialog.createDoubleBox(((double) settings.getStepSize()) / 1000, 5);
        panel.add(stepSizeBox);
        return panel;
    }

    protected DoubleBox getStepSizeBox() {
        return stepSizeBox;
    }
    
    @Override
    public Validator<SettingsType> getValidator() {
        return new Validator<SettingsType>() {
            @Override
            public String getErrorMessage(SettingsType valueToValidate) {
                String errorMessage = null;
                if (valueToValidate.getStepSize() < 1) {
                    errorMessage = getStringMessages().stepSizeMustBeGreaterThanNull();
                }
                return errorMessage;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return stepSizeBox;
    }

    protected StringMessages getStringMessages() {
        return stringMessages;
    }

    public ChartSettings getAbstractResult() {
        Double valueInSeconds = getStepSizeBox().getValue();
        Long value = valueInSeconds == null ? 0 : (long) (getStepSizeBox().getValue() * 1000);
        return new ChartSettings(value);
    }

}
