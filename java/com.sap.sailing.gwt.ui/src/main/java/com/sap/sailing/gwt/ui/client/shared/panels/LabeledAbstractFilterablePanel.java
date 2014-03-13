package com.sap.sailing.gwt.ui.client.shared.panels;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.client.shared.filter.AbstractListFilter;

/**
 * This Panel contains a label and a text box. Text entered into the text box filters the {@link CellTable} passed to
 * the constructor by adjusting the cell table's {@link ListDataProvider}'s contents using the {@link
 * applyFilter(String, List)} of the {@link AbstractListFilter} and then sorting the table again according the the
 * sorting criteria currently active (the sorting is the only reason why the {@link CellTable} actually needs to be
 * known to an instance of this class). To be initiated the method {@link #getSearchableStrings(Object)} has to be
 * defined, which gets those Strings from a <code>T</code> that should be considered when filtering, e.g. name or
 * boatClass. The cell table can be sorted independently from the text box (e.g. after adding new objects) by calling
 * the method {@link #updateAll(Iterable)} which then runs the filter over the new selection.
 * 
 * @param <T>
 * @author Nicolas Klose, Axel Uhl
 * 
 */
public abstract class LabeledAbstractFilterablePanel<T> extends AbstractFilterablePanel<T> {
    
    public LabeledAbstractFilterablePanel(Label label, Iterable<T> all, CellTable<T> display, final ListDataProvider<T> filtered) {
        super(all, display, filtered);
        insert(label, 0);
    }
    
}