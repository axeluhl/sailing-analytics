package com.sap.sailing.gwt.managementconsole.partials.contextmenu;

import com.sap.sailing.gwt.managementconsole.mvp.View;

/**
 * Interface for {@link View view} implementations which provide a {@link ContextMenu context menu}.
 *
 * @param <T>
 *            the actual type of the item to show the context menu for
 */
public interface HasContextMenuView<T> {

    /**
     * Shows the {@link ContextMenu context menu} for the given item.
     *
     * @param item
     *            the item to show the context menu for
     */
    void showContextMenu(T item);

    /**
     * Interface for presenter implementations associated to {@link HasContextMenuView views providing a context menu}
     *
     * @param <T>
     *            the actual type of the item to request the context menu for
     */
    public interface Presenter<T> {

        /**
         * Request the {@link ContextMenu context menu} for the given item.
         *
         * @param item
         *            the item to request the context menu for
         */
        void requestContextMenu(T item);
    }
}
