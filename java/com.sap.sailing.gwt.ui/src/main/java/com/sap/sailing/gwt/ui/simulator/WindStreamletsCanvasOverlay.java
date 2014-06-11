package com.sap.sailing.gwt.ui.simulator;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.maps.client.base.LatLng;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.streamlets.Swarm;
import com.sap.sse.gwt.client.player.Timer;

/**
 * A google map overlay based on a HTML5 canvas for drawing a wind field. The overlay covers the whole map and displays
 * the wind objects inside it.
 * 
 * @author Nidhi Sawhney (D054070)
 * 
 */
public class WindStreamletsCanvasOverlay extends FullCanvasOverlay implements TimeListenerWithStoppingCriteria {
    /** The wind field that is to be displayed in the overlay */
    private WindFieldDTO windFieldDTO;
    private final WindFieldGenParamsDTO windParams;
    private final SimulatorMap simulatorMap;

    private boolean visible = false;
    private final Timer timer;
    private Date endDate;

    private int nParticles;
    private Swarm swarm;

    private static Logger logger = Logger.getLogger(WindStreamletsCanvasOverlay.class.getName());

    public WindStreamletsCanvasOverlay(SimulatorMap simulatorMap, int zIndex, final Timer timer,
            final WindFieldGenParamsDTO windParams) {
        super(simulatorMap.getMap(), zIndex);
        this.simulatorMap = simulatorMap;
        this.timer = timer;
        this.windParams = windParams;
        this.nParticles = this.simulatorMap.getMainPanel().particles;
        windFieldDTO = null;
        getCanvas().getElement().setId("swarm-display");
    }

    public WindFieldDTO getWindFieldDTO() {
        return this.windFieldDTO;
    }

    public WindFieldGenParamsDTO getWindParams() {
        return this.windParams;
    }

    public JavaScriptObject getJSONRandomWindData() {

        JSONObject jsonWindData = new JSONObject();

        jsonWindData.put("timestamp", new JSONString("11:55 am on March 18, 2014"));

        LatLng boundsSW = LatLng.newInstance(53.854617, 8.159124);
        LatLng boundsNE = LatLng.newInstance(55.263922, 11.413824);

        jsonWindData.put("x0", new JSONNumber(boundsSW.getLongitude()));
        jsonWindData.put("y0", new JSONNumber(boundsSW.getLatitude()));
        jsonWindData.put("x1", new JSONNumber(boundsNE.getLongitude()));
        jsonWindData.put("y1", new JSONNumber(boundsNE.getLatitude()));

        int maxSteps = 1;
        int gridWidth = 100;
        int gridHeight = 100;
        jsonWindData.put("gridWidth", new JSONNumber(gridWidth));
        jsonWindData.put("gridHeight", new JSONNumber(gridHeight));

        JSONArray windField = new JSONArray();

        for (int stp = 0; stp < maxSteps; stp++) {
            JSONArray vectorField = new JSONArray();
            for (int idx = 0; idx < (gridWidth * gridHeight * 2); idx++) {
                vectorField.set(idx, new JSONNumber((Math.random() - 0.5) * 10.0 + 3.0));
                // windField.set(idx, new JSONNumber(10.0));
            }
            windField.set(stp, vectorField);
        }
        jsonWindData.put("field", windField);

        return jsonWindData.getJavaScriptObject();
    }

    public native boolean isSwarmDataExt() /*-{
		if ($wnd.swarmDataExt) {
			return true;
		} else {
			return false;
		}
    }-*/;

    public native String getSwarmData() /*-{
		return JSON.stringify($wnd.swarmData);
    }-*/;

    public native void setSwarmData(JavaScriptObject swarmData) /*-{
        $wnd.swarmData = swarmData;
    }-*/;

    private native void getJSNIWind(WindStreamletsCanvasOverlay wsc) /*-{
        $wnd.getWindfromSimulator = function(idx) {
            return wsc.@com.sap.sailing.gwt.ui.simulator.WindStreamletsCanvasOverlay::getWind(I)(idx);
        };
    }-*/;

    private native JavaScriptObject getWindInst(double x, double y) /*-{
        return {
            x : x,
            y : y
        };
    }-*/;

    public JavaScriptObject getWind(int idx) {
        SimulatorWindDTO wind = this.windFieldDTO.getMatrix().get(idx);
        double y = wind.trueWindSpeedInKnots * Math.cos(wind.trueWindBearingDeg * Math.PI / 180.0);
        double x = wind.trueWindSpeedInKnots * Math.sin(wind.trueWindBearingDeg * Math.PI / 180.0);
        return getWindInst(x, y);
    }

    public native void setWindDataJSON(String jsonField) /*-{
        eval(jsonField);
    }-*/;

    public WindStreamletsCanvasOverlay(SimulatorMap simulatorMap, int zIndex) {
        this(simulatorMap, zIndex, null, null);
    }

    public native void setMapInstance(Object mapInstance) /*-{
        $wnd.swarmMap = mapInstance;
    }-*/;

    public native void setCanvasProjectionInstance(Object instance) /*-{
        $wnd.swarmCanvasProjection = instance;
    }-*/;

    public void startStreamlets() {
        if (swarm == null) {
            this.swarm = new Swarm(this, map);
        }
        this.swarm.start(40, windFieldDTO);
    }

    public void setStreamletsStep(int step) {
        this.swarm.getField().setStep(step);
    }

    public void stopStreamlets() {
        if (swarm != null) {
            this.swarm.stop();
        }
    }

    public void extendWindDataJSON() {
        List<SimulatorWindDTO> data = this.windFieldDTO.getMatrix();
        String jsonData = "data:[";
        int p = 0;
        int imax = this.windParams.getyRes() + 2 * this.windParams.getBorderY();
        int jmax = this.windParams.getxRes() + 2 * this.windParams.getBorderX();
        int steps = data.size() / (imax * jmax);
        double maxWindSpeed = 0;
        for (int s = 0; s < steps; s++) {
            jsonData += "[";
            for (int i = 0; i < imax; i++) {
                jsonData += "[";
                for (int j = 0; j < jmax; j++) {
                    SimulatorWindDTO wind = data.get(p);
                    p++;
                    if (wind.trueWindSpeedInKnots > maxWindSpeed) {
                        maxWindSpeed = wind.trueWindSpeedInKnots;
                    }
                    double y = wind.trueWindSpeedInKnots * Math.cos(wind.trueWindBearingDeg * Math.PI / 180.0);
                    double x = wind.trueWindSpeedInKnots * Math.sin(wind.trueWindBearingDeg * Math.PI / 180.0);
                    jsonData += x + "," + y;
                    if (j < (jmax - 1)) {
                        jsonData += ",";
                    }
                }
                if (i < (imax - 1)) {
                    jsonData += "],";
                } else {
                    jsonData += "]";
                }
            }
            if (s < (steps - 1)) {
                jsonData += "],";
            } else {
                jsonData += "]";
            }
        }
        this.windFieldDTO.windDataJSON += jsonData + "],maxLength:" + maxWindSpeed + "};";
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setWindField(final WindFieldDTO windFieldDTO) {
        this.windFieldDTO = windFieldDTO;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(boolean isVisible) {
        if (getCanvas() != null) {
            if (isVisible) {
                this.startStreamlets();
                if (timer.getTime().compareTo(this.windParams.getStartTime()) != 0) {
                    this.timeChanged(timer.getTime(), null);
                }
                this.visible = isVisible;
            } else {
                this.stopStreamlets();
                this.visible = isVisible;
            }
        }
    }

    @Override
    public void addToMap() {
        if (timer != null) {
            timer.addTimeListener(this);
        }
    }

    @Override
    public void removeFromMap() {
        if (timer != null) {
            timer.removeTimeListener(this);
        }
        this.setVisible(false);
    }

    @Override
    protected void drawCenterChanged() {
    }

    @Override
    protected void draw() {
        super.draw();
        if (mapProjection != null) {
            if ((nParticles > 0) && (swarm == null)) {
                this.swarm = new Swarm(this, map);
                this.swarm.start(40, null);
            }
            if (windFieldDTO != null) {
                // drawing is done by external JavaScript for Streamlets
            }
        }
    }

    public Swarm getSwarm() {
        return this.swarm;
    }

    private void clear() {
        this.stopStreamlets();
    }

    @Override
    public void timeChanged(final Date newDate, Date oldDate) {
        int step = (int) ((newDate.getTime() - this.windParams.getStartTime().getTime()) / this.windParams
                .getTimeStep().getTime());
        this.setStreamletsStep(step);
    }

    @Override
    public boolean shallStop() {
        if (timer.getTime().getTime() >= this.endDate.getTime()) {
            return true;
        } else {
            return false;
        }
    }
}
