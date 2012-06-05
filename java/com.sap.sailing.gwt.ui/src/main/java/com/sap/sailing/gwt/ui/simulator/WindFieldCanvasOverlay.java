package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
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
public class WindFieldCanvasOverlay extends FullCanvasOverlay implements TimeListener {

    /* The wind field that is to be displayed in the overlay */
    protected WindFieldDTO wl;
    /*
     * Map containing the windfield for easy retrieval with key as time point.
     */
    protected TreeMap<Long, List<WindDTO>> timePointWindDTOMap;
    
    /* The points where ToolTip is to be displayed */
    protected Map<ToolTip, WindDTO> windFieldPoints;
    protected String arrowColor = "Blue";
    protected String arrowHeadColor = "Blue";
    protected WindFieldMapMouseMoveHandler mmHandler;
    protected double arrowLength = 15;
    
   
    
    protected final Timer timer;

    private static Logger logger = Logger.getLogger("com.sap.sailing");

    public WindFieldCanvasOverlay(Timer timer) {
        super();
        this.timer = timer;
        init();
    }

    public WindFieldCanvasOverlay() {
        super();
        this.timer = null;
        init();
    }

    private void init() {
        windFieldPoints = new HashMap<ToolTip, WindDTO>();

        mmHandler = new WindFieldMapMouseMoveHandler(this);

        mmHandler.setWindFieldPoints(windFieldPoints);
        
        timePointWindDTOMap = new TreeMap<Long, List<WindDTO>>();
        
    }

    public void setWindField(WindFieldDTO wl) {
        this.wl = wl;
        /*
        timePointWindDTOMap.clear();
        if (wl != null) {
            for(WindDTO w : wl.getMatrix()) {
                if (!timePointWindDTOMap.containsKey(w.timepoint)) {
                    timePointWindDTOMap.put(w.timepoint, new LinkedList<WindDTO>());
                }
                timePointWindDTOMap.get(w.timepoint).add(w);
            }
        }
        */
    }

    public void setArrowColor(String arrowColor, String arrowHeadColor) {
        this.arrowColor = arrowColor;
        this.arrowHeadColor = arrowHeadColor;
    }

    @Override
    protected void initialize(MapWidget map) {
        super.initialize(map);
        map.addMapMouseMoveHandler(mmHandler);
        if (timer != null) {
            this.timer.addTimeListener(this);
        }
    }

    @Override
    protected void remove() {
        getMap().removeMapMouseMoveHandler(mmHandler);
        if (timer != null) {
            this.timer.removeTimeListener(this);
        }
        super.remove();
    }

    @Override
    protected Overlay copy() {
        return new WindFieldCanvasOverlay(this.timer);
    }

    @Override
    protected void redraw(boolean force) {
        super.redraw(force);
        if (wl != null) {
            clear();
            drawCanvas();
            drawWindField();
        }

    }

    private void clear() {
        canvas.getContext2d().clearRect(canvas.getAbsoluteLeft(), canvas.getAbsoluteTop(),
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

    protected void drawWindField(final List<WindDTO> windDTOList) {
        clear();
        if (windDTOList != null && windDTOList.size() > 0) {
            Iterator<WindDTO> windDTOIter = windDTOList.iterator();
            int index = 0;
            while (windDTOIter.hasNext()) {
                WindDTO windDTO = windDTOIter.next();
                int width = (int) Math.max(1, Math.min(2, Math.round(windDTO.trueWindSpeedInMetersPerSecond)));
                DegreeBearingImpl dbi = new DegreeBearingImpl(windDTO.trueWindBearingDeg);
                drawArrow(windDTO, dbi.getRadians(), arrowLength, width, ++index);

            }
            String title = "Wind Field at " + windDTOList.size() + " points.";
            getCanvas().setTitle(title);
        }
    }

    protected void drawArrow(WindDTO windDTO, double angle, double length, double weight, int index) {
        String msg = "Wind @ P" + index + ": time : " + windDTO.timepoint + " speed: " + windDTO.trueWindSpeedInKnots
                + "knots " + windDTO.trueWindSpeedInMetersPerSecond + "m/s " + windDTO.trueWindBearingDeg;
        logger.fine(msg);

        PositionDTO position = windDTO.position;

        LatLng positionLatLng = LatLng.newInstance(position.latDeg, position.lngDeg);
        Point canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);

        int x = canvasPositionInPx.getX() - this.getWidgetPosLeft();
        int y = canvasPositionInPx.getY() - this.getWidgetPosTop();

        windFieldPoints.put(new ToolTip(x, y), windDTO);

        // TODO check if the angles are correct
        double dx = -length * Math.sin(angle); // -90 degree rotation
        double dy = length * Math.cos(angle);

        double x1 = x + dx / 2;
        double y1 = y + dy / 2;

        drawLine(x - dx / 2, y - dy / 2, x1, y1, weight, arrowColor);

        double theta = Math.atan2(-dy, dx);

        drawHead(x1, y1, theta, length / 2, weight);
        //String text = "P" + index;// + NumberFormat.getFormat("0.00").format(windDTO.trueWindBearingDeg) + "°";
        //drawPointWithText(x, y, text);
        drawPoint(x, y);
    }

    protected void drawHead(double x, double y, double theta, double headLength, double weight) {

        double t = theta + (Math.PI / 4);
        if (t > Math.PI)
            t -= 2 * Math.PI;
        double t2 = theta - (Math.PI / 4);
        if (t2 <= (-Math.PI))
            t2 += 2 * Math.PI;

        double x1 = Math.round(x - Math.cos(t) * headLength);
        double y1 = Math.round(y + Math.sin(t) * headLength);
        double x2 = Math.round(x - Math.cos(t2) * headLength);
        double y2 = Math.round(y + Math.sin(t2) * headLength);
        drawLine(x, y, x1, y1, weight, arrowHeadColor);
        drawLine(x, y, x2, y2, weight, arrowHeadColor);

    }

    @Override
    public void timeChanged(Date date) {
        // canvas.getContext2d().clearRect(canvas.getAbsoluteLeft(), canvas.getAbsoluteTop(),
        // canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
        List<WindDTO> windDTOToDraw = new ArrayList<WindDTO>();
        for (WindDTO windDTO : wl.getMatrix()) {
            if (windDTO.timepoint.equals(date.getTime())) {
                windDTOToDraw.add(windDTO);
            }
        }
        /*
        Entry<Long, List<WindDTO>> entry = (timePointWindDTOMap.floorEntry(date.getTime()));
        
        if(entry != null) {
          windDTOToDraw = entry.getValue();
        }*/
        logger.info("In WindFieldCanvasOverlay.drawWindField drawing " + windDTOToDraw.size() + " points" + " @ "
                + date);
        if (windDTOToDraw.size() == 0) {
            timer.stop();
        } else {
            drawWindField(windDTOToDraw);
        }
    }

}
