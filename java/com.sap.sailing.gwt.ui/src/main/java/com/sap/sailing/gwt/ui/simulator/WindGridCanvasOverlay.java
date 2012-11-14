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
import com.sap.sailing.gwt.ui.shared.WindDTO;
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
    protected SortedMap<Long, List<WindDTO>> timePointWindDTOMap;

    protected final Timer timer;

    private int xRes;
    private int yRes;
    private WindDTO[][] windMatrix;
    private Map<Pair<Integer, Integer>, GridCell> gridCellMap;
    private WindGridColorPalette colorPalette;

    private static Logger logger = Logger.getLogger(WindFieldCanvasOverlay.class.getName());

    private class GridCell {
        public PositionDTO bottomLeft;
        public PositionDTO bottomRight;
        public PositionDTO topLeft;
        public PositionDTO topRight;

        public double windSpeedInKnots;

        public GridCell(PositionDTO bl, PositionDTO br, PositionDTO tl, PositionDTO tr, Double windSpeedInKnots) {
            this.bottomLeft = bl;
            this.bottomRight = br;
            this.topLeft = tl;
            this.topRight = tr;
            this.windSpeedInKnots = windSpeedInKnots;
        }
    }

    private class SortByWindSpeed implements Comparator<WindDTO> {

        @Override
        public int compare(WindDTO w1, WindDTO w2) {
            return Double.compare(w1.trueWindSpeedInKnots, w2.trueWindSpeedInKnots);
        }

    }

    private class SortByLatitude implements Comparator<WindDTO> {

        @Override
        public int compare(WindDTO w1, WindDTO w2) {
            return Double.compare(w1.position.latDeg, w2.position.latDeg);
        }

    }

    private class SortByLongitude implements Comparator<WindDTO> {

        @Override
        public int compare(WindDTO w1, WindDTO w2) {
            return Double.compare(w1.position.lngDeg, w2.position.lngDeg);
        }

    }

    public WindGridCanvasOverlay(Timer timer, int xRes, int yRes) {
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
        timePointWindDTOMap = new TreeMap<Long, List<WindDTO>>();
        colorPalette = null;
    }

    public void setWindField(WindFieldDTO wl) {
        this.wl = wl;

        timePointWindDTOMap.clear();
        if (wl != null) {
            for (WindDTO w : wl.getMatrix()) {
                if (!timePointWindDTOMap.containsKey(w.timepoint)) {
                    timePointWindDTOMap.put(w.timepoint, new LinkedList<WindDTO>());
                }
                timePointWindDTOMap.get(w.timepoint).add(w);
            }

            SortByWindSpeed windSpeedSorter = new SortByWindSpeed();
            double maxSpeed = Collections.max(wl.getMatrix(), windSpeedSorter).trueWindSpeedInKnots;
            double minSpeed = Collections.min(wl.getMatrix(), windSpeedSorter).trueWindSpeedInKnots;
            System.out.println("minSpeed: " + minSpeed + " maxSpeed: " + maxSpeed);

            colorPalette = new WindGridColorPalette(minSpeed, maxSpeed);
            logger.fine("Color minSpeed: " + colorPalette.getColor(minSpeed));
            logger.fine("Color maxSpeed: " + colorPalette.getColor(maxSpeed));

            /**
             * Get the wind at first time point to capture the positions on the grid.
             */
            Long firstTimePoint = timePointWindDTOMap.firstKey();
            SortedMap<Long, List<WindDTO>> headMap = (timePointWindDTOMap.headMap(firstTimePoint + 1));
            List<WindDTO> windDTOToDraw;
            if (!headMap.isEmpty()) {
                windDTOToDraw = headMap.get(headMap.lastKey());
                createPositionGrid(windDTOToDraw);
            }

        }
    }

    @Override
    protected void initialize(MapWidget map) {
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
    public void timeChanged(Date date) {
        List<WindDTO> windDTOToDraw = new ArrayList<WindDTO>();

        SortedMap<Long, List<WindDTO>> headMap = (timePointWindDTOMap.headMap(date.getTime() + 1));

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
    protected void redraw(boolean force) {
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

    protected void drawWindGrid(final List<WindDTO> windDTOList) {
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

            String title = "Wind Grid at " + windDTOList.size() + " points.";
            getCanvas().setTitle(title);
        }
    }

    /**
     * 
     * @param windDTOList
     * @return the horizontal pixel distance between the first two points in the list
     */
    private double getGridWidth(List<WindDTO> windDTOList) {
        if (windDTOList.size() > 1) {
            SortByLatitude sortLatitude = new SortByLatitude();

            Collections.sort(windDTOList, sortLatitude);
            WindDTO windDTO1 = windDTOList.get(0);
            WindDTO windDTO2 = windDTOList.get(1);

            LatLng positionLatLng1 = LatLng.newInstance(windDTO1.position.latDeg, windDTO1.position.lngDeg);
            Point canvasPositionInPx1 = getMap().convertLatLngToDivPixel(positionLatLng1);

            LatLng positionLatLng2 = LatLng.newInstance(windDTO2.position.latDeg, windDTO2.position.lngDeg);
            Point canvasPositionInPx2 = getMap().convertLatLngToDivPixel(positionLatLng2);

            return canvasPositionInPx2.getX() - canvasPositionInPx1.getX();
        }
        return 0;
    }

    /**
     * 
     * @param windDTOList
     * @return the horizontal pixel distance between the first two points in the list
     */
    private double getGridHeight(List<WindDTO> windDTOList) {
        if (windDTOList.size() > 1) {
            SortByLongitude sortLongitude = new SortByLongitude();

            Collections.sort(windDTOList, sortLongitude);
            WindDTO windDTO1 = windDTOList.get(0);
            WindDTO windDTO2 = windDTOList.get(1);

            LatLng positionLatLng1 = LatLng.newInstance(windDTO1.position.latDeg, windDTO1.position.lngDeg);
            Point canvasPositionInPx1 = getMap().convertLatLngToDivPixel(positionLatLng1);

            LatLng positionLatLng2 = LatLng.newInstance(windDTO2.position.latDeg, windDTO2.position.lngDeg);
            Point canvasPositionInPx2 = getMap().convertLatLngToDivPixel(positionLatLng2);

            return canvasPositionInPx2.getY() - canvasPositionInPx1.getY();
        }
        return 0;
    }

    private void createPositionGrid(List<WindDTO> windDTOList) {
        if (windDTOList.size() != xRes * yRes) {
            logger.warning("Error in WindGridCanvasOverlay wind field is not rectangular.");
            this.windMatrix = null;
            return;
        }
        this.windMatrix = new WindDTO[yRes + 2][xRes];
        Iterator<WindDTO> windDTOIter = windDTOList.iterator();

        for (int i = 1; i < yRes + 1; ++i) {
            for (int j = 0; j < xRes; ++j) {
                windMatrix[i][j] = windDTOIter.next();
            }
        }
        extendPositionGrid();

        createGridCell();
    }

    private void updatePositionGrid(List<WindDTO> windDTOList) {
        if (windDTOList.size() != xRes * yRes) {
            logger.warning("Error in WindGridCanvasOverlay wind field is not rectangular.");
            return;
        }
        Iterator<WindDTO> windDTOIter = windDTOList.iterator();

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

        int numRow = windMatrix.length;

        if (numRow < 4) {
            return;
        }
        /*
         * Row before the first row
         */
        for (int j = 0; j < xRes; ++j) {
            PositionDTO position = new PositionDTO();
            PositionDTO p1 = windMatrix[1][j].position;
            PositionDTO p2 = windMatrix[2][j].position;
            position.latDeg = 2 * p1.latDeg - p2.latDeg;
            position.lngDeg = 2 * p1.lngDeg - p2.lngDeg;
            WindDTO windDTO = new WindDTO();
            // Only the position of this windDTO is used
            windDTO.position = position;
            // windDTO.trueWindSpeedInKnots = 0.0;
            windMatrix[0][j] = windDTO;
        }

        /*
         * Row after the last row
         */
        for (int j = 0; j < xRes; ++j) {
            PositionDTO position = new PositionDTO();
            PositionDTO p1 = windMatrix[numRow - 2][j].position;
            PositionDTO p2 = windMatrix[numRow - 3][j].position;
            position.latDeg = 2 * p1.latDeg - p2.latDeg;
            position.lngDeg = 2 * p1.lngDeg - p2.lngDeg;
            WindDTO windDTO = new WindDTO();
            // Only the position of this windDTO is used
            windDTO.position = position;
            // windDTO.trueWindSpeedInKnots = 0.0;
            windMatrix[numRow - 1][j] = windDTO;
        }
    }

    private void createGridCell() {
        if (windMatrix != null) {
            int numRow = windMatrix.length;
            if (numRow >= 4) {
                int numCol = windMatrix[0].length;
                if (numCol >= 2) {
                    gridCellMap = new HashMap<Pair<Integer, Integer>, GridCell>();
                    for (int i = 1; i < numRow - 1; ++i) {
                        for (int j = 1; j < numCol - 1; ++j) {
                            PositionDTO bl = getCenter(windMatrix[i - 1][j - 1].position,
                                    windMatrix[i - 1][j].position, windMatrix[i][j].position,
                                    windMatrix[i][j - 1].position);
                            PositionDTO tl = getCenter(windMatrix[i][j - 1].position, windMatrix[i][j].position,
                                    windMatrix[i + 1][j - 1].position, windMatrix[i + 1][j].position);
                            PositionDTO br = getCenter(windMatrix[i - 1][j].position,
                                    windMatrix[i - 1][j + 1].position, windMatrix[i][j].position,
                                    windMatrix[i][j + 1].position);
                            PositionDTO tr = getCenter(windMatrix[i][j].position, windMatrix[i][j + 1].position,
                                    windMatrix[i + 1][j].position, windMatrix[i + 1][j + 1].position);
                            GridCell cell = new GridCell(bl, br, tl, tr, windMatrix[i][j].trueWindSpeedInKnots);
                            Pair<Integer, Integer> cellPair = new Pair<Integer, Integer>(i, j);
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
            int numRow = windMatrix.length;
            if (numRow >= 4) {
                int numCol = windMatrix[0].length;
                if (numCol >= 2) {
                    for (int i = 1; i < numRow - 1; ++i) {
                        for (int j = 1; j < numCol - 1; ++j) {
                            Pair<Integer, Integer> cellPair = new Pair<Integer, Integer>(i, j);
                            GridCell cell = gridCellMap.get(cellPair);
                            cell.windSpeedInKnots = windMatrix[i][j].trueWindSpeedInKnots;
                        }
                    }
                }
            }
        }

    }

    private void drawGridCell() {
        if (gridCellMap != null & !gridCellMap.isEmpty()) {
            for (Entry<Pair<Integer, Integer>, GridCell> cell : gridCellMap.entrySet()) {
                drawGridCell(cell.getValue());
            }
        }
    }

    private void drawGridCell(GridCell cell) {

        LatLng positionLatLng = LatLng.newInstance(cell.bottomLeft.latDeg, cell.bottomLeft.lngDeg);
        Point blPoint = getMap().convertLatLngToDivPixel(positionLatLng);

        positionLatLng = LatLng.newInstance(cell.bottomRight.latDeg, cell.bottomRight.lngDeg);
        Point brPoint = getMap().convertLatLngToDivPixel(positionLatLng);

        positionLatLng = LatLng.newInstance(cell.topLeft.latDeg, cell.topLeft.lngDeg);
        Point tlPoint = getMap().convertLatLngToDivPixel(positionLatLng);

        positionLatLng = LatLng.newInstance(cell.topRight.latDeg, cell.topRight.lngDeg);
        Point trPoint = getMap().convertLatLngToDivPixel(positionLatLng);

        /*
         * Uncomment to see the center of the grid for debug drawCircle(blPoint.getX()-this.getWidgetPosLeft(),
         * blPoint.getY()-this.getWidgetPosTop(),2,"red"); drawCircle(brPoint.getX()-this.getWidgetPosLeft(),
         * brPoint.getY()-this.getWidgetPosTop(),2,"red"); drawCircle(tlPoint.getX()-this.getWidgetPosLeft(),
         * tlPoint.getY()-this.getWidgetPosTop(),2,"red"); drawCircle(trPoint.getX()-this.getWidgetPosLeft(),
         * trPoint.getY()-this.getWidgetPosTop(),2,"red");
         */
        Context2d context2d = canvas.getContext2d();
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

    private PositionDTO getCenter(PositionDTO a, PositionDTO b, PositionDTO c, PositionDTO d) {
        PositionDTO center = new PositionDTO();
        center.latDeg = (a.latDeg + b.latDeg + c.latDeg + d.latDeg) / 4.0;
        center.lngDeg = (a.lngDeg + b.lngDeg + c.lngDeg + d.lngDeg) / 4.0;
        return center;
    }
    
    public PositionDTO[] getGridCorners() {
        if (gridCellMap != null && !gridCellMap.isEmpty()) {
            PositionDTO[] corners = new PositionDTO[4];
            int numRow = windMatrix.length;
            int numCol = windMatrix[0].length;
            Pair<Integer, Integer> cellPair1 = new Pair<Integer, Integer>(1, 1);
            corners[0] = gridCellMap.get(cellPair1).bottomLeft;
            
            Pair<Integer, Integer> cellPair2 = new Pair<Integer, Integer>(1, numCol-2);
            corners[1] = gridCellMap.get(cellPair2).bottomRight;
            
            Pair<Integer, Integer> cellPair3 = new Pair<Integer, Integer>(numRow-2, numCol-2);
            corners[2] = gridCellMap.get(cellPair3).topRight;
            
            Pair<Integer, Integer> cellPair4 = new Pair<Integer, Integer>(numRow-2, 1);
            corners[3] = gridCellMap.get(cellPair4).topLeft;
          
            return corners;
        }
        return null;
    }
}
