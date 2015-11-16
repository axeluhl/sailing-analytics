package com.sap.sse.gwt.client.celltable;

public interface HasEqualIdentity<T> {
    /**
     * Compares the Objects and return <code> true </code>, if the objects have the same Identity.
     */
    public boolean compare(T o1, T o2);
}