package com.sap.sailing.resultimport;

import java.net.URL;

/**
 * A registry for result URL's of score correction providers
 * @author Frank
 */
public interface ResultUrlRegistry {
    void registerResultUrl(String resultProviderName, URL url);
    void unregisterResultUrl(String resultProviderName, URL url);

    Iterable<URL> getResultUrls(String resultProviderName);
    Iterable<String> getResultProviderNames();
}
