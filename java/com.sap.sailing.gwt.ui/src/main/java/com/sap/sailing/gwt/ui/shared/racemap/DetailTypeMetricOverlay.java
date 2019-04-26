package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.controls.ControlPosition;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.CoordinateSystem;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;
import com.sap.sse.common.ColorMapper;
import com.sap.sse.common.ValueRangeFlexibleBoundaries;

public class DetailTypeMetricOverlay extends FullCanvasOverlay {
    
    private final String textColor = "Black";
    private final String textFont = "10pt 'Open Sans'";
    private double lineWidth = 250;
    private double lineHeight = 13;
    private double lineMargin = 5;
    private final StringMessages stringMessages;
    private Canvas metricLegend;
    
    private ValueRangeFlexibleBoundaries valueRange;
    private ColorMapper colorMapper;
    
    private String detailTypeAndUnit = "";

    public DetailTypeMetricOverlay(MapWidget map, int zIndex, CoordinateSystem coordinateSystem, StringMessages stringMessages) {
        super(map, zIndex, coordinateSystem);
        this.stringMessages = stringMessages;
    }
    
    protected void createMetricLegend(MapWidget map) {
        metricLegend = Canvas.createIfSupported();
        metricLegend.setStyleName("MapMetricLegend");
        metricLegend.setTitle("Metric Legend");
        map.setControls(ControlPosition.TOP_CENTER, metricLegend);
        metricLegend.getParent().setStyleName("MapMetricLegendParentDiv");
    }

    @Override
    public void setVisible(boolean isVisible) {
        super.setVisible(isVisible);
        if (metricLegend != null) {
            metricLegend.setVisible(isVisible);
        }
    }

    @Override
    protected void draw() {
        if (mapProjection != null) {
            super.setCanvasSettings();
            drawLegend();
        }
    }

    @Override
    protected void drawCenterChanged() {
        draw();
    }

    public void clearCanvas() {
        if (metricLegend != null) {
            Context2d g = this.getCanvas().getContext2d();
            double w = metricLegend.getOffsetWidth();
            double h = metricLegend.getOffsetHeight();
            g = metricLegend.getContext2d();
            g.clearRect(0, 0, w, h);            
        }
    }
    
    public void updateLegend(ValueRangeFlexibleBoundaries valueRange, ColorMapper colorMapper, DetailType detailType) {
        this.valueRange = valueRange;
        this.colorMapper = colorMapper;
        this.detailTypeAndUnit = DetailTypeFormatter.format(detailType) + " - " + DetailTypeFormatter.getUnit(detailType);
        draw();
    }

    public void drawLegend() {
        if (isVisible()) {
            if (metricLegend == null) {
                createMetricLegend(map);
            }
            Context2d context2d = metricLegend.getContext2d();
            
            int canvasWidth = (int) Math.ceil(lineWidth + lineMargin * 2);
            int canvasHeight = (int) Math.ceil(lineHeight * 5 + lineMargin * 2);
            setCanvasSize(metricLegend, canvasWidth, canvasHeight);
            context2d.setGlobalAlpha(0.75); //TODO Check color
            drawRectangle(context2d, 0, 0, canvasWidth, canvasHeight, "white");
            context2d.setGlobalAlpha(1.0);
            drawTextCentered(context2d, lineMargin, lineToYOffset(0), canvasWidth - 2 * lineMargin, "Tail Color", textColor);
            drawText(context2d, lineMargin * 2, lineToYOffset(1), canvasWidth - 4 * lineMargin, detailTypeAndUnit, textColor);
            
            drawSpectrum(context2d, lineMargin * 2, lineToYOffset(2) - 6, canvasWidth - 4 * lineMargin);
        }
    }
    
    private double lineToYOffset(int line) {
        return lineMargin + (lineHeight * ++line);
    }
    
    protected void setCanvasSize(Canvas canvas, int canvasWidth, int canvasHeight) {
        canvas.setWidth(String.valueOf(canvasWidth));
        canvas.setHeight(String.valueOf(canvasHeight));
        canvas.setCoordinateSpaceWidth(canvasWidth);
        canvas.setCoordinateSpaceHeight(canvasHeight);
    }
    
    protected void drawRectangle(Context2d context2d, double fromX, double fromY, double toX, double toY, String color) {
        context2d.setFillStyle(color);
        context2d.setLineWidth(3);
        context2d.fillRect(fromX, fromY, toX, toY);
    }
    
    protected void drawText(Context2d context2d, double x, double y, double maxWidth, String text, String color) {
        context2d.setFillStyle(color);
        context2d.setFont(textFont);
        context2d.fillText(text, x, y, maxWidth);
    }
    
    protected void drawTextCentered(Context2d context2d, double x, double y, double width, String text, String color) {
        TextMetrics metrics = context2d.measureText(text);
        double offset = Math.max((width - metrics.getWidth()) / 2.0, 0.0);
        drawText(context2d, x + offset, y, width - 2 * offset, text, color);
    }
    
    protected void drawSpectrum(Context2d context2d, double x, double y, double width) {
        if (valueRange == null || colorMapper == null) return;
        final double min = valueRange.getMinLeft();
        final double spread = valueRange.getMaxRight() - min;
        final int scale_spread;
        if (spread < 0.5) {
            scale_spread = 300;
        } else if (spread < 1) {
            scale_spread = 100;
        } else {
            scale_spread = 30;
        }
        //final int maxIdx = 300;
        final double h = 15;
        String label;
        TextMetrics txtmet;
        final NumberFormat numberFormatOneDecimal = NumberFormatterFactory.getDecimalFormat(1);
        for (int idx = 0; idx <= width; idx++) {
            final double speedSteps = min + idx * (spread) / width;
            context2d.setFillStyle(colorMapper.getColor(speedSteps));
            context2d.beginPath();
            context2d.fillRect(x + idx, y, 1, h);
            context2d.closePath();
            context2d.stroke();
            if (idx % scale_spread == 0) {
                context2d.setStrokeStyle(textColor);
                context2d.setLineWidth(1.0);
                context2d.beginPath();
                context2d.moveTo(x + idx, y + h);
                context2d.lineTo(x + idx, y + h + 7.0);
                context2d.closePath();
                context2d.stroke();
                context2d.setFillStyle(textColor);
                label = numberFormatOneDecimal.format(speedSteps);
                txtmet = context2d.measureText(label);
                context2d.fillText(label, x + idx - txtmet.getWidth() / 2.0, y + h + 8.0 + 8.0);
            }
        }
    }
}
