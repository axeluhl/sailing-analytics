package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.gwt.client.StringMessages;

/**
 * A composite settings dialog that combines the settings of several {@link Component}s, providing a tab
 * for each component.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompositeTabbedSettingsDialog extends SettingsDialog<CompositeSettings> {
    
    public CompositeTabbedSettingsDialog(StringMessages stringConstants, final Iterable<Component<?>> components) {
        this(stringConstants, components, null);
    }

    public CompositeTabbedSettingsDialog(StringMessages stringConstants, final Iterable<Component<?>> components, final String title) {
        super(new CompositeTabbedSettingsComponent(components, title), stringConstants);
    }
    
}
