package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sse.common.Color;

public class SimpleBuoyMarkVectorGraphics extends AbstractMarkVectorGraphics implements MarkVectorGraphics {

    public SimpleBuoyMarkVectorGraphics(MarkType type, Color color, String shape, String pattern) {
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
        ctx.transform(4.98, 0, 0, 10.8, -205, 50.4);
        ctx.beginPath();
        ctx.moveTo(50.8, 9.84);
        ctx.lineTo(48.5, 9.84);
        ctx.lineTo(48.5, 1.44);
        ctx.lineTo(50.8, 1.9);
        ctx.lineTo(50.8, 9.84);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.transform(12.5, 0, 0, 12.5, -570, 22.2);
        ctx.save();
        ctx.setFillStyle(color);
        ctx.beginPath();
        ctx.moveTo(48.8, 3.82);
        ctx.lineTo(48.8, -1.32);
        ctx.lineTo(57, 1.05);
        ctx.lineTo(48.8, 3.82);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle(DEFAULT_MARK_BG_COLOR);
        ctx.beginPath();
        ctx.moveTo(48.5, 4.16);
        ctx.lineTo(48.5, -1.65);
        ctx.lineTo(57.8, 1.03);
        ctx.lineTo(48.5, 4.16);
        ctx.closePath();
        ctx.moveTo(49, -0.994);
        ctx.lineTo(49, 3.47);
        ctx.lineTo(56.2, 1.07);
        ctx.lineTo(49, -0.994);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        ctx.restore();
    }
}
