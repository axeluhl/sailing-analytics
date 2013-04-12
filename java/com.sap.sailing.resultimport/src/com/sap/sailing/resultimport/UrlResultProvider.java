package com.sap.sailing.resultimport;

import java.net.URL;

public interface UrlResultProvider {
    void registerResultUrl(URL url);
    void removeResultUrl(URL url);
    Iterable<URL> getAllUrls();
}
