package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class TimePanelSettingsDialogComponent implements SettingsDialogComponent<TimePanelSettings> {
    private IntegerBox timeDelayBox;
    private final StringMessages stringMessages;
    private final TimePanelSettings initialSettings;
    
    public TimePanelSettingsDialogComponent(TimePanelSettings settings, StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        initialSettings = settings;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<TimePanelSettings> dialog) {
        VerticalPanel vp = new VerticalPanel();
        HorizontalPanel labelAndTailLengthBoxPanel = new HorizontalPanel();
        labelAndTailLengthBoxPanel.add(new Label(stringMessages.timeDelay()));
        timeDelayBox = dialog.createIntegerBox((int) initialSettings.getDelayInSeconds(), 4);
        labelAndTailLengthBoxPanel.add(timeDelayBox);
        vp.add(labelAndTailLengthBoxPanel);
        return vp;
    }

    @Override
    public TimePanelSettings getResult() {
        TimePanelSettings result = new TimePanelSettings();
        result.setDelayInSeconds(timeDelayBox.getValue() == null ? -1 : timeDelayBox.getValue());
        return result;
    }

    @Override
    public Validator<TimePanelSettings> getValidator() {
        return new Validator<TimePanelSettings>() {
            @Override
            public String getErrorMessage(TimePanelSettings valueToValidate) {
                String errorMessage = null;
                if (valueToValidate.getDelayInSeconds() < 0) {
                    errorMessage = stringMessages.tailLengthMustBeNonNegative();
                }
                return errorMessage;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return timeDelayBox;
    }
}
