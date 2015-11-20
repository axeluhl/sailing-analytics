package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;

public class RegattaSelectionModel implements RegattaSelectionProvider, RefreshableSelectionModel<RegattaIdentifier> {
    private final List<RegattaIdentifier> selection;
    
    private final List<RegattaIdentifier> allRegattas;
    
    private final Set<RegattaSelectionChangeListener> listeners;
    
    private final boolean hasMultiSelection;

    public RegattaSelectionModel(boolean hasMultiSelection) {
        this.hasMultiSelection = hasMultiSelection;
        this.selection = new ArrayList<RegattaIdentifier>();
        this.allRegattas = new ArrayList<RegattaIdentifier>();
        listeners = new HashSet<RegattaSelectionChangeListener>();
    }

    @Override
    public List<RegattaIdentifier> getSelectedRegattas() {
        return Collections.unmodifiableList(selection);
    }

    @Override
    public void addRegattaSelectionChangeListener(RegattaSelectionChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeRegattaSelectionChangeListener(RegattaSelectionChangeListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public void setSelection(List<RegattaIdentifier> newSelection, RegattaSelectionChangeListener... listenersNotToNotify) {
        boolean notify = !selection.equals(newSelection);
        selection.clear();
        selection.addAll(newSelection);
        if (notify) {
            notifyListeners(listenersNotToNotify);
        }
    }

    private void notifyListeners(RegattaSelectionChangeListener[] listenersNotToNotify) {
        List<RegattaIdentifier> selectedRegattas = getSelectedRegattas();
        for (RegattaSelectionChangeListener listener : listeners) {
            if (listenersNotToNotify == null || !Arrays.asList(listenersNotToNotify).contains(listener)) {
                listener.onRegattaSelectionChange(selectedRegattas);
            }
        }
    }
    
    /**
     * Sets the universe of all regattas from which this selection model may select. An equal list will subsequently be
     * returned by {@link #getAllRegattas()}. Regattas from the {@link #getSelectedRegattas() selection} not in
     * <code>newAllRegattas</code> are removed from the selection. If this happens, the selection listeners are notified.
     */
    @Override
    public void setAllRegattas(List<RegattaIdentifier> newAllRegattas, RegattaSelectionChangeListener... listenersNotToNotify) {
        allRegattas.clear();
        for (RegattaIdentifier r : newAllRegattas) {
            allRegattas.add(r);
        }
        for (Iterator<RegattaIdentifier> i=selection.iterator(); i.hasNext(); ) {
            RegattaIdentifier selectedRegatta = i.next();
            if (!allRegattas.contains(selectedRegatta)) {
                i.remove();
            }
        }
        // when setting all regattas, the actual RegattaDTOs will usually have changed their identity and maybe also their state;
        // so notifying the selection listeners is necessary anyhow
        notifyListeners(listenersNotToNotify);
    }

    @Override
    public List<RegattaIdentifier> getAllRegattas() {
        return Collections.unmodifiableList(allRegattas);
    }

    @Override
    public boolean hasMultiSelection() {
        return hasMultiSelection;
    }

    /**
     * Returns an instance of <code>HasEqualIdentity&ltRegattaIdentifier&gt</code>. The compare
     * method works the same way as <code>o1.eqauls(o2)</code>.
     */
    @Override
    public EntityIdentityComparator<RegattaIdentifier> getHasEqualIdentity() {
        return new EntityIdentityComparator<RegattaIdentifier>() {
            @Override
            public boolean compare(RegattaIdentifier dto1, RegattaIdentifier dto2) {
                return dto1.equals(dto2);
            }
        };
    }

    /**
     * This method will have no impact on the object, because the only logical way to
     * compare <code>RegattaIdentifier</code> objects is to use the equals method.
     */
    @Override
    public void setHasEqualIdentity(EntityIdentityComparator<RegattaIdentifier> comp) {
        //TODO
    }

    @Override
    public Set<RegattaIdentifier> getSelectedSet() {
        Iterable<RegattaIdentifier> selected = getSelectedRegattas();
        Set<RegattaIdentifier> result = new HashSet<>();
        for (RegattaIdentifier it : selected) {
            result.add(it);
        }
        return result;
    }

    @Override
    public void clear() {
        setAllRegattas(new ArrayList<RegattaIdentifier>());
    }

    @Override
    public void setSelected(RegattaIdentifier item, boolean selected) {
        if (selected) {
            List<RegattaIdentifier> all = getAllRegattas();
            List<RegattaIdentifier> selection = getSelectedRegattas();
            all.add(item);
            selection.add(item);
            setAllRegattas(all);
            setSelection(selection);
        } else {
            List<RegattaIdentifier> all = getAllRegattas();
            all.add(item);
            setAllRegattas(all);
        }
    }

    @Override
    public void refreshSelectionModel(Iterable<RegattaIdentifier> newObjects) {
        if(newObjects != null) {
            List<RegattaIdentifier> all = new ArrayList<>();
            for(RegattaIdentifier it : newObjects) {
                all.add(it);
            }
            setAllRegattas(all);
        }
    }
}
