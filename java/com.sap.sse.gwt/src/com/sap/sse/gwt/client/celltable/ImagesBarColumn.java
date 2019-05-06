package com.sap.sse.gwt.client.celltable;

import com.google.gwt.user.cellview.client.Column;

public class ImagesBarColumn<T, S extends ImagesBarCell> extends Column<T, String> {
    public ImagesBarColumn(S imagesBarCell) {
        super(imagesBarCell);
    }

    @Override
    public String getValue(T object) {
        return "*";
    }
}