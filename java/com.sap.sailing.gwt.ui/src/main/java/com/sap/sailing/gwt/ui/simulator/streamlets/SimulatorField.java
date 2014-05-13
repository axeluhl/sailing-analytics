package com.sap.sailing.gwt.ui.simulator.streamlets;

import java.util.List;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;

public class SimulatorField implements VectorField {

	private boolean swarmDebug = false;
	
	private PositionDTO rcStart; 
	private PositionDTO rcEnd;
	
	private int resY;
	private int resX;
	
	private int borderY;
	private int borderX;
	
	private double bdXi;
	private double bdPhi;
	
	private PositionDTO bdA;
	private PositionDTO bdB;
	private PositionDTO bdC;
	
	private double xScale;
	
	private double x0;
	private double x1;
	private double y0;
	private double y1;

	public PositionDTO visSW;
	public PositionDTO visNE;

	private double maxLength;
	private double particleFactor;
	
	private double lngScale;
	
	private PositionDTO nvY;
	private PositionDTO nvX;
	private PositionDTO gvX;
	
	private double[][][] data;
	private int step;
	
	public SimulatorField(WindFieldDTO windData, WindFieldGenParamsDTO windParams) {

		this.step = 0;
		
		String parseString = windData.windDataJSON.substring(18, windData.windDataJSON.length()-1) + "}";
		JSONObject baseData = JSONParser.parseLenient(parseString).isObject();

		this.rcStart = new PositionDTO(baseData.get("rcStart").isObject().get("lat").isNumber().doubleValue(), baseData.get("rcStart").isObject().get("lng").isNumber().doubleValue());
		this.rcEnd = new PositionDTO(baseData.get("rcEnd").isObject().get("lat").isNumber().doubleValue(), baseData.get("rcEnd").isObject().get("lng").isNumber().doubleValue());

		this.resY = (int)baseData.get("resY").isNumber().doubleValue();
		this.resX = (int)baseData.get("resX").isNumber().doubleValue();

		this.borderY = (int)baseData.get("borderY").isNumber().doubleValue();
		this.borderX = (int)baseData.get("borderX").isNumber().doubleValue();
		
		this.bdXi = (this.borderY + 0.5) / (this.resY - 1);
		this.bdPhi = 1.0 + 2*this.bdXi;
		this.bdA = new PositionDTO(this.rcEnd.latDeg+(this.rcEnd.latDeg-this.rcStart.latDeg)*this.bdXi, this.rcEnd.lngDeg+(this.rcEnd.lngDeg-this.rcStart.lngDeg)*this.bdXi);
		this.bdB = new PositionDTO((this.rcStart.latDeg-this.rcEnd.latDeg)*this.bdPhi, (this.rcStart.lngDeg-this.rcEnd.lngDeg)*this.bdPhi);

		this.xScale = baseData.get("xScale").isNumber().doubleValue();

		this.x0 = baseData.get("boundsSW").isObject().get("lng").isNumber().doubleValue();
		this.x1 = baseData.get("boundsNE").isObject().get("lng").isNumber().doubleValue();
		this.y0 = baseData.get("boundsSW").isObject().get("lat").isNumber().doubleValue();
		this.y1 = baseData.get("boundsNE").isObject().get("lat").isNumber().doubleValue();

		this.visSW = new PositionDTO(0.0, 0.0);
		this.visNE = new PositionDTO(0.0, 0.0);
		
    	List<SimulatorWindDTO> gridData = windData.getMatrix();

    	int p = 0;
		int imax = windParams.getyRes() + 2*windParams.getBorderY();
		int jmax = windParams.getxRes() + 2*windParams.getBorderX();
    	int steps = gridData.size() / (imax * jmax);
    	
    	this.data = new double[steps][imax][2*jmax];
    	
    	double maxWindSpeed = 0;
    	for(int s=0; s<steps; s++) {
    		for(int i=0; i<imax; i++) {
        		for(int j=0; j<jmax; j++) {
        			SimulatorWindDTO wind = gridData.get(p);
        			p++;
   					if (wind.trueWindSpeedInKnots > maxWindSpeed) {
						maxWindSpeed = wind.trueWindSpeedInKnots;
					}

   					this.data[s][i][2*j+1] = wind.trueWindSpeedInKnots*Math.cos(wind.trueWindBearingDeg*Math.PI/180.0);
   					this.data[s][i][2*j] = wind.trueWindSpeedInKnots*Math.sin(wind.trueWindBearingDeg*Math.PI/180.0);

        		}
    		}
    	}
    	this.maxLength = maxWindSpeed;
		
    	this.particleFactor = 2.0;
		
		double latAvg = (this.rcEnd.latDeg + this.rcStart.latDeg) / 2.;
		this.lngScale = Math.cos(latAvg * Math.PI / 180.0);
		
		double difLat = this.rcEnd.latDeg - this.rcStart.latDeg;
		double difLng = (this.rcEnd.lngDeg - this.rcStart.lngDeg) * this.lngScale;
		double difLen = Math.sqrt(difLat*difLat + difLng*difLng);
		this.nvY = new PositionDTO(difLat/difLen/difLen*(this.resY-1), difLng/difLen/difLen*(this.resY-1));
		
	    double nrmLat = -difLng/difLen;
	    double nrmLng = difLat/difLen;
	    this.nvX = new PositionDTO( nrmLat/this.xScale/difLen*(this.resX-1), nrmLng/this.xScale/difLen*(this.resX-1) );
	    this.gvX = new PositionDTO( nrmLat*this.xScale*difLen, nrmLng/this.lngScale*this.xScale*difLen );
		this.bdC = new PositionDTO( this.gvX.latDeg*(this.resX+2*this.borderX-1)/(this.resX-1), this.gvX.lngDeg*(this.resX+2*this.borderX-1)/(this.resX-1) );    
	}

    public static native void console(String msg) /*-{
		console.log(msg);
	}-*/;

	public PositionDTO getRandomPosition() {
		double rndY = Math.random();
		double rndX = Math.random() - 0.5;
		PositionDTO result = new PositionDTO();
		result.latDeg = this.bdA.latDeg + rndY * this.bdB.latDeg + rndX * this.bdC.latDeg;
		result.lngDeg = this.bdA.lngDeg + rndY * this.bdB.lngDeg + rndX * this.bdC.lngDeg;
		
		if (swarmDebug&&(!this.inBounds(result))) {
			console("random-position: out of bounds");
		}
		
		return result;
	}


	public boolean inBounds(PositionDTO p) {
		Index idx = this.getIndex(p);
		boolean inBool = (idx.x >= 0) && (idx.x < (this.resX+2*this.borderX)) && (idx.y >= 0) && (idx.y < (this.resY+2*this.borderY));
		return inBool;
	}

	
	public Vector interpolate(PositionDTO p) {

		Neighbors idx = this.getNeighbors(p);
		
		if (swarmDebug&&((idx.xTop >= (this.resX+2*this.borderX))||(idx.yTop >= (this.resY+2*this.borderY)))) {
			console("interpolate: out of range: " + idx.xTop + "  " + idx.yTop);
		}
		
		//System.out.println("neighbors:"+idx.xBot+","+idx.xTop+","+idx.yBot+","+idx.yTop);
		
		double avgX = this.data[this.step][idx.yBot][2*idx.xBot] * (1 - idx.yMod) * (1 - idx.xMod) + this.data[this.step][idx.yTop][2*idx.xBot] * idx.yMod * (1 - idx.xMod)
					+ this.data[this.step][idx.yBot][2*idx.xTop] * (1 - idx.yMod) * idx.xMod + this.data[this.step][idx.yTop][2*idx.xTop] * idx.yMod * idx.xMod;
		double avgY = this.data[this.step][idx.yBot][2*idx.xBot+1] * (1 - idx.yMod) * (1 - idx.xMod) + this.data[this.step][idx.yTop][2*idx.xBot+1] * idx.yMod * (1 - idx.xMod)
					+ this.data[this.step][idx.yBot][2*idx.xTop+1] * (1 - idx.yMod) * idx.xMod + this.data[this.step][idx.yTop][2*idx.xTop+1] * idx.yMod * idx.xMod;
		
		return new Vector(avgX / this.lngScale, avgY);	
	}

	public void setStep(int step) {
		if (step < 0) {
			this.step = 0;
		} else if (step >= this.data.length) {
			this.step = this.data.length-1;
		} else {
			this.step = step;
		}
	}

	public void nextStep() {
		if (this.step < (this.data.length-1)) {
			this.step++;
		}
	}

	public void prevStep() {
		if (this.step > 0) {
			this.step--;
		}
	}

	public Vector getVector(PositionDTO p) {
		return this.interpolate(p);
	}

	public Index getIndex(PositionDTO p) {
		
		// calculate grid indexes
		PositionDTO posR = new PositionDTO( p.latDeg - this.rcStart.latDeg, (p.lngDeg - this.rcStart.lngDeg) * this.lngScale );

		// closest grid point
		long yIdx = Math.round( posR.latDeg * this.nvY.latDeg + posR.lngDeg * this.nvY.lngDeg ) + this.borderY;
		long xIdx = Math.round( posR.latDeg * this.nvX.latDeg + posR.lngDeg * this.nvX.lngDeg + (this.resX - 1) / 2. ) + this.borderX;

		return new Index( xIdx, yIdx );
	}

	public Neighbors getNeighbors(PositionDTO p) {
		
		// calculate grid indexes
		PositionDTO posR = new PositionDTO( p.latDeg - this.rcStart.latDeg, (p.lngDeg - this.rcStart.lngDeg) * this.lngScale );
		
		// surrounding grid points
		double yFlt = posR.latDeg * this.nvY.latDeg + posR.lngDeg * this.nvY.lngDeg + this.borderY;
		double xFlt = posR.latDeg * this.nvX.latDeg + posR.lngDeg * this.nvX.lngDeg + (this.resX - 1) / 2. + this.borderX;
		double yBot = Math.floor( yFlt );
		double xBot = Math.floor( xFlt );
		double yTop = Math.ceil( yFlt );
		double xTop = Math.ceil( xFlt );
		double yMod = yFlt - yBot;
		double xMod = xFlt - xBot;
		
		if (xBot < 0) {
			xBot = 0;
		}

		if (yBot < 0) {
			yBot = 0;
		}

		if (xTop >= (this.resX+2*this.borderX)) {
			xTop = this.resX+2*this.borderX-1;
		}

		if (yTop >= (this.resY+2*this.borderY)) {
			yTop = this.resY+2*this.borderY-1;
		}

		//System.out.println("neighbors:"+xFlt+","+xBot+","+xTop+","+yFlt+","+yBot+","+yTop);

		return new Neighbors((int)xTop, (int)yTop, (int)xBot, (int)yBot, xMod, yMod );	
	}

	public double getMaxLength() {
		return maxLength;
	}

	public double motionScale(int zoomLevel) {
		return 0.08 * Math.pow(1.6, Math.min(1.0, 6.0 - zoomLevel));
	}

	public double particleWeight(PositionDTO p, Vector v) {
		return v.length() / this.maxLength + 0.1;	
	}

	public String[] getColors() {
		String[] colors = new String[256];
		double alpha = 0.7;
		int greyValue = 255;
		for (int i = 0; i < 256; i++) {
			colors[i] = "rgba(" + (greyValue) + "," + (greyValue) + "," + (greyValue) + "," + (alpha*i/255.0) + ")";
			//this.colors[i] = 'hsla(' + 360*(0.55+0.9*(0.5-i/255)) + ',' + (100) + '% ,' + (50) + '%,' + (i/255) + ')';
		}
		return colors;
	}

	public double lineWidth(int alpha) {
		return 1.0;
	}

	public PositionDTO getFieldNE() {
		return new PositionDTO(Math.max(this.y0, this.y1), Math.max(this.x0, this.x1));
	}
	
	public PositionDTO getFieldSW() {
		return new PositionDTO(Math.min(this.y0, this.y1), Math.min(this.x0, this.x1));
	}
	
	public void setVisNE(PositionDTO visNE) {
		this.visNE = visNE;
	}
	
	public void setVisSW(PositionDTO visSW) {
		this.visSW = visSW;
	}

	public double getParticleFactor() {
		return this.particleFactor;
	}
}
