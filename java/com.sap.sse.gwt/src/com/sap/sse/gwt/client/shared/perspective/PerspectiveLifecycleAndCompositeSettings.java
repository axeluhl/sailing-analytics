package com.sap.sse.gwt.client.shared.perspective;



/**
 * @author Frank
 *
 * @param <PL>
 *      the {@link PerspectiveLifecycle} type
 * @param <PS>
 *      the {@link Perspective} composite settings type
 */
public class PerspectiveLifecycleAndCompositeSettings<PL extends PerspectiveLifecycle<PCS, ?>, PCS extends PerspectiveCompositeLifecycleSettings<?,?>> {
    private final PL perspectiveLifecycle;
    private PCS settings;

    public PerspectiveLifecycleAndCompositeSettings(PL perspectiveLifecycle, PCS compositeSettings) {
        this.perspectiveLifecycle = perspectiveLifecycle;
        this.settings = compositeSettings;
    }

    public PL getPerspectiveLifecycle() {
        return perspectiveLifecycle;
    }
    
    public PCS getSettings() {
        return settings;
    }
    
    public void setSettings(PCS settings) {
        this.settings = settings;
    }
}
