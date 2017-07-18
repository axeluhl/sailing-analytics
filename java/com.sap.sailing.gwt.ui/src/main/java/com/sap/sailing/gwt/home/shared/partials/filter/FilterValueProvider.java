package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.Collection;

@FunctionalInterface
public interface FilterValueProvider<C> {

    Collection<C> getFilterableValues();

}
