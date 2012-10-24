package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class SettingsDialog<SettingsType> extends DataEntryDialog<SettingsType> {
    private final SettingsDialogComponent<SettingsType> settingsDialogComponent;
    
    public SettingsDialog(final Component<SettingsType> component, StringMessages stringMessages) {
        this(component, stringMessages, /* animationEnabled */ true);
    }
    
    public SettingsDialog(final Component<SettingsType> component, StringMessages stringMessages, boolean animationEnabled) {
        super(stringMessages.settingsForComponent(component.getLocalizedShortName()), null, stringMessages.ok(),
                stringMessages.cancel(), component.getSettingsDialogComponent().getValidator(), animationEnabled,
                new DialogCallback<SettingsType>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(SettingsType newSettings) {
                        component.updateSettings(newSettings);
                    }
                });
        this.settingsDialogComponent = component.getSettingsDialogComponent();
    }

    @Override
    protected Widget getAdditionalWidget() {
        return settingsDialogComponent.getAdditionalWidget(this);
    }

    @Override
    protected SettingsType getResult() {
        return settingsDialogComponent.getResult();
    }

    @Override
    public void show() {
        super.show();
        FocusWidget focusWidget = settingsDialogComponent.getFocusWidget();
        if (focusWidget != null) {
            focusWidget.setFocus(true);
        }
    }
    
}
