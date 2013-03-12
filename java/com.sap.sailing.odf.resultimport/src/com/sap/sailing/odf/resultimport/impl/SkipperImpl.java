package com.sap.sailing.odf.resultimport.impl;

import org.w3c.dom.Node;

import com.sap.sailing.odf.resultimport.Skipper;

public class SkipperImpl extends PersonImpl implements Skipper {
    public SkipperImpl(Node node) {
        super(node);
    }
}
