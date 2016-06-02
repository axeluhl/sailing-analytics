package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;

/**
 * A composite settings dialog that combines all settings of a perspective provide by a {@link PerspectiveLifecycle}.
 * 
 * @author Frank Mittag (c5163874)
 * @param <PL>
 *      the type of the PerspectiveLifecycle
 * @param <PS>
 *      the type of the Perspective settings
 *       
 */
public class PerspectiveCompositeLifecycleTabbedSettingsDialog<PL extends PerspectiveLifecycle<PS, ?,?>, PS extends Settings>
    extends SettingsDialog<PerspectiveCompositeSettings<PS>> {

    public PerspectiveCompositeLifecycleTabbedSettingsDialog(StringMessages stringConstants, PerspectiveLifecycleWithAllSettings<PL, PS> perspectiveLifecycleWithAllSettings, String title) {
        super(new PerspectiveCompositeLifecycleTabbedSettingsComponent<PL,PS>(perspectiveLifecycleWithAllSettings, title), stringConstants);
    }

    public PerspectiveCompositeLifecycleTabbedSettingsDialog(StringMessages stringConstants, PerspectiveLifecycleWithAllSettings<PL, PS> perspectiveLifecycleWithAllSettings,
            String title, DialogCallback<PerspectiveCompositeSettings<PS>> callback) {
        super(new PerspectiveCompositeLifecycleTabbedSettingsComponent<PL,PS>(perspectiveLifecycleWithAllSettings, title), stringConstants, callback);
    }
}
