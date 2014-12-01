package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EventSelectionModel implements EventSelectionProvider {
    private final List<UUID> selection;
    private final List<UUID> allEvents;
    private final Set<EventSelectionChangeListener> listeners;
    private final boolean hasMultiSelection;

    public EventSelectionModel(boolean hasMultiSelection) {
        this.hasMultiSelection = hasMultiSelection;
        this.selection = new ArrayList<UUID>();
        this.allEvents = new ArrayList<UUID>();
        listeners = new HashSet<EventSelectionChangeListener>();
    }

    @Override
    public List<UUID> getSelectedEvents() {
        return Collections.unmodifiableList(selection);
    }

    @Override
    public void addEventSelectionChangeListener(EventSelectionChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventSelectionChangeListener(EventSelectionChangeListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public void setSelection(List<UUID> newSelection, EventSelectionChangeListener... listenersNotToNotify) {
        boolean notify = !selection.equals(newSelection);
        selection.clear();
        selection.addAll(newSelection);
        if (notify) {
            notifyListeners(listenersNotToNotify);
        }
    }

    private void notifyListeners(EventSelectionChangeListener[] listenersNotToNotify) {
        List<UUID> selectedEvents = getSelectedEvents();
        for (EventSelectionChangeListener listener : listeners) {
            if (listenersNotToNotify == null || !Arrays.asList(listenersNotToNotify).contains(listener)) {
                listener.onEventSelectionChange(selectedEvents);
            }
        }
    }
    
    /**
     * Sets the universe of all (sailing) events from which this selection model may select. An equal list will subsequently be
     * returned by {@link #getallEvents()}. Events from the {@link #getSelectedEvents() selection} not in
     * <code>newEvents</code> are removed from the selection. If this happens, the selection listeners are notified.
     */
    @Override
    public void setAllEvents(List<UUID> newEvents, EventSelectionChangeListener... listenersNotToNotify) {
        allEvents.clear();
        for (UUID r : newEvents) {
            allEvents.add(r);
        }
        for (Iterator<UUID> i=selection.iterator(); i.hasNext(); ) {
            UUID selectedRegatta = i.next();
            if (!allEvents.contains(selectedRegatta)) {
                i.remove();
            }
        }
        // when setting all events, the actual EventDTOs will usually have changed their identity and maybe also their state;
        // so notifying the selection listeners is necessary anyhow
        notifyListeners(listenersNotToNotify);
    }

    @Override
    public List<UUID> getAllEvents() {
        return Collections.unmodifiableList(allEvents);
    }

    @Override
    public boolean hasMultiSelection() {
        return hasMultiSelection;
    }
}
