package com.sap.sailing.util;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;

public class FilterTextBox<T> extends TextBox {

    ListDataProvider<T> filteredData;
    List<T> availableData;
    CellTable<T> display;
    List<String> strings;

    public FilterTextBox(ListDataProvider<T> filteredData, List<T> availableData, CellTable<T> display,
            List<String> strings) {
        this.filteredData = filteredData;
        this.availableData = availableData;
        this.display = display;
        this.strings = strings;

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
        filteredData.getList().clear();
        if (text != null && !text.isEmpty()) {
            for (T obj : availableData) {
                boolean failed = false;
                for (String word : wordsToFilter) {
                    String textAsUppercase = word.toUpperCase().trim();
                    for (String s : strings) {
                        try {
                            if (((String) obj.getClass().getField(s).get(obj)).toUpperCase().contains(textAsUppercase)) {
                                failed = true;
                                break;
                            }
                        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                                | SecurityException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                if (!failed) {
                    filteredData.getList().add(obj);
                }
            }
        } else {
            filteredData.getList().addAll(availableData);
        }
        // now sort again according to selected criterion
        ColumnSortEvent.fire(display, display.getColumnSortList());
    }
}
