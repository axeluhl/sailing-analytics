package com.sap.sailing.gwt.ui.simulator;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.sap.sailing.domain.common.Mile;
import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;

/**
 * This class implements the layer which displays the optimal path on the path. Currently there is a single path to be
 * displayed.
 * 
 * @author D054070
 * 
 */
public class PathCanvasOverlay extends WindFieldCanvasOverlay implements Named {

    /**
     * Generated serial version id.
     */
    private static final long serialVersionUID = -6284996043723173190L;

    // private static Logger logger = Logger.getLogger(PathCanvasOverlay.class.getName());

    private static final int MinimumPxDistanceBetweenArrows = 60;

    private LatLng startPoint;
    private LatLng endPoint;
    private boolean totalTimeIsGiven = false;
    private long totalTimeMilliseconds = 0;
    private double curSpeed;
    private double curBearing;
    
    
    protected String name;

    public String pathColor = "Green";
    public String textFont = "normal 10pt UbuntuRegular";

    
    /**
     * Whether or not to display the wind directions for the points on the optimal path.
     */
    public boolean displayWindAlongPath = true;

    public PathCanvasOverlay(final String name) {
        super();
        this.name = name;
    }

    public PathCanvasOverlay(final String name, final Timer timer) {
        super(timer);
        this.name = name;
    }

    public PathCanvasOverlay(final String name, final long totalTimeMilliseconds) {
        super();
        this.name = name;
        this.totalTimeIsGiven = true;
        this.totalTimeMilliseconds = totalTimeMilliseconds;
    }

    public PathCanvasOverlay(final String name, final Timer timer, final long totalTimeMilliseconds) {
        super(timer);
        this.name = name;
        this.totalTimeIsGiven = true;
        this.totalTimeMilliseconds = totalTimeMilliseconds;
    }

    public void setTotalTimeMilliseconds(final long totalTimeMilliseconds) {
        this.totalTimeIsGiven = true;
        this.totalTimeMilliseconds = totalTimeMilliseconds;
    }

    public long getTotalTimeMilliseconds() {
        return this.totalTimeMilliseconds;
    }

    public void setRaceCourse(final LatLng startPoint, final LatLng endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public void setCurrent(double curSpeed, double curBearing) {
        this.curSpeed = curSpeed;
        this.curBearing = curBearing;
    }
    
    /*
    @Override
    protected void drawWindField() {
        logger.fine("In PathCanvasOverlay.drawWindField");
        List<SimulatorWindDTO> windDTOList = wl.getMatrix();
        drawWindField(windDTOList);
    }
     */
    @Override
    protected void drawWindField(final List<SimulatorWindDTO> windDTOList) {

        //
        // TODO: draw current arrow
        //
        DegreeBearingImpl curBear = new DegreeBearingImpl(this.curBearing);

        Context2d context2d = canvas.getContext2d();
        
        if (this.curSpeed >= 0.0) {
            //drawScaledArrow(windDTO, dbi.getRadians(), index, true);
            double cFactor = 12.0;
            double cWidth = Math.max(1., 1. + (cFactor * PathPolyline.knotsToMetersPerSecond(this.curSpeed) / 3.0));
            double cLength = Math.max(10., 10. + (cFactor * 2. * PathPolyline.knotsToMetersPerSecond(this.curSpeed)));
            int cX = 100;
            int cY = 150; 
            if (this.curSpeed > 0.0) {
                drawArrowPx(cX, cY, curBear.getRadians(), cLength, cWidth, true, "Green");
            }

            context2d.setFont(textFont);
            context2d.setFillStyle(textColor);
        
            TextMetrics txtmet;
            String cText = "Current: " + SimulatorMainPanel.formatSliderValue(curSpeed) + "kn";
            txtmet = context2d.measureText(cText);
            final double timewidth = txtmet.getWidth();
            context2d.fillText(cText, cX-(timewidth/2.0), cY-25);
        }
        
        final WindFieldGenParamsDTO windParams = new WindFieldGenParamsDTO();

        final int numPoints = windDTOList.size();
        if (numPoints < 1) {
            return;
        }
        String title = "Path at " + numPoints + " points.";
        final long totalTime = windDTOList.get(numPoints - 1).timepoint - windDTOList.get(0).timepoint;

        //LatLng start = LatLng.newInstance(windDTOList.get(0).position.latDeg, windDTOList.get(0).position.latDeg);
        //LatLng end = LatLng.newInstance(windDTOList.get(numPoints - 1).position.latDeg, windDTOList.get(numPoints - 1).position.latDeg);

        final double distance = startPoint.distanceFrom(endPoint) / Mile.METERS_PER_NAUTICAL_MILE;

        // final Point startPx = getMap().convertLatLngToDivPixel(startPoint);
        // final Point endPx = getMap().convertLatLngToDivPixel(endPoint);
        // final double rcLengthPx = Math.sqrt(Math.pow(startPx.getX() - endPx.getX(), 2) + Math.pow(startPx.getY() -
        // endPx.getY(), 2));
        // System.out.println("Race Course Pixel Length: "+rcLengthPx);

        // final double arrowDistPx = 60;
        // final long arrowInterleave = Math.max(1, Math.round(arrowDistPx * numPoints / rcLengthPx));
        // System.out.print("Arrow Interleave: "+arrowInterleave+"\n");
        // arrowInterleave = 3;

        if (windDTOList != null && windDTOList.size() > 0) {

            //final Context2d context2d = canvas.getContext2d();
            context2d.setGlobalAlpha(0.8);

            Iterator<SimulatorWindDTO> windDTOIter = windDTOList.iterator();
            SimulatorWindDTO prevWindDTO = null;
            while (windDTOIter.hasNext()) {
                final SimulatorWindDTO windDTO = windDTOIter.next();
                if (prevWindDTO != null) {
                    drawLine(prevWindDTO, windDTO);
                }
                prevWindDTO = windDTO;
            }

            windDTOIter = windDTOList.iterator();
            int index = 0;
            final long startTime = windDTOList.get(0).timepoint;
            prevWindDTO = null; //For the last time arrow was displayed
            while (windDTOIter.hasNext()) {
                final SimulatorWindDTO windDTO = windDTOIter.next();

                //if ((displayWindAlongPath) && ((index % arrowInterleave) == 0)) {
                if (displayWindAlongPath) {
                    if (checkPointsAreFarEnough(windDTO,prevWindDTO)) {
                        final DegreeBearingImpl dbi = new DegreeBearingImpl(windDTO.trueWindBearingDeg);
                        // System.out.print("index: "+index+"\n");

                        drawScaledArrow(windDTO, dbi.getRadians(), index, true);
                        prevWindDTO = windDTO;
                    }
                }
                index++;

                final long timeStep = windParams.getTimeStep().getTime();
                if ((windDTO.timepoint - startTime) % (timeStep) == 0) {
                    drawPoint(windDTO);
                }

            }

            context2d.setGlobalAlpha(1.0);

            // MeterDistance meterDistance = new MeterDistance(distance);
            final Date timeDiffDate = new Date(totalTime);
            final TimeZone gmt = TimeZone.createTimeZone(0);
            title += " " + NumberFormat.getFormat("0.00").format(distance) + " nmi";
            title += " in " + DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.HOUR24_MINUTE_SECOND).format(timeDiffDate, gmt);

            // logger.info(title);
            getCanvas().setTitle(title);
        }
    }

    private void drawLine(final SimulatorWindDTO p1, final SimulatorWindDTO p2) {

        final double weight = 3.0;

        PositionDTO position = p1.position;

        LatLng positionLatLng = LatLng.newInstance(position.latDeg, position.lngDeg);
        Point canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);

        final int x1 = canvasPositionInPx.getX() - this.getWidgetPosLeft();
        final int y1 = canvasPositionInPx.getY() - this.getWidgetPosTop();

        position = p2.position;
        positionLatLng = LatLng.newInstance(position.latDeg, position.lngDeg);
        canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);
        final int x2 = canvasPositionInPx.getX() - this.getWidgetPosLeft();
        final int y2 = canvasPositionInPx.getY() - this.getWidgetPosTop();

        // Context2d context2d = canvas.getContext2d();
        // context2d.setShadowBlur(weight);
        drawLine(x1, y1, x2, y2, weight, pathColor);
        // context2d.setShadowBlur(0.0);
        // System.out.print("x1:"+x1+" y1:"+y1+" x2:"+x2+" y2:"+y2+"\n");
    }

    private void drawPoint(final SimulatorWindDTO p) {

        final double weight = 3.0;

        final PositionDTO position = p.position;

        final LatLng positionLatLng = LatLng.newInstance(position.latDeg, position.lngDeg);
        final Point canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);

        final int x1 = canvasPositionInPx.getX() - this.getWidgetPosLeft();
        final int y1 = canvasPositionInPx.getY() - this.getWidgetPosTop();

        drawCircle(x1, y1, weight / 2., pathColor);
    }

    /**
     * 
     * @return true if the pixel distance between the two points is greater than
     * a threshold. returns true if either of the points is null
     */
    private boolean checkPointsAreFarEnough(final SimulatorWindDTO p1, final SimulatorWindDTO p2) {
        if (p1 == null || p2 == null) {
            return true;
        }
        PositionDTO position = p1.position;

        LatLng positionLatLng = LatLng.newInstance(position.latDeg, position.lngDeg);
        Point canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);

        final int x1 = canvasPositionInPx.getX();
        final int y1 = canvasPositionInPx.getY();

        position = p2.position;
        positionLatLng = LatLng.newInstance(position.latDeg, position.lngDeg);
        canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);
        final int x2 = canvasPositionInPx.getX();
        final int y2 = canvasPositionInPx.getY();

        final double pxDistance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        if (pxDistance >= MinimumPxDistanceBetweenArrows) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public long getPathTime() {
        if (this.totalTimeIsGiven) {
            return this.totalTimeMilliseconds;
        } else {
            final List<SimulatorWindDTO> windDTOList = wl.getMatrix();
            final int numPoints = windDTOList.size();
            final long totalTime = windDTOList.get(numPoints - 1).timepoint - windDTOList.get(0).timepoint;
            return totalTime;
        }
    }
}
