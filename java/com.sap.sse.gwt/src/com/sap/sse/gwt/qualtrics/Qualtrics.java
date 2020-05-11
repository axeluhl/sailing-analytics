package com.sap.sse.gwt.qualtrics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;

/**
 * Convenience class to inject Qualtrics script content. To use a Qualtrics survey in your entry point, inherit the
 * {@code com.sap.sse.gwt.Qualtrics} module which has an entry point that injects the script code needed. Then, if your
 * Qualtrics "Intercept" is setup of for an explicit JavaScript trigger, invoke the {@link #triggerIntercepts()} method
 * to trigger them.
 * <p>
 * The Qualtrics loading code can be copied from the Qualtrics dashboard
 * (see, e.g., <a href="https://sapdemo.eu.qualtrics.com/">https://sapdemo.eu.qualtrics.com/</a>) in the category
 * "Settings / Deployment" where the Javascript snippet can be found. Copy it to the {@code resources/qualtrics.js}
 * file, stripping off the HTML comments and the &lt;script&gt; tag surrounding it. Copy the project ID that you find
 * in the &lt;div&gt; element's {@code id} field at the bottom of the snippet as well as in your Qualtrics dashboard's
 * URL parameter {@code ContextZone} into the file {@code resources/qualtricsProjectId.txt}.
 * 
 * An example UiBinder usage with an {@link AnchorElement} can look like this:
 * 
 * <pre>
 *&nbsp;@UiField AnchorElement feedbackAnchor;
 * ...
 * Event.sinkEvents(feedbackAnchor, Event.ONCLICK);
 * Event.setEventListener(feedbackAnchor, event -> {
 *     if (Event.ONCLICK == event.getTypeInt()) {
 *         event.preventDefault();
 *         event.stopPropagation();
 *         Qualtrics.triggerIntercepts();
 *     }
 * });
 * </pre>
 * 
 * In your {@code .gwt.ui} file things would then look something like this:
 * 
 * <pre>
 * &lt;a ui:field="feedbackAnchor" href="" title="{i18n.footerFeedback}">
 *       &lt;ui:text from='{i18n.footerFeedback}' /&gt;
 * &lt;/a&gt;
 * </pre>
 */
public final class Qualtrics {
    public static QualtricsRessource QUALTRICS_RESSOURCES = GWT.create(QualtricsRessource.class);

    private static boolean isInjected = false;

    protected Qualtrics() {
    }

    /**
     * inject minimal js required for Qualtrics survey
     */
    static void ensureInjected() {
        if (!isInjected) {
            ScriptInjector.fromString(QUALTRICS_RESSOURCES.qualtricsLoadingCode().getText()).setWindow(ScriptInjector.TOP_WINDOW).inject();
            // Now produce the following div:
            //          <div id='ZN_7WmsxxHQyCeUivX'><!--DO NOT REMOVE-CONTENTS PLACED HERE--></div>
            final DivElement div = Document.get().createDivElement();
            div.setId(QUALTRICS_RESSOURCES.qualtricsProjectId().getText());
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
