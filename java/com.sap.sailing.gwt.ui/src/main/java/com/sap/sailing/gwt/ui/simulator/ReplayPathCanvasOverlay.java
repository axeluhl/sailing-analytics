package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.maps.client.MapWidget;
import com.sap.sailing.gwt.ui.client.shared.racemap.CoordinateSystem;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sse.gwt.client.player.Timer;

public class ReplayPathCanvasOverlay extends PathCanvasOverlay {

    private static final long serialVersionUID = -6284996043723173190L;
    private static Logger logger = Logger.getLogger(ReplayPathCanvasOverlay.class.getName());
    private List<SimulatorWindDTO> windDTOToDraw;

    public ReplayPathCanvasOverlay(MapWidget map, int zIndex, final String name, final Timer timer,
            WindFieldGenParamsDTO windParams, CoordinateSystem coordinateSystem) {
        super(map, zIndex, name, timer, windParams, coordinateSystem);
        this.displayWindAlongPath = false;
        windDTOToDraw = null;
        canvas.setStyleName("replayPanel");
    }

    /*
    @Override
    protected void drawWindField() {
        timeChanged(timer.getTime());
    }
     */
    @Override
    public void timeChanged(final Date newTime, Date oldTime) {

        canvas.getContext2d().clearRect(0/* canvas.getAbsoluteLeft() */, 0/* canvas.getAbsoluteTop() */,
                canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());

        windDTOToDraw = new ArrayList<SimulatorWindDTO>();
        for (final SimulatorWindDTO windDTO : windFieldDTO.getMatrix()) {
            if (windDTO.timepoint <= newTime.getTime()) {
                windDTOToDraw.add(windDTO);
            }
        }
        logger.info("In ReplayPathCanvasOverlay.drawWindField drawing " + windDTOToDraw.size() + " points");

        drawWindField(windDTOToDraw);
    }

    @Override
    public boolean shallStop() {
        if (!isVisible() || windDTOToDraw == null || windFieldDTO == null) {
            return true;
        }
        if (windDTOToDraw.size() >= (windFieldDTO.getMatrix().size()-1)) {
            return true;
        } else {
            return false;
        }
    }
}
