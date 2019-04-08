package com.sap.sailing.gwt.common.theme.component.celltable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.user.cellview.client.CellTable;

/**
 * {@link ClientBundle} with avoids the standard {@link CellTable} styles.
 */
public interface CleanCellTableResources extends CellTable.Resources {

    public static final CleanCellTableResources INSTANCE = GWT.create(CleanCellTableResources.class);
    
    public interface CleanCellTableStyle extends CellTable.Style {
    }

    @Override
    @Source("CleanCellTable.css")
    public CleanCellTableStyle cellTableStyle();

}
