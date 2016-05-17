package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;

/**
 * A utility class to combine the {@link PerspectiveLifecycle} and the settings of the corresponding {@link Perspective} (not the composite settings)
 * @param <PL>
 *      the {@link PerspectiveLifecycle} type
 * @param <PS>
 *      the {@link Perspective} composite settings type
 * @author Frank
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
