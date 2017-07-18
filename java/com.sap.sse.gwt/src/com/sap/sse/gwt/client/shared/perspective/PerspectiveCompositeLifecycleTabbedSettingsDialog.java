package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

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
public class PerspectiveCompositeLifecycleTabbedSettingsDialog<PL extends PerspectiveLifecycle<PS>, PS extends Settings>
    extends SettingsDialog<PerspectiveCompositeSettings<PS>> {

    public PerspectiveCompositeLifecycleTabbedSettingsDialog(Component<?> parent, ComponentContext<?> context,
            StringMessages stringConstants,
            PL lifecycle,
            PerspectiveCompositeSettings<PS> settings, String title) {
        super(new PerspectiveCompositeLifecycleTabbedSettingsComponent<PL, PS>(parent, context, lifecycle, settings,
                title),
                stringConstants);
    }

    public PerspectiveCompositeLifecycleTabbedSettingsDialog(Component<?> parent, ComponentContext<?> context,
            StringMessages stringConstants,
            PL lifecycle,
            PerspectiveCompositeSettings<PS> settings,
            String title, DialogCallback<PerspectiveCompositeSettings<PS>> callback) {
        super(new PerspectiveCompositeLifecycleTabbedSettingsComponent<PL, PS>(parent, context, lifecycle, settings,
                title),
                stringConstants, callback);
    }
}
