package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Logger;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.gwt.ui.client.TimeListenerWithStoppingCriteria;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindLinesDTO;
import com.sap.sailing.gwt.ui.shared.racemap.FullCanvasOverlay;

public class WindLineCanvasOverlay extends FullCanvasOverlay implements TimeListenerWithStoppingCriteria {

    private WindLinesDTO windLinesDTO;

    protected String lineColor = "Black";
    protected final Timer timer;

    private static Logger logger = Logger.getLogger(WindLineCanvasOverlay.class.getName());

    public WindLineCanvasOverlay(Timer timer) {
        super();
        this.timer = timer;
        windLinesDTO = null;
    }

    @Override
    public void timeChanged(Date date) {
        List<PositionDTO> positionDTOToDraw = new ArrayList<PositionDTO>();

        SortedMap<Long, List<PositionDTO>> headMap = (windLinesDTO.getWindLinesMap().headMap(date.getTime() + 1));

        if (!headMap.isEmpty()) {
            positionDTOToDraw = headMap.get(headMap.lastKey());
        }
        logger.info("In WindLineCanvasOverlay.drawWindField drawing " + positionDTOToDraw.size() + " points" + " @ "
                + date);

        drawWindLine(positionDTOToDraw);

    }

    @Override
    public int stop() {
        SortedMap<Long, List<PositionDTO>> timePointPositionDTOMap = windLinesDTO.getWindLinesMap();

        if (!this.isVisible() || timePointPositionDTOMap == null || timer == null || timePointPositionDTOMap.isEmpty()) {
            return 0;
        }
        if (timePointPositionDTOMap.lastKey() < timer.getTime().getTime()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    protected Overlay copy() {
        return new WindLineCanvasOverlay(this.timer);
    }

    public WindLinesDTO getWindLinesDTO() {
        return windLinesDTO;
    }

    public void setWindLinesDTO(WindLinesDTO windLinesDTO) {
        this.windLinesDTO = windLinesDTO;
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
    protected void redraw(boolean force) {
        super.redraw(force);
        if (this.windLinesDTO != null) {
            clear();
            drawWindLine();
        }

    }

    private void clear() {
        canvas.getContext2d().clearRect(0.0 /* canvas.getAbsoluteLeft() */, 0.0/* canvas.getAbsoluteTop() */,
                canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
    }

    protected void drawWindLine() {

        if (timer != null) {
            timeChanged(timer.getTime());
        }
    }

    protected void drawWindLine(final List<PositionDTO> positionDTOList) {
        
        clear();
        
        if (positionDTOList == null) {
            return;
        }
        int numPoints = positionDTOList.size();
        if (numPoints < 1) {
            return;
        }

        String title = "Wind line at " + numPoints + " points.";
        getCanvas().setTitle(title);

        Context2d context2d = canvas.getContext2d();
        context2d.setGlobalAlpha(0.4);

        Iterator<PositionDTO> positionDTOIter = positionDTOList.iterator();
        PositionDTO prevPositionDTO = null;
        while (positionDTOIter.hasNext()) {
            PositionDTO positionDTO = positionDTOIter.next();
            if (prevPositionDTO != null) {
                drawLine(prevPositionDTO, positionDTO);
            } 
            prevPositionDTO = positionDTO;
        }

    }

    private void drawLine(PositionDTO p1, PositionDTO p2) {

        double weight = 3.0;

        LatLng positionLatLng = LatLng.newInstance(p1.latDeg, p1.lngDeg);
        Point canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);

        int x1 = canvasPositionInPx.getX() - this.getWidgetPosLeft();
        int y1 = canvasPositionInPx.getY() - this.getWidgetPosTop();

        positionLatLng = LatLng.newInstance(p2.latDeg, p2.lngDeg);
        canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);
        int x2 = canvasPositionInPx.getX() - this.getWidgetPosLeft();
        int y2 = canvasPositionInPx.getY() - this.getWidgetPosTop();

        drawLine(x1, y1, x2, y2, weight, lineColor);
        
    }

}
