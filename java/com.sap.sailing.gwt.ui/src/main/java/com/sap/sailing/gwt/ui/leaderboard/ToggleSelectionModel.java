package com.sap.sailing.gwt.ui.leaderboard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.DefaultSelectionEventManager.EventTranslator;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SelectionModel.AbstractSelectionModel;

/**
 * A selection model with multiple selected elements.<br \>
 * If you select an element, the selected state of it gets toggled.<br \>
 * <br \>
 * 
 * This only works, because of some reasons:<br \>
 * <li>It does NOT extend the {@link MultiSelectionModel}.<br \>
 * The {@link DefaultSelectionEventManager#onCellPreview(com.google.gwt.view.client.CellPreviewEvent)
 * DefaultSelectionEventManager} handles MultiSelectinModels and other SelectionModels different.<br \>
 * For example the selection of a MultiSelectionModel is cleared, if a normal click on the table is performed.</li> <li>
 * The {@link DefaultSelectionEventManager} doesn't clear the selection of other SelectionModels, when a selection is
 * performed.</li> <li>The {@link ToggleSelectionModel#isSelected(Object)} method does NOT perform a
 * {@link ToggleSelectionModel#resolveChanges()}.<br \>
 * Would result in "blinking" boats in the race board.</li><br \>
 * <br \>
 * 
 * If this model doesn't work anymore, have a look if the code of the
 * {@link DefaultSelectionEventManager#onCellPreview(com.google.gwt.view.client.CellPreviewEvent)
 * DefaultSelectionEventManager} has changed (This model was developed with GWT 2.4). Therefore see the out commented
 * code furhter down.<br \><br \>
 * 
 * If you can't fix the issue, have a look at the
 * {@link DefaultSelectionEventManager#createCustomManager(com.google.gwt.view.client.DefaultSelectionEventManager.EventTranslator)
 * DefaultSelectionEventManager.createCustomManager(...)} and a custom {@link EventTranslator}.
 * 
 * @author Lennart Hensler (D054527)
 */
//public void onCellPreview(CellPreviewEvent<T> event) {
//    // Early exit if selection is already handled or we are editing.
//    if (event.isCellEditing() || event.isSelectionHandled()) {
//      return;
//    }
//
//    // Early exit if we do not have a SelectionModel.
//    HasData<T> display = event.getDisplay();
//    SelectionModel<? super T> selectionModel = display.getSelectionModel();
//    if (selectionModel == null) {
//      return;
//    }
//
//    // Check for user defined actions.
//    SelectAction action = (translator == null) ? SelectAction.DEFAULT
//        : translator.translateSelectionEvent(event);
//
//    // Handle the event based on the SelectionModel type.
//    if (selectionModel instanceof MultiSelectionModel<?>) {
//      // Add shift key support for MultiSelectionModel.
//      handleMultiSelectionEvent(event, action,
//          (MultiSelectionModel<? super T>) selectionModel);
//    } else {
//      // Use the standard handler.
//      handleSelectionEvent(event, action, selectionModel);
//    }
//  }
public class ToggleSelectionModel<T> extends AbstractSelectionModel<T> {
    //!!Before you change something in this Model read the description!!

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
        return selectedSet.containsKey(getKey(object));
    }

    /**
     * Sets the selection state of an object.<br \>
     * If <code>selected</code> is <code>true</code>, then the selection state of the object is toggled.<br \>
     * Else the state is forced to <code>false</code>. (Needed to clear the model via the methods of the {@link SelectionModel})
     */
    @Override
    public void setSelected(T object, boolean selected) {
        if (selected) {
            selectionChanges.put(object, !isSelected(object));
        } else {
            selectionChanges.put(object, false);
        }
        scheduleSelectionChangeEvent();
    }

    @Override
    protected void fireSelectionChangeEvent() {
      if (isEventScheduled()) {
        setEventCancelled(true);
      }
      resolveChanges();
    }
    
    private void resolveChanges() {
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
