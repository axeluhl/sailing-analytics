package com.sap.sailing.odf.resultimport.impl;

import org.w3c.dom.Node;

import com.sap.sailing.odf.resultimport.Crewmember;

public class CrewmemberImpl extends PersonImpl implements Crewmember {

    public CrewmemberImpl(Node node) {
        super(node);
    }

}
