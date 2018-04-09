package com.sap.sse.gwt.client.controls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.ListBox;

public class GenericListBox<T> extends ListBox implements TakesValue<T> {
    private final List<T> items;
    private final ValueBuilder<T> valueBuilder;
    
    public interface ValueBuilder<T> {
        String getValue(T item);
    }
    
    public GenericListBox(List<T> items, ValueBuilder<T> valueBuilder) {
        this.items = new ArrayList<T>(items);
        this.valueBuilder = valueBuilder;
    }
    
    public GenericListBox(ValueBuilder<T> valueBuilder) {
        this(Collections.<T>emptyList(), valueBuilder);
    }
    
    public void addItem(T item) {
        items.add(item);
        addItem(valueBuilder.getValue(item));
    }
    
    public void addItems(Collection<T> items) {
        for (T item : items) {
            addItem(item);
        }
    }
    
    @Override
    public void setValue(T value) {
        int i = items.indexOf(value);
        if (i > -1) {
            setSelectedIndex(i);
        }
    }
    
    public void setValues(List<T> values) {
        int i = 0;
        for (T item : items) {
            setItemSelected(i++, values.contains(item));
        }
    }

    @Override
    public T getValue() {
        int i = getSelectedIndex();
        if (i < 0) {
            return null;
        }
        return items.get(i);
    }
    
    public ArrayList<T> getValues() {
        ArrayList<T> result = new ArrayList<>();
        int i = 0;
        for (T item : items) {
            if (isItemSelected(i++)) {
                result.add(item);
            }
        }
        return result;
    }
    
    @Override
    public void setItemSelected(int index, boolean selected) {
        super.setItemSelected(index, selected);

    }
}
