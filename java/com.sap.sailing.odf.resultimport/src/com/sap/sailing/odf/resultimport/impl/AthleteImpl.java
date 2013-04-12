package com.sap.sailing.odf.resultimport.impl;

import org.w3c.dom.Node;

import com.sap.sailing.odf.resultimport.Athlete;

public class AthleteImpl extends NamedImpl implements Athlete {

    public AthleteImpl(Node node) {
        super(node);
    }

    @Override
    public Gender getGender() {
        return Gender.valueOf(getExtendedResults("ER_SA").get("SA_GENDER").firstEntry().getValue());
    }

    @Override
    public String toString() {
        return getName()+" ("+getGender()+")";
    }
}
