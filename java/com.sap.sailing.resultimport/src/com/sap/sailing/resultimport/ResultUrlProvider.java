package com.sap.sailing.resultimport;

import java.net.URL;

import com.sap.sse.common.Named;

public interface ResultUrlProvider extends Named {
    /**
     * The URLs configured for this provider
     */
    Iterable<URL> getUrls();
    
    /**
     * An optional example for how the URLs should be structured. Can be shown by a
     * user interface for URL list editing to instruct the user how to craft the URLs
     * for this provider. May return <code>null</code>.
     */
    String getOptionalSampleURL();

}
