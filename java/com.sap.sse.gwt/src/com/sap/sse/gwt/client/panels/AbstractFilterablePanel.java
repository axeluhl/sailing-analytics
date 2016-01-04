package com.sap.sse.gwt.client.panels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;

/**
 * This Panel contains a text box. Text entered into the text box filters the {@link CellTable} passed to
 * the constructor by adjusting the cell table's {@link ListDataProvider}'s contents using the {@link
 * applyFilter(String, List)} of the {@link AbstractListFilter} and then sorting the table again according the the
 * sorting criteria currently active (the sorting is the only reason why the {@link CellTable} actually needs to be
 * known to an instance of this class). To be initiated the method {@link #getSearchableStrings(Object)} has to be
 * defined, which gets those Strings from a <code>T</code> that should be considered when filtering, e.g. name or
 * boatClass. The cell table can be sorted independently from the text box (e.g. after adding new objects) by calling
 * the method {@link #updateAll(Iterable)} which then runs the filter over the new selection.<p>
 * 
 * Note that this panel does <em>not</em> contain the table that it filters. With this, this class's clients are free
 * to position the table wherever they want, not necessarily related to the text box provided by this panel in any
 * specific way.
 * 
 * @param <T>
 * @author Nicolas Klose, Axel Uhl
 * 
 */
public abstract class AbstractFilterablePanel<T> extends HorizontalPanel {
    protected Collection<T> all;
    protected final AbstractCellTable<T> display;
    protected final ListDataProvider<T> filtered;
    protected final TextBox textBox;
    
    private final AbstractListFilter<T> filterer = new AbstractListFilter<T>(){
        @Override
        public Iterable<String> getStrings(T t) {
            return getSearchableStrings(t);
        }
    };
    
    /**
     * @param all
     *            the sequence of all objects that may be displayed in the table and from which the filter may choose.
     *            This panel keeps a copy, so modifications to the <code>all</code> object do not reflect in the table
     *            contents. Use {@link #updateAll(Iterable)} instead to update the sequence of available objects.
     */
    public AbstractFilterablePanel(Iterable<T> all, AbstractCellTable<T> display, final ListDataProvider<T> filtered) {
        setSpacing(5);
        this.display = display;
        this.filtered = filtered;
        this.textBox = new TextBox();
        this.textBox.ensureDebugId("FilterTextBox");
        this.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        setAll(all);
        add(getTextBox());
        getTextBox().addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                filter();
            }
        });
    }
    
    private void setAll(Iterable<T> all) {
        this.all = new ArrayList<T>();
        if (all != null) {
            for (T t : all) {
                this.all.add(t);
            }
        }
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
    public void updateAll(Iterable<T> all){
        setAll(all);
        filter();
    }
    
    /**
     * Adds an object and applies the search filter.
     */
    public void add(T object) {
        all.add(object);
        filter();
    }
    
    public void addAll(Iterable<T> objects) {
        Util.addAll(objects, all);
        filter();
    }
    
    public void filter() {
        filtered.getList().clear();
        Util.addAll(filterer.applyFilter(Arrays.asList(getTextBox().getText().split(" ")), all), filtered.getList());
        filtered.refresh();
        sort();
    }
   
    private void sort(){
        ColumnSortEvent.fire(display, display.getColumnSortList());
    }

    public TextBox getTextBox() {
        return textBox;
    }
    
    public Iterable<T> getAll() {
        return all;
    }
}