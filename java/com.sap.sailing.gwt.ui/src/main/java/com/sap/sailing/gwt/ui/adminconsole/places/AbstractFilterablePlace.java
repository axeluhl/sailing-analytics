package com.sap.sailing.gwt.ui.adminconsole.places;

import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sse.gwt.adminconsole.AdminConsolePlace;
import com.sap.sse.gwt.adminconsole.FilterableAdminConsolePlace;
import com.sap.sse.gwt.client.panels.FilterParameter;

public abstract class AbstractFilterablePlace extends AdminConsolePlace implements FilterableAdminConsolePlace {
    
    private FilterParameter filterParameter = new FilterParameter();
    
    public String getFilter() {
        return filterParameter.getFilter();
    }
    
    public String getSelect() {
        return filterParameter.getSelect();
    }  

    void setFilter(String filter) {
        this.filterParameter.setFilter(filter);
    }
    
    void setSelect(String select) {
        this.filterParameter.setSelect(select);
    }
    
    void setSelectExact(String selectExact) {
        this.filterParameter.setSelectExact(selectExact);
    }
    
    public String getSelectExact() {
        return filterParameter.getSelectExact();
    } 
    
    public String getFilterAndSelect() {
        return this.filterParameter.getFilterAndSelect();
    }
    
    public void setFilterAndSelect(String filterAndSelect) {
        this.filterParameter.setFilterAndSelect(filterAndSelect);
    }
    
    public FilterParameter getFilterParameter() {
        return this.filterParameter;
    }
    
    @Override
    public String getVerticalTabName() {
        return "";
    }
    
    protected static abstract class TablePlaceTokenizer<P extends AbstractFilterablePlace> implements PlaceTokenizer<P> {

        private static final String IS = "=";
        private static final String PARAMETER_SEPARATOR = "&";     
        private static final String FILTER_KEY = "filter";
        private static final String SELECT_KEY = "select";
        private static final String FILTER_AND_SELECT_KEY = "filterAndSelect";
        private static final String SELECT_EXACT_KEY = "selectExact";

        @Override
        public P getPlace(final String token) {
            P place = getPlaceFactory().get();
            
            if (token == null || !token.contains(IS)) {
                return place;
           }
            
            final String[] parameters = token.split(PARAMETER_SEPARATOR);
            for (String parameter : parameters) {
                final String[] split = parameter.split(IS, 2);
                String key = split[0];
                String value = split[1];
                if (isParameter(key, FILTER_KEY)) {
                    place.setFilter(value);
                } else if (isParameter(key, SELECT_KEY)) {
                    place.setSelect(value);
                } else if (isParameter(key, FILTER_AND_SELECT_KEY)) {
                    place.setFilterAndSelect(value);
                } else if (isParameter(key, SELECT_EXACT_KEY)) {
                    place.setSelectExact(value);
                } 
            }
            return place;
        }

        private boolean isParameter(final String key, final String parameter) {
            return key != null && key.equalsIgnoreCase(parameter);
        }
        
        @Override
        public String getToken(final P place) {
            StringJoiner tokenJoiner = new StringJoiner(PARAMETER_SEPARATOR);
            if (place.getFilter() != null) {
                tokenJoiner.add(FILTER_KEY + IS + place.getFilter());
            }
            if (place.getSelect() != null) {
                tokenJoiner.add(SELECT_KEY + IS + place.getSelect()); 
            }
            if (place.getSelectExact() != null) {
                tokenJoiner.add(SELECT_EXACT_KEY + IS + place.getSelectExact()); 
            }
            if (place.getFilterAndSelect() != null) {
                tokenJoiner.add(FILTER_AND_SELECT_KEY + IS + place.getFilterAndSelect()); 
            }
            return tokenJoiner.toString();
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
