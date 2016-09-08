package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.ui.ValueListBox;
import com.sap.sse.common.filter.Filter;

public abstract class AbstractSelectionFilter<T, C> extends AbstractFilterWidget<T, C> {
    
    private final ValueListBox<C> valueListBox = new ValueListBox<>(new AbstractRenderer<C>() {
        @Override
        public String render(C object) {
            return getFilterItemLabel(object);
        }
    });
    
    protected AbstractSelectionFilter() {
        valueListBox.addValueChangeHandler(new ValueChangeHandler<C>() {
            @Override
            public void onValueChange(ValueChangeEvent<C> event) {
                valueListBox.getElement().blur();
                notifyValueChangeHandlers();
            }
        });
        initWidget(valueListBox);
    }
    
    @Override
    public void setSelectableValues(Collection<C> selectableValues) {
        List<C> acceptableValues = new ArrayList<>(selectableValues);
        acceptableValues.add(0, null);
        C previouslySelectedValue = valueListBox.getValue();
        valueListBox.setValue(null);
        valueListBox.setAcceptableValues(acceptableValues);
        if (previouslySelectedValue != null && selectableValues.contains(previouslySelectedValue)) {
            valueListBox.setValue(previouslySelectedValue);
        }
    }
    
    @Override
    public Filter<T> getFilter() {
        return new SelectionFilter();
    }
    
    protected abstract C getFilterCriteria(T object);
    
    protected abstract String getFilterItemLabel(C item);
    
    protected C getSelectedValue() {
        return valueListBox.getValue();
    }

    private class SelectionFilter implements Filter<T> {
        @Override
        public boolean matches(T object) {
            C selectedFilterItem = getSelectedValue();
            return selectedFilterItem == null || selectedFilterItem.equals(getFilterCriteria(object));
        }

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }
    }

}
