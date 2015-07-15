package com.sap.sse.gwt.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;

/**
 * Convinience class to inject common control css content
 * 
 * @author pgtaboada
 *
 */
public class Highcharts {
    public static LegacyResources LEGACY = GWT.create(LegacyResources.class);
    public static void ensureInjected() {
        ScriptInjector.fromString(LEGACY.highcharts().getText()).setWindow(ScriptInjector.TOP_WINDOW).inject();
        ScriptInjector.fromString(LEGACY.highchartsMore().getText()).setWindow(ScriptInjector.TOP_WINDOW).inject();
        ScriptInjector.fromString(LEGACY.highchartsThemeGrid().getText()).setWindow(ScriptInjector.TOP_WINDOW).inject();
        ScriptInjector.fromString(LEGACY.highchartsModuleExport().getText()).setWindow(ScriptInjector.TOP_WINDOW)
                .inject();
    }
}
