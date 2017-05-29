package com.sap.sailing.gwt.ui.leaderboardedit;

import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;

/**
 * Between a column that uses a {@link CompositeCell} and the cell providers with their {@link FieldUpdater}s there
 * needs to be some synchronization in case the field updater performs an asynchronous call to the server and needs
 * to know the row index to update the row in the table after receiving the server response. The column's
 * {@link FieldUpdater} is called after the component cell's {@link FieldUpdater}. A white board pattern
 * is needed where the column's field updater contributes the row index and the server response-handling
 * {@link AsyncCallback#onSuccess(Object) onSuccess} method looks it up there. Whoever comes last will
 * trigger the actual row update.<p>
 * 
 * Assuming a single thread traversing the UI event hierarchy, the call to the olumn's field updater that follows
 * the call to the cell's field updater is expected to belong to the same update sequence. Therefore, the cell's
 * field updater can set a row update "white board" on the column to which the column writes the row to update
 * once it knows.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RowUpdateWhiteboard<T> {
    private final ListDataProvider<T> listToUpdate;
    
    private int indexOfRowToUpdate;
    
    private T objectWithWhichToUpdateRow;
    
    public RowUpdateWhiteboard(ListDataProvider<T> listToUpdate) {
        this.listToUpdate = listToUpdate;
        indexOfRowToUpdate = -1;
    }
    
    public synchronized void setObjectWithWhichToUpdateRow(T object) {
        if (objectWithWhichToUpdateRow != null) {
            throw new IllegalStateException("Can't use the same whiteboard twice");
        }
        objectWithWhichToUpdateRow = object;
        if (indexOfRowToUpdate != -1) {
            update();
        }
    }

    private void update() {
        listToUpdate.getList().set(indexOfRowToUpdate, objectWithWhichToUpdateRow);
        listToUpdate.flush(); // in case we're being called outside the UI thread, e.g., in a server update
    }
    
    public synchronized void setIndexOfRowToUpdate(int indexOfRowToUpdate) {
        if (this.indexOfRowToUpdate != -1) {
            throw new IllegalStateException("Can't use the same whiteboard twice");
        }
        this.indexOfRowToUpdate = indexOfRowToUpdate;
        if (objectWithWhichToUpdateRow != null) {
            update();
        }
    }
}
