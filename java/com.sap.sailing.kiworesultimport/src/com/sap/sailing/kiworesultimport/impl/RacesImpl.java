package com.sap.sailing.kiworesultimport.impl;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.sailing.kiworesultimport.Race;
import com.sap.sailing.kiworesultimport.Races;

public class RacesImpl extends AbstractNodeWrapper implements Races {

    public RacesImpl(Node node) {
        super(node);
    }

    @Override
    public Double getTotalPoints() {
        return Double.valueOf(getNode().getAttributes().getNamedItem("totalpoints").getNodeValue().replace(',', '.'));
    }

    @Override
    public Iterable<Race> getRaces() {
        List<Race> result = new ArrayList<Race>();
        final NodeList races = ((Element) getNode()).getElementsByTagName("Race");
        for (int i=0; i<races.getLength(); i++) {
            result.add(new RaceImpl(races.item(i)));
        }
        return result;
    }

}
