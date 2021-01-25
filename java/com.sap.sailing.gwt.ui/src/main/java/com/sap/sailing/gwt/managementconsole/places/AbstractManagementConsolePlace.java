package com.sap.sailing.gwt.managementconsole.places;

import java.util.function.Supplier;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public abstract class AbstractManagementConsolePlace extends Place {

    protected static abstract class DefautTokenizer<P extends AbstractManagementConsolePlace>
            implements PlaceTokenizer<P> {

        private final Supplier<P> placeFactory;

        protected DefautTokenizer(final Supplier<P> placeFactory) {
            this.placeFactory = placeFactory;
        }

        @Override
        public P getPlace(String token) {
            return placeFactory.get();
        }

        @Override
        public String getToken(P place) {
            return "";
        }
    }

}
