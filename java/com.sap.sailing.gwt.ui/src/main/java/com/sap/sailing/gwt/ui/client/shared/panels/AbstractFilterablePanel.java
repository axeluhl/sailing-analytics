package com.sap.sailing.gwt.ui.client.shared.panels;

import java.util.Arrays;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.client.shared.filter.AbstractListFilter;

/**
 * This Panel contains a label and a text box. Text entered into the text box filters the {@link CellTable} passed to
 * the constructor by adjusting the cell table's {@link ListDataProvider}'s contents and then sorting the table again
 * according the the sorting criteria currently active (the sorting is the only reason why the {@link CellTable}
 * actually needs to be known to an instance of this class). To be initiated the method
 * {@link #getSearchableStrings(Object)} has to be defined, which gets those Strings from a <code>T</code> that should
 * be considered when filtering, e.g. name or boatClass. The cell table can be sorted independently from the text box 
 * (e.g. after adding new objects) by calling the method  {@link #updateAll(Iterable)} which then runs the filter over 
 * the new selection.
 * 
 * @param <T>
 * @author Nicolas Klose, Axel Uhl
 * 
 */
public abstract class AbstractFilterablePanel<T> extends HorizontalPanel {
    private Iterable<T> all;
    private final CellTable<T> display;
    private final ListDataProvider<T> filtered;
    private final TextBox textBox;
    private final AbstractListFilter<T> filterer = new AbstractListFilter<T>(){

        @Override
        public Iterable<String> getStrings(T t) {
            return getSearchableStrings(t);
        }
    };

    public AbstractFilterablePanel(Label label, Iterable<T> all, CellTable<T> display, final ListDataProvider<T> filtered) {
        setSpacing(5);
        this.display = display;
        this.filtered = filtered;
        this.textBox = new TextBox();
        this.all = all;
        add(label);
        add(getTextBox());
        getTextBox().addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                filter();
            }
        });
    }

    /**
     * Subclasses must implement this to extract the strings from an object of type <code>T</code> based on which the
     * filter performs its filtering
     * 
     * @param t
     *            the object from which to extract the searchable strings
     * @return the searchable strings
     */
    public abstract Iterable<String> getSearchableStrings(T t);

    /**
     * Updates the set of all objects to be shown in the table and applies the search filter to update the
     * table view.
     */
    
    public void upDateAll(Iterable<T> all){
        this.all = all;
        filter();
    }
    
    public void filter(){
        filtered.getList().clear(); 
        filtered.getList().addAll(filterer.applyFilter(Arrays.asList(getTextBox().getText().split(" ")), all));
        sort();
    }
   
    private void sort(){
        ColumnSortEvent.fire(display, display.getColumnSortList());
    }

    public TextBox getTextBox() {
        return textBox;
    }
}