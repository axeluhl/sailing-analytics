package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.cellview.client.CellTable;

public interface TagButtonCellTableResources extends CellTable.Resources {

    public static final TagButtonCellTableResources INSTANCE = GWT.create(TagCellListResources.class);

    @Override
    @Source("tag-button-celltable.gss")
    public CellTable.Style cellTableStyle();
}
