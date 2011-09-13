package com.sap.sailing.gwt.ui.client;

import com.google.gwt.cell.client.TextCell;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;

public class StringRenderingLegDetailColumn<FieldType> extends LegDetailColumn<FieldType, String> {

    protected StringRenderingLegDetailColumn(String title,
            com.sap.sailing.gwt.ui.client.LegDetailColumn.LegDetailField<FieldType> field) {
        super(title, field, new TextCell());
    }

    @Override
    public String getValue(LeaderboardRowDAO row) {
        FieldType fieldValue = getField().get(row);
        return fieldValue == null ? "" : fieldValue.toString();
    }
}
