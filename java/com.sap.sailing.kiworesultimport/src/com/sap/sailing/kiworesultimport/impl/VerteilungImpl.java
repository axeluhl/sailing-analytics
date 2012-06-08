package com.sap.sailing.kiworesultimport.impl;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.sailing.kiworesultimport.Boat;
import com.sap.sailing.kiworesultimport.Verteilung;

public class VerteilungImpl extends AbstractNodeWrapper implements Verteilung {

    public VerteilungImpl(Node node) {
        super(node);
    }

    @Override
    public Iterable<Boat> getBoats() {
        List<Boat> result = new ArrayList<Boat>();
        final NodeList boats = ((Element) getNode()).getElementsByTagName("Boat");
        for (int i=0; i<boats.getLength(); i++) {
            result.add(new BoatImpl(boats.item(i)));
        }
        return result;
    }

}
