package com.sap.sailing.resultimport.impl;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.resultimport.ResultUrlRegistry;

public class ResultUrlRegistryImpl implements ResultUrlRegistry {
    private Map<String, Set<URL>> resultUrls;
    
    public ResultUrlRegistryImpl() {
        resultUrls = new HashMap<String, Set<URL>>();
    }
    
    @Override
    public void registerResultUrl(String resultProviderName, URL url) {
        Set<URL> urlSet = resultUrls.get(resultProviderName);
        if(urlSet == null) {
            urlSet = new HashSet<URL>();
            resultUrls.put(resultProviderName, urlSet);
        }
        urlSet.add(url);
    }

    @Override
    public void unregisterResultUrl(String resultProviderName, URL url) {
        Set<URL> urlSet = resultUrls.get(resultProviderName);
        if(urlSet != null) {
            urlSet.remove(url);
        }
    }

    @Override
    public Iterable<URL> getResultUrls(String resultProviderName) {
        return resultUrls.get(resultProviderName);
    }

    @Override
    public Iterable<String> getResultProviderNames() {
        return resultUrls.keySet();
    }
}
