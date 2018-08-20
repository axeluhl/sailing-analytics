package com.sap.sailing.kiworesultimport.impl;

import org.w3c.dom.Node;

import com.sap.sailing.kiworesultimport.Named;

public class NamedImpl extends AbstractNodeWrapper implements Named {

    public NamedImpl(Node node) {
        super(node);
    }

    @Override
    public String getName() {
        return getNode().getAttributes().getNamedItem("name").getNodeValue();
    }

    @Override
    public String toString() {
        return getName();
    }
}
