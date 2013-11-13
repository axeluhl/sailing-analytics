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
 * This Panel contains a label and a text box. Text entered into the text box filters the given CellTable. To be
 * initiated the method {@link #getSearchableStrings(Object)} has to be defined, which gets those Strings from a
 * <code>T</code> that should be considered when filtering, e.g. name or boatClass. The method {@link #applyFilter()}
 * can be called outside of this Panel (e.g. after loading), but then the method {@link #updateAll(Iterable)} should be
 * called to ensure the correct selection is filtered.
 * 
 * @param <T>
 * @author Nicolas Klose, Axel Uhl
 * 
 */
public abstract class AbstractFilterablePanel<T> extends HorizontalPanel {
    private Iterable<T> all;
    private final CellTable<T> display;
    private final ListDataProvider<T> filtered;
    private final TextBox textBox = new TextBox();

    public AbstractFilterablePanel(Label label, Iterable<T> all, CellTable<T> display, ListDataProvider<T> filtered) {
        setSpacing(5);
        this.all = all;
        this.display = display;
        this.filtered = filtered;
        add(label);
        add(getTextBox());
        getTextBox().addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                applyFilter();
            }
        });
    }

    /**
     * Subclasses must implement this to extract the strings from an object of type <code>T</code> based on which the
     * search box performs its filtering
     * 
     * @param t
     *            the object from which to extract the searchable strings
     * @return the searchable strings
     */
    protected abstract Iterable<String> getSearchableStrings(T t);

    /**
     * Updates the set of all objects to be shown in the table and applies the search filter to update the
     * table view.
     */
    public void updateAll(Iterable<T> all) {
        this.all = all;
        applyFilter();
    }

    /**
     * Reconstructs the {@link #filtered} list contents based on the contents of {@link #all} as provided through
     * {@link #updateAll(Iterable)} and the current search phrase entered in the search {@link #textBox text box}. After
     * filtering, the original sort order is restored.
     */
    public void applyFilter() {
        String text = getTextBox().getText();
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
        for (String s : getSearchableStrings(obj)) {
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

    public TextBox getTextBox() {
        return textBox;
    }
}