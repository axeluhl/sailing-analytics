package com.sap.sailing.gwt.ui.client;

/**
 * A {@link Refresher} will offer methods to register a displayer of a specific DTO, force reloads and give the
 * possibility to update the centrally managed data. The displayer will be informed asynchronously after data changes
 * have applied.
 * 
 * @param <D>
 *            the {@link Displayer}
 * @param <T>
 *            the base DTO of {@link Displayer} and {@link Refresher}.
 */
public interface Refresher<T> {

    /**
     * Register the {@link Displayer} and call the fill method asynchronously after the initial load of the data is
     * done.
     * 
     * @param displayer
     *            the {@link Displayer} to add.
     */
    void addDisplayerAndCallFillOnInit(Displayer displayer);

    /**
     * Force a reload of the data from server and call afterwards asynchronously the fill method of the given
     * {@link Displayer}.
     * 
     * @param fillOnlyDisplayer
     *            if not <code>null</code> only the fill method of the given {@link Displayer} will be called.
     */
    void reloadAndCallFillOnly(Displayer fillOnlyDisplayer);

    /**
     * Force a reload of the data from server and call afterwards asynchronously the fill method of all registered
     * {@link Displayer}.
     */
    void reloadAndCallFillAll();

    /**
     * Updates the list of DTOs and call asynchronously the fill method of all registered {@link Displayer} except an
     * optional origin {@link Displayer} if set.
     * 
     * @param dtos
     *            full data set of new DTOs
     * @param origin
     *            if not <code>null</code> the given {@link Displayer} will be ignored when calling the fill method.
     */
    void updateAndCallFillForAll(Iterable<T> dtos, Displayer origin);

    /**
     * Call fill method of given {@link Displayer}. If data was not loaded before, do an initial load and call fill
     * method of all registered {@link Displayer} asynchronously.
     * 
     * @param displayer
     *            the {@link Displayer} on which the fill method should be called.
     */
    void callFillAndReloadInitially(Displayer displayer);

}
