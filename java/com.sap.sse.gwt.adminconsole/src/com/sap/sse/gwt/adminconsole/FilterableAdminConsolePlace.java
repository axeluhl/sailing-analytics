package com.sap.sse.gwt.adminconsole;

import com.sap.sse.gwt.client.panels.FilterAndSelectParameters;

/**
 * Places that have filterable tables and should be filterable by URL parameters must implement this interface. The
 * panel that is rendered when the place is called must implement FilterablePanelProvider. Otherwise the
 * filter-parameters will be ignored when the place is called.
 * 
 * @see com.sap.sse.gwt.adminconsole.FilterablePanelProvider
 */
public interface FilterableAdminConsolePlace {
    FilterAndSelectParameters getFilterParameter();
}
