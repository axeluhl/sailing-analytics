package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.WindDTO;

public class ReplayPathCanvasOverlay extends PathCanvasOverlay {

    private static final long serialVersionUID = -6284996043723173190L;
    private static Logger logger = Logger.getLogger(ReplayPathCanvasOverlay.class.getName());
    private List<WindDTO> windDTOToDraw;

    public ReplayPathCanvasOverlay(String name, Timer timer) {
        super(name, timer);
        this.displayWindAlongPath = false;
        windDTOToDraw = null;
        // this.timer.addTimeListener(this);
        canvas.setStyleName("replayPanel");
    }
    
    /*
    @Override
    protected void drawWindField() {
        timeChanged(timer.getTime());
    }
    */
    @Override
    public void timeChanged(Date date) {

        canvas.getContext2d().clearRect(0/* canvas.getAbsoluteLeft() */, 0/* canvas.getAbsoluteTop() */,
                canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());

        windDTOToDraw = new ArrayList<WindDTO>();
        for (WindDTO windDTO : wl.getMatrix()) {
            if (windDTO.timepoint <= date.getTime()) {
                windDTOToDraw.add(windDTO);
            }
        }
        logger.info("In ReplayPathCanvasOverlay.drawWindField drawing " + windDTOToDraw.size() + " points");

        drawWindField(windDTOToDraw);
    }

    @Override
    public int stop() {
        if (!isVisible() || windDTOToDraw == null || wl == null) {
            return 0;
        }
        if (windDTOToDraw.size() == wl.getMatrix().size()) {
            return 0;
        } else {
            return 1;
        }
    }
}
