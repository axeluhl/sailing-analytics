package com.sap.sse.gwt.client.panels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class FilterParameter implements Serializable {

    private static final long serialVersionUID = 8498346319001912273L;
    
    private String filter, select, selectExact, filterAndSelect;
    
    public String getFilter() {
        return filter;
    }
    
    public String getSelect() {
        return select;
    }  

    public void setFilter(String filter) {
        this.filter = filter;
    }
    
    public void setSelect(String select) {
        this.select = select;
    }
    
    public void setSelectExact(String selectExact) {
        this.selectExact = selectExact;
    }
    
    public String getSelectExact() {
        return selectExact;
    }
    
    public String getFilterAndSelect() {
        return filterAndSelect;
    }
    
    public void setFilterAndSelect (String filterAndSelect) {
        this.filterAndSelect = filterAndSelect;
    }
    
    public String getFilterString() {
        StringJoiner filterString = new StringJoiner(" ");
        if (filter != null) {
            filterString.add(filter);
        }
        if (filterAndSelect != null) {
            filterString.add(filterAndSelect);
        }
        return filterString.length() <= 0 ? null : filterString.toString();
    }
    
    public List<String> getSelectList() {
        List<String> selects = new ArrayList<String>();
        if (select != null) {
            selects.add(select);
        }
        if (filterAndSelect != null) {
            selects.add(filterAndSelect);
        }
        return selects;
    }
    
    public void clear() {
        this.filter = null;
        this.filterAndSelect = null;
        this.selectExact = null;
        this.filterAndSelect = null;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filter == null) ? 0 : filter.hashCode());
        result = prime * result + ((filterAndSelect == null) ? 0 : filterAndSelect.hashCode());
        result = prime * result + ((select == null) ? 0 : select.hashCode());
        result = prime * result + ((selectExact == null) ? 0 : selectExact.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FilterParameter other = (FilterParameter) obj;
        if (filter == null) {
            if (other.filter != null)
                return false;
        } else if (!filter.equals(other.filter))
            return false;
        if (filterAndSelect == null) {
            if (other.filterAndSelect != null)
                return false;
        } else if (!filterAndSelect.equals(other.filterAndSelect))
            return false;
        if (select == null) {
            if (other.select != null)
                return false;
        } else if (!select.equals(other.select))
            return false;
        if (selectExact == null) {
            if (other.selectExact != null)
                return false;
        } else if (!selectExact.equals(other.selectExact))
            return false;
        return true;
    }
}
