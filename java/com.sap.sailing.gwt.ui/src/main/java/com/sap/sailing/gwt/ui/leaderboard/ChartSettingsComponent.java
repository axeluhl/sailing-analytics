package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class ChartSettingsComponent extends AbstractChartSettingsComponent<ChartSettings> implements SettingsDialogComponent<ChartSettings> {
    public ChartSettingsComponent(ChartSettings settings, StringMessages stringMessages) {
        super(settings, stringMessages);
    }

    @Override
    public Validator<ChartSettings> getValidator() {
        return new Validator<ChartSettings>() {
            @Override
            public String getErrorMessage(ChartSettings valueToValidate) {
                String errorMessage = null;
                if (valueToValidate.getStepsToLoad() < 2) {
                    errorMessage = getStringMessages().numberOfStepsToLoadMustAtLeastBeTwo();
                }
                return errorMessage;
            }
        };
    }
    
    @Override
    public ChartSettings getResult() {
        return getAbstractResult();
    }
}
