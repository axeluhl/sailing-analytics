package com.sap.sailing.dashboards.gwt.client.visualeffects;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.Widget;


public class DarkeningEffect {

    private static DarkeningEffect INSTANCE = null;
    private VisualEffectsResources visualEffectsResources = GWT.create(VisualEffectsResources.class);

    private DarkeningEffect() {
        visualEffectsResources.darkeningEffectStyle().ensureInjected();
    }

    public static DarkeningEffect getInstance() {
        synchronized (DarkeningEffect.class) {
            if (INSTANCE == null) {
                INSTANCE = new DarkeningEffect();
            }
        }
        return INSTANCE;
    }
    
    public void addToView(Widget widget){
        widget.removeStyleName(visualEffectsResources.darkeningEffectStyle().not_darkened());
        widget.addStyleName(visualEffectsResources.darkeningEffectStyle().darkened());
    }
    
    public void removeFromView(Widget widget){
        widget.removeStyleName(visualEffectsResources.darkeningEffectStyle().darkened());
        widget.addStyleName(visualEffectsResources.darkeningEffectStyle().not_darkened());
    }
}
