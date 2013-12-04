package com.sap.sailing.domain.igtimiadapter.test;

import org.junit.Test;

import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.Wind;

/**
 * Igtimi fixes come as isolated single fixes from separate sensors, and even if they are attached to the same device,
 * their time stamps may not be synchronized. Therefore, separate {@link Track} tracks will be used to hold the data
 * coming from the different sensors attached to the same device, and several of them need to be joined to allow for the
 * construction, e.g., of a single {@link Wind} of {@link GPSFixMoving} fix.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class IgtimiFixTrackTest {
    @Test
    public void testFetchFixesIntoTracks() {
    }
}
