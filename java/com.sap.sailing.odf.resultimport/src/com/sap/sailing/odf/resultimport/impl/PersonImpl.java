package com.sap.sailing.odf.resultimport.impl;

import org.w3c.dom.Node;

import com.sap.sailing.odf.resultimport.Person;

public class PersonImpl extends NamedImpl implements Person {

    public PersonImpl(Node node) {
        super(node);
    }

    @Override
    public Gender getGender() {
        return Gender.valueOf(getNode().getAttributes().getNamedItem("name").getNodeValue());
    }

    @Override
    public String toString() {
        return getName();
    }
}
