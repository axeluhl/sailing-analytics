package com.sap.sse.gwt.qualtrics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;

/**
 * Convenience class to inject Qualtrics script content. To use a Qualtrics survey in your entry point, inherit the
 * {@code com.sap.sse.gwt.Qualtrics} module which has an entry point that injects the script code needed. Then,
 * if your Qualtrics "Intercept" is setup of for an explicit JavaScript trigger, establish 
 */
public final class Qualtrics {
    public static QualtricsRessource QUALTRICS_RESSOURCES = GWT.create(QualtricsRessource.class);

    private static boolean isInjected = false;

    protected Qualtrics() {
    }

    /**
     * inject minimal js required for Qualtrics survey
     */
    public static void ensureInjected() {
        if (!isInjected) {
            ScriptInjector.fromString(QUALTRICS_RESSOURCES.qualtricsLoadingCode().getText()).setWindow(ScriptInjector.TOP_WINDOW).inject();
            // Now produce the following div:
            //          <div id='ZN_7WmsxxHQyCeUivX'><!--DO NOT REMOVE-CONTENTS PLACED HERE--></div>
            final DivElement div = Document.get().createDivElement();
            div.setId("ZN_7WmsxxHQyCeUivX");
            div.setInnerHTML("<!--DO NOT REMOVE-CONTENTS PLACED HERE-->");
            isInjected = true;
        }
    }
    
    /**
     * Use this, e.g., in your "onclick" handler for your anchor, link or button to trigger the Qualtrics Intercepts
     * loaded for your page that are configured for explicit JavaScript invocation.
     */
    public static native void triggerIntercepts() /*-{
        $wnd.QSI.API.unload();
        $wnd.QSI.API.load();
        $wnd.QSI.API.run();
    }-*/;
}
