package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.gwt.client.StringMessages;

/**
 * A composite settings dialog that combines the settings of several {@link Component}s, providing a tab
 * for each component.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompositeLifecycleTabbedSettingsDialog extends SettingsDialog<CompositeLifecycleSettings> {

    public CompositeLifecycleTabbedSettingsDialog(StringMessages stringConstants, final Iterable<ComponentLifecycle<?,?,?,?>> components, final String title) {
        super(new CompositeLifecycleTabbedSettingsComponent(components, title), stringConstants);
    }

    public CompositeLifecycleTabbedSettingsDialog(StringMessages stringConstants, final Iterable<ComponentLifecycle<?,?,?,?>> components, final String title, DialogCallback<CompositeLifecycleSettings> callback) {
        super(new CompositeLifecycleTabbedSettingsComponent(components, title), stringConstants, callback);
    }
}
