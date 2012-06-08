package com.sap.sailing.kiworesultimport.impl;

import org.w3c.dom.Node;

import com.sap.sailing.kiworesultimport.Crew;
import com.sap.sailing.kiworesultimport.Crewmember;
import com.sap.sailing.kiworesultimport.Skipper;

public class CrewImpl extends AbstractNodeWrapper implements Crew {

    public CrewImpl(Node node) {
        super(node);
    }

    @Override
    public Skipper getSkipper() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<Crewmember> getCrewmembers() {
        // TODO Auto-generated method stub
        return null;
    }

}
