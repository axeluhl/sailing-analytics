package com.sap.sse.security.ui.client.component;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.annotations.IsSafeHtml;
import com.google.gwt.user.client.ui.Button;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;

import java.util.Set;

/**
 * The implementation of Button which re-draws it's label based on SelectionEvent with showing the number of 
 * selected items
 * 
 * @author dmitry
 *
 */
public class SelectedElementsCountingRemoveButton extends Button {
    public SelectedElementsCountingRemoveButton(RefreshableSelectionModel selectionModel, @IsSafeHtml String html,
            ClickHandler handler) {
        setHTML(html);
        addClickHandler(handler);
        selectionModel.addSelectionChangeHandler(event -> {
            Set selectedSet = selectionModel.getSelectedSet();
            setEnabled(!selectedSet.isEmpty());
            setText(selectedSet.size() <= 1 ? html : html + '(' + selectedSet.size() + ')');
        });

    }
}
