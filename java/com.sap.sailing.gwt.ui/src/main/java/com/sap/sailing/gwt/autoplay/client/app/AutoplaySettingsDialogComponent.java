package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class AutoplaySettingsDialogComponent implements SettingsDialogComponent<AutoplayPerspectiveOwnSettings> {
    private AutoplayPerspectiveOwnSettings initialSettings;
    private CheckBox fullScreen;
    private CheckBox switchToLiveRaceAutomatically;
    private IntegerBox timeBeforeRaceStartInput;
    private IntegerBox waitTimeAfterRaceEndInput;

    AutoplaySettingsDialogComponent(AutoplayPerspectiveOwnSettings settings) {
        this.initialSettings = settings;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();

        StringMessages stringMessages = StringMessages.INSTANCE;

        fullScreen = dialog.createCheckbox(stringMessages.startBrowserFullscreen());
        fullScreen.setValue(initialSettings.isFullscreen());
        vp.add(fullScreen);

        switchToLiveRaceAutomatically = dialog.createCheckbox(stringMessages.switchToLiveRaceAutomatically());
        switchToLiveRaceAutomatically.setValue(initialSettings.isSwitchToLive());
        vp.add(switchToLiveRaceAutomatically);

        Label timeBeforeRaceStartDescription = new Label(stringMessages.timeBeforeRaceStart());
        vp.add(timeBeforeRaceStartDescription);
        
        timeBeforeRaceStartInput = dialog.createIntegerBox(initialSettings.getTimeToSwitchBeforeRaceStart(), 5);
        vp.add(timeBeforeRaceStartInput);
        
        Label timeAfterRaceEndDescription = new Label(stringMessages.timeAfterRaceEnd());
        vp.add(timeAfterRaceEndDescription);
        
        waitTimeAfterRaceEndInput = dialog.createIntegerBox(initialSettings.getWaitTimeAfterRaceEndInMillis(), 5);
        vp.add(waitTimeAfterRaceEndInput);

        return vp;
    }

    @Override
    public AutoplayPerspectiveOwnSettings getResult() {
        return new AutoplayPerspectiveOwnSettings(fullScreen.getValue(), switchToLiveRaceAutomatically.getValue(),
                timeBeforeRaceStartInput.getValue(), waitTimeAfterRaceEndInput.getValue());
    }

    @Override
    public Validator<AutoplayPerspectiveOwnSettings> getValidator() {
        return new Validator<AutoplayPerspectiveOwnSettings>() {
            @Override
            public String getErrorMessage(AutoplayPerspectiveOwnSettings valueToValidate) {
                if (valueToValidate.timeToSwitchBeforeRaceStart.getValue() < 0) {
                    // TODO i18n
                    return "negative delay";
                } else {
                    return null;
                }
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return fullScreen;
    }

}
