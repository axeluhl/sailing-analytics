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
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.SimulatorService;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;
import com.sap.sailing.gwt.ui.shared.PositionDTO;

public class SimulatorEntryPoint implements EntryPoint {
	private final SimulatorServiceAsync simulatorSvc = GWT.create(SimulatorService.class);

	private final VerticalPanel 	panel			= new VerticalPanel();
	private final ListBox 			raceSelector	= new ListBox();
	
	private MapWidget mapw;
	private Polyline []	windlattice					= new Polyline[0];
	private List<PositionDTO> locations				= new ArrayList<PositionDTO>();
	
	@Override
	public void onModuleLoad() {
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
		loadRaceLocations();
		
		initMap();
		initRaceSelector();
		initPanel();

		// Add the map to the HTML host page
		RootLayoutPanel.get().add(panel);
	}
	
	private void initMap() {
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
		panel.add(raceSelector);
		panel.add(mapw);
		panel.setSize("100%", "100%");
	}

	private void selectRaceLocation(PositionDTO rl) {
		mapw.clearOverlays();
		LatLng position = LatLng.newInstance(rl.latDeg, rl.lngDeg);
		mapw.panTo(position);
	}

	private void generateWindLattice(LatLng center) {
		WindLatticeGenParamsDTO params = new WindLatticeGenParamsDTO();
		params.setCenter(new PositionDTO(center.getLatitude(), center.getLongitude()));
		params.setGridsizeX(25);
		params.setGridsizeY(25);
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
		PositionDTO [][] matrix = wl.getMatrix();
		int numRows = matrix.length;
		int numCols = matrix[0].length;
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
	}
}
