package com.sap.sse.gwt.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;

/**
 * Convinience class to inject common control css content
 * 
 * @author pgtaboada
 *
 */
public class CommonControlsCSS {
    public static HighChartsRessource LEGACY = GWT.create(HighChartsRessource.class);
    public static void ensureInjected() {
        StyleInjector.injectAtStart(LEGACY.commonControls().getText(), true);
    }
}
