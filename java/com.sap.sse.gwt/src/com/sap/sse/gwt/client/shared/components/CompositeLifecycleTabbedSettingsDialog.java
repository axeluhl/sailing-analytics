package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.gwt.client.StringMessages;

/**
 * A composite settings dialog that combines the settings of several {@link ComponentLifecycle}s, providing a tab
 * for each setting.
 * 
 * @author Frank Mittag (c5163874)
 *
 */
public class CompositeLifecycleTabbedSettingsDialog extends SettingsDialog<CompositeLifecycleSettings> {

    public CompositeLifecycleTabbedSettingsDialog(StringMessages stringConstants, CompositeLifecycleSettings compositeLifecycleSettings, String title) {
        super(new CompositeLifecycleTabbedSettingsComponent(compositeLifecycleSettings, title), stringConstants);
    }

    public CompositeLifecycleTabbedSettingsDialog(StringMessages stringConstants, CompositeLifecycleSettings compositeLifecycleSettings, String title, DialogCallback<CompositeLifecycleSettings> callback) {
        super(new CompositeLifecycleTabbedSettingsComponent(compositeLifecycleSettings, title), stringConstants, callback);
    }
}
