package com.sap.sse.gwt.adminconsole;

import java.util.Map;
import java.util.function.Function;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sse.gwt.client.panels.FilterAndSelectParameters;

public abstract class AbstractFilterablePlace extends AbstractAdminConsolePlace implements FilterableAdminConsolePlace {
    /**
     * Name of the place parameter that adds the parameter's value into the place's main table's filter box unchanged.
     */
    public static final String FILTER_KEY = "filter";
    
    /**
     * Name of the place parameter that can be used to make a selection in the place's main table. The logic for
     * identifying the rows based on the parameter value is the same as implemented by the filter box.
     */
    private static final String SELECT_KEY = "select";
    
    /**
     * Name of the place parameter that combines {@link #FILTER_KEY} and {@link #SELECT_KEY}, filtering and selecting
     * based on the same search string.
     */
    private static final String FILTER_AND_SELECT_KEY = "filterAndSelect";
    
    /**
     * Name of the place parameter that requests an exact and complete match for the parameter value with at least one
     * searchable column value of the place's main table's rows.
     */
    public static final String SELECT_EXACT_KEY = "selectExact";
    
    private final FilterAndSelectParameters filterAndSelectParameters;
    
    protected AbstractFilterablePlace(String token) {
        super(token);
        filterAndSelectParameters = getFilterAndSelectParametersFromPlaceParams();
    }

    protected AbstractFilterablePlace(Map<String, String> paramKeysAndValues) {
        super(paramKeysAndValues);
        filterAndSelectParameters = getFilterAndSelectParametersFromPlaceParams();
    }

    private FilterAndSelectParameters getFilterAndSelectParametersFromPlaceParams() {
        return new FilterAndSelectParameters(getParameterDecoded(FILTER_KEY), getParameterDecoded(SELECT_KEY),
                getParameterDecoded(SELECT_EXACT_KEY), getParameterDecoded(FILTER_AND_SELECT_KEY));
    }
    
    public String getFilter() {
        return filterAndSelectParameters.getFilter();
    }
    
    public String getSelect() {
        return filterAndSelectParameters.getSelect();
    }  

    public String getSelectExact() {
        return filterAndSelectParameters.getSelectExact();
    } 
    
    public String getFilterAndSelect() {
        return this.filterAndSelectParameters.getFilterAndSelect();
    }
    
    public FilterAndSelectParameters getFilterAndSelectParameters() {
        return this.filterAndSelectParameters;
    }
    
    protected static abstract class TablePlaceTokenizer<P extends AbstractFilterablePlace> implements PlaceTokenizer<P> {
        @Override
        public String getToken(P place) {
            return place.getParametersAsToken();
        }

        @Override
        public P getPlace(String token) {
            P place = getPlaceFactory().apply(token);   
            place.extractUrlParams(token);
            return place;
        }

        /**
         * Provides a {@link Function factory} to create a new place from parsed {@link String token} on deserialization.
         *
         * @return the {@link Function} to get the place instance from
         */
        protected abstract Function<String, P> getPlaceFactory();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filterAndSelectParameters == null) ? 0 : filterAndSelectParameters.hashCode());
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
        if (filterAndSelectParameters == null) {
            if (other.filterAndSelectParameters != null)
                return false;
        } else if (!filterAndSelectParameters.equals(other.filterAndSelectParameters))
            return false;
        return true;
    }
}
