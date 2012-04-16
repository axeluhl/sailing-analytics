package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.SimulatorService;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeGenParamsDTO;
import java.util.logging.*;

public class SimulatorEntryPoint implements EntryPoint {
	private final SimulatorServiceAsync simulatorSvc = GWT.create(SimulatorService.class);

	private final VerticalPanel 	panel			= new VerticalPanel();
	private final ListBox 			raceSelector	= new ListBox();
	
	private MapWidget mapw;
	
	private Polyline []	windlattice					= new Polyline[0];
	private List<PositionDTO> locations				= new ArrayList<PositionDTO>();
	
	private static Logger logger = Logger.getLogger("com.sap.sailing");
	@Override
	public void onModuleLoad() {
		logger.fine("In onModuleLoad");
		/*
		 * Asynchronously loads the Maps API.
		 *
		 * The first parameter should be a valid Maps API Key to deploy this
		 * application on a public server, but a blank key will work for an
		 * application served from localhost.
		 */
		Maps.loadMapsApi("", "2", false, new Runnable() {
			public void run() {
				buildUi();
			}
		});
	}

	private void buildUi() {
		logger.fine("In buildUi");
		loadRaceLocations();
		
		initMap();
		initRaceSelector();
		initPanel();

		// Add the map to the HTML host page
		RootLayoutPanel.get().add(panel);
	}
	
	private void initMap() {
		logger.fine("In initMap");
		mapw = new MapWidget();
		mapw.setUI(SimulatorMapOptions.newInstance());
		mapw.setZoomLevel(7);
		mapw.setSize("100%", "650px");
		
		mapw.addMapClickHandler(new MapClickHandler() {
			@Override
			public void onClick(MapClickEvent event) {
				generateWindLattice(event.getLatLng());
			}
		});
	}
	
	private void loadRaceLocations() {
		logger.fine("In loadRaceLocations");
		simulatorSvc.getRaceLocations(new AsyncCallback<PositionDTO[]>() {
			@Override
			public void onFailure(Throwable caught) {
				String message = caught.getMessage();
				for( StackTraceElement ste : caught.getStackTrace() ) {
					message += ste.toString() + "\n\t";
				}
				Window.alert("Failed servlet call to SimulatorService\n" + message);
			}

			@Override
			public void onSuccess(PositionDTO[] result) {
				locations.clear();
				int i = 0;
				for( PositionDTO rl : result ) {
					locations.add(rl);
					raceSelector.addItem(Integer.toString(++i));
				}
			}
		});
	}
	
	private void initRaceSelector() {
		logger.fine("In initRaceSelector");
		raceSelector.setVisibleItemCount(1);
		raceSelector.setTitle("Race Location Selection");
		raceSelector.setSize("100%", "20px");
		
		raceSelector.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				PositionDTO rl = locations.get(raceSelector.getSelectedIndex());
				selectRaceLocation(rl);
			}
		});
	}
	
	private void initPanel() {
		logger.fine("In initPanel");
		panel.add(raceSelector);
		panel.add(mapw);
		panel.setSize("100%", "100%");
	}

	private void selectRaceLocation(PositionDTO rl) {
		logger.fine("In selectRaceLocation");
		mapw.clearOverlays();
		LatLng position = LatLng.newInstance(rl.latDeg, rl.lngDeg);
		mapw.panTo(position);
	}

	private void generateWindLattice(LatLng center) {
		if (center != null) {
			logger.info("In generateWindLattice with center " + center.getLatitude() + " " + center.getLongitude());
		} else {
			logger.info("Center is null");
			return;
		}
		WindLatticeGenParamsDTO params = new WindLatticeGenParamsDTO();
		params.setCenter(new PositionDTO(center.getLatitude(), center.getLongitude()));
		params.setGridsizeX(5);
		params.setGridsizeY(5);
		params.setxSize(1);
		params.setySize(1);
		
		simulatorSvc.getWindLatice(params, new AsyncCallback<WindLatticeDTO>(){
			@Override
			public void onFailure(Throwable message) {
				Window.alert("Failed servlet call to SimulatorService\n" + message);
			}
			@Override
			public void onSuccess(WindLatticeDTO wl) {
				refreshWindLattice(wl);
			}
		});
	}
	
	private void refreshWindLattice(WindLatticeDTO wl) {
		logger.fine("In refereshWindLattice");
		PositionDTO [][] matrix = wl.getMatrix();
		int numRows = matrix.length;
		int numCols = matrix[0].length;
		logger.fine("Numrows : " + numRows + " Numcols : " + numCols);
		int line = 0;
		
		Polyline [] newWindLattice = new Polyline[numRows + numCols];
		
		for( int i = 0; i < numRows; i++ ) {
			LatLng [] points = new LatLng[numCols];
			for( int j = 0; j < numCols; j++ ) {
				points[j] = LatLng.newInstance(matrix[i][j].latDeg, matrix[i][j].lngDeg);
			}
			newWindLattice[line++] = new Polyline(points, "#000000", 1);
		}

		for( int i = 0; i < numCols; i++ ) {
			LatLng [] points = new LatLng[numRows];
			for( int j = 0; j < numRows; j++ ) {
				points[j] = LatLng.newInstance(matrix[j][i].latDeg, matrix[j][i].lngDeg);
			}
			newWindLattice[line++] = new Polyline(points, "#000000", 1);
		}

		mapw.clearOverlays();
		for( Polyline pl : newWindLattice ) {
			this.mapw.addOverlay(pl);
		}
		generateWindDirection(wl);
	}
	
	private void generateWindDirection(WindLatticeDTO wl) {
		logger.fine("In generateWindDirection");
		PositionDTO [][] matrix = wl.getMatrix();
		int numRows = matrix.length;
		int numCols = matrix[0].length;
		//addArrow(matrix[0][0], matrix[1][1]);
		if (numRows > 0 && numCols > 0) {
			LatLng start = LatLng.newInstance(matrix[0][0].latDeg, matrix[0][0].lngDeg);
			LatLng end = LatLng.newInstance(matrix[1][1].latDeg, matrix[1][1].lngDeg);
			Point sPoint = mapw.convertLatLngToContainerPixel(start);
			Point ePoint = mapw.convertLatLngToContainerPixel(end);
			double dx = ePoint.getX() - sPoint.getX();
			double dy = ePoint.getY() - ePoint.getY();
			double distance = Math.sqrt((dx*dx) + (dy*dy));
			logger.fine("Distance : " + distance);
			double theta = Math.PI - Math.PI/4; //Math.atan2(-dy,dx);
			for(int i = 1; i < numRows; ++i) {
				for (int j = 1; j < numCols; ++j) {
					addArrow(matrix[i][j], theta, distance, (int) (0.5 + i*0.5));
				}
			
			}
		}
	}
	
	private void addArrow(PositionDTO start, PositionDTO end) {
		LatLng[] arrowPoints = new LatLng[2];
		arrowPoints[0] = LatLng.newInstance(start.latDeg, start.lngDeg);
		arrowPoints[1] = LatLng.newInstance(end.latDeg, end.lngDeg);

		Polyline arrow = new Polyline(arrowPoints, "Green", 1);
		this.mapw.addOverlay(arrow);
		double theta = Math.PI/4;
		addHead(arrowPoints[1], theta, 1, 1);
		logger.info("In addArrow(start, end) arrowPoints[0]" + arrowPoints[0]);
		logger.info("In addArrow(start, end) arrowPoints[1]" + arrowPoints[1]);
	}
	
	private void addArrow(PositionDTO start, double angle, double length, int weight) {
		logger.fine("In addArrow(start, angle, length)");
		LatLng[] arrowPoints = new LatLng[2];
		arrowPoints[0] = LatLng.newInstance(start.latDeg, start.lngDeg);
		Point point = mapw.convertLatLngToContainerPixel(arrowPoints[0]);
		double x1 = point.getX() + length*Math.cos(angle);
		double y1 = point.getY() + length*Math.sin(angle);
		LatLng end = mapw.convertContainerPixelToLatLng(Point.newInstance((int)x1, (int)y1));
		
		arrowPoints[1] = LatLng.newInstance(end.getLatitude(), end.getLongitude());
		Polyline arrow = new Polyline(arrowPoints, "Green", weight);
		this.mapw.addOverlay(arrow);
		logger.fine("In addArrow(start, angle, length) arrowPoints[0]" + arrowPoints[0]);
		logger.fine("In addArrow(start, angle, length) arrowPoints[1]" + arrowPoints[1]);
		double dx = x1 - point.getX();
	    double dy = y1 - point.getY();
	    double theta = Math.atan2(-dy,dx);
		addHead(arrowPoints[1], theta, length/20, weight);
	}
	
	private void addHead(LatLng point, double theta, double headLength, int weight) {
		logger.fine("In addHead");
		//add an arrow head at the specified point
	    double t = theta + (Math.PI/4) ;
	    if(t > Math.PI)
	        t -= 2*Math.PI;
	    double t2 = theta - (Math.PI/4) ;
	    if(t2 <= (-Math.PI))
	        t2 += 2*Math.PI;
	    LatLng[] pts = new LatLng[3];
	    Point ppoint = mapw.convertLatLngToContainerPixel(point);
	    int x = ppoint.getX();
	    int y = ppoint.getY();
	    int x1 = (int) Math.round(x-Math.cos(t)*headLength);
	    int y1 = (int) Math.round(y+Math.sin(t)*headLength);
	    int x2 = (int) Math.round(x-Math.cos(t2)*headLength);
	    int y2 = (int) Math.round(y+Math.sin(t2)*headLength);
	    pts[0] = mapw.convertContainerPixelToLatLng(Point.newInstance(x1, y1));
	    pts[1] = mapw.convertContainerPixelToLatLng(Point.newInstance(x, y));
	    pts[2] = mapw.convertContainerPixelToLatLng(Point.newInstance(x2, y2));
	    
	    Polyline polyline = new Polyline(pts, "Red", weight);
	    mapw.addOverlay(polyline);
	}
}
