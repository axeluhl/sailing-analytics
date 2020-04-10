package com.sap.sse.security.ui.client.component;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.annotations.IsSafeHtml;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.view.client.SetSelectionModel;

import java.util.Set;

/**
 * The implementation of Button which re-draws it's label based on SelectionEvent with showing the number of selected
 * items. If number of selected items is <= 1 then no number is shown
 * 
 * @author Dmitry Bilyk
 *
 */
public class SelectedElementsCountingButton<T> extends Button {
    public SelectedElementsCountingButton(final SetSelectionModel<T> selectionModel, final @IsSafeHtml String html,
            final ClickHandler handler) {
        super(html, handler);
        selectionModel.addSelectionChangeHandler(event -> {
            Set<T> selectedSet = selectionModel.getSelectedSet();
            setText(selectedSet.size() <= 1 ? html : html + " (" + selectedSet.size() + ")");
        });

    }
}
