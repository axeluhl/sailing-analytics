package com.sap.sse.gwt.client.celltable;

import java.util.Set;

public interface RefreshableSelectionModel<T> {
    public HasEqualIdentity<T> getHasEqualIdentity();
    public void setHasEqualIdentity(HasEqualIdentity<T> comp);
    public Set<T> getSelectedSet();
    public void clear();
    public void setSelected(T item, boolean selected);
    public void refreshSelectionModel(Iterable<T> newObjects);
}
