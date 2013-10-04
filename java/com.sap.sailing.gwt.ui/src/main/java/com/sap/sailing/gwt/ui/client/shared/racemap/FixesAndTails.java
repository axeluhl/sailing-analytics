package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.overlays.Polyline;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;

/**
 * Manages the cache of {@link GPSFixDTO}s for the competitors and the polylines encoding the tails that visualize the
 * course the boats took. The tails are based on the GPS fix data. This class offers methods to update the fixes and
 * the tails, making sure that the data is always managed consistently. In particular, it keeps an eye on
 * {@link GPSFixDTO#extrapolated extrapolated fixes}. Those are just a guess where a boat may have been and will need
 * to be removed once actual data for that time is available.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class FixesAndTails {
    /**
     * Fixes of each competitors tail. If a list is contained for a competitor, the list contains a timely "contiguous"
     * list of fixes for the competitor. This means the server has no more data for the time interval covered, unless
     * the last fix was {@link GPSFixDTO#extrapolated obtained by extrapolation}.
     */
    private final Map<CompetitorDTO, List<GPSFixDTO>> fixes;
    
    /**
     * Tails of competitors currently displayed as overlays on the map.
     */
    private final Map<CompetitorDTO, Polyline> tails;


    /**
     * Key set is equal to that of {@link #tails} and tells what the index in in {@link #fixes} of the first fix shown
     * in {@link #tails} is. If a key is contained in this map, it is also contained in {@link #lastShownFix} and vice
     * versa.
     */
    private final Map<CompetitorDTO, Integer> firstShownFix;

    /**
     * Key set is equal to that of {@link #tails} and tells what the index in in {@link #fixes} of the last fix shown in
     * {@link #tails} is. If a key is contained in this map, it is also contained in {@link #firstShownFix} and vice
     * versa.
     */
    private final Map<CompetitorDTO, Integer> lastShownFix;

    public FixesAndTails() {
        fixes = new HashMap<CompetitorDTO, List<GPSFixDTO>>();
        tails = new HashMap<CompetitorDTO, Polyline>();
        firstShownFix = new HashMap<CompetitorDTO, Integer>();
        lastShownFix = new HashMap<CompetitorDTO, Integer>();
    }

    public List<GPSFixDTO> get(CompetitorDTO competitor) {
        return fixes.get(competitor);
    }

    /**
     * Creates a polyline for the competitor represented by <code>competitorDTO</code>, taking the fixes from
     * {@link #fixes fixes.get(competitorDTO)} and using the fixes starting at time point <code>from</code> (inclusive)
     * up to the last fix with time point before <code>to</code>. The polyline is returned. Updates are applied to
     * {@link #lastShownFix}, {@link #firstShownFix} and {@link #tails}.
     */
    protected Polyline createTailAndUpdateIndices(final CompetitorDTO competitorDTO, Date from, Date to, TailFactory tailFactory) {
        List<LatLng> points = new ArrayList<LatLng>();
        List<GPSFixDTO> fixesForCompetitor = fixes.get(competitorDTO);
        int indexOfFirst = -1;
        int indexOfLast = -1;
        int i = 0;
        for (Iterator<GPSFixDTO> fixIter = fixesForCompetitor.iterator(); fixIter.hasNext() && indexOfLast == -1;) {
            GPSFixDTO fix = fixIter.next();
            if (!fix.timepoint.before(to)) {
                indexOfLast = i-1;
            } else {
                LatLng point = null;
                if (indexOfFirst == -1) {
                    if (!fix.timepoint.before(from)) {
                        indexOfFirst = i;
                        point = LatLng.newInstance(fix.position.latDeg, fix.position.lngDeg);
                    }
                } else {
                    point = LatLng.newInstance(fix.position.latDeg, fix.position.lngDeg);
                }
                if (point != null) {
                    points.add(point);
                }
            }
            i++;
        }
        if (indexOfLast == -1) {
            indexOfLast = i - 1;
        }
        if (indexOfFirst != -1 && indexOfLast != -1) {
            firstShownFix.put(competitorDTO, indexOfFirst);
            lastShownFix.put(competitorDTO, indexOfLast);
        }
        final Polyline result = tailFactory.createTail(competitorDTO, points);
        tails.put(competitorDTO, result);
        return result;
    }


}
