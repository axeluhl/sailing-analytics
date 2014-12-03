package com.sap.sailing.gwt.ui.client;

import java.util.List;
import java.util.UUID;


/**
 * Allows UI components to observe a saling event selector, such as a drop-down box showing a list of sailing events
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface EventSelectionChangeListener {

    /**
     * The first element is the first one selected
     * 
     * @param selectedEvents a non-<code>null</code> list which is empty if nothing is selected
     */
    void onEventSelectionChange(List<UUID> selectedEvents);

}
