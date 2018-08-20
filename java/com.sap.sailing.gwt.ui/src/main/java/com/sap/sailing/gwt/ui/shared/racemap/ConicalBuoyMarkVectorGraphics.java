package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sse.common.Color;

public class ConicalBuoyMarkVectorGraphics extends AbstractMarkVectorGraphics implements MarkVectorGraphics {

    public ConicalBuoyMarkVectorGraphics(MarkType type, Color color, String shape, String pattern) {
        super(type, color, shape, pattern);
    }

    @Override
    protected void drawMarkBody(Context2d ctx, boolean isSelected, String color) {
        ctx.setStrokeStyle("rgba(0,0,0,0)");

        ctx.save();
        ctx.setFillStyle(color);
        ctx.transform(12.5, 0, 0, 12.5, -578, 22.2);
        ctx.beginPath();
        ctx.arc(49.7, 12.8, 2.66, 0, doublePi, false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.transform(12.5, 0, 0, 12.5, -578, 22.2);
        ctx.beginPath();
        ctx.arc(49.7, 11.3, 3.37, 0, doublePi, false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle(color);
        ctx.transform(12.5, 0, 0, 12.5, -578, 22.2);
        ctx.beginPath();
        ctx.arc(49.6, 10.6, 2.66, 0, doublePi, false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.beginPath();
        ctx.moveTo(36.9, 98);
        ctx.lineTo(47.5, 98);
        ctx.quadraticCurveTo(47.5, 98, 47.5, 98);
        ctx.lineTo(47.5, 154.5);
        ctx.quadraticCurveTo(47.5, 154.5, 47.5, 154.5);
        ctx.lineTo(36.9, 154.5);
        ctx.quadraticCurveTo(36.9, 154.5, 36.9, 154.5);
        ctx.lineTo(36.9, 98);
        ctx.quadraticCurveTo(36.9, 98, 36.9, 98);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.beginPath();
        ctx.moveTo(43.6, 0.372);
        ctx.lineTo(87.3, 102);
        ctx.lineTo(-0.714, 102);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle(color);
        ctx.beginPath();
        ctx.moveTo(43.6, 14.7);
        ctx.lineTo(78.5, 96.6);
        ctx.lineTo(8.07, 96.6);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
    }
}
