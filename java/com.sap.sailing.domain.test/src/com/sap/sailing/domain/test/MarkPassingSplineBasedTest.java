package com.sap.sailing.domain.test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;

/**
 * This class tests a mark passing detection algorithm that interpolates the boats' positions between
 * their GPS fixes.
 * @author Martin Hanysz
 *
 */
public class MarkPassingSplineBasedTest extends AbstractMarkPassingTest {

	public MarkPassingSplineBasedTest() throws MalformedURLException, URISyntaxException {
		super();
	}

	/* (non-Javadoc)
	 * @see com.sap.sailing.domain.test.AbstractMarkPassingTest#computeMarkPassings()
	 */
	@Override
	Map<Competitor, Map<Waypoint, List<List<MarkPassing>>>> computeMarkPassings() {
		Map<Competitor, Map<Waypoint, List<List<MarkPassing>>>> markPassings = new HashMap<>();
		
		
		
		return markPassings;
	}

}
