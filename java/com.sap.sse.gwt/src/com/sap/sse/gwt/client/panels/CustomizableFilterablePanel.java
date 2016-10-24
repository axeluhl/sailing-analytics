package com.sap.sse.gwt.client.panels;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sse.common.filter.Filter;

/**
 * Customizable filter panel which allows to create a flexible filter panel with various number of filter inputs (e.g.
 * combo box and text box). Need to set filter value on each input field change handler.
 * 
 * @param <T>
 * @author Oleg Zheleznov
 * 
 */
public abstract class CustomizableFilterablePanel<T> extends AbstractFilterablePanel<T> {

    private final Set<Filter<T>> filters;

    private final Filter<T> compositeFilter = new CompositeFilter();

    public CustomizableFilterablePanel(Iterable<T> all, AbstractCellTable<T> display, ListDataProvider<T> filtered) {
        super(all, display, filtered, /* show default filter text box */ false);
        this.filters = new HashSet<>();
    }

    public void add(Label label, Widget widget, Filter<T> filter) {
        add(label);
        add(widget);
        filters.add(filter);
        setCellVerticalAlignment(label, HasVerticalAlignment.ALIGN_MIDDLE);
    }

    @Override
    protected Filter<T> getFilter() {
        return compositeFilter;
    }

    public boolean matches(T t) {
        boolean matches = super.getFilter().matches(t);
        for (final Filter<T> filter : filters) {
            matches &= filter.matches(t);
        }
        return matches;
    }

    private class CompositeFilter implements Filter<T> {
        @Override
        public boolean matches(T object) {
            return CustomizableFilterablePanel.this.matches(object);
        }

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }
    }
}
