package com.sap.sailing.kiworesultimport.impl;

import org.w3c.dom.Node;

public class AbstractNodeWrapper {
    private final Node node;

    public AbstractNodeWrapper(Node node) {
        super();
        this.node = node;
    }
    
    protected Node getNode() {
        return node;
    }
}
