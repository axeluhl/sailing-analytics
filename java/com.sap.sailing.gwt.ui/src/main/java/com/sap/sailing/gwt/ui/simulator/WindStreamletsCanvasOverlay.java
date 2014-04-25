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
    
	com.google.gwt.user.client.Timer loopTimer;
    private String[] color;
    private Mercator projection;
    private RectField field;
    private int nParticles;
    private Particle[] particles;
    private GeoPos pNE;
    private GeoPos pSW;
    private boolean swarmOffScreen = false;
    private int swarmPause = 0;
    private GeoPos boundsNE;
	private GeoPos boundsSW;

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
    
    public static native void console(String msg) /*-{
    	console.log(msg);
    }-*/;
    
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

    	this.updateBounds();
    	
    	color = field.getColors();

    	Context2d ctxt = canvas.getContext2d();
    	ctxt.setFillStyle("white");

    	particles = this.makeNewParticles();

    	/*// Create animation-loop using scheduler for repeated method calls
    	Scheduler scheduler = Scheduler.get();
    	scheduler.scheduleFixedPeriod(this, 40);
    	//scheduler.scheduleFixedDelay(this, 10);*/
    	
    	startLoop(40);
    }

    public Particle[] makeNewParticles() {
 
    	Particle[] newParticles = new Particle[nParticles];

    	for(int idx=0; idx<newParticles.length; idx++) {
    		GeoPos q = field.getRandomPosition();
    		Particle particle = new Particle();
    		particle.pos = q;
    		particle.age = Math.max(2, (int)Math.round(Math.random()*40));
    		particle.pxOld = projection.latlng2pixel(q);
    		particle.alpha = 0;
    		particle.v = field.getVector(q);
    		newParticles[idx] = particle;
    	}
    	
    	return newParticles;
    }

    public void onBoundsChanged() {
    	projection.clearCanvas();
    	swarmPause = 5;
    }
    
    public void onMoveEnd() {
    	
    }
    
    public void updateBounds() {

    	GeoPos mapNE = new GeoPos(map.getBounds().getNorthEast());
    	GeoPos mapSW = new GeoPos(map.getBounds().getSouthWest());

    	GeoPos fieldNE = new GeoPos(Math.max(this.field.y0, this.field.y1), Math.max(this.field.x0, this.field.x1));
    	GeoPos fieldSW = new GeoPos(Math.min(this.field.y0, this.field.y1), Math.min(this.field.x0, this.field.x1));

    	Vector visibleNE = this.isVisible(fieldNE);
    	Vector visibleSW = this.isVisible(fieldSW);

    	boolean useBoundsNorth = (visibleNE.y == 0);
    	boolean useBoundsEast = (visibleNE.x == 0);
    	boolean useBoundsSouth = (visibleSW.y == 0);
    	boolean useBoundsWest = (visibleSW.x == 0);

    	swarmOffScreen = (visibleNE.y > 0)||(visibleSW.y < 0)||(visibleNE.x < 0)||(visibleSW.x > 0);

    	if (swarmOffScreen) {

    		this.boundsNE = fieldNE;
    		this.boundsSW = fieldSW;

    	} else {

    		if ((!useBoundsNorth)&&(!useBoundsEast)) {
    			this.boundsNE = mapNE;
    		} else if (!useBoundsNorth) {
    			this.boundsNE = new GeoPos(mapNE.lat, fieldNE.lng);
    		} else if (!useBoundsEast) {
    			this.boundsNE = new GeoPos(fieldNE.lat, mapNE.lng);		
    		} else {
    			this.boundsNE = fieldNE;
    		}

    		if ((!useBoundsSouth)&&(!useBoundsWest)) {
    			this.boundsSW = mapSW;
    		} else if (!useBoundsSouth) {
    			this.boundsSW = new GeoPos(mapSW.lat, fieldSW.lng);
    		} else if (!useBoundsWest) {
    			this.boundsSW = new GeoPos(fieldSW.lat, mapSW.lng);		
    		} else {
    			this.boundsSW = fieldSW;
    		}

    	}

    	this.field.visX0 = this.boundsSW.lng;
    	this.field.visY0 = this.boundsSW.lat;
    	this.field.visX1 = this.boundsNE.lng;
    	this.field.visY1 = this.boundsNE.lat;

    	Vector boundsSWpx = this.projection.latlng2pixel(this.boundsSW);
    	Vector boundsNEpx = this.projection.latlng2pixel(this.boundsNE);

    	double boundsWidthpx = Math.abs(boundsNEpx.x - boundsSWpx.x);
    	double boundsHeightpx = Math.abs(boundsSWpx.y - boundsNEpx.y);

    	this.nParticles = (int)Math.round(Math.sqrt(boundsWidthpx * boundsHeightpx) * this.field.numParticleFactor);
    	//this.numParticles = Math.sqrt(boundsWidthpx*boundsWidthpx + boundsHeightpx*boundsHeightpx) * this.field.numParticleFactor;
    	//console.log("numParticles: "+this.numParticles + " at " + (boundsWidthpx) +"x" + (boundsHeightpx) + "px  (" + (boundsWidthpx * boundsHeightpx) + " pixels)");
    };

    public Vector isVisible(GeoPos pos) {

    	// test for visibility of swarm
    	Vector proj = this.projection.latlng2pixel(pos);

    	Vector result = new Vector();
    	result.x = (proj.x < 0 ? -1 : 0) + (proj.x > this.getCanvas().getOffsetWidth() ? 1 : 0);
    	result.y = (proj.y < 0 ? -1 : 0) + (proj.y > this.getCanvas().getOffsetHeight() ? 1 : 0);

    	return result;
    }
    
    public void startLoop(final int millis) {
    	// Create animation-loop based on timer timeout
    	loopTimer = new com.google.gwt.user.client.Timer() {
    		public void run() {
    			Date time0 = new Date();
    			if (swarmPause > 1) {
    				swarmPause--;
    			} else if (swarmPause == 1) {    				
    	        	setCanvasSettings();
    	    		projection.calibrate();
    	    		updateBounds();
    	    		particles = makeNewParticles();
    	    		swarmPause = 0;
    			}
    			//console("swarmOffScreen:"+swarmOffScreen);
    			if ((!swarmOffScreen)&&(swarmPause == 0)) {
    				execute();
    			}
    			Date time1 = new Date();
    			//console("delta:"+(time1.getTime()-time0.getTime())+"/"+millis);
    			loopTimer.schedule((int)Math.max(10, millis - (time1.getTime()-time0.getTime())));
    		}
    	};
    	loopTimer.schedule(millis);
    }
    
    public void drawSwarm() {

    	Context2d ctxt = canvas.getContext2d();

    	ctxt.setGlobalAlpha(0.08);
    	ctxt.setGlobalCompositeOperation("destination-out");
    	ctxt.setFillStyle("black");
    	ctxt.fillRect(0, 0, canvas.getOffsetWidth(), canvas.getOffsetHeight());
    	ctxt.setGlobalAlpha(1.0);
    	ctxt.setGlobalCompositeOperation("source-over");
    	ctxt.setFillStyle("white");
    	//int nDrawn = 0;
    	for(int idx=0; idx<particles.length; idx++) {
    		Particle particle = particles[idx];
    		if (particle.age == 0) {
    			continue;
    		}
    		//nDrawn++;
    		ctxt.setLineWidth(field.lineWidth(particle.alpha));
    		ctxt.setStrokeStyle(color[particle.alpha]);
    		ctxt.beginPath();
    		ctxt.moveTo(particle.pxOld.x, particle.pxOld.y);
    		particle.pxOld = projection.latlng2pixel(particle.pos);
    		ctxt.lineTo(particle.pxOld.x, particle.pxOld.y);
    		ctxt.stroke();
    	}
    	//console("drawn particles:"+nDrawn);
    }
 
/*    public void swarmStart() {
    	int millis = 40; //opt_millis; // || 20;
    	//self = this;
    	//function go() {
    	Date start = new Date();
    	if (!execute()) {
    			return;
    	}
    	Date time = new Date();
    	int passed = (int)(time.getTime() - start.getTime());
    	setTimeout(swarmStart(), Math.max(10, passed));
    	//}
    	//go();
    }*/
    
    public boolean execute() {

    	double speed = 0.01*field.motionScale(map.getZoom());
    	for(int idx=0; idx<particles.length; idx++) {
    		Particle particle = particles[idx];
    		Vector v = null;
    		if (particle.age <= 0) {
    			boolean done = false;
    			while (!done) {
    				particle.pos = field.getRandomPosition();
    				v = field.getVector(particle.pos);
    				double weight = field.particleWeight(particle.pos, v);
    				if ((weight >= Math.random())&&(v != null)) {
    					particle.age = Math.max(2, (int)Math.round(Math.random()*40));
    					particle.pxOld = projection.latlng2pixel(particle.pos);
    					particle.alpha = 0;
    					particle.v = v;
    					done = true;
    				}
    			}
    		}
    		if (particle.age > 0) {
    			if (particle.v == null) {
    				particle.age = 0;
    			} else {
    				particle.pos.lat = particle.pos.lat + speed*particle.v.y;
    				particle.pos.lng = particle.pos.lng + speed*particle.v.x;
    				double s = particle.v.length() / field.getMaxLength();
    				particle.alpha = (int)Math.min(255, 90 + Math.round(350 * s));
    				particle.age--;
    				if (particle.age > 0) {
    					particle.v = field.getVector(particle.pos);
    				}
    			}
    		}

    	}    	

    	drawSwarm();

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
