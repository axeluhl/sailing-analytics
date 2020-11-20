package com.sap.sse.gwt.adminconsole;

import com.sap.sse.gwt.client.panels.AbstractFilterablePanel;

/**
 * Panels in the AdminConsolePanel that are filterable by url-parameters, must implement this interface.
 * The filter/select logic itself can be found in AbstractFilterablePanel.
 * The Place that belongs to the panel must implement AbstractFilterablePlace.
 * Otherwise the filter-parameters will be ignored when the place is called.
 * @see com.sap.sse.gwt.client.panels.AbstractFilterablePanel
 * @see com.sap.sse.gwt.adminconsole.FilterableAdminConsolePlace
 */
public interface FilterablePanelProvider<T> {
    AbstractFilterablePanel<T> getFilterablePanel();
}
