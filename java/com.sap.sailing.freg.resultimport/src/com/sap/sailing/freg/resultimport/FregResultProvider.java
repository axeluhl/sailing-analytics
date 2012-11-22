package com.sap.sailing.freg.resultimport;

import java.net.URL;

public interface FregResultProvider {
    void registerResultUrl(URL url);
    void removeResultUrl(URL url);
    Iterable<URL> getAllUrls();
}
