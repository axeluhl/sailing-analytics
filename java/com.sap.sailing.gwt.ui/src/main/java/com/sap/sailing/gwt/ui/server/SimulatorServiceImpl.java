package com.sap.sailing.gwt.ui.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.gwt.ui.client.SimulatorService;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.simulator.WindLatticeDTO;
import com.sap.sailing.gwt.ui.simulator.WindLatticeGenParamsDTO;

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
		
		return new PositionDTO[]{lakeGeneva, lakeGarda};
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
		PositionDTO [][] matrix = new PositionDTO[gridsizeX][gridsizeY];

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
}
