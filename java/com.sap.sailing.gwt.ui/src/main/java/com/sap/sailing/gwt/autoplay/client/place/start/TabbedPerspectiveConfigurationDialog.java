package com.sap.sailing.gwt.autoplay.client.place.start;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleTabbedSettingsDialog;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycle;

/**
 * @author Frank Mittag (c5163874)
 *
 */
public class TabbedPerspectiveConfigurationDialog extends CompositeLifecycleTabbedSettingsDialog {

    public TabbedPerspectiveConfigurationDialog(StringMessages stringConstants, PerspectiveLifecycle<?,?,?,?> perspectiveLifecycle,
            CompositeLifecycleSettings perspectiveComponentsLifeyclesAndSettings) {
        super(stringConstants, perspectiveComponentsLifeyclesAndSettings, perspectiveLifecycle.getPerspectiveName());
    }

    public TabbedPerspectiveConfigurationDialog(StringMessages stringConstants, PerspectiveLifecycle<?,?,?,?> perspectiveLifecycle, 
            CompositeLifecycleSettings perspectiveComponentsLifeyclesAndSettings, DialogCallback<CompositeLifecycleSettings> callback) {
        super(stringConstants, perspectiveComponentsLifeyclesAndSettings, perspectiveLifecycle.getPerspectiveName(), callback);
    }
}
