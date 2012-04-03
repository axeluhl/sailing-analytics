package com.sap.sailing.simulator;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.simulator.BoundariesIterator;
import java.util.Collections;
import java.util.List;

public interface Boundaries {
	final static Boundaries UNBOUNDED = new Boundaries() {
		
		public List<Position> getCorners() {
			return Collections.emptyList();
		}
		
		public boolean isWithinBoundaries(Position P) {
			return true;
		}
		
		public boolean isOnBoundaries(Position P) {
			return false;
		}
		
		public boolean isBetweenBoundaries(Position P) {
			return true;
		}
		
	};
	
	List<Position> getCorners();
	
	boolean isWithinBoundaries(Position P);
	boolean isOnBoundaries(Position P);
	boolean isBetweenBoundaries(Position P);

}
