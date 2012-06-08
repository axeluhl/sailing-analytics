package com.sap.sailing.kiworesultimport.impl;

import org.w3c.dom.Node;

import com.sap.sailing.kiworesultimport.Race;

public class RaceImpl extends AbstractNodeWrapper implements Race {

    public RaceImpl(Node node) {
        super(node);
    }

    @Override
    public Double getPoints() {
        return Double.valueOf(getNode().getAttributes().getNamedItem("points").getNodeValue().replace(',', '.'));
    }

    @Override
    public Integer getNumber() {
        return Integer.valueOf(getNode().getAttributes().getNamedItem("number").getNodeValue());
    }

    @Override
    public String getStatus() {
        final Node namedItem = getNode().getAttributes().getNamedItem("status");
        return namedItem == null || namedItem.getNodeValue() == null || namedItem.getNodeValue().trim().length() == 0 ? null : namedItem.getNodeValue();
    }

}
