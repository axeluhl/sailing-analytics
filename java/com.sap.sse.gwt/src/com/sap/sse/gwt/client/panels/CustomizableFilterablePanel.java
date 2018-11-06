package com.sap.sse.gwt.client.panels;

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
    /**
     * Does not add the default filter text box yet; use {@link #addDefaultTextBox()} to do so; you may
     * add other widgets before the default text filter box.
     */
    public CustomizableFilterablePanel(Iterable<T> all, ListDataProvider<T> filtered) {
        super(all, filtered, /* show default filter text box */ false);
    }

    public void add(Label label, Widget widget, Filter<T> filter) {
        add(label);
        add(widget);
        addFilter(filter);
        setCellVerticalAlignment(label, HasVerticalAlignment.ALIGN_MIDDLE);
    }
}
