package com.sap.sailing.gwt.ui.leaderboard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel.AbstractSelectionModel;

public class ToggleSelectionModel<T> extends AbstractSelectionModel<T> {

    // Ensure one value per key
    private final HashMap<Object, T> selectedSet = new HashMap<Object, T>();

    private final HashMap<T, Boolean> selectionChanges = new HashMap<T, Boolean>();
    
    public ToggleSelectionModel() {
        this(null);
    }

    public ToggleSelectionModel(ProvidesKey<T> keyProvider) {
        super(keyProvider);
    }

    /**
     * Get the set of selected items as a copy.
     *
     * @return the set of selected items
     */
    public Set<T> getSelectedSet() {
      resolveChanges();
      return new HashSet<T>(selectedSet.values());
    }

    @Override
    public boolean isSelected(T object) {
//        resolveChanges();
        return selectedSet.containsKey(getKey(object));
    }

    @Override
    public void setSelected(T object, boolean selected) {
        if (selected) {
            selectionChanges.put(object, !isSelected(object));
            scheduleSelectionChangeEvent();
        }
    }

    @Override
    protected void fireSelectionChangeEvent() {
      if (isEventScheduled()) {
        setEventCancelled(true);
      }
      resolveChanges();
    }
    
    private void resolveChanges() {
        //TODO
        if (selectionChanges.isEmpty()) {
            return;
          }

          boolean changed = false;
          for (Map.Entry<T, Boolean> entry : selectionChanges.entrySet()) {
            T object = entry.getKey();
            boolean selected = entry.getValue();

            Object key = getKey(object);
            T oldValue = selectedSet.get(key);
            if (selected) {
              if (oldValue == null || !oldValue.equals(object)) {
                selectedSet.put(getKey(object), object);
                changed = true;
              }
            } else {
              if (oldValue != null) {
                selectedSet.remove(key);
                changed = true;
              }
            }
          }
          selectionChanges.clear();

          // Fire a selection change event.
          if (changed) {
            SelectionChangeEvent.fire(this);
          }
    }

}
