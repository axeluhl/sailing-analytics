package com.sap.sse.gwt.client.panels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * FilterAndSelectParameters defines the url-parameters that can be used for filtering in tables and for selecting table items.<br/>
 * All parameters can be used combined by using the ampersand. Use url-Encoding for combining parameters.
 * Value characters have to be url-encoded, so the url for filtering for 'Worlds 2017' looks like this:<br/>
 * https://dev.sapsailing.com/gwt/AdminConsole.html#EventsPlace:filter=Worlds%202017<br/><br/>
 * 
 * See <a href="https://www.urlencoder.org/">Url Encoder</a> for a full list of url-encoded characters (scroll down to the table "reserved characters after percent-encoding").<br/><br/>
 * 
 * The filter-parameter and the select-parameter both find always the same list of lineItems in the table.<br/>
 * So when used both are used with the same value like this:<br/>
 * https://dev.sapsailing.com/gwt/AdminConsole.html#EventsPlace:filter=World%26select=World<br/>
 * Then all filtered table items will also be selected. Instead of using both parameters with the same value, you can instead use filterAndSelect.
 * It will be the same outcome.<br/><br/>
 * 
 * If the filter and the filterAndSelect-parameters are combined, values of both will be set into the search-box above the table 
 * and used for filtering.<br/><br/>
 * 
 * The selectExact-parameter works slightly different from the select-parameter.<br/>
 * Less line items will be selected when it is used because it matches with the whole column value, not just parts of it (like filter and select-parameters do).<br/>
 * When using different select-parameters like select and selectExact as url-parameters, 
 * the selection result will be disjoint, which means that all given select-values must match a table-line item to be selected.<br/><br/>
 * 
 * When the table is a singleSelection-table, the first selectExact-match will be selected. When there is no selectExact-value given, 
 * the first select or filterAndSelect-match will be selected if there is one.
 * 
 * @author sdohren
 *
 */
public class FilterAndSelectParameters implements Serializable {

    private static final long serialVersionUID = 8498346319001912273L;
    
    private final String filter, select, selectExact, filterAndSelect;
    
    public FilterAndSelectParameters(String filter, String select, String selectExact, String filterAndSelect) {
        super();
        this.filter = filter;
        this.select = select;
        this.selectExact = selectExact;
        this.filterAndSelect = filterAndSelect;
    }

    public String getFilter() {
        return filter;
    }
    
    public String getSelect() {
        return select;
    }  

    /**
     * When the table is a singleSelection-table, the first selectExact-Match will be selected. When theres no selectExact-value given, the first select/filterAndSelect match will be selected if there is one
     * @return selectExact parameter
     */
    public String getSelectExact() {
        return selectExact;
    }
    
    public String getFilterAndSelect() {
        return filterAndSelect;
    }
    
    public String getFilterString() {
        StringJoiner filterString = new StringJoiner(" ");
        if (filter != null) {
            filterString.add(filter);
        }
        if (filterAndSelect != null) {
            filterString.add(filterAndSelect);
        }
        return filterString.length() <= 0 && !isSelectParameterSet() ? null : filterString.toString();
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
    
    private boolean isSelectParameterSet() {
        return select != null || selectExact != null || filterAndSelect != null;
    }
    
    public boolean isAnyParameterSet() {
        return isSelectParameterSet() || getFilterString() != null;
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
        FilterAndSelectParameters other = (FilterAndSelectParameters) obj;
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
