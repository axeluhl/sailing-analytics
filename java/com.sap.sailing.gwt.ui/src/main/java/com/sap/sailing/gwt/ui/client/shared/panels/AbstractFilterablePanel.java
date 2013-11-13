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

/**
 * This Panel contains a label and a textbox. Text entered into the TextBox filters the given CellTable<T>. To be
 * initiated the method <code>getStrings(T t)</code> has to be defined, which gets those Strings from <code>T</code>
 * that should be considered when filtering, e.g. name or boatClass. The method <code>applyFilter()</code> can be called
 * outside of this Panel (e.g. after loading), but then the method <code>updateAll(List<T> all)</code> should be called
 * to ensure the correct selection is filtered.
 * 
 * @param <T>
 * @author Nicolas Klose
 * 
 */
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

    /**
     * Returns <code>true</code> if <code>wordsToFilter</code> contain a value of the <code>valuesToCheck</code>
     * 
     * @param wordsToFilter
     *            the words to filter on
     * @param valuesToCheck
     *            the values to check for. These values contain the values of the current rows.
     * @return <code>true</code> if the <code>valuesToCheck</code> contains all <code>wordsToFilter</code>,
     *         <code>false</code> if not
     */
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
