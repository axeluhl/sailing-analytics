package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.gwt.ui.client.TimeListenerWithStoppingCriteria;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.racemap.FullCanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.util.ToolTip;
import com.sap.sailing.gwt.ui.simulator.util.WindFieldMapMouseMoveHandler;

/**
 * A google map overlay based on a HTML5 canvas for drawing a wind field. The overlay covers the whole map and displays
 * the wind objects inside it.
 * 
 * @author Nidhi Sawhney(D054070)
 * 
 */
public class WindStreamletsCanvasOverlay extends FullCanvasOverlay implements TimeListenerWithStoppingCriteria {

    /* The wind field that is to be displayed in the overlay */
    protected WindFieldDTO wl;
    /*
     * Map containing the windfield for easy retrieval with key as time point.
     */
    protected SortedMap<Long, List<SimulatorWindDTO>> timePointWindDTOMap;

    /* The points where ToolTip is to be displayed */
    protected Map<ToolTip, SimulatorWindDTO> windFieldPoints;
    protected String arrowColor = "Black";
    protected String arrowHeadColor = "Blue";
    protected WindFieldMapMouseMoveHandler mmHandler;
    protected double arrowLength = 15;

    private int xRes;
    private int yRes;
    private Timer timer;

    private static Logger logger = Logger.getLogger(WindStreamletsCanvasOverlay.class.getName());

    public WindStreamletsCanvasOverlay(final Timer timer, int xRes, int yRes) {
        super();
        this.timer = timer;
        this.xRes = xRes;
        this.yRes = yRes;
        init();
    }

    public WindStreamletsCanvasOverlay() {
        super();
        this.timer = null;
        init();
    }

    private void init() {
        wl = null;
        windFieldPoints = new HashMap<ToolTip, SimulatorWindDTO>();

        mmHandler = new WindFieldMapMouseMoveHandler(this);

        mmHandler.setWindFieldPoints(windFieldPoints);

        timePointWindDTOMap = new TreeMap<Long, List<SimulatorWindDTO>>();

    }

    public void setWindField(final WindFieldDTO wl) {
        this.wl = wl;

        timePointWindDTOMap.clear();
        if (wl != null) {
            for(final SimulatorWindDTO w : wl.getMatrix()) {
                if (!timePointWindDTOMap.containsKey(w.timepoint)) {
                    timePointWindDTOMap.put(w.timepoint, new LinkedList<SimulatorWindDTO>());
                }
                timePointWindDTOMap.get(w.timepoint).add(w);
            }
        }

    }

    public void setArrowColor(final String arrowColor, final String arrowHeadColor) {
        this.arrowColor = arrowColor;
        this.arrowHeadColor = arrowHeadColor;
    }

    @Override
    protected void initialize(final MapWidget map) {
        super.initialize(map);
        map.addMapMouseMoveHandler(mmHandler);
        if (timer != null) {
            this.timer.addTimeListener(this);
        }
        setVisible(true);
    }

    @Override
    protected void remove() {
        setVisible(false);
        getMap().removeMapMouseMoveHandler(mmHandler);
        if (timer != null) {
            this.timer.removeTimeListener(this);
        }
        super.remove();
    }

    @Override
    protected Overlay copy() {
        return new WindStreamletsCanvasOverlay(this.timer, this.xRes, this.yRes);
    }

    @Override
    protected void redraw(final boolean force) {
        super.redraw(force);
        if (wl != null) {
            clear();
            //drawCanvas();
            drawWindField();
        }

    }

    private void clear() {
        canvas.getContext2d().clearRect(0.0 /*canvas.getAbsoluteLeft()*/, 0.0/*canvas.getAbsoluteTop()*/,
                canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
        windFieldPoints.clear();
        mmHandler.clear();
    }

    protected void drawWindField() {

        if (timer != null) {
            timeChanged(timer.getTime());
        } else {
            drawWindField(wl.getMatrix());
        }

    }

    protected void drawScaledArrow(final SimulatorWindDTO windDTO, final double angle, final int index, final double pxLength, final boolean drawHead) {

        final double aWidth = 1.0;
        drawArrow(windDTO, angle, pxLength, aWidth, index, drawHead);

    }

    protected void drawWindField(final List<SimulatorWindDTO> windDTOList) {
        final boolean drawHead = false;
        clear();
        final Context2d context2d = canvas.getContext2d();
        context2d.setGlobalAlpha(0.4);

        if (windDTOList != null && windDTOList.size() > 0) {
            Iterator<SimulatorWindDTO> windDTOIter = windDTOList.iterator();
            final SimulatorWindDTO w0 = windDTOIter.next();
            final SimulatorWindDTO w1 = windDTOIter.next();
            final LatLng pg0 = LatLng.newInstance(w0.position.latDeg, w0.position.lngDeg);
            final LatLng pg1 = LatLng.newInstance(w1.position.latDeg, w1.position.lngDeg);
            final Point px0 = getMap().convertLatLngToDivPixel(pg0);
            final Point px1 = getMap().convertLatLngToDivPixel(pg1);
            final double dx = px0.getX()-px1.getX();
            final double dy = px0.getY()-px1.getY();
            final double pxLength = Math.sqrt( dx*dx + dy*dy );
            logger.fine("pxLength = "+pxLength);

            windDTOIter = windDTOList.iterator();
            int index = 0;
            while (windDTOIter.hasNext()) {
                SimulatorWindDTO windDTO = windDTOIter.next();
                //System.out.println("wind angle: "+index+", "+windDTO.trueWindBearingDeg);
                if (((index % xRes) > 0)&&((index % xRes) < (xRes-1))) {
                    DegreeBearingImpl dbi = new DegreeBearingImpl(windDTO.trueWindBearingDeg);
                    drawScaledArrow(windDTO, dbi.getRadians(), index, pxLength, drawHead);
                }
                index++;
            }
            final String title = "Wind Field at " + windDTOList.size() + " points.";
            getCanvas().setTitle(title);
        }
    }

    protected void drawArrow(final SimulatorWindDTO windDTO, final double angle, final double length, final double weight, final int index, final boolean drawHead) {
        final String msg = "Wind @ P" + index + ": time : " + windDTO.timepoint + " speed: " + windDTO.trueWindSpeedInKnots + "knots "
                + windDTO.trueWindBearingDeg;
        logger.fine(msg);

        final Context2d context2d = canvas.getContext2d();
        context2d.setGlobalAlpha(0.2);

        final PositionDTO position = windDTO.position;

        final LatLng positionLatLng = LatLng.newInstance(position.latDeg, position.lngDeg);
        final Point canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);

        final int x = canvasPositionInPx.getX() - this.getWidgetPosLeft();
        final int y = canvasPositionInPx.getY() - this.getWidgetPosTop();

        windFieldPoints.put(new ToolTip(x, y), windDTO);

        final double dx = length * Math.sin(angle);
        final double dy = -length * Math.cos(angle);

        final double x1 = x + dx / 2;
        final double y1 = y + dy / 2;

        drawLine(x - dx / 2, y - dy / 2, x1, y1, weight, arrowColor);

        final double theta = Math.atan2(-dy, dx);

        final double hLength = Math.max(6.,6.+(10./(60.-10.))*Math.max(length-6.,0));
        logger.finer("headlength: "+hLength+", arrowlength: "+length);

        if (drawHead) {
            drawHead(x1, y1, theta, hLength, weight);
        }
        //String text = "P" + index;// + NumberFormat.getFormat("0.00").format(windDTO.trueWindBearingDeg) + "°";
        //drawPointWithText(x, y, text);
        //drawPoint(x, y);
        
        context2d.setGlobalAlpha(0.4);

    }

    protected void drawHead(final double x, final double y, final double theta, final double headLength, final double weight) {

        double t = theta + (Math.PI / 4);
        if (t > Math.PI) {
            t -= 2 * Math.PI;
        }
        double t2 = theta - (Math.PI / 4);
        if (t2 <= (-Math.PI)) {
            t2 += 2 * Math.PI;
        }

        final double x1 = (x - Math.cos(t) * headLength);
        final double y1 = (y + Math.sin(t) * headLength);
        final double x1o = (x + Math.cos(t) * weight/2);
        final double y1o = (y - Math.sin(t) * weight/2);
        final double x2 = (x - Math.cos(t2) * headLength);
        final double y2 = (y + Math.sin(t2) * headLength);
        final double x2o = (x + Math.cos(t2) * weight/2);
        final double y2o = (y - Math.sin(t2) * weight/2);
        drawLine(x1o, y1o, x1, y1, weight, arrowHeadColor);
        drawLine(x2o, y2o, x2, y2, weight, arrowHeadColor);

    }

    @Override
    public void timeChanged(final Date date) {

        List<SimulatorWindDTO> windDTOToDraw = new ArrayList<SimulatorWindDTO>();

        final SortedMap<Long, List<SimulatorWindDTO>> headMap = (timePointWindDTOMap.headMap(date.getTime()+1));

        if (!headMap.isEmpty()) {
            windDTOToDraw = headMap.get(headMap.lastKey());
        }
        logger.info("In WindFieldCanvasOverlay.drawWindField drawing " + windDTOToDraw.size() + " points" + " @ "
                + date);

        drawWindField(windDTOToDraw);

    }

    @Override
    public int stop() {
        if (!this.isVisible() || timePointWindDTOMap == null || timer == null   || timePointWindDTOMap.isEmpty()) {
            return 0;
        }
        if (timePointWindDTOMap.lastKey() < timer.getTime().getTime()) {
            return 0;
        } else {
            return 1;
        }
    }

    public void setTimer(final Timer timer) {
        this.timer = timer;
    }

    public Timer getTimer() {
        return timer;
    }
}
