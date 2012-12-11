package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.TimeListenerWithStoppingCriteria;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.racemap.FullCanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.util.WindGridColorPalette;

/**
 * A google map overlay based on a HTML5 canvas for representing a wind field as a heat map. The overlay covers the
 * whole map and colors the cells of the field based on the trueWindSpeedInKnots
 * 
 * @author Nidhi Sawhney(D054070)
 * 
 */
public class WindGridCanvasOverlay extends FullCanvasOverlay implements TimeListenerWithStoppingCriteria {
    /* The wind field that is to be displayed in the overlay */
    protected WindFieldDTO wl;
    /*
     * Map containing the windfield for easy retrieval with key as time point.
     */
    protected SortedMap<Long, List<SimulatorWindDTO>> timePointWindDTOMap;

    protected final Timer timer;

    private int xRes;
    private int yRes;
    private SimulatorWindDTO[][] windMatrix;
    private Map<Pair<Integer, Integer>, GridCell> gridCellMap;
    private WindGridColorPalette colorPalette;

    private static Logger logger = Logger.getLogger(WindFieldCanvasOverlay.class.getName());

    private class GridCell {
        public PositionDTO bottomLeft;
        public PositionDTO bottomRight;
        public PositionDTO topLeft;
        public PositionDTO topRight;

        public double windSpeedInKnots;

        public GridCell(final PositionDTO bl, final PositionDTO br, final PositionDTO tl, final PositionDTO tr, final Double windSpeedInKnots) {
            this.bottomLeft = bl;
            this.bottomRight = br;
            this.topLeft = tl;
            this.topRight = tr;
            this.windSpeedInKnots = windSpeedInKnots;
        }
    }

    private class SortByWindSpeed implements Comparator<SimulatorWindDTO> {

        @Override
        public int compare(final SimulatorWindDTO w1, final SimulatorWindDTO w2) {
            return Double.compare(w1.getTrueWindSpeedInKnots(), w2.getTrueWindSpeedInKnots());
        }

    }

    private class SortByLatitude implements Comparator<SimulatorWindDTO> {

        @Override
        public int compare(final SimulatorWindDTO w1, final SimulatorWindDTO w2) {
            return Double.compare(w1.getPosition().latDeg, w2.getPosition().latDeg);
        }

    }

    private class SortByLongitude implements Comparator<SimulatorWindDTO> {

        @Override
        public int compare(final SimulatorWindDTO w1, final SimulatorWindDTO w2) {
            return Double.compare(w1.getPosition().lngDeg, w2.getPosition().lngDeg);
        }

    }

    public WindGridCanvasOverlay(final Timer timer, final int xRes, final int yRes) {
        super();
        this.timer = timer;
        this.xRes = xRes;
        this.yRes = yRes;
        init();
    }

    public WindGridCanvasOverlay() {
        super();
        this.timer = null;
        init();
    }

    private void init() {
        wl = null;
        timePointWindDTOMap = new TreeMap<Long, List<SimulatorWindDTO>>();
        colorPalette = null;
    }

    public void setWindField(final WindFieldDTO wl) {
        this.wl = wl;

        timePointWindDTOMap.clear();
        if (wl != null) {
            for (final SimulatorWindDTO w : wl.getMatrix()) {
                if (!timePointWindDTOMap.containsKey(w.getTimepoint())) {
                    timePointWindDTOMap.put(w.getTimepoint(), new LinkedList<SimulatorWindDTO>());
                }
                timePointWindDTOMap.get(w.getTimepoint()).add(w);
            }

            final SortByWindSpeed windSpeedSorter = new SortByWindSpeed();
            final double maxSpeed = Collections.max(wl.getMatrix(), windSpeedSorter).getTrueWindSpeedInKnots();
            final double minSpeed = Collections.min(wl.getMatrix(), windSpeedSorter).getTrueWindSpeedInKnots();
            System.out.println("minSpeed: " + minSpeed + " maxSpeed: " + maxSpeed);

            colorPalette = new WindGridColorPalette(minSpeed, maxSpeed);
            logger.fine("Color minSpeed: " + colorPalette.getColor(minSpeed));
            logger.fine("Color maxSpeed: " + colorPalette.getColor(maxSpeed));

            /**
             * Get the wind at first time point to capture the positions on the grid.
             */
            final Long firstTimePoint = timePointWindDTOMap.firstKey();
            final SortedMap<Long, List<SimulatorWindDTO>> headMap = (timePointWindDTOMap.headMap(firstTimePoint + 1));
            List<SimulatorWindDTO> windDTOToDraw;
            if (!headMap.isEmpty()) {
                windDTOToDraw = headMap.get(headMap.lastKey());
                createPositionGrid(windDTOToDraw);
            }

        }
    }

    @Override
    protected void initialize(final MapWidget map) {
        super.initialize(map);

        if (timer != null) {
            this.timer.addTimeListener(this);
        }
        setVisible(true);
    }

    @Override
    protected void remove() {
        setVisible(false);
        if (timer != null) {
            this.timer.removeTimeListener(this);
        }
        super.remove();
    }

    @Override
    public void timeChanged(final Date date) {
        List<SimulatorWindDTO> windDTOToDraw = new ArrayList<SimulatorWindDTO>();

        final SortedMap<Long, List<SimulatorWindDTO>> headMap = (timePointWindDTOMap.headMap(date.getTime() + 1));

        if (!headMap.isEmpty()) {
            windDTOToDraw = headMap.get(headMap.lastKey());
        }
        logger.info("In WindGridCanvasOverlay.timeChanged drawing " + windDTOToDraw.size() + " points" + " @ " + date);

        drawWindGrid(windDTOToDraw);
    }

    @Override
    public int stop() {
        if (!this.isVisible() || timePointWindDTOMap == null || timer == null || timePointWindDTOMap.isEmpty()) {
            return 0;
        }
        if (timePointWindDTOMap.lastKey() < timer.getTime().getTime()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    protected Overlay copy() {
        return new WindFieldCanvasOverlay(this.timer);
    }

    @Override
    protected void redraw(final boolean force) {
        super.redraw(force);
        if (wl != null) {
            clear();

            drawWindGrid();
        }

    }

    private void clear() {
        canvas.getContext2d().clearRect(0.0 /* canvas.getAbsoluteLeft() */, 0.0/* canvas.getAbsoluteTop() */,
                canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
    }

    protected void drawWindGrid() {

        if (timer != null) {
            timeChanged(timer.getTime());

        } else {
            drawWindGrid(wl.getMatrix());
        }

    }

    protected void drawWindGrid(final List<SimulatorWindDTO> windDTOList) {
        clear();
        if (windDTOList != null && windDTOList.size() > 1) {
            if (windDTOList.size() != xRes * yRes) {
                logger.warning("Error in WindGridCanvasOverlay wind field is not rectangular.");
                return;
            }
            // createPositionGrid(windDTOList);
            // createGridCell();
            updatePositionGrid(windDTOList);
            drawGridCell();

            final String title = "Wind Grid at " + windDTOList.size() + " points.";
            getCanvas().setTitle(title);
        }
    }

    /**
     * 
     * @param windDTOList
     * @return the horizontal pixel distance between the first two points in the list
     */
    private double getGridWidth(final List<SimulatorWindDTO> windDTOList) {
        if (windDTOList.size() > 1) {
            final SortByLatitude sortLatitude = new SortByLatitude();

            Collections.sort(windDTOList, sortLatitude);
            final SimulatorWindDTO windDTO1 = windDTOList.get(0);
            final SimulatorWindDTO windDTO2 = windDTOList.get(1);

            final LatLng positionLatLng1 = LatLng.newInstance(windDTO1.getPosition().latDeg, windDTO1.getPosition().lngDeg);
            final Point canvasPositionInPx1 = getMap().convertLatLngToDivPixel(positionLatLng1);

            final LatLng positionLatLng2 = LatLng.newInstance(windDTO2.getPosition().latDeg, windDTO2.getPosition().lngDeg);
            final Point canvasPositionInPx2 = getMap().convertLatLngToDivPixel(positionLatLng2);

            return canvasPositionInPx2.getX() - canvasPositionInPx1.getX();
        }
        return 0;
    }

    /**
     * 
     * @param windDTOList
     * @return the horizontal pixel distance between the first two points in the list
     */
    private double getGridHeight(final List<SimulatorWindDTO> windDTOList) {
        if (windDTOList.size() > 1) {
            final SortByLongitude sortLongitude = new SortByLongitude();

            Collections.sort(windDTOList, sortLongitude);
            final SimulatorWindDTO windDTO1 = windDTOList.get(0);
            final SimulatorWindDTO windDTO2 = windDTOList.get(1);

            final LatLng positionLatLng1 = LatLng.newInstance(windDTO1.getPosition().latDeg, windDTO1.getPosition().lngDeg);
            final Point canvasPositionInPx1 = getMap().convertLatLngToDivPixel(positionLatLng1);

            final LatLng positionLatLng2 = LatLng.newInstance(windDTO2.getPosition().latDeg, windDTO2.getPosition().lngDeg);
            final Point canvasPositionInPx2 = getMap().convertLatLngToDivPixel(positionLatLng2);

            return canvasPositionInPx2.getY() - canvasPositionInPx1.getY();
        }
        return 0;
    }

    private void createPositionGrid(final List<SimulatorWindDTO> windDTOList) {
        if (windDTOList.size() != xRes * yRes) {
            logger.warning("Error in WindGridCanvasOverlay wind field is not rectangular.");
            this.windMatrix = null;
            return;
        }
        this.windMatrix = new SimulatorWindDTO[yRes + 2][xRes];
        final Iterator<SimulatorWindDTO> windDTOIter = windDTOList.iterator();

        for (int i = 1; i < yRes + 1; ++i) {
            for (int j = 0; j < xRes; ++j) {
                windMatrix[i][j] = windDTOIter.next();
            }
        }
        extendPositionGrid();

        createGridCell();
    }

    private void updatePositionGrid(final List<SimulatorWindDTO> windDTOList) {
        if (windDTOList.size() != xRes * yRes) {
            logger.warning("Error in WindGridCanvasOverlay wind field is not rectangular.");
            return;
        }
        final Iterator<SimulatorWindDTO> windDTOIter = windDTOList.iterator();

        for (int i = 0; i < yRes; ++i) {
            for (int j = 0; j < xRes; ++j) {
                windMatrix[i][j] = windDTOIter.next();
            }
        }
        updateGridCell();
    }

    /**
     * Create extra row before the first and after the last row to ensure the start and end points are covered by the
     * grid cells.
     */
    private void extendPositionGrid() {

        final int numRow = windMatrix.length;

        if (numRow < 4) {
            return;
        }
        /*
         * Row before the first row
         */
        for (int j = 0; j < xRes; ++j) {
            final PositionDTO position = new PositionDTO();
            final PositionDTO p1 = windMatrix[1][j].getPosition();
            final PositionDTO p2 = windMatrix[2][j].getPosition();
            position.latDeg = 2 * p1.latDeg - p2.latDeg;
            position.lngDeg = 2 * p1.lngDeg - p2.lngDeg;
            final SimulatorWindDTO windDTO = new SimulatorWindDTO();
            // Only the position of this windDTO is used
            windDTO.setPosition(position);
            windDTO.setTrueWindSpeedInKnots(0.0);
            windMatrix[0][j] = windDTO;
        }

        /*
         * Row after the last row
         */
        for (int j = 0; j < xRes; ++j) {
            final PositionDTO position = new PositionDTO();
            final PositionDTO p1 = windMatrix[numRow - 2][j].getPosition();
            final PositionDTO p2 = windMatrix[numRow - 3][j].getPosition();
            position.latDeg = 2 * p1.latDeg - p2.latDeg;
            position.lngDeg = 2 * p1.lngDeg - p2.lngDeg;
            final SimulatorWindDTO windDTO = new SimulatorWindDTO();
            // Only the position of this windDTO is used
            windDTO.setPosition(position);
            // windDTO.trueWindSpeedInKnots = 0.0;
            windMatrix[numRow - 1][j] = windDTO;
        }
    }

    private void createGridCell() {
        if (windMatrix != null) {
            final int numRow = windMatrix.length;
            if (numRow >= 4) {
                final int numCol = windMatrix[0].length;
                if (numCol >= 2) {
                    gridCellMap = new HashMap<Pair<Integer, Integer>, GridCell>();
                    for (int i = 1; i < numRow - 1; ++i) {
                        for (int j = 1; j < numCol - 1; ++j) {
                            final PositionDTO bl = getCenter(windMatrix[i - 1][j - 1].getPosition(), windMatrix[i - 1][j].getPosition(),
                                    windMatrix[i][j].getPosition(), windMatrix[i][j - 1].getPosition());
                            final PositionDTO tl = getCenter(windMatrix[i][j - 1].getPosition(), windMatrix[i][j].getPosition(),
                                    windMatrix[i + 1][j - 1].getPosition(), windMatrix[i + 1][j].getPosition());
                            final PositionDTO br = getCenter(windMatrix[i - 1][j].getPosition(), windMatrix[i - 1][j + 1].getPosition(),
                                    windMatrix[i][j].getPosition(), windMatrix[i][j + 1].getPosition());
                            final PositionDTO tr = getCenter(windMatrix[i][j].getPosition(), windMatrix[i][j + 1].getPosition(),
                                    windMatrix[i + 1][j].getPosition(), windMatrix[i + 1][j + 1].getPosition());
                            final GridCell cell = new GridCell(bl, br, tl, tr, windMatrix[i - 1][j].getTrueWindSpeedInKnots());
                            final Pair<Integer, Integer> cellPair = new Pair<Integer, Integer>(i, j);
                            gridCellMap.put(cellPair, cell);
                            // drawGridCell(cell);
                        }
                    }
                }
            }

        }
    }

    private void updateGridCell() {
        if (windMatrix != null) {
            final int numRow = windMatrix.length;
            if (numRow >= 4) {
                final int numCol = windMatrix[0].length;
                if (numCol >= 2) {
                    for (int i = 1; i < numRow - 1; ++i) {
                        for (int j = 1; j < numCol - 1; ++j) {
                            final Pair<Integer, Integer> cellPair = new Pair<Integer, Integer>(i, j);
                            final GridCell cell = gridCellMap.get(cellPair);
                            cell.windSpeedInKnots = windMatrix[i - 1][j].getTrueWindSpeedInKnots();
                        }
                    }
                }
            }
        }

    }

    private void drawGridCell() {
        if (gridCellMap != null & !gridCellMap.isEmpty()) {
            for (final Entry<Pair<Integer, Integer>, GridCell> cell : gridCellMap.entrySet()) {
                drawGridCell(cell.getValue());
            }
        }
    }

    private void drawGridCell(final GridCell cell) {

        LatLng positionLatLng = LatLng.newInstance(cell.bottomLeft.latDeg, cell.bottomLeft.lngDeg);
        final Point blPoint = getMap().convertLatLngToDivPixel(positionLatLng);

        positionLatLng = LatLng.newInstance(cell.bottomRight.latDeg, cell.bottomRight.lngDeg);
        final Point brPoint = getMap().convertLatLngToDivPixel(positionLatLng);

        positionLatLng = LatLng.newInstance(cell.topLeft.latDeg, cell.topLeft.lngDeg);
        final Point tlPoint = getMap().convertLatLngToDivPixel(positionLatLng);

        positionLatLng = LatLng.newInstance(cell.topRight.latDeg, cell.topRight.lngDeg);
        final Point trPoint = getMap().convertLatLngToDivPixel(positionLatLng);

        /*
         * Uncomment to see the center of the grid for debug drawCircle(blPoint.getX()-this.getWidgetPosLeft(),
         * blPoint.getY()-this.getWidgetPosTop(),2,"red"); drawCircle(brPoint.getX()-this.getWidgetPosLeft(),
         * brPoint.getY()-this.getWidgetPosTop(),2,"red"); drawCircle(tlPoint.getX()-this.getWidgetPosLeft(),
         * tlPoint.getY()-this.getWidgetPosTop(),2,"red"); drawCircle(trPoint.getX()-this.getWidgetPosLeft(),
         * trPoint.getY()-this.getWidgetPosTop(),2,"red");
         */
        final Context2d context2d = canvas.getContext2d();
        context2d.setLineWidth(1);
        context2d.setStrokeStyle("Black");
        context2d.setGlobalAlpha(0.5f);
        context2d.setFillStyle(colorPalette.getColor(cell.windSpeedInKnots));

        context2d.beginPath();
        context2d.moveTo(blPoint.getX() - this.getWidgetPosLeft(), blPoint.getY() - this.getWidgetPosTop());
        context2d.lineTo(brPoint.getX() - this.getWidgetPosLeft(), brPoint.getY() - this.getWidgetPosTop());
        context2d.lineTo(trPoint.getX() - this.getWidgetPosLeft(), trPoint.getY() - this.getWidgetPosTop());
        context2d.lineTo(tlPoint.getX() - this.getWidgetPosLeft(), tlPoint.getY() - this.getWidgetPosTop());
        context2d.lineTo(blPoint.getX() - this.getWidgetPosLeft(), blPoint.getY() - this.getWidgetPosTop());
        context2d.closePath();

        context2d.fill();
        // context2d.stroke(); // Dont show the lines
    }

    private PositionDTO getCenter(final PositionDTO a, final PositionDTO b, final PositionDTO c, final PositionDTO d) {
        final PositionDTO center = new PositionDTO();
        center.latDeg = (a.latDeg + b.latDeg + c.latDeg + d.latDeg) / 4.0;
        center.lngDeg = (a.lngDeg + b.lngDeg + c.lngDeg + d.lngDeg) / 4.0;
        return center;
    }

    public PositionDTO[] getGridCorners() {
        if (gridCellMap != null && !gridCellMap.isEmpty()) {
            final PositionDTO[] corners = new PositionDTO[4];
            final int numRow = windMatrix.length;
            final int numCol = windMatrix[0].length;
            final Pair<Integer, Integer> cellPair1 = new Pair<Integer, Integer>(1, 1);
            corners[0] = gridCellMap.get(cellPair1).bottomLeft;

            final Pair<Integer, Integer> cellPair2 = new Pair<Integer, Integer>(1, numCol-2);
            corners[1] = gridCellMap.get(cellPair2).bottomRight;

            final Pair<Integer, Integer> cellPair3 = new Pair<Integer, Integer>(numRow-2, numCol-2);
            corners[2] = gridCellMap.get(cellPair3).topRight;

            final Pair<Integer, Integer> cellPair4 = new Pair<Integer, Integer>(numRow-2, 1);
            corners[3] = gridCellMap.get(cellPair4).topLeft;

            return corners;
        }
        return null;
    }
}
