package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.Collection;

public interface FilterValueProvider<C> {

    Collection<C> getFilterableValues();

}
