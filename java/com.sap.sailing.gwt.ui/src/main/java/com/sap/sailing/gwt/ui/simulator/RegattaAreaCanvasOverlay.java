package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.TextAlign;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MapMoveEndHandler;
import com.google.gwt.maps.client.event.PolygonClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.maps.client.overlay.Polygon;
import com.sap.sailing.gwt.ui.shared.racemap.FullCanvasOverlay;
import com.sap.sailing.simulator.util.SailingSimulatorUtil;

public class RegattaAreaCanvasOverlay extends FullCanvasOverlay {

	private SimulatorMap simulatorMap;
    private RaceCourseCanvasOverlay raceCourseCanvasOverlay;
	private ArrayList<RegattaArea> regAreas;
	private boolean pan;
	private RegattaArea currentRegArea = null;
	private double raceBearing = 0.0;
	private double diffBearing = 0.0;

	public RegattaAreaCanvasOverlay(SimulatorMap simulatorMap) {
		super();
		this.simulatorMap = simulatorMap;
	}

	
	@Override
	protected Overlay copy() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	protected void initialize(final MapWidget map) {
		super.initialize(map);

		LatLng cPos;
		regAreas = new ArrayList<RegattaArea>();

		// TV
		cPos = LatLng.newInstance(54.43450,10.19559167);
		currentRegArea = new RegattaArea("TV", cPos, 0.5, "#EAB75A");
		regAreas.add(currentRegArea);

		
		// Golf
		cPos = LatLng.newInstance(54.41985556,10.19454167);
		regAreas.add(new RegattaArea("Golf", cPos, 0.35, "#F1F3EF","silver"));

		// Foxtrot
		cPos = LatLng.newInstance(54.445775,10.29223889);
		regAreas.add(new RegattaArea("Foxtrot", cPos, 0.65, "#B4287C"));

		// India
		cPos = LatLng.newInstance(54.44803611,10.20863611);
		regAreas.add(new RegattaArea("India", cPos, 0.40, "#774741"));

		// Juliett
		cPos = LatLng.newInstance(54.46183611,10.2239);
		regAreas.add(new RegattaArea("Juliett", cPos, 0.55, "#818585"));

		// Echo
		cPos = LatLng.newInstance(54.47640278,10.20090556);
		regAreas.add(new RegattaArea("Echo", cPos, 0.60, "#1CADD9"));
		
		// Kilo
		cPos = LatLng.newInstance(54.47808889,10.24033889);
		regAreas.add(new RegattaArea("Kilo", cPos, 0.55, "#9FC269"));

		// Charlie
		cPos = LatLng.newInstance(54.49327222,10.17525833);
		regAreas.add(new RegattaArea("Charlie", cPos, 0.70, "#0A5998"));

		// Delta
		cPos = LatLng.newInstance(54.49706111,10.21921944);
		regAreas.add(new RegattaArea("Delta", cPos, 0.75, "#179E8B"));

		// Bravo
		cPos = LatLng.newInstance(54.50911667,10.13973333);
		regAreas.add(new RegattaArea("Bravo", cPos, 0.80, "#CE3032"));

		// Alfa
		cPos = LatLng.newInstance(54.52905,10.18515278);
		regAreas.add(new RegattaArea("Alfa", cPos, 1.00, "#D9699B"));

		setVisible(true);

		int regIdx = 0;
		for(RegattaArea regArea : regAreas) {
			this.drawCircleFromRadius(regIdx, regArea, 10);
			regIdx++;
		}

		// Middle of Echo and Klio
		cPos = LatLng.newInstance(54.477245795,10.220622225);
		map.panTo(cPos);
	}

	@Override
	protected void remove() {
		setVisible(false);
		super.remove();
	}

	@Override
	protected void redraw(final boolean force) {
		super.redraw(force);

		clear();
		drawRegattaAreas();

	}

	private void clear() {
		canvas.getContext2d().clearRect(0.0 /* canvas.getAbsoluteLeft() */, 0.0/* canvas.getAbsoluteTop() */,
				canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
	}

	protected void drawRegattaAreas() {

		clear();

		LatLng cPos = LatLng.newInstance(54.4344,10.19659167);
		Point centerPoint = getMap().convertLatLngToDivPixel(cPos);
		Point borderPoint = getMap().convertLatLngToDivPixel(this.getEdgePoint(cPos, 0.015));
		double pxStroke = Math.pow(2.0, (getMap().getZoomLevel()-10.0)/2.0);
		
		final Context2d context2d = canvas.getContext2d();
		context2d.setLineWidth(3);
		context2d.setStrokeStyle("Black");

		for(RegattaArea regArea : regAreas) {
			centerPoint = getMap().convertLatLngToDivPixel(regArea.centerPos);
			borderPoint = getMap().convertLatLngToDivPixel(regArea.edgePos);
			drawRegattaAreaBackground(context2d, centerPoint, borderPoint, regArea.color, pxStroke);
		}

		for(RegattaArea regArea : regAreas) {
			centerPoint = getMap().convertLatLngToDivPixel(regArea.centerPos);
			borderPoint = getMap().convertLatLngToDivPixel(regArea.edgePos);
			drawRegattaArea(regArea.name, context2d, centerPoint, borderPoint, regArea.color, regArea.colorText, pxStroke);
		}

	}

	public void drawCircleFromRadius(int regIdx, RegattaArea regArea, int nbOfPoints) {

		LatLngBounds bounds = LatLngBounds.newInstance();
		LatLng[] circlePoints = new LatLng[nbOfPoints];

		double EARTH_RADIUS = 6371;
		double d = 1.852 * regArea.radius / EARTH_RADIUS;
		double lat1 = Math.toRadians(regArea.centerPos.getLatitude());
		double lng1 = Math.toRadians(regArea.centerPos.getLongitude());

		double a = 0;
		double step = 360.0 / (double) nbOfPoints;
		for (int i = 0; i < nbOfPoints; i++) {
			double tc = Math.toRadians(a);
			double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d) + Math.cos(lat1) * Math.sin(d) * Math.cos(tc));
			double lng2 = lng1 + Math.atan2(Math.sin(tc) * Math.sin(d) * Math.cos(lat1), Math.cos(d) - Math.sin(lat1) * Math.sin(lat2));
			LatLng point = LatLng.newInstance(Math.toDegrees(lat2), Math.toDegrees(lng2));
			circlePoints[i] = point;
			bounds.extend(point);
			a += step;
		}

		// transparent circle-like polygons to make regatta areas clickable
		Polygon circle = new Polygon(circlePoints, "white", 1, 0.0, "green", 0.0);

		final int regIdxFinal = regIdx;

		/*circle.addPolygonMouseOverHandler( new PolygonMouseOverHandler() {
			public void onMouseOver(PolygonMouseOverEvent e) {
				System.out.println("MouseOver: "+name);
			}
		});*/
				
		circle.addPolygonClickHandler( new PolygonClickHandler() {
			public void onClick(PolygonClickEvent e) {
				//System.out.println("Click: "+currentRegArea.name);
				RegattaArea newRegArea = regAreas.get(regIdxFinal);

				if (newRegArea != currentRegArea) {
					currentRegArea = newRegArea;
					simulatorMap.clearOverlays();
					updateRaceCourse(0,0);
					raceCourseCanvasOverlay.redraw(true);
				}
				
				pan = true;
				map.panTo(currentRegArea.centerPos);
			}
		});

		getMap().addOverlay(circle);
		getMap().addMapMoveEndHandler( new MapMoveEndHandler() {
			public void onMoveEnd(MapMoveEndEvent event) {
				// TODO Auto-generated method stub
				if (pan) {
					//System.out.println("PanEnd.");
					pan = false;
					if (map.getZoomLevel() < 14) {
						map.setZoomLevel(14);
					}
				}

			};	
		});
		circle.setVisible(true);
	}

	protected void updateRaceCourse(int type, double bearing) {
		if (type == 1) {
			raceBearing = bearing;
		} else if (type == 2) {
			diffBearing = bearing;			
		}
		if (currentRegArea != null) {
			//simulatorMap.getMainPanel().setUpdateButtonEnabled(true);
			if (raceCourseCanvasOverlay.rcDirection == SailingSimulatorUtil.RaceCourseDownwind) {
				raceCourseCanvasOverlay.endPoint = getDistantPoint(currentRegArea.centerPos, 0.9*currentRegArea.radius, 180.0 + raceBearing - diffBearing);
				raceCourseCanvasOverlay.startPoint = getDistantPoint(currentRegArea.centerPos, 0.9*currentRegArea.radius, 0.0 + raceBearing - diffBearing);
			} else {
				raceCourseCanvasOverlay.startPoint = getDistantPoint(currentRegArea.centerPos, 0.9*currentRegArea.radius, 180.0 + raceBearing - diffBearing);
				raceCourseCanvasOverlay.endPoint = getDistantPoint(currentRegArea.centerPos, 0.9*currentRegArea.radius, 0.0 + raceBearing - diffBearing);
			}
		} else {
			//simulatorMap.getMainPanel().setUpdateButtonEnabled(false);
		}
	}
	
	
	protected void drawRegattaAreaBackground(Context2d context2d, Point centerPoint, Point borderPoint, String color, double pxStroke) {

		Point diffPoint = Point.newInstance(centerPoint.getX()-borderPoint.getX(), centerPoint.getY()-borderPoint.getY());
		double pxRadius = Math.sqrt(diffPoint.getX()*diffPoint.getX() + diffPoint.getY()*diffPoint.getY());

		context2d.setGlobalAlpha(1.0f);
		context2d.setFillStyle("#a6bfde"); // Google Blue
		context2d.beginPath();
		context2d.arc(centerPoint.getX() - this.getWidgetPosLeft(), centerPoint.getY() - this.getWidgetPosTop(), pxRadius*1.2, 0.0, 2*Math.PI);
		context2d.closePath();
		context2d.fill();

	}


	protected void drawRegattaArea(String name, Context2d context2d, Point centerPoint, Point borderPoint, String color, String colorText, double pxStroke) {

		Point diffPoint = Point.newInstance(centerPoint.getX()-borderPoint.getX(), centerPoint.getY()-borderPoint.getY());
		double pxRadius = Math.sqrt(diffPoint.getX()*diffPoint.getX() + diffPoint.getY()*diffPoint.getY());

		context2d.setGlobalAlpha(1.0f);
		context2d.setFillStyle("#DEDEDE");
		context2d.setLineWidth(pxStroke);
		context2d.setStrokeStyle(color);
		context2d.beginPath();
		context2d.arc(centerPoint.getX() - this.getWidgetPosLeft(), centerPoint.getY() - this.getWidgetPosTop(), pxRadius, 0.0, 2*Math.PI);
		context2d.closePath();
		context2d.fill();
		context2d.stroke();

		context2d.setGlobalAlpha(0.4f);
		context2d.setFillStyle(color);
		context2d.beginPath();
		context2d.arc(centerPoint.getX() - this.getWidgetPosLeft(), centerPoint.getY() - this.getWidgetPosTop(), pxRadius, 0.0, 2*Math.PI);
		context2d.closePath();
		context2d.fill();

		if (getMap().getZoomLevel() >= 11) {
			context2d.setGlobalAlpha(0.8f);
			context2d.setFillStyle(colorText);
			
			double fontsize = 14.0 + (32.0-14.0)*(getMap().getZoomLevel() - 11.0)/(14.0-11.0);
			context2d.setFont("normal "+fontsize+"px Calibri");
	        //TextMetrics txtmet = context2d.measureText(name);
			context2d.setTextAlign(TextAlign.CENTER);
			context2d.fillText(name, centerPoint.getX() - this.getWidgetPosLeft(), centerPoint.getY()+0.32*fontsize - this.getWidgetPosTop());
			
			context2d.setGlobalAlpha(1.0f);
		}
		
	}

	protected LatLng getEdgePoint(LatLng pos, double dist) {
		return getDistantPoint(pos,dist,0.0);
	}
	
	protected LatLng getDistantPoint(LatLng pos, double dist, double degBear) {

		double lat1 = pos.getLatitudeRadians();
		double lon1 = pos.getLongitudeRadians();

		double brng = degBear * Math.PI / 180;
		
		double R = 6371;
		double d = 1.852*dist;
		double lat2 = Math.asin( Math.sin(lat1)*Math.cos(d/R) + Math.cos(lat1)*Math.sin(d/R)*Math.cos(brng) );
		double lon2 = lon1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(lat1), Math.cos(d/R)-Math.sin(lat1)*Math.sin(lat2));
		lon2 = (lon2+3*Math.PI) % (2*Math.PI) - Math.PI;  // normalise to -180° ... +180°*/

		double lat2deg = lat2/Math.PI*180;
		double lon2deg = lon2/Math.PI*180;

		LatLng result = LatLng.newInstance(lat2deg, lon2deg);

		return result;
	}

	public void setRaceCourseCanvas(RaceCourseCanvasOverlay rcCanvas) {
		this.raceCourseCanvasOverlay = rcCanvas;
	}
	
}
