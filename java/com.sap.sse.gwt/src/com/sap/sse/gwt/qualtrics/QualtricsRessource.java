package com.sap.sse.gwt.qualtrics;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

/**
 * Encapsulates the embedding information of a Qualtrics project. Copy the JavaScript part of your deployment code
 * as found in your Qualtrics project's Settings/Deployment section, without the &lt;script&gt; tags and the HTML comment
 * tags surrounding it, into the {@code resources/qualtrics.js} file and place the project ID, as found, e.g., in the
 * {@code ContextZone} URL parameter in your Qualtrics dashboard page or at the end of the deployment HTML snippet
 * in the {@code id} attribute of the {@code div} tag shown there, into the file {@code resources/qualtricsProjectId.txt}.
 * Then see the {@link Qualtrics} class for how to use in your GWT project.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface QualtricsRessource extends ClientBundle {
    @Source("resources/qualtrics.js")
    TextResource qualtricsLoadingCode();
    
    @Source("resources/qualtricsProjectId.txt")
    TextResource qualtricsProjectId();
}
