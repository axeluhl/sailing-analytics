package com.sap.sailing.gwt.ui.shared.components;

import java.util.Iterator;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A GWT component that visualizes a {@link ComponentForest} including menus to scroll quickly to the embedded view
 * of the respective component, collapse/expand buttons for the views embedded and homogeneous settings buttons for
 * those components that have settings.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompositePanel extends Panel {
    private final ComponentForest components;
    
    public CompositePanel(ComponentForest components) {
        super();
        this.components = components;
    }

    @Override
    public Iterator<Widget> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean remove(Widget arg0) {
        // TODO Auto-generated method stub
        return false;
    }

}
