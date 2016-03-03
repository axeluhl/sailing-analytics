package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;

/**
 * A perspective composite settings dialog that combines the settings of a perspective and it's {@link Component}s, providing one tab
 * for the perspective and one for each component.
 * 
 * @author Frank Mittag (c5163874)
 *
 */
public class PerspectiveCompositeTabbedSettingsDialog extends SettingsDialog<PerspectiveCompositeSettings> {
    
    public PerspectiveCompositeTabbedSettingsDialog(StringMessages stringConstants, final Perspective<?> perspective) {
        this(stringConstants, perspective, null);
    }

    public PerspectiveCompositeTabbedSettingsDialog(StringMessages stringConstants, Perspective<?> perspective, final String title) {
        super(new PerspectiveCompositeTabbedSettingsComponent(perspective, title), stringConstants);
    }
    
}
