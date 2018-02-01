package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.cellview.client.Column;
import com.sap.sailing.gwt.ui.client.shared.controls.ImagesBarCell;

public class ImagesBarColumn<T, S extends ImagesBarCell> extends Column<T, String> {
    public ImagesBarColumn(S imagesBarCell) {
        super(imagesBarCell);
    }

    @Override
    public String getValue(T object) {
        return "";
    }
}