package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.ui.DoubleBox;
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
    private DoubleBox refreshIntervalBox;
    private final StringMessages stringMessages;
    private final TimePanelSettings initialSettings;
    
    public TimePanelSettingsDialogComponent(TimePanelSettings settings, StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        initialSettings = settings;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<TimePanelSettings> dialog) {
        VerticalPanel vp = new VerticalPanel();
        
        HorizontalPanel labelAndTimeDelayBoxPanel = new HorizontalPanel();
        labelAndTimeDelayBoxPanel.add(new Label(stringMessages.timeDelay()));
        timeDelayBox = dialog.createIntegerBox((int) initialSettings.getDelayToLivePlayInSeconds(), 4);
        labelAndTimeDelayBoxPanel.add(timeDelayBox);
        vp.add(labelAndTimeDelayBoxPanel);
        
        HorizontalPanel labelAndRefreshIntervalBoxPanel = new HorizontalPanel();
        labelAndRefreshIntervalBoxPanel.add(new Label(stringMessages.refreshInterval()));
        refreshIntervalBox = dialog.createDoubleBox(((double) initialSettings.getRefreshInterval()) / 1000, 4);
        labelAndRefreshIntervalBoxPanel.add(refreshIntervalBox);
        vp.add(labelAndRefreshIntervalBoxPanel);
        
        return vp;
    }

    @Override
    public TimePanelSettings getResult() {
        TimePanelSettings result = new TimePanelSettings();
        result.setDelayToLivePlayInSeconds(timeDelayBox.getValue() == null ? -1 : timeDelayBox.getValue());
        result.setRefreshInterval(refreshIntervalBox.getValue() == null ? -1 : (long) (refreshIntervalBox.getValue() * 1000));
        return result;
    }

    @Override
    public Validator<TimePanelSettings> getValidator() {
        return new Validator<TimePanelSettings>() {
            @Override
            public String getErrorMessage(TimePanelSettings valueToValidate) {
                String errorMessage = null;
                if (valueToValidate.getDelayToLivePlayInSeconds() < 0) {
                    errorMessage = stringMessages.tailLengthMustBeNonNegative();
                }
                if (valueToValidate.getRefreshInterval() < 500) {
                    errorMessage = stringMessages.refreshIntervalMustBeGreaterThanXSeconds("0.5");
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
