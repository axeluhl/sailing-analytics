package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;

/**
 * The class combining the PerspectiveLifecycle and the settings of the corresponding Perspective (not the composite settings)
 * @author Frank
 *
 * @param <PLCT>
 * @param <PST>
 */
public class PerspectiveLifecycleAndSettings<PLC extends PerspectiveLifecycle<?, ?>, PST extends Settings> {
    private final PLC perspectiveLifecycle;
    private final PST settings;

    public PerspectiveLifecycleAndSettings(PLC perspectiveLifecycle, PST settings) {
        super();
        this.perspectiveLifecycle = perspectiveLifecycle;
        this.settings = settings;
    }

    public PLC getPerspectiveLifecycle() {
        return perspectiveLifecycle;
    }
    
    public PST getSettings() {
        return settings;
    }
}
