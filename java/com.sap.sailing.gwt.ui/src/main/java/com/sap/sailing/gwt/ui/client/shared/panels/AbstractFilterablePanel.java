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

    List<T> all;
    CellTable<T> display;
    ListDataProvider<T> filtered;
    TextBox textBox = new TextBox();

    public AbstractFilterablePanel(Label label, List<T> all, CellTable<T> display, ListDataProvider<T> filtered) {
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

    public void updateAll(List<T> all) {
        this.all = all;
    }

    public void applyFilter() {
        String text = textBox.getText();
        List<String> inputText = Arrays.asList(text.split(" "));
        filtered.getList().clear();
        if (text != null && !text.isEmpty()) {
            for (int i = 0; i < all.size(); i++) {
                if (filter(all.get(i), inputText)) {
                    filtered.getList().add(all.get(i));
                }
            }
        } else {
            filtered.getList().addAll(all);
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
    
    List<String> getStrings(T t);

}
