package com.sap.sailing.kiworesultimport.impl;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sap.sailing.kiworesultimport.ResultList;
import com.sap.sailing.kiworesultimport.Verteilung;

public class ResultListImpl extends AbstractNodeWrapper implements ResultList {
    public ResultListImpl(Node node) {
        super(node);
    }

    @Override
    public String getLegende() {
        return getNode().getAttributes().getNamedItem("legende").getNodeValue();
    }

    @Override
    public String getImagePfad() {
        return getNode().getAttributes().getNamedItem("imagePfad").getNodeValue();
    }

    @Override
    public String getStatus() {
        return getNode().getAttributes().getNamedItem("status").getNodeValue();
    }

    @Override
    public String getBoatClass() {
        return getNode().getAttributes().getNamedItem("class").getNodeValue();
    }

    @Override
    public String getEvent() {
        return getNode().getAttributes().getNamedItem("event").getNodeValue();
    }

    @Override
    public String getTime() {
        return getNode().getAttributes().getNamedItem("time").getNodeValue();
    }

    @Override
    public String getDate() {
        return getNode().getAttributes().getNamedItem("date").getNodeValue();
    }

    @Override
    public Verteilung getVerteilung() {
        return new VerteilungImpl(((Element) getNode()).getElementsByTagName("Verteilung").item(0));
    }
}
