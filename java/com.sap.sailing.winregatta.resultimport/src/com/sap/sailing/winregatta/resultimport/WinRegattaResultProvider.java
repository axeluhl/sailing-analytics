package com.sap.sailing.winregatta.resultimport;

import java.net.URL;

public interface WinRegattaResultProvider {
    void registerResultUrl(URL url);
    void removeResultUrl(URL url);
    Iterable<URL> getAllUrls();
}
