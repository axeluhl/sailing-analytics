package com.sap.sailing.gwt.ui.datamining.selection;

import com.sap.sailing.datamining.shared.SharedDimension;

public class SimpleSelectionTable<ContentType> extends SelectionTable<ContentType, ContentType> {

    public SimpleSelectionTable(String title, SharedDimension dimension) {
        super(title, dimension);
    }

    @Override
    public ContentType getValue(ContentType content) {
        return content;
    }

}
