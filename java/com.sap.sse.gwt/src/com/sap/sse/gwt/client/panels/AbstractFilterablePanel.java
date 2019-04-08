package com.sap.sse.gwt.client.panels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractKeywordFilter;
import com.sap.sse.common.filter.AbstractListFilter;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;

/**
 * This Panel contains a text box. Text entered into the text box filters the {@link CellTable} passed to the
 * constructor by adjusting the cell table's {@link ListDataProvider}'s contents using the {@link applyFilter(String,
 * List)} of the {@link AbstractListFilter} and then sorting the table again according the the sorting criteria
 * currently active (the sorting is the only reason why the {@link CellTable} actually needs to be known to an instance
 * of this class). To be initiated the method {@link #getSearchableStrings(Object)} has to be defined, which gets those
 * Strings from a <code>T</code> that should be considered when filtering, e.g. name or boatClass. The cell table can be
 * sorted independently from the text box (e.g., after adding new objects) by calling the method
 * {@link #updateAll(Iterable)} which then runs the filter over the new selection.
 * <p>
 * 
 * Note that this panel does <em>not</em> contain the table that it filters. With this, this class's clients are free to
 * position the table wherever they want, not necessarily related to the text box provided by this panel in any specific
 * way.<p>
 * 
 * It is recommended to use the {@link #getAllListDataProvider()} as the data provider for the table's selection model.
 * This way, when the filter reduces the elements displayed in the table the selection will still refer to all elements
 * and will not be modified solely by the act of filtering.
 * 
 * @param <T>
 * @author Nicolas Klose, Axel Uhl
 * 
 */
public abstract class AbstractFilterablePanel<T> extends HorizontalPanel {
    protected ListDataProvider<T> all;
    protected final ListDataProvider<T> filtered;
    protected final TextBox textBox;
    
    private final Set<Filter<T>> filters = new HashSet<>();

    protected final AbstractKeywordFilter<T> filterer = new AbstractKeywordFilter<T>() {
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
     * @param drawTextBox
     *            if {@code true}, the default text box will be shown that can be used to provide the filter text; if
     *            {@code false}, the box may optionally be added later using the {@link #addDefaultTextBox()} method.
     *            This way, subclasses may choose to add other filter elements before the default text filter box.
     *            Filtering will still use the text box contents, even if the box is ultimately not shown; but under
     *            normal circumstances the text box will be empty in this case, not making filtering any stricter.
     */
    public AbstractFilterablePanel(Iterable<T> all, final ListDataProvider<T> filtered,
            boolean drawTextBox) {
        filters.add(filterer);
        setSpacing(5);
        this.all = new ListDataProvider<>();
        this.filtered = filtered;
        this.textBox = new TextBox();
        this.textBox.ensureDebugId("FilterTextBox");
        this.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        setAll(all);
        if (drawTextBox) {
            addDefaultTextBox();
        }
    }

    public AbstractFilterablePanel(Iterable<T> all, final ListDataProvider<T> filtered) {
        this(all, filtered, /* show default filter text box */ true);
    }

    private void setAll(Iterable<? extends T> all) {
        this.all.getList().clear();
        if (all != null) {
            for (T t : all) {
                this.all.getList().add(t);
            }
        }
    }
    
    public void addFilter(Filter<T> filterToAdd) {
        filters.add(filterToAdd);
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
     * Updates the set of all objects to be shown in the table and applies the search filter to update the table view.
     */
    public void updateAll(Iterable<? extends T> all) {
        setAll(all);
        filter();
    }

    /**
     * Adds an object and applies the search filter.
     */
    public void add(T object) {
        all.getList().add(object);
        filter();
    }

    /**
     * Adds an object at a certain position and applies the search filter.
     */
    public void add(int index, T object) {
        all.getList().add(index, object);
        filter();
    }

    /**
     * Returns the index of the first occurrence of the specified element in this list, or -1 if this list does not
     * contain the element.
     */
    public int indexOf(T object) {
        return all.getList().indexOf(object);
    }

    /**
     * Removes an object and applies the search filter.
     */
    public void remove(T object) {
        all.getList().remove(object);
        filter();
    }

    public void addAll(Iterable<T> objects) {
        Util.addAll(objects, all.getList());
        filter();
    }

    /**
     * Would better be called {@code clear()}, but {@link #clear} is already the method inherited from {@link Panel}...
     * This method removes all entries from this filterable panel. The effect is the same as invoking
     * <code>removeAll(all)</code> with <code>all</code> being a copy of what you get when calling {@link #getAll()}.
     */
    public void removeAll() {
        all.getList().clear();
        filter();
    }

    public void removeAll(Iterable<T> objects) {
        Util.removeAll(objects, all.getList());
        filter();
    }

    public void filter() {
        filtered.getList().clear();
        retainElementsInFilteredThatPassFilter();
        filtered.flush();
        sort();
    }
    

    public abstract AbstractCellTable<T> getCellTable();
   
    protected void retainElementsInFilteredThatPassFilter() {
        List<T> filteredElements = new ArrayList<>();
        for (T t : all.getList()) {
            if (matches(t)) {
                filteredElements.add(t);
            }
        }
        Util.addAll(filteredElements, filtered.getList());
    }

    private boolean matches(T t) {
        for (Filter<T> filter : filters) {
            if (!filter.matches(t)) {
                return false;
            }
        }
        return true;
    }

    protected void sort() {
        if (getCellTable() != null) {
            ColumnSortEvent.fire(getCellTable(), getCellTable().getColumnSortList());
        }
   }

    public void addDefaultTextBox() {
        add(getTextBox());
        getTextBox().addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                filterer.setKeywords(Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(getTextBox().getText()));
                filter();
            }
        });
    }

    public TextBox getTextBox() {
        return textBox;
    }

    public Iterable<T> getAll() {
        return all.getList();
    }

    /**
     * You can use this method to get the {@link ListDataProvider} that represents the all data structure. On this
     * {@link ListDataProvider} a {@link RefreshableSelectionModel} can be registered. So the selection can also be
     * maintained when the data is filtered.
     * 
     * @return The original all data structure. It's no copy.
     */
    public ListDataProvider<T> getAllListDataProvider() {
        return all;
    }

    /**
     * This method can be used to set the CellTable, when the {@link CellTable} on which this Panel should work is
     * created after this Panel. When the table isn't set correctly the order of elements, could be wrong after
     * filtering the data.
     * 
     * @param table
     *            {@link AbstractCellTable} on which this panel works.
     */
    /*
    public void setTable(AbstractCellTable<T> table) {
        display = table;
    }
    */
}