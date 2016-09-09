package com.sap.sse.gwt.client.panels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sse.common.Util;

/**
 * Customizable filter panel which allows to create a flexible filter panel with various number of filter inputs (e.g.
 * combo box and text box). Need to set filter value on each input field change handler.
 * 
 * @param <T>
 * @author Oleg Zheleznov
 * 
 */
public abstract class CustomizableFilterablePanel<T> extends AbstractFilterablePanel<T> {

    private List<Widget> widgets;
    private Map<Widget, String[]> values;

    public CustomizableFilterablePanel(Iterable<T> all, AbstractCellTable<T> display, ListDataProvider<T> filtered) {
        super(all, display, filtered);
        this.widgets = new ArrayList<>();
        this.values = new HashMap<>();
        remove(getTextBox());
    }

    private List<String> getFilterValue(Widget widget) {
        return values.containsKey(widget) ? Arrays.asList(values.get(widget)) : new ArrayList<String>();
    }

    public void setFilterValue(Widget widget, String... value) {
        values.put(widget, value);
    }

    public void add(Label label, Widget widget) {
        widgets.add(widget);
        add(label);
        add(widget);
        setCellVerticalAlignment(label, HasVerticalAlignment.ALIGN_MIDDLE);
    }

    @Override
    public void filter() {
        filtered.getList().clear();
        List<String> keywords = new ArrayList<>();
        for (Widget widget : widgets) {
            keywords.addAll(getFilterValue(widget));
        }
        Util.addAll(filterer.applyFilter(keywords, all.getList()), filtered.getList());
        filtered.refresh();
        sort();
    }

    public void clearFilter() {
        filtered.getList().clear();
        Util.addAll(all.getList(), filtered.getList());
    }

}
