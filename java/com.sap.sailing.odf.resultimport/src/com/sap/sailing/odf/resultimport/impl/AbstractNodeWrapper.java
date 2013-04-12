package com.sap.sailing.odf.resultimport.impl;

import java.util.Map;
import java.util.TreeMap;

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
     * Obtains the node's <code>ExtendedResults</code> element and reads the <code>ExtendedResult</code> elements inside
     * whose <code>Type</code> attribute equals <code>type</code>. The result is a map whose keys are the
     * <code>Code</code> and whose values are iterables containing the one or more <code>Value</code> fields of the
     * <code>ExtendedResult</code> elements with the key code. If there is a <code>Pos</code> field set, the iteration
     * order will be in incrementing order of the <code>Pos</code> attribute value. 
     */
    protected Map<String, TreeMap<Integer, String>> getExtendedResults(String type) {
        Map<String, TreeMap<Integer, String>> orderedMap = new TreeMap<>();
        Element element = (Element) getNode();
        NodeList extendedResultsNodes = element.getElementsByTagName("ExtendedResults");
        int defaultPos = 0;
        for (int i=0; i<extendedResultsNodes.getLength(); i++) {
            Node extendedResultsNode = extendedResultsNodes.item(i);
            NodeList extendedResults = ((Element) extendedResultsNode).getElementsByTagName("ExtendedResult");
            for (int j = 0; j < extendedResults.getLength(); j++) {
                Node extendedResult = extendedResults.item(j);
                final NamedNodeMap attributes = extendedResult.getAttributes();
                final int pos;
                Node nodeType = attributes.getNamedItem("Type");
                if (nodeType == null || type.equals(nodeType.getNodeValue())) {
                    if (attributes.getNamedItem("Pos") != null) {
                        pos = Integer.valueOf(attributes.getNamedItem("Pos").getNodeValue());
                    } else {
                        pos = defaultPos++;
                    }
                    final String key = attributes.getNamedItem("Code").getNodeValue();
                    TreeMap<Integer, String> entry = orderedMap.get(key);
                    if (entry == null) {
                        entry = new TreeMap<>();
                        orderedMap.put(key, entry);
                    }
                    entry.put(pos, attributes.getNamedItem("Value").getNodeValue());
                }
            }
        }
        return orderedMap;
    }
}
