package com.sap.sailing.gwt.common.theme.component.celltable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.user.cellview.client.CellTable;

/**
 * {@link ClientBundle} with avoids the standard {@link CellTable} styles.
 */
public interface DesignedCellTableResources extends CellTable.Resources {

    public static final DesignedCellTableResources INSTANCE = GWT.create(DesignedCellTableResources.class);
    
    public interface DesignedCellTableStyle extends CellTable.Style {
        String buttonCell();
    }

    @Override
    @Source({"CleanCellTable.css", "DesignedCellTable.css"})
    public DesignedCellTableStyle cellTableStyle();

}
