package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.Distance;

public interface MarkVectorGraphics {
    void drawMarkToCanvas(Context2d ctx, boolean isSelected, double width, double height, double scaleFactor);

    Distance getMarkHeight();

    Distance getMarkWidth();
}
