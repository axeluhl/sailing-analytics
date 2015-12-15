package com.sap.sailing.gwt.autoplay.client.place.start;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.CompositeTabbedSettingsDialog;
import com.sap.sse.gwt.client.shared.perspective.Perspective;

/**
 * @author Frank
 *
 */
public class TabbedPerspectiveConfigurationDialog extends CompositeTabbedSettingsDialog {

    public TabbedPerspectiveConfigurationDialog(StringMessages stringConstants, Perspective<?> perspective) {
        super(stringConstants, perspective.getComponents(), perspective.getPerspectiveName());
    }

    public TabbedPerspectiveConfigurationDialog(StringMessages stringConstants, Perspective<?> perspective, DialogCallback<CompositeSettings> callback) {
        super(stringConstants, perspective.getComponents(), perspective.getPerspectiveName(), callback);
    }
}
