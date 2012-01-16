package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A GWT component that visualizes a {@link ComponentForest} including menus to scroll quickly to the embedded view
 * of the respective component, collapse/expand buttons for the views embedded and homogeneous settings buttons for
 * those components that have settings.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompositePanel extends VerticalPanel {
    
    public CompositePanel(ComponentForest components) {
        super();
        for (ComponentForestEntry entry : components.getEntries()) {
            entry.toString(); // TODO continue here...
        }
    }
}
