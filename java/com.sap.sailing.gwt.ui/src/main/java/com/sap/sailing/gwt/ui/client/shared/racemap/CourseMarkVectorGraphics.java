package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;

/**
 * The base class for course mark graphics based on SVG graphics drawn to a HTML5 canvas
 * @author Frank
 *
 */
public abstract class CourseMarkVectorGraphics {
    protected double courseMarkSizeInMeters = 1.0;

    public void drawCourseMarkToCanvas(Context2d ctx, boolean isSelected, 
            double width, double height, double scaleFactor, String color) {

        ctx.save();
        ctx.clearRect(0,  0,  width, height);

        ctx.translate(width / 2.0, height / 2.0);
        ctx.scale(scaleFactor, scaleFactor);

        ctx.translate(-courseMarkSizeInMeters * 100 / 2.0,- courseMarkSizeInMeters * 100 / 2.0);

        drawCourseMark(ctx, isSelected, color);
        
        ctx.restore();
    }

    protected void drawCourseMark(Context2d ctx, boolean isSelected, String color) {
        
    }

}
