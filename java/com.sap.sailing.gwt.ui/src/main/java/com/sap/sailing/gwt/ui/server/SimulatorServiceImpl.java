package com.sap.sailing.gwt.ui.server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.gwt.ui.client.SimulatorService;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO.WindPattern;
import com.sap.sailing.gwt.ui.shared.WindLatticeDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeGenParamsDTO;
import com.sap.sailing.simulator.WindField;
import com.sap.sailing.simulator.impl.RectangularBoundary;
import com.sap.sailing.simulator.impl.TimedPositionWithSpeedSimple;
import com.sap.sailing.simulator.impl.WindFieldImpl;

public class SimulatorServiceImpl extends RemoteServiceServlet  implements SimulatorService {

	/**
	 * Generated uid serial version
	 */
	private static final long serialVersionUID = 4445427185387524086L;

	@Override
	public PositionDTO [] getRaceLocations() {
		PositionDTO lakeGarda = new PositionDTO();
		lakeGarda.latDeg = 45.57055337226086;
		lakeGarda.lngDeg = 10.693345069885254;
		
		PositionDTO lakeGeneva = new PositionDTO();
		lakeGeneva.latDeg = 46.23376539670794;
		lakeGeneva.lngDeg = 6.168651580810547;
		
		PositionDTO kiel = new PositionDTO();
		kiel.latDeg = 54.3232927;
		kiel.lngDeg = 10.122765200000003;
		
		return new PositionDTO[]{kiel, lakeGeneva, lakeGarda};
	}

	public  WindLatticeDTO getWindLatice(WindLatticeGenParamsDTO params) {
		final Bearing north = new DegreeBearingImpl(0);
		final Bearing east = new DegreeBearingImpl(90);
		final Bearing south = new DegreeBearingImpl(180);
		final Bearing west = new DegreeBearingImpl(270);
		
		final double xSize	= params.getxSize();
		final double ySize	= params.getySize();
		final int gridsizeX	= params.getGridsizeX();
		final int gridsizeY	= params.getGridsizeY();
		
		Position center = new DegreePosition(params.getCenter().latDeg, params.getCenter().lngDeg);
		
		WindLatticeDTO wl = new WindLatticeDTO();
		PositionDTO [][] matrix = new PositionDTO[gridsizeY][gridsizeX];

		Distance deastwest		= new NauticalMileDistance((gridsizeX-1.) / (2*gridsizeX) * xSize);
		Distance dnorthsouth	= new NauticalMileDistance((gridsizeY-1.) / (2*gridsizeY) * ySize);
		Position start = center.translateGreatCircle(south, dnorthsouth).translateGreatCircle(west, deastwest);

		deastwest	= new NauticalMileDistance(xSize / gridsizeX);
		dnorthsouth	= new NauticalMileDistance(ySize / gridsizeY);
		
		Position rowStart = null, crt = null;
		for( int i = 0; i < gridsizeY; i++ ) {
			if( i == 0 ) {
				rowStart = start;
			} else {
				rowStart = rowStart.translateGreatCircle(north, dnorthsouth);
			}
				
			for( int j = 0; j < gridsizeX; j++) {
				if( j == 0 ) {
					crt = rowStart;
				} else {
					crt = crt.translateGreatCircle(east, deastwest);
					if( (i == 3) && (j == 5) ) {
						crt = crt.translateGreatCircle(north, new NauticalMileDistance(ySize / gridsizeY * Math.random()));
						crt = crt.translateGreatCircle(east, new NauticalMileDistance(xSize / gridsizeX * Math.random()));
						crt = crt.translateGreatCircle(south, new NauticalMileDistance(ySize / gridsizeY * Math.random()));
						crt = crt.translateGreatCircle(west, new NauticalMileDistance(xSize / gridsizeX * Math.random()));
					}
				}
				
				PositionDTO pdto = new PositionDTO(crt.getLatDeg(), crt.getLngDeg());
				matrix[i][j] = pdto;
			}
			
		}
		
		wl.setMatrix(matrix);
		
		return wl;
	}


	public WindFieldDTO getWindField(WindFieldGenParamsDTO params){
		
		
		Position nw = new DegreePosition(params.getNorthWest().latDeg, params.getNorthWest().lngDeg);
		Position se = new DegreePosition(params.getSouthEast().latDeg, params.getSouthEast().lngDeg);
		RectangularBoundary bd = new RectangularBoundary(nw,se);
		List<Position> lattice = bd.extractLattice(5, 5);
		
		//TODO remove this, only placed so that we can display some points
		if (lattice == null) {
		    lattice = new LinkedList<Position>();
		    lattice.add(nw);
		    lattice.add(se);
		}
		
		WindField wf = new WindFieldImpl(bd, params.getWindSpeed(), params.getWindBearing());
		List<WindDTO> wList = new ArrayList<WindDTO>();
		
		if (lattice != null) {
		    for (Position p : lattice) {			
			Wind localWind = wf.getWind(new TimedPositionWithSpeedSimple(p));
			WindDTO w = new WindDTO();
			w.position = new PositionDTO(localWind.getPosition().getLatDeg(),localWind.getPosition().getLngDeg());
			w.trueWindBearingDeg = localWind.getBearing().getDegrees();
			w.trueWindSpeedInMetersPerSecond = localWind.getMetersPerSecond();
			wList.add(w);	
		    }
		}
		WindFieldDTO wfDTO = new WindFieldDTO();
		wfDTO.setMatrix(wList);
		return wfDTO;
		
	}
	
	public WindPattern[] getWindPatterns() {
	   return WindFieldGenParamsDTO.WindPattern.values();
	}
	
	public BoatClassDTO[] getBoatClasses() {
	    BoatClassDTO boatClassDTO = new BoatClassDTO("49er");
	    
	    return new BoatClassDTO[]{boatClassDTO};
	    
	}
}
