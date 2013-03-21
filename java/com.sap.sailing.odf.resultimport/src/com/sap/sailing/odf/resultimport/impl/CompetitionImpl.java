package com.sap.sailing.odf.resultimport.impl;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.sailing.odf.resultimport.Competition;
import com.sap.sailing.odf.resultimport.CumulativeResult;

public class CompetitionImpl extends AbstractNodeWrapper implements Competition {

    public CompetitionImpl(Node node) {
        super(node);
    }

    @Override
    public Iterable<CumulativeResult> getCumulativeResults() {
        List<CumulativeResult> result = new ArrayList<>();
        Element element = (Element) getNode();
        NodeList cumulativeResults = element.getElementsByTagName("CumulativeResult");
        for (int i=0; i<cumulativeResults.getLength(); i++) {
            result.add(new CumulativeResultImpl(cumulativeResults.item(i)));
        }
        return result;
    }

}
