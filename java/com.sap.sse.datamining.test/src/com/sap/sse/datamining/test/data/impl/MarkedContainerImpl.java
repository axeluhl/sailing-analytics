package com.sap.sse.datamining.test.data.impl;

public class MarkedContainerImpl implements MarkedContainer {

    private ContainerElement containerElement;

    public MarkedContainerImpl(ContainerElement containerElement) {
        this.containerElement = containerElement;
    }

    @Override
    public ContainerElement getContainerElement() {
        return containerElement;
    }

}
