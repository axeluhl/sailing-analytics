package com.sap.sailing.resultimport;

import java.net.URL;

import com.sap.sailing.resultimport.impl.ResultUrlRegistryImpl;

/**
 * A registry for result URL's of score correction providers
 * @author Frank
 */
public interface ResultUrlRegistry {
    ResultUrlRegistry INSTANCE = new ResultUrlRegistryImpl();
    
    void registerResultUrl(String resultProviderName, URL url);
    void unregisterResultUrl(String resultProviderName, URL url);

    Iterable<URL> getResultUrls(String resultProviderName);
    Iterable<String> getResultProviderNames();
}
