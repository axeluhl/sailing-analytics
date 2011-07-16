package com.sap.sailing.gwt.ui.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.LineCap;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.Composite;

/**
 * WindIndicator Widget allows visualizing the wind direction and other wind related information.
 */
public class WindIndicator extends Composite {

	private static final String RGB_WHITE = "rgb(255,255,255)";
	private static final String RGB_BLACK = "rgb(0,0,0)";
	
	private Canvas canvas;
	/**
	 * direction from where the wind blows
	 */
	private double fromDeg = 0.0;
	/**
	 * wind speed in knots
	 */
	private double speed = 0.0;
	/**
	 * cloud coverage from 0.0 to 1.0
	 */
	private double cloudCoverage = 0.0;
	
	/**
	 * Create a new WindIndiator
	 */
	public WindIndicator() {
		canvas = Canvas.createIfSupported();
		this.initWidget(canvas);
		this.addAttachHandler(new AttachEvent.Handler() {			
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if(event.isAttached()) {
					WindIndicator.this.updateRendering();
				}
			}
		});
	}
	
	/**
	 * Set the direction from where the wind blows
	 * @param fromDeg
	 */
	public void setFromDeg(double fromDeg) {
		this.fromDeg = fromDeg;
		this.updateRendering();
	}

	/**
	 * Get the currently set direction from where the wind blows
	 * @return
	 */
	public double getFromDeg() {
		return fromDeg;
	}
	
	/**
	 * Set the cloud coverage from 0.0 to 1.0
	 * @param cloudCoverage
	 */
	public void setCloudCoverage(double cloudCoverage) {
		this.cloudCoverage = cloudCoverage;
	}
	
	/**
	 * get the currently set cloud coverage
	 * @return
	 */
	public double getCloudCoverage() {
		return cloudCoverage;
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.UIObject#setSize(java.lang.String, java.lang.String)
	 */
	@Override
	public void setSize(String width, String height) {
		super.setSize(width, height);
		canvas.setSize(width, height);
	}
	
	
	/**
	 * Updates the rendering
	 */
	private void updateRendering() {
		
		int minSize = Math.min(canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
		canvas.setCoordinateSpaceWidth(minSize);
		canvas.setCoordinateSpaceHeight(minSize);
		
		Context2d ctx = canvas.getContext2d();
		
		// draw the line
		ctx.setLineWidth(4);
		ctx.setLineCap(LineCap.ROUND);
		ctx.beginPath();		
		ctx.moveTo( minSize / 2, minSize / 2);
		double dirRad = (fromDeg - 90) * Math.PI / 180; 
		ctx.arc(minSize / 2, minSize / 2, minSize / 3, dirRad, dirRad + 0.001 /* ok, not nice, but works */, false);
		ctx.stroke();
		ctx.fill();
		ctx.setStrokeStyle(RGB_BLACK);
		ctx.closePath();
		ctx.stroke();

		// draw the circle
		ctx.setFillStyle(RGB_WHITE);
		ctx.setStrokeStyle(RGB_BLACK);
		//ctx.beginPath();
		ctx.arc(minSize / 2, minSize / 2, minSize / 10, 0, 2 * Math.PI, true);
		ctx.stroke();
		ctx.fill();
		//draw the cloud coverage
		ctx.setFillStyle(RGB_BLACK);
		ctx.beginPath();
		ctx.moveTo(minSize / 2, minSize / 2);
		double cc = Math.max(0.0, Math.min(1.0, cloudCoverage));
		ctx.arc(minSize / 2, minSize / 2, minSize / 10, -0.5 * Math.PI, (-0.5 + cc * 2) * Math.PI, false);
		ctx.fill();
		
		//TODO render wind speed
//		double bf = getBeaufortFromKnots(this.speed);
	}

	private double getRadianFromDegree(double deg) {
		return deg  * Math.PI / 180;
	}
	
	
	/**
	 * Calculates Beaufort from Knots applying some emprical formula
	 * @see http://de.wikipedia.org/wiki/Beaufortskala
	 * @param knots
	 * @return wind speed in Beaufort
	 */
	private double getBeaufortFromKnots(double knots) {
		return knots + 10.0 / 6.0;
	}
	
}
