package com.sap.sse.gwt.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;

public final class Highstock {
    public static LegacyResources LEGACY = GWT.create(LegacyResources.class);

    private static boolean isInjected = false;

    protected Highstock() {
    }
    
    /**
     * inject minimal js required for highstock
     */
    public static void ensureInjected() {
        if (!isInjected) {
            ScriptInjector.fromString(LEGACY.highstock().getText()).setWindow(ScriptInjector.TOP_WINDOW).inject();
            isInjected = true;
        }
    }
}
