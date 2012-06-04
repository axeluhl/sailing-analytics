package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.WindDTO;

public class ReplayPathCanvasOverlay extends PathCanvasOverlay  {

    private static Logger logger = Logger.getLogger("com.sap.sailing");
    
    public ReplayPathCanvasOverlay(Timer timer) {
        super(timer);
        this.displayWindAlongPath = false;
        //this.timer.addTimeListener(this);
        canvas.setStyleName("replayPanel");
    }
    
    @Override
    protected void drawWindField() {
        timeChanged(timer.getTime());
    }
    
    @Override
    public void timeChanged(Date date) {
        
       canvas.getContext2d().clearRect(canvas.getAbsoluteLeft(), canvas.getAbsoluteTop(), 
               canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
        List<WindDTO> windDTOToDraw = new ArrayList<WindDTO>();
        for(WindDTO windDTO : wl.getMatrix()) {
            if (windDTO.timepoint <= date.getTime()) {
                windDTOToDraw.add(windDTO);
            }
        }
        logger.info("In ReplayPathCanvasOverlay.drawWindField drawing " + windDTOToDraw.size() + " points");
        if (windDTOToDraw.size() == wl.getMatrix().size()) {
            timer.stop();
        }
        drawWindField(windDTOToDraw);
    }

}
