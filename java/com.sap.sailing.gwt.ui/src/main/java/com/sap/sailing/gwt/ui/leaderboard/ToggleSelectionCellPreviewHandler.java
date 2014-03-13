package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.SelectionModel;

/**
 * Handler for CellPreviewEvents, which catches clicks on the table and toggles the clicked element
 * in the selection model.
 * 
 * @author D054527
 */
public class ToggleSelectionCellPreviewHandler<T> implements CellPreviewEvent.Handler<T> {

    @Override
    public void onCellPreview(CellPreviewEvent<T> event) {
        if (BrowserEvents.CLICK.equals(event.getNativeEvent().getType())) {
            final T element = event.getValue();
            SelectionModel<? super T> selectionModel = event.getDisplay().getSelectionModel();
            final Boolean state = !selectionModel.isSelected(element);
            selectionModel.setSelected(element, state);
            event.setCanceled(true);
        }
    }

}
