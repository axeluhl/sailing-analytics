package com.sap.sailing.gwt.ui.adminconsole.places;

import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sse.gwt.adminconsole.FilterableAdminConsolePlace;
import com.sap.sse.gwt.client.panels.FilterParameter;

public abstract class AbstractFilterablePlace extends AbstractAdminConsolePlace implements FilterableAdminConsolePlace {
    
    private static final String FILTER_KEY = "filter";
    private static final String SELECT_KEY = "select";
    private static final String FILTER_AND_SELECT_KEY = "filterAndSelect";
    private static final String SELECT_EXACT_KEY = "selectExact";
    
    private FilterParameter filterParameter = new FilterParameter();
    
    public String getFilter() {
        return filterParameter.getFilter();
    }
    
    public String getSelect() {
        return filterParameter.getSelect();
    }  

    void loadFilterParam() {
        this.filterParameter.setFilter(getParameter(FILTER_KEY));
    }
    
    void loadSelectParam() {
        this.filterParameter.setSelect(getParameter(SELECT_KEY));
    }
    
    void loadSelectExactParam() {
        this.filterParameter.setSelectExact(getParameter(SELECT_EXACT_KEY));
    }
    
    public void loadFilterAndSelectParam() {
        this.filterParameter.setFilterAndSelect(getParameter(FILTER_AND_SELECT_KEY));
    }
    
    public String getSelectExact() {
        return filterParameter.getSelectExact();
    } 
    
    public String getFilterAndSelect() {
        return this.filterParameter.getFilterAndSelect();
    }
    
    public FilterParameter getFilterParameter() {
        return this.filterParameter;
    }
    
    @Override
    public String getVerticalTabName() {
        return "";
    }
    
    protected static abstract class TablePlaceTokenizer<P extends AbstractFilterablePlace> implements PlaceTokenizer<P> {
        
        @Override
        public String getToken(P place) {
            return place.getParametersAsToken();
        }

        @Override
        public P getPlace(String token) {
            P place = getPlaceFactory().get();   
            place.extractUrlParams(token);
            
            place.loadFilterParam();
            place.loadSelectParam();
            place.loadSelectExactParam();
            place.loadFilterAndSelectParam();    
            return place;
        }

        /**
         * Provides a {@link Function factory} to create a new place from parsed {@link Long token} on deserialization.
         *
         * @return the {@link Function} to get the place instance from
         */
        protected abstract Supplier<P> getPlaceFactory();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filterParameter == null) ? 0 : filterParameter.hashCode());
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
        AbstractFilterablePlace other = (AbstractFilterablePlace) obj;
        if (filterParameter == null) {
            if (other.filterParameter != null)
                return false;
        } else if (!filterParameter.equals(other.filterParameter))
            return false;
        return true;
    }
}
