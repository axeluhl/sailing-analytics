package com.sap.sailing.kiworesultimport.impl;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import com.sap.sailing.kiworesultimport.Skipper;

public class SkipperImpl extends NamedImpl implements Skipper {

    public SkipperImpl(Node node) {
        super(node);
    }

    @Override
    public URL getIsaf() throws MalformedURLException, DOMException {
        final String nodeValue = getNode().getAttributes().getNamedItem("isaf").getNodeValue();
        URL result = null;
        if (nodeValue != null && nodeValue.trim().length() > 0) {
            result = new URL(nodeValue);
        }
        return result;
    }
}
