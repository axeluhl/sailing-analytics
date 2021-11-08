package com.sap.sailing.domain.resultimport;

import java.net.MalformedURLException;
import java.net.URL;

import com.sap.sse.common.Named;

public interface ResultUrlProvider extends Named {
    /**
     * The URLs configured for this provider and readable with {@code Subject}'s permissions.
     */
    Iterable<URL> getReadableUrls();

    /**
     * All URLs configured for this provider.
     */
    Iterable<URL> getAllUrls();

    /**
     * Validate that this provider can use the given {@link URL} which can be an URL, an event ID or a short name
     * and resolve it to an actual URL (if its not already one).
     * @param url {@link URL} to validate.
     * @return {@link URL} if resolved successfully.
     */
    URL resolveUrl(String url) throws MalformedURLException;

    /**
     * An optional example for how the URLs should be structured. Can be shown by a
     * user interface for URL list editing to instruct the user how to craft the URLs
     * for this provider. May return <code>null</code>.
     */
    String getOptionalSampleURL();

}
