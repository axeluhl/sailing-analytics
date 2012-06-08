package com.sap.sailing.kiworesultimport.impl;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.sailing.kiworesultimport.Boat;
import com.sap.sailing.kiworesultimport.Crew;
import com.sap.sailing.kiworesultimport.Races;

public class BoatImpl extends NamedImpl implements Boat {

    public BoatImpl(Node node) {
        super(node);
    }

    @Override
    public String getSailingNumber() {
        return getNode().getAttributes().getNamedItem("sailingnumber").getNodeValue();
    }

    @Override
    public Integer getPosition() {
        return Integer.valueOf(getNode().getAttributes().getNamedItem("position").getNodeValue());
    }

    @Override
    public String getPreis() {
        return getNode().getAttributes().getNamedItem("preis").getNodeValue();
    }

    @Override
    public Crew getCrew() {
        final NodeList crew = ((Element) getNode()).getElementsByTagName("Crew");
        return new CrewImpl(crew.item(0));
    }

    @Override
    public Races getRaces() {
        final NodeList races = ((Element) getNode()).getElementsByTagName("Races");
        return new RacesImpl(races.item(0));
    }

}
