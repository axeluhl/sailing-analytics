package com.sap.sse.gwt.qualtrics;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface QualtricsRessource extends ClientBundle {
    @Source("resources/qualtrics.js")
    TextResource qualtricsLoadingCode();
    
    @Source("resources/qualtricsProjectId.txt")
    TextResource qualtricsProjectId();
}
