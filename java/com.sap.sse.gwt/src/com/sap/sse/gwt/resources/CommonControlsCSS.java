package com.sap.sse.gwt.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;

/**
 * Convinience class to inject common control css content
 * 
 * @author pgtaboada
 *
 */
public final class CommonControlsCSS {
    private static boolean isInjected;

    public static HighChartsRessource LEGACY = GWT.create(HighChartsRessource.class);

    public static void ensureInjected() {
        if (!isInjected) {
            StyleInjector.injectAtStart(LEGACY.commonControls().getText(), true);
            isInjected = true;
        }
    }
}
