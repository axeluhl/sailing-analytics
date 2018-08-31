package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.cellview.client.CellList;

public interface TagCellListResources extends CellList.Resources {
    public static final TagCellListResources INSTANCE = GWT.create(TagCellListResources.class);

    @Override
    @Source("tagging-celllist.gss")
    public TagCellListStyle cellListStyle();

    public interface TagCellListStyle extends CellList.Style {
        String cellListEventItem();
        String cellListWidget();
        String cellListEvenItem();
        String cellListOddItem();
        String cellListSelectedItem();
        String cellListKeyboardSelectedItem();
    }
}
