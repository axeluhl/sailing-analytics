package com.sap.sse.gwt.client.celltable;

import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SetSelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * The interface {@link RefreshableSelectionModel} unifies the refresh behavior of SelectionModels e.g. of a
 * {@link SingleSelectionModel}. To refresh this SelectionModel you can call the
 * {@link RefreshableSelectionModel#refreshSelectionModel(Iterable)} method and this method will refresh the
 * SelectionModel. The refresh should be triggered after the {@link ListDataProvider} was updated.
 * <p>
 * 
 * TODO Lukas, please enhance this comment to explain how this re-establishes the invariant of same elements in
 * SetSelectionModel.getSelectedElements and DataProvider
 * 
 * @author D064976
 */

public interface RefreshableSelectionModel<T> extends SetSelectionModel<T> {
    /**
     * @return Returns an instance of {@link EntityIdentityComparator}, to compare objects from the type
     *         <code>&ltT&gt</code>.
     */
    public EntityIdentityComparator<T> getEntityIdentityComparator();

    /**
     * Refreshes the {@link RefreshableSelectionModel} with the <code>newObjects</code>. All objects from the current
     * selection that {@link EntityIdentityComparator#representSameEntity(Object, Object) represent the same entity} as
     * an object from <code>newObjects</code> will be reselected. All others are de-selected. That means a selected
     * object is not contained in <code>newObjects</code> the object wouldn't be selected anymore. If this selection
     * model has no {@link EntityIdentityComparator} set, this method will use the {@link #equals(Object)} method to
     * compare.
     * <p>
     * 
     * When the selection is refreshed this method triggers a
     * {@link SelectionChangeEvent.Handler#onSelectionChange(SelectionChangeEvent) onSelectionChangedEvent} using
     * {@link AbstractSelectionModel#fireEvent(com.google.gwt.event.shared.GwtEvent)}.
     */
    // TODO / FIXME delete this method and describe interaction
    // public void refreshSelectionModel(Iterable<T> newObjects);
}
