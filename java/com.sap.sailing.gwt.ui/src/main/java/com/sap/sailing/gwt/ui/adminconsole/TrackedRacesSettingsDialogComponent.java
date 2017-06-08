package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class TrackedRacesSettingsDialogComponent<T extends TrackedRacesSettings> implements SettingsDialogComponent<T> {
    protected LongBox timeDelayBox;
    protected final StringMessages stringMessages;
    protected final T initialSettings;
    protected FlowPanel mainContentPanel;

    private static String STYLE_LABEL = "settingsDialogLabel";
    private static String STYLE_INPUT = "settingsDialogValue";
    private static String STYLE_BOXPANEL = "boxPanel";

    public TrackedRacesSettingsDialogComponent(T settings, StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        initialSettings = settings;
    }

    protected StringMessages getStringMessages() {
        return stringMessages;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        mainContentPanel = new FlowPanel();

        FlowPanel labelAndTimeDelayBoxPanel = new FlowPanel();
        Label labelTimeDelayBox = new Label(stringMessages.delayInSeconds() + ":");
        labelTimeDelayBox.setStyleName(STYLE_LABEL);
        labelAndTimeDelayBoxPanel.add(labelTimeDelayBox);

        timeDelayBox = dialog.createLongBox(initialSettings.getDelayToLiveInSeconds(), 4);
        timeDelayBox.setStyleName(STYLE_INPUT);

        labelAndTimeDelayBoxPanel.setStyleName(STYLE_BOXPANEL);
        labelAndTimeDelayBoxPanel.add(timeDelayBox);

        mainContentPanel.add(labelAndTimeDelayBoxPanel);

        return mainContentPanel;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getResult() {
        T result = (T) new TrackedRacesSettings();
        result.setDelayToLiveInSeconds(timeDelayBox.getValue() == null ? -1 : timeDelayBox.getValue());
        return result;
    }

    @Override
    public Validator<T> getValidator() {
        return new Validator<T>() {
            @Override
            public String getErrorMessage(TrackedRacesSettings valueToValidate) {
                String errorMessage = null;
                if (valueToValidate.getDelayToLiveInSeconds() < 0) {
                    errorMessage = stringMessages.delayMustBeNonNegative();
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