package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public abstract class AbstractChartSettingsComponent<SettingsType extends ChartSettings> implements SettingsDialogComponent<SettingsType> {

    private LongBox stepsBox;
    private final ChartSettings settings;
    private final StringMessages stringMessages;

    public AbstractChartSettingsComponent(ChartSettings settings, StringMessages stringMessages) {
        this.settings = settings;
        this.stringMessages = stringMessages;
    }

    @Override
    public VerticalPanel getAdditionalWidget(DataEntryDialog<SettingsType> dialog) {
        VerticalPanel panel = new VerticalPanel();
        Label lblSteps = new Label(getStringMessages().pointsToLoad());
        panel.add(lblSteps);
        stepsBox = dialog.createLongBox(settings.getStepsToLoad(), 4);
        panel.add(stepsBox);
        return panel;
    }

    protected LongBox getStepsBox() {
        return stepsBox;
    }
    
    @Override
    public Validator<SettingsType> getValidator() {
        return new Validator<SettingsType>() {
            @Override
            public String getErrorMessage(SettingsType valueToValidate) {
                String errorMessage = null;
                if (valueToValidate.getStepsToLoad() < 2) {
                    errorMessage = getStringMessages().numberOfStepsToLoadMustAtLeastBeTwo();
                }
                return errorMessage;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return stepsBox;
    }

    protected StringMessages getStringMessages() {
        return stringMessages;
    }

    public ChartSettings getAbstractResult() {
        Long value = getStepsBox().getValue();
        if (value == null) {
            value = -1l;
        }
        return new ChartSettings(value.intValue());
    }

}
