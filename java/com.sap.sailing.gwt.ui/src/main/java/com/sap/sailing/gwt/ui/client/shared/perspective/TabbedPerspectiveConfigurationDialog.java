package com.sap.sailing.gwt.ui.client.shared.perspective;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.CompositeTabbedSettingsDialog;

/**
 * TODO: Do we really need an extra class for this?
 * @author Frank
 *
 */
public class TabbedPerspectiveConfigurationDialog extends CompositeTabbedSettingsDialog {
    
    public TabbedPerspectiveConfigurationDialog(StringMessages stringConstants, Perspective perspective) {
        super(stringConstants, perspective.getComponents(), perspective.getPerspectiveName());
    }
}
