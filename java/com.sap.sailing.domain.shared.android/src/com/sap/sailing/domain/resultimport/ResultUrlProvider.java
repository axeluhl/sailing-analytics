package com.sap.sailing.domain.resultimport;

import java.net.MalformedURLException;
import java.net.URL;

import com.sap.sse.common.Named;

/**
 * The {@link Named#getName() name} of a result URL provider object must identify it uniquely. As this
 * name may be used in UI controls such as list boxes, the name must not have leading nor trailing blanks
 * because they may otherwise be removed, leading to issues with name matching when round-tripping such
 * a name. Being used as a key also for storing URLs of the provider persistently, changing the name
 * over the life-cycle of a result URL provider will most likely lose all URLs stored persistently
 * for a server environment.
 * 
 * @author Axel Uhl (d043530)
 *
 */
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
