package com.sap.sailing.dashboards.gwt.client.visualeffects;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.Widget;


public class BlurEffect {

    private static BlurEffect INSTANCE = null;
    private VisualEffectsResources visualEffectsResources = GWT.create(VisualEffectsResources.class);

    private BlurEffect() {
        visualEffectsResources.blurEffectStyle().ensureInjected();
    }

    public static BlurEffect getInstance() {
        synchronized (BlurEffect.class) {
            if (INSTANCE == null) {
                INSTANCE = new BlurEffect();
            }
        }
        return INSTANCE;
    }
    
    public void addToView(Widget widget){
        widget.removeStyleName(visualEffectsResources.blurEffectStyle().not_blurred());
        widget.addStyleName(visualEffectsResources.blurEffectStyle().blurred());
    }
    
    public void removeFromView(Widget widget){
        widget.removeStyleName(visualEffectsResources.blurEffectStyle().blurred());
        widget.addStyleName(visualEffectsResources.blurEffectStyle().not_blurred());
    }
}
