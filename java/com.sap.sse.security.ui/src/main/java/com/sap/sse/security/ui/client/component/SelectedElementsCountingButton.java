package com.sap.sse.security.ui.client.component;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.annotations.IsSafeHtml;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.view.client.SetSelectionModel;

import java.util.Set;

/**
 * The implementation of Button which re-draws it's label based on SelectionEvent with showing the number of selected
 * items. If number of selected items is <= 1 then no number is shown. The button's enablement state depends on whether
 * the selection is empty or not. It will be shown disabled for an empty selection, also upon first creation.
 * 
 * @author Dmitry Bilyk
 *
 */
public class SelectedElementsCountingButton<T> extends Button {
    public SelectedElementsCountingButton(final SetSelectionModel<T> selectionModel, final @IsSafeHtml String html,
            final ClickHandler handler) {
        super(html, handler);
        setEnabled(!selectionModel.getSelectedSet().isEmpty());
        selectionModel.addSelectionChangeHandler(event -> {
            Set<T> selectedSet = selectionModel.getSelectedSet();
            setText(selectedSet.size() <= 1 ? html : html + " (" + selectedSet.size() + ")");
            setEnabled(!selectedSet.isEmpty());
        });
    }
}
