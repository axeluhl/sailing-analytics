package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;

/**
 * A composite settings dialog that combines the settings of several {@link ComponentLifecycle}s, providing a tab
 * for each setting.
 * 
 * @author Frank Mittag (c5163874)
 *
 */
public class PerspectiveCompositeLifecycleTabbedSettingsDialog<P extends PerspectiveLifecycle<?,S,?>, S extends Settings> extends SettingsDialog<PerspectiveCompositeLifecycleSettings<P,S>> {

    public PerspectiveCompositeLifecycleTabbedSettingsDialog(StringMessages stringConstants, PerspectiveCompositeLifecycleSettings<P,S> compositeLifecycleSettings, String title) {
        super(new PerspectiveCompositeLifecycleTabbedSettingsComponent<P,S>(compositeLifecycleSettings, title), stringConstants);
    }

    public PerspectiveCompositeLifecycleTabbedSettingsDialog(StringMessages stringConstants, PerspectiveCompositeLifecycleSettings<P,S> compositeLifecycleSettings, String title, DialogCallback<PerspectiveCompositeLifecycleSettings<P,S>> callback) {
        super(new PerspectiveCompositeLifecycleTabbedSettingsComponent<P,S>(compositeLifecycleSettings, title), stringConstants, callback);
    }
}
