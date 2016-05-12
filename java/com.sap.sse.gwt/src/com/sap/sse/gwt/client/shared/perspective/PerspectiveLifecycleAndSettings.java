package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;

/**
 * The class combining the PerspectiveLifecycle and the settings of the corresponding Perspective (not the composite settings)
 * @author Frank
 *
 * @param <PL>
 * @param <PS>
 */
public class PerspectiveLifecycleAndSettings<PL extends PerspectiveLifecycle<PS, ?, ?>, PS extends Settings> {
    private final PL perspectiveLifecycle;
    private final PS settings;

    public PerspectiveLifecycleAndSettings(PL perspectiveLifecycle, PS settings) {
        super();
        this.perspectiveLifecycle = perspectiveLifecycle;
        this.settings = settings;
    }

    public PL getPerspectiveLifecycle() {
        return perspectiveLifecycle;
    }
    
    public PS getSettings() {
        return settings;
    }
}
