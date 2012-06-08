package com.sap.sailing.kiworesultimport.impl;

import org.w3c.dom.Node;

import com.sap.sailing.kiworesultimport.Boat;
import com.sap.sailing.kiworesultimport.Verteilung;

public class VerteilungImpl extends AbstractNodeWrapper implements Verteilung {

    public VerteilungImpl(Node node) {
        super(node);
    }

    @Override
    public Iterable<Boat> getBoats() {
        // TODO Auto-generated method stub
        return null;
    }

}
