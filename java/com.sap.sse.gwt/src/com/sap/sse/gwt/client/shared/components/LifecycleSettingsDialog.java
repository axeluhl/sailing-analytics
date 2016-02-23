package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class LifecycleSettingsDialog<SettingsType extends Settings> extends DataEntryDialog<SettingsType> {
    private final SettingsDialogComponent<SettingsType> settingsDialogComponent;

    public LifecycleSettingsDialog(final ComponentLifecycle<? ,SettingsType, ?> componentLifecycle, SettingsDialogComponent<SettingsType> dialogComponent,
            StringMessages stringMessages, DialogCallback<SettingsType> callback) {
        this(componentLifecycle, dialogComponent, stringMessages, /* animationEnabled */ true, callback);
    }

    /**
     * This auxiliary constructor is required to avoid duplicate calls to {@link ComponentLifecycle#getSettingsDialogComponent()}
     * which may choose to create a new instance each call. Such duplicate instances would cause the validator to
     * operate on a different instance as the one used for displaying, hence not allowing the validator to use the UI
     * elements, neither for update nor read.
     */
    private LifecycleSettingsDialog(final ComponentLifecycle<?, SettingsType, ?> componentLifecycle,
            SettingsDialogComponent<SettingsType> dialogComponent, StringMessages stringMessages,
            boolean animationEnabled, final DialogCallback<SettingsType> callback) {
        super(stringMessages.settingsForComponent(componentLifecycle.getLocalizedShortName()), null, stringMessages.ok(),
                stringMessages.cancel(), dialogComponent.getValidator(), animationEnabled,
                    new DialogCallback<SettingsType>() {
                        @Override
                        public void cancel() {
                            if(callback != null) {
                                callback.cancel();
                            }
                        }
    
                        @Override
                        public void ok(SettingsType newSettings) {
                            if(callback != null) {
                                callback.ok(newSettings);
                            }
                        }
                    });
        this.settingsDialogComponent = dialogComponent;
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
