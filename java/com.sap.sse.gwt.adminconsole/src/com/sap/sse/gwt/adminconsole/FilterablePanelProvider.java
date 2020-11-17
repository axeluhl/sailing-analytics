package com.sap.sse.gwt.adminconsole;

import com.sap.sse.gwt.client.panels.AbstractFilterablePanel;

public interface FilterablePanelProvider<T> {
    AbstractFilterablePanel<T> getFilterablePanel();
}
