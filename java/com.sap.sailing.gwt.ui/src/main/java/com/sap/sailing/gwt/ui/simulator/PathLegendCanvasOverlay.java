package com.sap.sailing.gwt.ui.simulator;

import java.util.Date;
import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.gwt.ui.shared.racemap.CanvasOverlay;

/**
 * Class to draw the legend for the different paths on the map.
 * 
 * @author Nidhi Sawhney (D054070)
 *
 */
public class PathLegendCanvasOverlay extends CanvasOverlay {

    private List<PathCanvasOverlay> pathOverlays;

    /* x coordinate where the widget is placed */
    private int widgetPosLeft = 0;
    /* y coordinate where the widget is placed */
    private int widgetPosTop = 0;

    /**
     * Offset where the legend starts
     */
    private final int xOffset = 10;
    private final int yOffset = 20;

    private final double rectWidth = 20;

    private final double rectHeight = 20;

    public String textColor = "Black";

    public String textFont = "normal 10pt UbuntuRegular";
    
    public PathLegendCanvasOverlay() {
        this.setPathOverlays(null);
    }

    @Override
    protected Overlay copy() {
        return new PathLegendCanvasOverlay();
    }

    @Override
    protected void redraw(boolean force) {
   
        if (pathOverlays == null || pathOverlays.size() < 1) {
            return;
        }
        setCanvasSettings();
        int index = 0;
        Context2d context2d = canvas.getContext2d();
        context2d.setFont(textFont);
        TextMetrics txtmet;
        double txtmaxwidth = 0.0;
        for (PathCanvasOverlay path : pathOverlays) {            
             txtmet = context2d.measureText(path.name+" 00:00:00");
             txtmaxwidth = Math.max(txtmaxwidth, txtmet.getWidth());
        }
        for (PathCanvasOverlay path : pathOverlays) {
            drawRectangleWithText(xOffset, yOffset + (pathOverlays.size()-1-index) * rectHeight, path.pathColor, 
                    path.name + " " + getFormattedTime(path.getPathTime()),txtmaxwidth);
            index++;
        }
    }

    protected void setCanvasSettings() {
        int canvasWidth = (int) rectWidth + 200;
        int canvasHeight = getMap().getSize().getHeight();

        canvas.setWidth(String.valueOf(canvasWidth));
        canvas.setHeight(String.valueOf(canvasHeight));
        canvas.setCoordinateSpaceWidth(canvasWidth);
        canvas.setCoordinateSpaceHeight(canvasHeight);

        Point sw = getMap().convertLatLngToDivPixel(getMap().getBounds().getSouthWest());
        Point ne = getMap().convertLatLngToDivPixel(getMap().getBounds().getNorthEast());
        setWidgetPosLeft(Math.min(sw.getX(), ne.getX()));
        setWidgetPosTop(Math.min(sw.getY(), ne.getY()));
        //setWidgetPosLeft(xOffset);
        //setWidgetPosTop(yOffset);
        
        getPane().setWidgetPosition(getCanvas(), getWidgetPosLeft(), getWidgetPosTop());

    }

    public List<PathCanvasOverlay> getPathOverlays() {
        return pathOverlays;
    }

    public void setPathOverlays(List<PathCanvasOverlay> pathOverlays) {
        this.pathOverlays = pathOverlays;
    }

    public int getWidgetPosLeft() {
        return widgetPosLeft;
    }

    public void setWidgetPosLeft(int widgetPosLeft) {
        this.widgetPosLeft = widgetPosLeft;
    }

    public int getWidgetPosTop() {
        return widgetPosTop;
    }

    public void setWidgetPosTop(int widgetPosTop) {
        this.widgetPosTop = widgetPosTop;
    }

    protected void drawRectangle(double x, double y, String color) {
        Context2d context2d = canvas.getContext2d();
        context2d.setFillStyle(color);
        context2d.setLineWidth(3);
        context2d.fillRect(x, y, rectWidth, rectHeight);

    }

    protected void drawRectangleWithText(double x, double y, String color, String text, double textmaxwidth) {
        Context2d context2d = canvas.getContext2d();
        context2d.setFont(textFont);
        drawRectangle(x, y, color);
        context2d.setGlobalAlpha(0.65);
        context2d.setFillStyle("white");
        context2d.fillRect(x + rectWidth, y, 10.0 + textmaxwidth, rectHeight);
        context2d.setGlobalAlpha(1.0);
        context2d.setFillStyle(textColor);
        context2d.fillText(text, x + rectWidth + 5.0, y + 12.0 + 3.0);
    }

    protected String getFormattedTime(long pathTime) {
        TimeZone gmt = TimeZone.createTimeZone(0);
        Date timeDiffDate = new Date(pathTime);
        String pathTimeStr = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.HOUR24_MINUTE_SECOND).format(
                timeDiffDate, gmt);
        return pathTimeStr;
    }
}
