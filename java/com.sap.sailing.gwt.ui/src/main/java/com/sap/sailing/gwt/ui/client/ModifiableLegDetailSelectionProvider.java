package com.sap.sailing.gwt.ui.client;

public interface ModifiableLegDetailSelectionProvider extends LegDetailSelectionProvider {
    void select(LegDetailColumnType type);
    void deselect(LegDetailColumnType type);
}
