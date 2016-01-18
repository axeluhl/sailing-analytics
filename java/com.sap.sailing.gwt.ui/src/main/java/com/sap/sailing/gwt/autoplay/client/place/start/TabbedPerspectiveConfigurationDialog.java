package com.sap.sailing.gwt.autoplay.client.place.start;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleTabbedSettingsDialog;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycle;

/**
 * @author Frank
 *
 */
public class TabbedPerspectiveConfigurationDialog extends CompositeLifecycleTabbedSettingsDialog {

    public TabbedPerspectiveConfigurationDialog(StringMessages stringConstants, PerspectiveLifecycle<?,?,?,?> perspective) {
        super(stringConstants, perspective.getComponentLifecycles(), perspective.getPerspectiveName());
    }

    public TabbedPerspectiveConfigurationDialog(StringMessages stringConstants, PerspectiveLifecycle<?,?,?,?> perspective, DialogCallback<CompositeLifecycleSettings> callback) {
        super(stringConstants, perspective.getComponentLifecycles(), perspective.getPerspectiveName(), callback);
    }
}
