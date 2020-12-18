package com.sap.sailing.gwt.ui.client;

/**
 * Marks a panel as a displayer. Fill method will be called from {@link Refresher} implementation if data reloading
 * needs a rendering or specific reload action from the dialog.
 * 
 * @param <T>
 *            the DTO
 */
public interface Displayer {

    /**
     * This method will be called after data changes take effect and can be used to react on such events, e.g. to
     * re-render the affected part of the UI.
     * 
     * @param result
     *            the currently valid set of DTOs
     */
    //void fill(Iterable<T> result);
}