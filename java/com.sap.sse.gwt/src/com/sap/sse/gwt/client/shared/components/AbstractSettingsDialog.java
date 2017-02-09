package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class AbstractSettingsDialog<SettingsType extends Settings> extends DataEntryDialog<SettingsType> {
    private final SettingsDialogComponent<SettingsType> settingsDialogComponent;
    
    protected AbstractSettingsDialog(final String shortName, SettingsDialogComponent<SettingsType> dialogComponent,
            StringMessages stringMessages, boolean animationEnabled, final DialogCallback<SettingsType> callback) {
        super(stringMessages.settingsForComponent(shortName), null, stringMessages.ok(), stringMessages.cancel(),
                dialogComponent.getValidator(), animationEnabled, callback != null ? callback : new NoOpDialogCallback<SettingsType>());
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
    protected FocusWidget getInitialFocusWidget() {
        return settingsDialogComponent.getFocusWidget();
    }
    
    private static class NoOpDialogCallback<SettingsType extends Settings> implements DialogCallback<SettingsType> {
        @Override
        public void ok(SettingsType editedObject) {
        }
        @Override
        public void cancel() {
        }
    }
}
