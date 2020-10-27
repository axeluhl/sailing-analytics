package com.sap.sailing.gwt.ui.adminconsole.places;

import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sse.gwt.adminconsole.AdminConsolePlace;
import com.sap.sse.gwt.adminconsole.FilterableAdminConsolePlace;
import com.sap.sse.gwt.adminconsole.SelectableAdminConsolePlace;

public abstract class AbstractFilterablePlace extends AdminConsolePlace implements FilterableAdminConsolePlace, SelectableAdminConsolePlace {
    
    private String filter;
    
    private String select; 
    
    public String getFilter() {
        return filter;
    }
    
    public String getSelect() {
        return select;
    }  

    void setFilter(String filter) {
        this.filter = filter;
    }
    
    void setSelect(String select) {
        this.select = select;
    }
    
    @Override
    public String getVerticalTabName() {
        return "";
    }
    
    protected static abstract class TablePlaceTokenizer<P extends AbstractFilterablePlace> implements PlaceTokenizer<P> {

        private static final String IS = "=";
        
        private static final String FILTER_KEY = "filter";
        private static final String SELECT_KEY = "select";

        @Override
        public P getPlace(final String token) {
            P place = getPlaceFactory().get();
            
            if (token == null || !token.contains(IS)) {
                return place;
            }
            
            final String[] split = token.split(IS, 2);
            String key = split[0];
            if (key.equals(FILTER_KEY)) {
                place.setFilter(split[1]);
            } else if (key.equals(SELECT_KEY)) {
                place.setSelect(split[1]);
            }

            return place;
        }

        @Override
        public String getToken(final P place) {
            if (place.getFilter() != null) {
                return FILTER_KEY + IS + place.getFilter();
            } else if(place.getSelect() != null) {
                return SELECT_KEY + IS + place.getSelect();
            }
            return "";
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
        result = prime * result + ((filter == null) ? 0 : filter.hashCode());
        result = prime * result + ((select == null) ? 0 : select.hashCode());
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
        if (filter == null) {
            if (other.filter != null)
                return false;
        } else if (!filter.equals(other.filter))
            return false;
        if (select == null) {
            if (other.select != null)
                return false;
        } else if (!select.equals(other.select))
            return false;
        return true;
    }

}
