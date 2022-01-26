package com.sap.sailing.gwt.managementconsole.places;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

/**
 * Abstract super class for management console {@link Place place} implementations providing abstract super classes for
 * convenient {@link PlaceTokenizer place tokenizer} implementations.
 */
public abstract class AbstractManagementConsolePlace extends Place {

    protected static abstract class DefautTokenizer<P extends AbstractManagementConsolePlace>
            implements PlaceTokenizer<P> {

        private final Supplier<P> placeFactory;

        protected DefautTokenizer(final Supplier<P> placeFactory) {
            this.placeFactory = placeFactory;
        }

        @Override
        public P getPlace(final String token) {
            return placeFactory.get();
        }

        @Override
        public String getToken(final P place) {
            return "";
        }
    }

    protected static abstract class UUIDTokenizer<P extends AbstractManagementConsolePlace>
            implements PlaceTokenizer<P> {

        private final Function<UUID, P> placeFactory;
        private final Function<P, UUID> tokenProvider;

        protected UUIDTokenizer(final Function<UUID, P> placeFactory, final Function<P, UUID> tokenProvider) {
            this.placeFactory = placeFactory;
            this.tokenProvider = tokenProvider;
        }

        @Override
        public P getPlace(final String token) {
            return placeFactory.apply(UUID.fromString(token));
        }

        @Override
        public String getToken(final P place) {
            return tokenProvider.apply(place).toString();
        }
    }

}
