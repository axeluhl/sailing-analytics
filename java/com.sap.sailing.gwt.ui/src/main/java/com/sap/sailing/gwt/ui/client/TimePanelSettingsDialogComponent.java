package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.domain.common.security.SailingPermissionsForRoleProvider;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.security.ui.client.UserService;

public class TimePanelSettingsDialogComponent<T extends TimePanelSettings> implements SettingsDialogComponent<T> {
    protected DoubleBox refreshIntervalBox;
    protected final StringMessages stringMessages;
    protected final T initialSettings;
    protected FlowPanel mainContentPanel;
    private UserService userService;

    private static String STYLE_LABEL = "settingsDialogLabel";
    private static String STYLE_INPUT = "settingsDialogValue";
    private static String STYLE_BOXPANEL = "boxPanel";

    public TimePanelSettingsDialogComponent(T settings, StringMessages stringMessages, UserService userService) {
        this.stringMessages = stringMessages;
        initialSettings = settings;
        this.userService = userService;
    }

    protected StringMessages getStringMessages() {
        return stringMessages;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        mainContentPanel = new FlowPanel();
        FlowPanel labelAndRefreshIntervalBoxPanel = new FlowPanel();
        Label labelIntervalBox = new Label(stringMessages.refreshInterval() + ":");
        labelIntervalBox.setStyleName(STYLE_LABEL);
        labelAndRefreshIntervalBoxPanel.add(labelIntervalBox);
        refreshIntervalBox = dialog.createDoubleBox(((double) initialSettings.getRefreshInterval()) / 1000, 4);
        refreshIntervalBox.setStyleName(STYLE_INPUT);
        labelAndRefreshIntervalBoxPanel.setStyleName(STYLE_BOXPANEL);
        labelAndRefreshIntervalBoxPanel.add(refreshIntervalBox);
        mainContentPanel.add(labelAndRefreshIntervalBoxPanel);
        return mainContentPanel;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getResult() {
        long refreshInterval = refreshIntervalBox.getValue() == null ? -1
                : (long) (refreshIntervalBox.getValue() * 1000);
        return (T) new TimePanelSettings(refreshInterval);
    }

    @Override
    public Validator<T> getValidator() {
        return new Validator<T>() {
            @Override
            public String getErrorMessage(TimePanelSettings valueToValidate) {
                String errorMessage = null;
                if (userService.getCurrentUser() != null
                        && userService.getCurrentUser().hasPermission(Permission.DETAIL_TIMER.name(), SailingPermissionsForRoleProvider.INSTANCE)) {
                    if (valueToValidate.getRefreshInterval() < 50) {
                        errorMessage = stringMessages.refreshIntervalMustBeGreaterThanXSeconds("0.05");
                    }
                } else {
                    if (valueToValidate.getRefreshInterval() < 500) {
                        errorMessage = stringMessages.refreshIntervalMustBeGreaterThanXSeconds("0.5");
                    }
                }
                return errorMessage;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return refreshIntervalBox;
    }
}
