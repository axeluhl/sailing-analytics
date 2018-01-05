package com.sap.sse.gwt.client.celltable;

import java.util.List;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SetSelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * The {@link RefreshableSelectionModel} unifies the refresh behavior of SelectionModels e.g. of a
 * {@link SingleSelectionModel}. This {@link RefreshableSelectionModel selection model} will automatically refresh the
 * selection when there are changes on the {@link ListDataProvider}. Therefore the {@link RefreshableSelectionModel
 * selection model} is a listener of the {@link ListDataProvider} and implements the {@link HasData}-interface.
 * <p>
 * All objects from the current selection that {@link EntityIdentityComparator#representSameEntity(Object, Object)
 * represent the same entity} as a new object from {@link ListDataProvider} will be reselected. All others are
 * de-selected. That means a selected object that is not contained in {@link ListDataProvider#getList()} wouldn't be
 * selected anymore. If an element is reselected the element will be replaced with new version of it.
 * <p>
 * If this selection model has no {@link EntityIdentityComparator} set, this method will use the {@link #equals(Object)}
 * method to compare.
 * <P>
 * When the selection is refreshed this {@link RefreshableSelectionModel selection model} triggers a
 * {@link SelectionChangeEvent.Handler#onSelectionChange(SelectionChangeEvent) onSelectionChangedEvent} using
 * {@link AbstractSelectionModel#fireEvent(com.google.gwt.event.shared.GwtEvent)}.
 * <p>
 * When you use this {@link RefreshableSelectionModel selection model} make sure that you don't modify
 * {@link ListDataProvider} in {@link Handler#onSelectionChange(com.google.gwt.view.client.SelectionChangeEvent)
 * onSelectionChange()} (it causes a stack overflow).
 * 
 * @author D064976
 */

public interface RefreshableSelectionModel<T> extends SetSelectionModel<T> {
    /**
     * @return Returns an instance of {@link EntityIdentityComparator}, to compare objects from the type
     *         <code>&ltT&gt</code>.
     */
    EntityIdentityComparator<T> getEntityIdentityComparator();
    
    boolean itemIsSelectedButNotVisible(List<T> visibleItemList);
    
    void refreshSelectionModel(Iterable<T> newObjects);
}