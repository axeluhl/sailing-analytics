package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;

/**
 * Utility class extending {@link FlowPanel} and implementing {@link RequiresResize} interface. All
 * {@link #getChildren() children} also implementing {@link RequiresResize} are informed by calling their
 * {@link RequiresResize#onResize()} method.
 */
class ResizableFlowPanel extends FlowPanel implements RequiresResize {
    @Override
    public void onResize() {
        WidgetCollection children = getChildren();
        for (Widget widget : children) {
            if (widget instanceof RequiresResize) {
                ((RequiresResize) widget).onResize();
            }
        }
    }
}
