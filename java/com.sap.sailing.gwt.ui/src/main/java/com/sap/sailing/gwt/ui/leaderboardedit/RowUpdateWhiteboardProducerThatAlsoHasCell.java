package com.sap.sailing.gwt.ui.leaderboardedit;

import com.google.gwt.cell.client.HasCell;

public interface RowUpdateWhiteboardProducerThatAlsoHasCell<T, C> extends RowUpdateWhiteboardProducer<T>, HasCell<T, C> {
    /**
     * When this {@link HasCell} is used to provide a cell in a {@link CompositeCellRememberingRenderingContextAndObject},
     * then when the composite cell renders a value of type <code>T</code>, this method is called before {@link HasCell#getCell()}
     * and {@link HasCell#getValue(Object)} are called.
     */
    void setCurrentlyRendering(T object);
}
