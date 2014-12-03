package com.sap.sailing.gwt.ui.client;

import java.util.List;
import java.util.UUID;

public interface EventSelectionProvider {
    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<UUID> getSelectedEvents();

    void addEventSelectionChangeListener(EventSelectionChangeListener listener);

    void removeEventSelectionChangeListener(EventSelectionChangeListener listener);
    
    void setSelection(List<UUID> newSelection, EventSelectionChangeListener... listenersNotToNotify);

    boolean hasMultiSelection();

    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<UUID> getAllEvents();
    
    void setAllEvents(List<UUID> events, EventSelectionChangeListener... listenersNotToNotify);
}
