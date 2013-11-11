package com.sap.sailing.util;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;

public class FilterTextBox extends TextBox {

    ListDataProvider<Object> data;
    List<Object> availableData;
    CellTable<Object> table;

    public FilterTextBox(ListDataProvider<?> data, List<?> availableData, CellTable<?> table) {
        
        addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                filter();
            }
        });
    }

    public void filter() {

        String text = getText();
        List<String> wordsToFilter = Arrays.asList(text.split(" "));
        data.getList().clear();
        if (text != null && !text.isEmpty()) {
            for (Object obj : availableData) {
                boolean failed = false;
                for (String word : wordsToFilter) {
                    String textAsUppercase = word.toUpperCase().trim();
                    if (!obj.toString().toUpperCase().contains(textAsUppercase)) {
                        failed = true;
                        break;
                    }
                }
                if (!failed) {
                    data.getList().add(obj);
                }
            }
        } else {
            data.getList().addAll(availableData);
        }
        // now sort again according to selected criterion
        ColumnSortEvent.fire(table, table.getColumnSortList());

    }
}
