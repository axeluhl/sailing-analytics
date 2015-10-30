package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.Collection;

import com.sap.sse.common.filter.Filter;

public abstract class AbstractSelectionFilter<T, C> extends AbstractFilterWidget<T, C> {
    
    @Override
    public void setSelectableValues(Collection<C> selectableValues) {
        addFilterItem(null);
        for (C value : selectableValues) {
            addFilterItem(value);
        }
    }
    
    @Override
    public Filter<T> getFilter() {
        return new SelectionFilter() ;
    }
    
    protected abstract void addFilterItem(C value);
    
    protected abstract C getFilterCriteria(T object);
    
    protected abstract C getSelectedValue();
    
    private class SelectionFilter implements Filter<T> {
        @Override
        public boolean matches(T object) {
            C selectedValue = getSelectedValue();
            return selectedValue == null || selectedValue.equals(getFilterCriteria(object));
        }

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }
    }

}
