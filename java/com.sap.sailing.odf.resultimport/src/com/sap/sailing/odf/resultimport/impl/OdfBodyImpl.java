package com.sap.sailing.odf.resultimport.impl;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.sailing.odf.resultimport.Competition;
import com.sap.sailing.odf.resultimport.OdfBody;

public class OdfBodyImpl extends AbstractNodeWrapper implements OdfBody {
    public OdfBodyImpl(Node node) {
        super(node);
    }

    @Override
    public Iterable<Competition> getCompetitions() {
        List<Competition> result = new ArrayList<>();
        Element element = (Element) getNode();
        NodeList cumulativeResults = element.getElementsByTagName("Competition");
        for (int i=0; i<cumulativeResults.getLength(); i++) {
            result.add(new CompetitionImpl(cumulativeResults.item(i)));
        }
        return result;
    }
}
