package com.sap.sailing.simulator.test.util;

import java.util.List;

import com.sap.sailing.domain.tracking.TrackedRace;

public interface TracTracReader {
	public List<TrackedRace> read() throws Exception;
}
