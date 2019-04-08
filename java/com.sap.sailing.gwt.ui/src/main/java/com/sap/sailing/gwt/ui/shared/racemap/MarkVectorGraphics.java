package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.gwt.ui.shared.CoursePositionsDTO;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;

public interface MarkVectorGraphics {
    void drawMarkToCanvas(Context2d ctx, boolean isSelected, double width, double height, double scaleFactor);

    Distance getMarkHeight();

    Distance getMarkWidth();

    /**
     * If this graphical representation requires different rotations based on its state, this method can be implemented
     * to compute the rotation desired. It is called after the drawing to the canvas, if it was necessary, has been
     * updated.
     * 
     * @return {@code null} if no rotation is desired; this will save the application of a CSS rotation; otherwise, a
     *         rotation as an angle
     */
    Bearing getRotationInDegrees(CoursePositionsDTO coursePositionsDTO);
}
