package com.sap.sailing.odf.resultimport.impl;

import org.w3c.dom.Node;

import com.sap.sailing.odf.resultimport.Named;

public class NamedImpl extends AbstractNodeWrapper implements Named {

    public NamedImpl(Node node) {
        super(node);
    }

    @Override
    public String getName() {
        return getExtendedResults("ER_SA").get("SA_NAME").firstEntry().getValue();
    }

    @Override
    public String toString() {
        return getName();
    }
}
