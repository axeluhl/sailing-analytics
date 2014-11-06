package com.sap.sailing.dashboards.gwt.client.blureffect;

public class BlurEffect {

    private static BlurEffect INSTANCE = null;

    public static BlurEffect getInstance() {
        // No more tension of threads
        synchronized (BlurEffect.class) {
            if (INSTANCE == null) {
                INSTANCE = new BlurEffect();
            }
        }
        return INSTANCE;
    }
}
