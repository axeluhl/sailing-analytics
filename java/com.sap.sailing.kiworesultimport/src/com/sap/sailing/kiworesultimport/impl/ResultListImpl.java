package com.sap.sailing.kiworesultimport.impl;

import org.w3c.dom.Node;

import com.sap.sailing.kiworesultimport.ResultList;

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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBoatClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getEvent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDate() {
        // TODO Auto-generated method stub
        return null;
    }
}
