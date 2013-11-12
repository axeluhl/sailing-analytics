package com.sap.sailing.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class FilterableTable<T> extends Panel {

    List<T> all;
    CellTable<T> display;
    Iterable<String> fieldsByWhichToFilter;
    ListDataProvider<T> filtered;
    TextBox textBox = new TextBox();

    public FilterableTable(Label label, List<T> all, CellTable<T> display, Iterable<String> fieldsByWhichToFilter,
            ListDataProvider<T> filtered) {
        this.all = all;
        this.display = display;
        this.fieldsByWhichToFilter = fieldsByWhichToFilter;
        this.filtered = filtered;
        add(label);
        add(textBox);
        textBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                applyFilter();
            }
        });
    }

    public boolean filter(T obj, List<String> wordsToFilter) {
        for (String field : fieldsByWhichToFilter) {
            boolean failed = false;
            for (String word : wordsToFilter) {
                String textAsUppercase = word.toUpperCase().trim();
                try {
                    if (!obj.getClass().getField(field).get(obj).toString().toUpperCase().contains(textAsUppercase)) {
                        failed = true;
                    }
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                    failed = true;
                }
            }
            if (!failed) {
                return true;
            }
        }
        return false;
    }

    public void applyFilter() {
        String text = textBox.getText();
        List<String> wordsToFilter = Arrays.asList(text.split(" "));
        filtered.getList().clear();
        if (text != null && !text.isEmpty()) {
            for (T obj : all) {
                if (filter(obj, wordsToFilter)) {
                    filtered.getList().add(obj);
                }
            }
        } else {
            filtered.getList().addAll(all);
        }
        // now sort again according to selected criterion
        ColumnSortEvent.fire(display, display.getColumnSortList());
    }

    @Override
    public Iterator<Widget> iterator() {
        return this.iterator();
    }

    @Override
    public boolean remove(Widget child) {
        return this.remove(child);
    }
}
