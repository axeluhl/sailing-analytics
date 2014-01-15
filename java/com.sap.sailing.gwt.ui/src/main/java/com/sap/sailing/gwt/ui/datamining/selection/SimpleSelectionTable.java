package com.sap.sailing.gwt.ui.datamining.selection;

import com.sap.sailing.datamining.shared.DimensionIdentifier;

public class SimpleSelectionTable<ContentType> extends SelectionTable<ContentType, ContentType> {

    public SimpleSelectionTable(String title, DimensionIdentifier dimension) {
        super(title, dimension);
    }

    @Override
    public ContentType getValue(ContentType content) {
        return content;
    }

}
