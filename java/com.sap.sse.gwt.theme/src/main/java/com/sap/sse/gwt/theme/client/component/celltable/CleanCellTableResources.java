package com.sap.sse.gwt.theme.client.component.celltable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Style;

/**
 * {@link ClientBundle} with avoids the standard {@link CellTable} styles.
 */
public interface CleanCellTableResources extends CellTable.Resources {

    public static final CleanCellTableResources INSTANCE = GWT.create(CleanCellTableResources.class);

    @Override
    @Source("CleanCellTable.css")
    public Style cellTableStyle();

}
