package com.sap.sailing.gwt.ui.client.shared.controls;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.view.client.HasData;

/**
 * This {@link Flushable}-interface should be implemented of the {@link HasData display} which uses the
 * {@link SelectionCheckboxColumn}. You need this interface to ensure that the display, displays the correct selection
 * state of the {@link SelectionCheckboxColumn}. The method {@link Flushable#flush()} will trigger a redraw of selected
 * lines e.g. with calling the method {@link CellTable#flush()}.
 * 
 * @author D064976
 */
public interface Flushable {
    /**
     * This method will trigger a redraw of the {@link HasData display} so the {@link SelectionCheckboxColumn} is shown
     * correctly again. This is especially imported when the selection comes form the program and not from the user.
     */
    public void flush();
}