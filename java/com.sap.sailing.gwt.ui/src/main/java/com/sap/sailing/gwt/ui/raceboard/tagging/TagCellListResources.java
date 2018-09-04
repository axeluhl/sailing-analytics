package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.cellview.client.CellList;

/**
 * Interface for styling {@link TaggingPanel#tagCellList cell list} containing {@link TagCell} at
 * {@link TaggingPanel#contentPanel TaggingPanel}.
 */
public interface TagCellListResources extends CellList.Resources {
    public static final TagCellListResources INSTANCE = GWT.create(TagCellListResources.class);

    @Override
    @Source("tag-celllist.gss")
    public CellList.Style cellListStyle();
}
