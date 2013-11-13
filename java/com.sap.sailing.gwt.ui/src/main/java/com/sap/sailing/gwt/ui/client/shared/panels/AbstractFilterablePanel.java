package com.sap.sailing.gwt.ui.client.shared.panels;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;

public abstract class AbstractFilterablePanel<T> extends HorizontalPanel implements FilteringRule<T> {

    Iterable<T> all;
    CellTable<T> display;
    ListDataProvider<T> filtered;
    public TextBox textBox = new TextBox();

    public AbstractFilterablePanel(Label label, Iterable<T> all, CellTable<T> display, ListDataProvider<T> filtered) {
        setSpacing(5);
        this.all = all;
        this.display = display;
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

    public void updateAll(Iterable<T> all) {
        this.all = all;
    }

    public void applyFilter() {
        String text = textBox.getText();
        List<String> inputText = Arrays.asList(text.split(" "));
        filtered.getList().clear();
        if (text != null && !text.isEmpty()) {
            for (T t : all) {
                if (filter(t, inputText)) {
                    filtered.getList().add(t);
                }
            }
        } else {
            for (T t : all) {
                filtered.getList().add(t);
            }
        }
        // now sort again according to selected criterion
        ColumnSortEvent.fire(display, display.getColumnSortList());
    }

    private boolean filter(T obj, List<String> wordsToFilter) {
        for (String s : getStrings(obj)) {
            boolean failed = false;
            if (s == null) {
                failed = true;
            } else {
                for (String word : wordsToFilter) {
                    String textAsUppercase = word.toUpperCase().trim();
                    if (!s.toUpperCase().contains(textAsUppercase)) {
                        failed = true;
                        break;
                    }
                }
            }
            if (!failed) {
                return true;

            }
        }
        return false;
    }

}

abstract interface FilteringRule<T> {

    Iterable<String> getStrings(T t);

}
