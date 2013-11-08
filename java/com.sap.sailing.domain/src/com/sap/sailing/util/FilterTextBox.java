package com.sap.sailing.util;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.Named;

public class FilterTextBox extends TextBox {

    ListDataProvider<Named> data;
    List<Named> availableData;
    CellTable<Named> table;
    String sortingCriteria;

    public FilterTextBox(ListDataProvider<Named> data, List<Named> availableData, CellTable<Named> table, String sortingCriteria) {
        this.data = data;
        this.availableData = availableData;
        this.table = table;
        this.sortingCriteria = sortingCriteria;
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
            for (Named obj : availableData) {
                boolean failed = false;
                for (String word : wordsToFilter) {
                    String textAsUppercase = word.toUpperCase().trim();
                    if (!obj.getName().toUpperCase().contains(textAsUppercase)) {
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
