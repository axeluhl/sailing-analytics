package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;

/**
 * A named area in a sailing {@link Venue} where races take place. Usually, the areas carry names according to the NATO
 * alphabet, such as "Alpha", "Bravo" or "Charlie".
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface CourseArea extends Named {

	void addRace(RaceDefinition race);
	
	Iterable<RaceDefinition> getRaces();
	
}
