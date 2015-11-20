package com.sap.sse.gwt.client.celltable;

import java.util.Set;

public interface RefreshableSelectionModel<T> {
    /**
     * @return Returns an instance of <code>HasEqualIdentity&ltT&gt</code>, to compare objects from the type
     *         <code>&ltT&gt</code>.
     */
    public EntityIdentityComparator<T> getHasEqualIdentity();

    /**
     * Sets the <code>HasEqualIdentity&ltT&gt</code> field, to compare objects from the type <code>&ltT&gt</code>.
     * 
     * @param comp
     *            When you set <code>null</code>, the <code>T.eqauls()</code> will be used.
     */
    public void setHasEqualIdentity(EntityIdentityComparator<T> comp);

    public Set<T> getSelectedSet();

    public void clear();

    public void setSelected(T item, boolean selected);

    /**
     * Refreshes the <code>RefreshableSelectionModel&ltT&gt</code> with the <code>newObjects</code>. If the selected
     * objects are according to <code>HasEqualIdentity&ltT&gt.compare()</code> are the same, the selection will be
     * reselected. If the object have no <code>HasEqualIdentity&ltT&gt</code>, this method will use the
     * <code>&ltT&gt.eqauls()</code> method to compare.
     * 
     * @param newObjects
     */
    public void refreshSelectionModel(Iterable<T> newObjects);
}
