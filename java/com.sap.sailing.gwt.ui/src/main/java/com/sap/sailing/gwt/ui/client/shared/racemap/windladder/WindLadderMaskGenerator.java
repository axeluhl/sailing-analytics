package com.sap.sailing.gwt.ui.client.shared.racemap.windladder;

import com.google.gwt.canvas.dom.client.Context2d;

@FunctionalInterface
public interface WindLadderMaskGenerator {
    /**
     * Contract: The state of {@code ctx} has to be the same when exiting the method call as what it was when it was
     * entered.
     */
    void drawMask(int width, int height, Context2d ctx);
}