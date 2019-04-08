package com.sap.sailing.gwt.ui.leaderboardedit;



/**
 * Produces {@link RowUpdateWhiteboard} instances to be set on a {@link RowUpdateWhiteboardOwner}.
 * When an implementing class produces a {@link RowUpdateWhiteboard}, it has to call
 * {@link RowUpdateWhiteboardOwner#whiteboardProduced(RowUpdateWhiteboard)}.
 * 
 * 
 * @see RowUpdateWhiteboard
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface RowUpdateWhiteboardProducer<T> {
    void setWhiteboardOwner(RowUpdateWhiteboardOwner<T> toReceiveWhiteboardsProduced);
}
