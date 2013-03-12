package com.sap.sailing.odf.resultimport.impl;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AbstractNodeWrapper {
    private final Node node;

    public AbstractNodeWrapper(Node node) {
        super();
        this.node = node;
    }
    
    protected Node getNode() {
        return node;
    }
    
    /**
     * Obtains the node's <code>ExtendedResults</code> element and reads the <code>ExtendedResult</code> elements inside whose <code>Type</code>
     * attribute equals <code>type</code>. The result is a map whose keys are the <code>Code</code> and whose values are the <code>Value</code> field
     * of the <code>ExtendedResult</code> element. 
     */
    protected Map<String, String> getExtendedResults(String type) {
        Map<String, String> result = new HashMap<>();
        Element element = (Element) getNode();
        NodeList extendedResults = element.getElementsByTagName("ExtendedResults");
        for (int i=0; i<extendedResults.getLength(); i++) {
            Node extendedResult = extendedResults.item(i);
            NamedNodeMap attributes = extendedResult.getAttributes();
            System.out.println(attributes);
        }
        return result;
    }
}
