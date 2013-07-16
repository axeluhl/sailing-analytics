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
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.util.ToolTip;
import com.sap.sailing.gwt.ui.simulator.util.WindFieldMapMouseMoveHandler;

/**
 * A google map overlay based on a HTML5 canvas for drawing a wind field. The overlay covers the whole map and displays
 * the wind objects inside it.
 * 
 * @author Nidhi Sawhney(D054070)
 * 
 */
public class WindFieldCanvasOverlay extends FullCanvasOverlay implements TimeListenerWithStoppingCriteria {

    /* The wind field that is to be displayed in the overlay */
    protected WindFieldDTO wl;
    /*
     * Map containing the windfield for easy retrieval with key as time point.
     */
    protected SortedMap<Long, List<SimulatorWindDTO>> timePointWindDTOMap;

    /* The points where ToolTip is to be displayed */
    protected Map<ToolTip, SimulatorWindDTO> windFieldPoints;
    protected String arrowColor = "Blue";
    protected String arrowHeadColor = "Blue";
    protected WindFieldMapMouseMoveHandler mmHandler;
    protected double arrowLength = 15;
    protected WindFieldGenParamsDTO windParams = null;

    private Timer timer;

    private static Logger logger = Logger.getLogger(WindFieldCanvasOverlay.class.getName());

    public WindFieldCanvasOverlay(final Timer timer, final WindFieldGenParamsDTO windParams) {
        super();
        this.timer = timer;
        this.windParams = windParams;
        init();
    }

    public WindFieldCanvasOverlay() {
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
        return new WindFieldCanvasOverlay(this.timer, this.windParams);
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

    protected void drawScaledArrow(final SimulatorWindDTO windDTO, final double angle, final int index, final boolean drawHead) {

        final double aWidth = Math.max(1., (PathPolyline.knotsToMetersPerSecond(windDTO.trueWindSpeedInKnots) / 2.));
        final double aLength = Math.max(10., (4. * PathPolyline.knotsToMetersPerSecond(windDTO.trueWindSpeedInKnots)));
        //System.out.println("arrow speed: "+windDTO.trueWindSpeedInMetersPerSecond+" angle:"+angle+" aWidth: "+aWidth+" aLength: "+aLength);
        drawArrow(windDTO, angle, aLength, aWidth, arrowColor, index, drawHead);

    }

    protected void drawWindField(final List<SimulatorWindDTO> windDTOList) {
        final boolean drawHead = true;
        clear();
        final Context2d context2d = canvas.getContext2d();
        context2d.setGlobalAlpha(0.4);

        if (windDTOList != null && windDTOList.size() > 0) {
            final Iterator<SimulatorWindDTO> windDTOIter = windDTOList.iterator();
            int index = 0;
            while (windDTOIter.hasNext()) {
                final SimulatorWindDTO windDTO = windDTOIter.next();
                //System.out.println("wind angle: "+windDTO.trueWindBearingDeg);
                final DegreeBearingImpl dbi = new DegreeBearingImpl(windDTO.trueWindBearingDeg);
                drawScaledArrow(windDTO, dbi.getRadians(), ++index, drawHead);
            }
            final String title = "Wind Field at " + windDTOList.size() + " points.";
            getCanvas().setTitle(title);
        }
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
    public boolean shallStop() {
        if (!this.isVisible() || timePointWindDTOMap == null || timer == null   || timePointWindDTOMap.isEmpty()) {
            return true;
        }
        if (timePointWindDTOMap.lastKey() < timer.getTime().getTime()) {
            return true;
        } else {
            return false;
        }
    }

    public void setTimer(final Timer timer) {
        this.timer = timer;
    }

    public Timer getTimer() {
        return timer;
    }
}
