package com.sap.sse.gwt.client.celltable;

import com.google.gwt.user.cellview.client.Column;

public class ImagesBarColumn<T, S extends ImagesBarCell> extends Column<T, String> {
    public ImagesBarColumn(S imagesBarCell) {
        super(imagesBarCell);
    }

    /**
     * All actions are allowed by default because this method returns the wildcard string "*".
     * Subclasses may choose to restrict this to specific actions, based on context or security
     * aspects.
     */
    @Override
    public String getValue(T object) {
        return "*";
    }
}