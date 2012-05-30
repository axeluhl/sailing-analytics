package com.sap.sailing.simulator.impl;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.simulator.WindLattice;

public class WindLatticeImpl implements WindLattice {
	
	public class LatticePoint extends DegreePosition {
		public LatticePoint(double lat, double lng) {
			super(lat, lng);
		}
		public LatticePoint previous;
		public LatticePoint left;
		public LatticePoint right;
	}
	
	public LatticePoint[][] points;

	@Override
	public Position[][] getPoints() {

		return points;
	}

}
