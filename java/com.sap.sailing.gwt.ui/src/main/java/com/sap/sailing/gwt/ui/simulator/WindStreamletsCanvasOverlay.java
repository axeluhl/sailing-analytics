package com.sap.sailing.gwt.ui.simulator;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;
import com.sap.sse.gwt.client.player.Timer;

/**
 * A google map overlay based on a HTML5 canvas for drawing a wind field. The overlay covers the whole map and displays
 * the wind objects inside it.
 * 
 * @author Nidhi Sawhney(D054070)
 * 
 */
public class WindStreamletsCanvasOverlay extends FullCanvasOverlay implements TimeListenerWithStoppingCriteria, Scheduler.RepeatingCommand {

    /** The wind field that is to be displayed in the overlay */
    protected WindFieldDTO windFieldDTO;
    protected WindFieldGenParamsDTO windParams = null;
    protected SimulatorMap simulatorMap;

    private boolean visible = false;
    private Timer timer;
    
    private String[] color;
    private Mercator projection;
    private RectField field;
    private int nParticles;
    private Particle[] particles;
    private LatLng pNE;
    private LatLng pSW;

    private static Logger logger = Logger.getLogger(WindStreamletsCanvasOverlay.class.getName());

    public WindStreamletsCanvasOverlay(SimulatorMap simulatorMap, int zIndex, final Timer timer, final WindFieldGenParamsDTO windParams) {
        super(simulatorMap.getMap(), zIndex);
        
        this.simulatorMap = simulatorMap;
        this.timer = timer;
        this.windParams = windParams;
        
    	this.nParticles = this.simulatorMap.getMainPanel().particles;

        
        windFieldDTO = null;

        getCanvas().getElement().setId("swarm-display");
        
        /*if (!this.isSwarmDataExt()) {
        	// random test windfield
        	JavaScriptObject swarmData = this.getJSONRandomWindData();
        	this.setSwarmData(swarmData);
        }*/
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

    	for(int stp=0; stp < maxSteps; stp++) {
    		JSONArray vectorField = new JSONArray();
    		for(int idx=0; idx < (gridWidth*gridHeight*2); idx++) {
    			vectorField.set(idx, new JSONNumber((Math.random()-0.5)*10.0+3.0));
    			//windField.set(idx, new JSONNumber(10.0));
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
    	return {x:x, y:y};
    }-*/;
    
    public JavaScriptObject getWind(int idx){
    	SimulatorWindDTO wind = this.windFieldDTO.getMatrix().get(idx);
    	double y = wind.trueWindSpeedInKnots*Math.cos(wind.trueWindBearingDeg*Math.PI/180.0);
    	double x = wind.trueWindSpeedInKnots*Math.sin(wind.trueWindBearingDeg*Math.PI/180.0);
    	return getWindInst(x,y);
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

    public native void startStreamlets() /*-{
    	if ($wnd.swarmAnimator) {
    		$wnd.swarmUpdData = true;
    		$wnd.updateStreamlets($wnd.swarmUpdData);
    	} else {
			$wnd.initStreamlets($wnd.swarmMap);
    	}
	}-*/;

    public native void setStreamletsStep(int step) /*-{
		$wnd.swarmField.setStep(step);
	}-*/;

    public native void stopStreamlets() /*-{
    	if ($wnd.stopStreamlets) {
			$wnd.stopStreamlets();
    	};
	}-*/;
    
    public void extendWindDataJSON() {
    	
    	List<SimulatorWindDTO> data = this.windFieldDTO.getMatrix();
    	String jsonData = "data:[";
    	int p = 0;
		int imax = this.windParams.getyRes() + 2*this.windParams.getBorderY();
		int jmax = this.windParams.getxRes() + 2*this.windParams.getBorderX();
    	int steps = data.size() / (imax * jmax);
    	double maxWindSpeed = 0;
    	for(int s=0; s<steps; s++) {
    		jsonData +="[";
    		for(int i=0; i<imax; i++) {
    			jsonData +="[";
        		for(int j=0; j<jmax; j++) {
        			SimulatorWindDTO wind = data.get(p);
        			p++;
   					if (wind.trueWindSpeedInKnots > maxWindSpeed) {
						maxWindSpeed = wind.trueWindSpeedInKnots;
					}
   					double y = wind.trueWindSpeedInKnots*Math.cos(wind.trueWindBearingDeg*Math.PI/180.0);
   					double x = wind.trueWindSpeedInKnots*Math.sin(wind.trueWindBearingDeg*Math.PI/180.0);
   					jsonData += x + "," + y;
        			if (j<(jmax-1)) {
        				jsonData += ",";
        			}
        		}
    			if (i<(imax-1)) {
    				jsonData += "],";
    			} else {
    				jsonData += "]";    				
    			}
    		}
			if (s<(steps-1)) {
				jsonData += "],";
			} else {
				jsonData += "]";    				
			}    		
    	}
    	this.windFieldDTO.windDataJSON += jsonData + "],maxLength:" + maxWindSpeed + "};";
    	
    }
    
    
    public void setWindField(final WindFieldDTO windFieldDTO) {
        this.windFieldDTO = windFieldDTO;

        this.extendWindDataJSON();
        this.setWindDataJSON(this.windFieldDTO.windDataJSON);

        //this.getJSNIWind(this);

        this.setMapInstance(this.simulatorMap.getMap().getJso());
		this.setCanvasProjectionInstance(this.simulatorMap.getRegattaAreaCanvasOverlay().getMapProjection());
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
        	if ((nParticles > 0)&&(projection == null)) {
        		this.gwtSwarmTest();
        	}
        	if (windFieldDTO != null) { 
        		// drawing is done by external JavaScript for Streamlets
        	}
        }
    }
    
    public void gwtSwarmTest() {
    	
    	projection = new Mercator(this, map);
    	
    	if (field == null) {
    		SimulatorJSBundle bundle = GWT.create(SimulatorJSBundle.class);
    		String jsonStr = bundle.windStreamletsDataJS().getText();
    		field = RectField.read(jsonStr.substring(19, jsonStr.length()-1), false);
        
    		map.setZoom(5);
        	map.panTo(field.getCenter());
        	
        	projection.calibrate();
    	}
    	    	
    	color = new String[256];
    	for(int alpha=0; alpha<=255; alpha++) {
    		color[alpha] = "rgba(255,255,255,"+(((double)alpha)/255.0)+")";
    	}
    	
    	Context2d ctxt = canvas.getContext2d();
    	ctxt.setFillStyle("white");
    	//ctxt.fillRect(100, 100, 300, 300);
    	
    	particles = new Particle[nParticles];

    	pNE = map.getBounds().getNorthEast();
    	pSW = map.getBounds().getSouthWest();

    	for(int idx=0; idx<particles.length; idx++) {
    		LatLng q = field.getRandomPosition();
    		particles[idx] = new Particle();
    		particles[idx].pos = q;
    		particles[idx].age = Math.max(2, (int)Math.round(Math.random()*40));
    		particles[idx].pxOld = projection.latlng2pixel(particles[idx].pos);
    		particles[idx].alpha = 0;
    	}

    	Scheduler scheduler = Scheduler.get();
    	scheduler.scheduleFixedPeriod(this, 40);    	    	
    }
    
    public void drawSwarm() {
    	Context2d ctxt = canvas.getContext2d();
    	
    	ctxt.setGlobalAlpha(0.05);
    	ctxt.setGlobalCompositeOperation("destination-out");
    	ctxt.setFillStyle("black");
    	ctxt.fillRect(0, 0, canvas.getOffsetWidth(), canvas.getOffsetHeight());
    	ctxt.setGlobalAlpha(1.0);
    	ctxt.setGlobalCompositeOperation("source-over");
    	ctxt.setFillStyle("white");
    	for(int idx=0; idx<particles.length; idx++) {
    		if (particles[idx].age == 0) {
    			continue;
    		}
    		//ctxt.fillRect(x.getX(), x.getY(), 2, 2);
    		ctxt.setLineWidth(1.0);
    		ctxt.setStrokeStyle(color[particles[idx].alpha]);
        	ctxt.beginPath();
			ctxt.moveTo(particles[idx].pxOld.getX(), particles[idx].pxOld.getY());
    		particles[idx].pxOld = projection.latlng2pixel(particles[idx].pos);
			ctxt.lineTo(particles[idx].pxOld.getX(), particles[idx].pxOld.getY());
			ctxt.stroke();
    	}    	
    }
    
    public boolean execute() {

    	double off = 0.01;
    	for(int idx=0; idx<particles.length; idx++) {
    		if (particles[idx].age <= 0) {
    			particles[idx].pos = field.getRandomPosition();
        		particles[idx].age = Math.max(2, (int)Math.round(Math.random()*40));
        		particles[idx].pxOld = projection.latlng2pixel(particles[idx].pos);
        		particles[idx].alpha = 0;
    		}
    		if (particles[idx].age > 0) {
    		Point v = field.getValue(particles[idx].pos);
    		if (v == null) {
    			particles[idx].age = 0;
    		} else {
    		//p[idx].pos = LatLng.newInstance(p[idx].pos.getLatitude()+off, p[idx].pos.getLongitude()+off);
    		double lat = particles[idx].pos.getLatitude() + off*v.getY();
    		double lng = particles[idx].pos.getLongitude() + off*v.getX();
    		double s = RectField.length(v) / field.getMaxLength();
    		particles[idx].alpha = (int)Math.min(255, 90 + Math.round(350 * s));
    		particles[idx].pos = LatLng.newInstance(lat, lng);
    		particles[idx].age--;
    		}
    		}
    		
    	}    	
    	
    	drawSwarm();
    	//System.out.println("loop execute");
    	return true;
    }
    
    private void clear() {
        this.stopStreamlets();        
    }

    @Override
    public void timeChanged(final Date newDate, Date oldDate) {
    	int step = (int)((newDate.getTime() - this.windParams.getStartTime().getTime()) / this.windParams.getTimeStep().getTime());
    	this.setStreamletsStep(step);
    }

    @Override
    public boolean shallStop() {
    	if (timer.getTime().getTime() >= this.windParams.getEndTime().getTime()) {
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
