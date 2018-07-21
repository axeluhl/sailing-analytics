package com.sap.sailing.kiworesultimport;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.DOMException;

public interface Skipper extends Named {
    URL getIsaf() throws MalformedURLException, DOMException;
}
