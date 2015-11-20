package com.sap.sse.gwt.client.celltable;

import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SetSelectionModel;

public interface RefreshableSelectionModel<T> extends SetSelectionModel<T> {
    /**
     * @return Returns an instance of {@link EntityIdentityComparator}, to compare objects from the type
     *         <code>&ltT&gt</code>.
     */
    public EntityIdentityComparator<T> getEntityIdentityComparator();

    /**
     * Sets the {@link EntityIdentityComparator} field, to compare objects from the type <code>&ltT&gt</code>.
     * 
     * @param comp
     *            When you set <code>null</code>, the <code>T.eqauls()</code> will be used.
     */
    // TODO remove this setter and pass the comparator to the constructor of the implementing class(es)
    public void setHasEqualIdentity(EntityIdentityComparator<T> comp);

    /**
     * Refreshes the {@link RefreshableSelectionModel} with the <code>newObjects</code>. All objects from the current
     * selection that {@link EntityIdentityComparator#representSameEntity(Object, Object) represent the same entity} as
     * an object from <code>newObjects</code> will be reselected. All others are de-selected. If this selection model
     * has no {@link EntityIdentityComparator} set, this method will use the {@link #equals(Object)} method to compare.
     * <p>
     * 
     * TODO what happens with objects that were in the selection but are not in newObjects?
     * TODO what about triggering an {@link SelectionChangeEvent.Handler#onSelectionChange(SelectionChangeEvent)}, e.g., using {@link AbstractSelectionModel#scheduleSelectionChangeEvent}
     * 
     */
    public void refreshSelectionModel(Iterable<T> newObjects);
}
