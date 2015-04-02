package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;

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
     * <p>
     * 
     * If the fixes for a competitor contain an {@link GPSFixDTO#extrapolated extrapolated} fix, that fix is always
     * guaranteed to be the last element of the list when outside the execution of a method on this class. This in
     * particular means that when more fixes are added, and there is now one fix later than the extrapolated fix, the
     * extrapolated fix will be removed, re-establishing the invariant of an extrapolated fix always being the last
     * in the list.
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

    /**
     * @return the list of fixes cached for the competitor; if a fix is {@link GPSFixDTO#extrapolated extrapolated}, it
     *         must be the last fix in the list. <code>null</code> may be returned in no fixes are cached for
     *         <code>competitor</code>. The list returned is unmodifiable for the caller.
     */
    public List<GPSFixDTO> getFixes(CompetitorDTO competitor) {
        final List<GPSFixDTO> competitorFixes = fixes.get(competitor);
        return competitorFixes == null ? null : Collections.unmodifiableList(competitorFixes);
    }
    
    public Polyline getTail(CompetitorDTO competitor) {
        return tails.get(competitor);
    }

    public int getFirstShownFix(CompetitorDTO competitor) {
        return firstShownFix.get(competitor);
    }
    
    /**
     * The set of all competitors for which this object maintains tails. The collection is unmodifiable for the caller.
     */
    public Set<CompetitorDTO> getCompetitorsWithTails() {
        return Collections.unmodifiableSet(tails.keySet());
    }

    public boolean hasFixesFor(CompetitorDTO competitor) {
        return fixes.containsKey(competitor);
    }
    
    /**
     * When for a competitor the last fix obtained from the server is {@link GPSFixDTO#extrapolated extrapolated}, the quality
     * of this fix depends on the time difference between the extrapolated fix's time point and the last non-extrapolated
     * fix's time point. This time difference in milliseconds is returned by this method, or <code>0</code> in case
     * the last fix for <code>competitor</code> is not extrapolated.
     */
    protected long getMillisecondsBetweenExtrapolatedAndLastNonExtrapolatedFix(CompetitorDTO competitor) {
        List<GPSFixDTO> competitorFixes = getFixes(competitor);
        final long result;
        if (competitorFixes == null || competitorFixes.size() < 2 || !competitorFixes.get(competitorFixes.size()-1).extrapolated) {
            result = 0;
        } else {
            // last fix is extrapolated and another fix present; check time difference
            GPSFixDTO extrapolatedFix = competitorFixes.get(competitorFixes.size()-1);
            GPSFixDTO fixBeforeExtrapolated = competitorFixes.get(competitorFixes.size()-2);
            result = extrapolatedFix.timepoint.getTime() - fixBeforeExtrapolated.timepoint.getTime();
        }
        return result;
    }

    /**
     * Creates a polyline for the competitor represented by <code>competitorDTO</code>, taking the fixes from
     * {@link #fixes fixes.get(competitorDTO)} and using the fixes starting at time point <code>from</code> (inclusive)
     * up to the last fix with time point before <code>to</code>. The polyline is returned. Updates are applied to
     * {@link #lastShownFix}, {@link #firstShownFix} and {@link #tails}.
     */
    protected Polyline createTailAndUpdateIndices(final CompetitorDTO competitorDTO, Date from, Date to, TailFactory tailFactory) {
        List<LatLng> points = new ArrayList<LatLng>();
        List<GPSFixDTO> fixesForCompetitor = getFixes(competitorDTO);
        int indexOfFirst = -1;
        int indexOfLast = -1;
        int i = 0;
        // TODO consider binary search to find beginning of interesting segment faster
        for (Iterator<GPSFixDTO> fixIter = fixesForCompetitor.iterator(); fixIter.hasNext() && indexOfLast == -1;) {
            GPSFixDTO fix = fixIter.next();
            if (!fix.timepoint.before(to)) {
                indexOfLast = i-1;
            } else {
                LatLng point = null;
                if (indexOfFirst == -1) {
                    if (!fix.timepoint.before(from)) {
                        indexOfFirst = i;
                        point = LatLng.newInstance(fix.position.getLatDeg(), fix.position.getLngDeg());
                    }
                } else {
                    point = LatLng.newInstance(fix.position.getLatDeg(), fix.position.getLngDeg());
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

    /**
     * Adds the fixes received in <code>result</code> to {@link #fixes} and ensures they are still contiguous for each
     * competitor. If <code>overlapsWithKnownFixes</code> indicates that the fixes received in <code>result</code>
     * overlap with those already known, the fixes are merged into the list of already known fixes for the competitor.
     * Otherwise, the fixes received in <code>result</code> replace those known so far for the respective competitor.
     * The {@link #tails} affected by these fixes are updated accordingly when modifications fall inside the interval
     * shown by the tail, as defined by {@link #firstShownFix} and {@link #lastShownFix}. The tails are, however,
     * not trimmed according to the specification for the tail length. This has to happen elsewhere (see also
     * {@link #updateTail}).
     * 
     * @param fixesForCompetitors
     *            For each list the invariant must hold that an {@link GPSFixDTO#extrapolated extrapolated} fix must be
     *            the last one in the list
     */
    protected void updateFixes(Map<CompetitorDTO, List<GPSFixDTO>> fixesForCompetitors,
            Map<CompetitorDTO, Boolean> overlapsWithKnownFixes, TailFactory tailFactory, long timeForPositionTransitionMillis) {
        for (Map.Entry<CompetitorDTO, List<GPSFixDTO>> e : fixesForCompetitors.entrySet()) {
            if (e.getValue() != null && !e.getValue().isEmpty()) {
                List<GPSFixDTO> fixesForCompetitor = fixes.get(e.getKey());
                if (fixesForCompetitor == null) {
                    fixesForCompetitor = new ArrayList<GPSFixDTO>();
                    fixes.put(e.getKey(), fixesForCompetitor);
                }
                if (!overlapsWithKnownFixes.get(e.getKey())) {
                    // clearing and then re-populating establishes the invariant that an extrapolated fix must be the last
                    fixesForCompetitor.clear();
                    // to re-establish the invariants for tails, firstShownFix and lastShownFix, we now need to remove
                    // all points from the competitor's polyline and clear the entries in firstShownFix and lastShownFix
                    if (tails.containsKey(e.getKey())) {
                        Polyline removedTail = tails.remove(e.getKey());
                        removedTail.setMap(null);
                    }
                    firstShownFix.remove(e.getKey());
                    lastShownFix.remove(e.getKey());
                    fixesForCompetitor.addAll(e.getValue());
                } else {
                    mergeFixes(e.getKey(), e.getValue(), timeForPositionTransitionMillis);
                }
            }
        }
    }

    /**
     * While updating the {@link #fixes} for <code>competitorDTO</code>, the invariants for {@link #tails} and
     * {@link #firstShownFix} and {@link #lastShownFix} are maintained: each time a fix is inserted and we have a tail
     * in {@link #tails} for <code>competitorDTO</code>, the {@link #firstShownFix} record for
     * <code>competitorDTO</code> is incremented if it is greater than the insertion index, and the
     * {@link #lastShownFix} records for <code>competitorDTO</code> is incremented if is is greater than or equal to the
     * insertion index. This means, in particular, that when a fix is inserted exactly at the index that points to the
     * first fix shown so far, the fix inserted will become the new first fix shown. When inserting a fix exactly at
     * index {@link #lastShownFix}, the fix that so far was the last one shown remains the last one shown because in
     * this case, {@link #lastShownFix} will be incremented by one. If {@link #firstShownFix} &lt;=
     * <code>insertindex</code> &lt;= {@link #lastShownFix}, meaning that the fix is in the range of fixes shown in the
     * competitor's tail, the tail is adjusted by inserting the corresponding fix.
     * <p>
     * 
     * If the last fix so far was an {@link GPSFixDTO#extrapolated extrapolated} fix, and the merge leads to a different
     * fix being the last one shown, the previously last fix that was extrapolated will be removed. This way, at most
     * one extrapolated fix is shown, avoiding jitter on the map as actual fixes are received that obsolete the
     * extrapolated ones.<p>
     * 
     * Precondition: {@link #hasFixesFor(CompetitorDTO) hasFixesFor(competitorDTO)}<code>==true</code>
     * @param mergeThis
     *            If this list contains an {@link GPSFixDTO#extrapolated extrapolated} fix, that fix must be the last in
     *            the list
     */
    private void mergeFixes(CompetitorDTO competitorDTO, List<GPSFixDTO> mergeThis, final long timeForPositionTransitionMillis) {
        List<GPSFixDTO> intoThis = fixes.get(competitorDTO);
        int indexOfFirstShownFix = firstShownFix.get(competitorDTO) == null ? -1 : firstShownFix.get(competitorDTO);
        int indexOfLastShownFix = lastShownFix.get(competitorDTO) == null ? -1 : lastShownFix.get(competitorDTO);
        final Polyline tail = getTail(competitorDTO);
        int intoThisIndex = 0;
        for (GPSFixDTO mergeThisFix : mergeThis) {
            intoThisIndex = Collections.binarySearch(intoThis, mergeThisFix, new Comparator<GPSFixDTO>() {
                @Override
                public int compare(GPSFixDTO o1, GPSFixDTO o2) {
                    return o1.timepoint.compareTo(o2.timepoint);
                }
            });
            if (intoThisIndex < 0) {
                intoThisIndex = -intoThisIndex-1;
            }
            if (intoThisIndex < intoThis.size() && intoThis.get(intoThisIndex).timepoint.equals(mergeThisFix.timepoint)) {
                // exactly same time point; replace with fix from mergeThis unless the new fix is extrapolated and there is a later fix in intoThis;
                // in the (unlikely) case the existing non-extrapolated fix is replaced by an extrapolated one, the indices of the shown fixes
                // need according adjustments
                if (!mergeThisFix.extrapolated || intoThis.size() == intoThisIndex+1) {
                    intoThis.set(intoThisIndex, mergeThisFix);
                    if (tail != null && intoThisIndex >= indexOfFirstShownFix && intoThisIndex <= indexOfLastShownFix) {
                        tail.getPath().setAt(intoThisIndex - indexOfFirstShownFix, LatLng.newInstance(mergeThisFix.position.getLatDeg(), mergeThisFix.position.getLngDeg()));
                    }
                } else {
                    // extrapolated fix would be added one or more positions before the last fix in intoThis; instead,
                    // remove the fix at the respective index with the same time point and adjust indices:
                    intoThis.remove(intoThisIndex);
                    if (tail != null && intoThisIndex >= indexOfFirstShownFix && intoThisIndex <= indexOfLastShownFix) {
                        final int finalIntoThisIndex = intoThisIndex;
                        final int finalIndexOfFirstShownFix = indexOfFirstShownFix;
                        Timer timer = new Timer() {
                            @Override
                            public void run() {
                                tail.getPath().removeAt(finalIntoThisIndex - finalIndexOfFirstShownFix);
                            }
                        };
                        runDelayedOrImmediately(timer, (int) (timeForPositionTransitionMillis==-1?-1:timeForPositionTransitionMillis/2));
                    }
                    if (intoThisIndex < indexOfFirstShownFix) {
                        indexOfFirstShownFix--;
                    }
                    if (intoThisIndex <= indexOfLastShownFix) {
                        indexOfLastShownFix--;
                    }
                    intoThisIndex--;
                }
            } else {
                intoThis.add(intoThisIndex, mergeThisFix);
                if (tail != null && intoThisIndex >= indexOfFirstShownFix && intoThisIndex <= indexOfLastShownFix) {
                    tail.getPath().insertAt(intoThisIndex - indexOfFirstShownFix,
                            LatLng.newInstance(mergeThisFix.position.getLatDeg(), mergeThisFix.position.getLngDeg()));
                }
                if (intoThisIndex < indexOfFirstShownFix) {
                    indexOfFirstShownFix++;
                }
                if (intoThisIndex <= indexOfLastShownFix) {
                    indexOfLastShownFix++;
                }
                // If there is a fix prior to the one added and that prior fix was obtained by extrapolation, remove it now because
                // extrapolated fixes can only be the last in the list
                if (intoThisIndex > 0 && intoThis.get(intoThisIndex-1).extrapolated) {
                    intoThis.remove(intoThisIndex-1);
                    if (tail != null && intoThisIndex-1 >= indexOfFirstShownFix && intoThisIndex-1 <= indexOfLastShownFix) {
                        final int finalIntoThisIndex = intoThisIndex;
                        final int finalIndexOfFirstShownFix = indexOfFirstShownFix;
                        Timer timer = new Timer() {
                            @Override
                            public void run() {
                                tail.getPath().removeAt(finalIntoThisIndex-1 - finalIndexOfFirstShownFix);
                            }
                        };
                        runDelayedOrImmediately(timer, (int) (timeForPositionTransitionMillis==-1?-1:timeForPositionTransitionMillis/2));
                    }
                    if (intoThisIndex-1 < indexOfFirstShownFix) {
                        indexOfFirstShownFix--;
                    }
                    if (intoThisIndex-1 <= indexOfLastShownFix) {
                        indexOfLastShownFix--;
                    }
                    intoThisIndex--;
                }
            }
            intoThisIndex++;
        }
        // invariant: for one CompetitorDTO, either both of firstShownFix and lastShownFix have an entry for that key,
        // or both don't
        if (indexOfFirstShownFix != -1) {
            firstShownFix.put(competitorDTO, indexOfFirstShownFix);
        }
        if (indexOfLastShownFix != -1) {
            lastShownFix.put(competitorDTO, indexOfLastShownFix);
        }
    }

    /**
     * If the tail starts before <code>from</code>, removes leading vertices from <code>tail</code> that are before
     * <code>from</code>. This is determined by using the {@link #firstShownFix} index which tells us where in
     * {@link #fixes} we find the sequence of fixes currently represented in the tail.
     * <p>
     * 
     * If the tail starts after <code>from</code>, vertices for those {@link #fixes} for <code>competitorDTO</code> at
     * or after time point <code>from</code> and before the time point of the first fix displayed so far in the tail and
     * before <code>to</code> are prepended to the tail.
     * <p>
     * 
     * Now to the end of the tail: if the existing tail's end exceeds <code>to</code>, the vertices in excess are
     * removed (aided by {@link #lastShownFix}). Otherwise, for the competitor's fixes starting at the tail's end up to
     * <code>to</code> are appended to the tail.
     * <p>
     * 
     * When this method returns, {@link #firstShownFix} and {@link #lastShownFix} have been updated accordingly.
     * 
     * @param delayForTailChangeInMillis
     *            the time in milliseconds after which to actually draw the tail update, or <code>-1</code> to perform
     *            the update immediately
     */
    protected void updateTail(final Polyline tail, final  CompetitorDTO competitorDTO,
            final Date from, final Date to, final int delayForTailChangeInMillis) {
        Timer delayedOrImmediateExecutor = new Timer() {
            @Override
            public void run() {
                int vertexCount = tail.getPath().getLength();
                final List<GPSFixDTO> fixesForCompetitor = getFixes(competitorDTO);
                int indexOfFirstShownFix = firstShownFix.get(competitorDTO) == null ? -1 : firstShownFix
                        .get(competitorDTO);
                // remove fixes before what is now to be the beginning of the polyline:
                while (indexOfFirstShownFix != -1 && vertexCount > 0
                        && fixesForCompetitor.get(indexOfFirstShownFix).timepoint.before(from)) {
                    tail.getPath().removeAt(0);
                    vertexCount--;
                    indexOfFirstShownFix++;
                }
                // now the polyline contains no more vertices representing fixes before "from";
                // go back in time starting at indexOfFirstShownFix while the fixes are still at or after "from"
                // and insert corresponding vertices into the polyline
                while (indexOfFirstShownFix > 0
                        && !fixesForCompetitor.get(indexOfFirstShownFix - 1).timepoint.before(from)) {
                    indexOfFirstShownFix--;
                    GPSFixDTO fix = fixesForCompetitor.get(indexOfFirstShownFix);
                    tail.getPath().insertAt(0, LatLng.newInstance(fix.position.getLatDeg(), fix.position.getLngDeg()));
                    vertexCount++;
                }
                // now adjust the polylines tail: remove excess vertices that are after "to"
                int indexOfLastShownFix = lastShownFix.get(competitorDTO) == null ? -1 : lastShownFix
                        .get(competitorDTO);
                while (indexOfLastShownFix != -1 && vertexCount > 0
                        && fixesForCompetitor.get(indexOfLastShownFix).timepoint.after(to)) {
                    if (vertexCount-1 == 0 || (indexOfLastShownFix-1 >= 0 && !fixesForCompetitor.get(indexOfLastShownFix-1).timepoint.after(to))) {
                        // the loop will abort after this iteration
                    }
                    tail.getPath().removeAt(--vertexCount);
                    indexOfLastShownFix--;
                }
                // now the polyline contains no more vertices representing fixes after "to";
                // go forward in time starting at indexOfLastShownFix while the fixes are still at or before "to"
                // and insert corresponding vertices into the polyline
                while (indexOfLastShownFix < fixesForCompetitor.size() - 1
                        && !fixesForCompetitor.get(indexOfLastShownFix + 1).timepoint.after(to)) {
                    indexOfLastShownFix++;
                    GPSFixDTO fix = fixesForCompetitor.get(indexOfLastShownFix);
                    tail.getPath().insertAt(vertexCount++, LatLng.newInstance(fix.position.getLatDeg(), fix.position.getLngDeg()));
                }
                firstShownFix.put(competitorDTO, indexOfFirstShownFix);
                lastShownFix.put(competitorDTO, indexOfLastShownFix);
            }
        };
        runDelayedOrImmediately(delayedOrImmediateExecutor, delayForTailChangeInMillis);
    }

    private void runDelayedOrImmediately(Timer runThis, final int delayForTailChangeInMillis) {
        if (delayForTailChangeInMillis == -1) {
            runThis.run();
        } else {
            runThis.schedule(delayForTailChangeInMillis);
        }
    }

    /**
     * Consistently removes the <code>competitor</code>'s tail from {@link #tails} and from the map, and the corresponding position
     * data from {@link #firstShownFix} and {@link #lastShownFix}.
     */
    protected void removeTail(CompetitorDTO competitor) {
        Polyline removedTail = tails.remove(competitor);
        if (removedTail != null) {
            removedTail.setMap(null);
        }
        firstShownFix.remove(competitor);
        lastShownFix.remove(competitor);
    }

    /**
     * From {@link #fixes} as well as the selection of {@link #getCompetitorsToShow competitors to show}, computes the
     * from/to times for which to request GPS fixes from the server. No update is performed here to {@link #fixes}. The
     * result guarantees that, when used in
     * {@link SailingServiceAsync#getBoatPositions(String, String, Map, Map, boolean, AsyncCallback)}, for each
     * competitor from {@link #competitorsToShow} there are all fixes known by the server for that competitor starting
     * at <code>upTo-{@link #tailLengthInMilliSeconds}</code> and ending at <code>upTo</code> (exclusive).
     * @param effectiveTailLengthInMilliseconds 
     * 
     * @return a triple whose {@link Triple#getA() first} component contains the "from", and whose {@link Triple#getB()
     *         second} component contains the "to" times for the competitors whose trails / positions to show; the
     *         {@link Triple#getC() third} component tells whether the existing fixes can remain and be augmented by
     *         those requested or need to be replaced
     */
    protected Util.Triple<Map<CompetitorDTO, Date>, Map<CompetitorDTO, Date>, Map<CompetitorDTO, Boolean>> computeFromAndTo(
            Date upTo, Iterable<CompetitorDTO> competitorsToShow, long effectiveTailLengthInMilliseconds) {
        Date tailstart = new Date(upTo.getTime() - effectiveTailLengthInMilliseconds);
        Map<CompetitorDTO, Date> from = new HashMap<CompetitorDTO, Date>();
        Map<CompetitorDTO, Date> to = new HashMap<CompetitorDTO, Date>();
        Map<CompetitorDTO, Boolean> overlapWithKnownFixes = new HashMap<CompetitorDTO, Boolean>();
        
        for (CompetitorDTO competitor : competitorsToShow) {
            List<GPSFixDTO> fixesForCompetitor = getFixes(competitor);
            Date fromDate;
            Date toDate;
            Date timepointOfLastKnownFix = fixesForCompetitor == null ? null : getTimepointOfLastNonExtrapolated(fixesForCompetitor);
            Date timepointOfFirstKnownFix = fixesForCompetitor == null ? null : getTimepointOfFirstNonExtrapolated(fixesForCompetitor);
            boolean overlap = false;
            if (fixesForCompetitor != null && timepointOfFirstKnownFix != null
                    && !tailstart.before(timepointOfFirstKnownFix) && timepointOfLastKnownFix != null
                    && !tailstart.after(timepointOfLastKnownFix)) {
                // the beginning of what we need is contained in the interval we already have; skip what we already have
                fromDate = new Date(timepointOfLastKnownFix.getTime()+1l); // "from" is "inclusive", so add 1ms to also skip the last fix we have
                overlap = true;
            } else {
                fromDate = tailstart;
            }
            if (fixesForCompetitor != null && timepointOfFirstKnownFix != null
                    && !upTo.before(timepointOfFirstKnownFix) && timepointOfLastKnownFix != null
                    && !upTo.after(timepointOfLastKnownFix)) {
                // the end of what we need is contained in the interval we already have; skip what we already have
                toDate = timepointOfFirstKnownFix;
                overlap = true;
            } else {
                toDate = upTo;
            }
            // only request something for the competitor if we're missing information at all
            if (fromDate.before(toDate) || fromDate.equals(toDate)) {
                from.put(competitor, fromDate);
                to.put(competitor, toDate);
                overlapWithKnownFixes.put(competitor, overlap);
            }
        }
        return new Util.Triple<Map<CompetitorDTO, Date>, Map<CompetitorDTO, Date>, Map<CompetitorDTO, Boolean>>(from, to,
                overlapWithKnownFixes);
    }

    private Date getTimepointOfFirstNonExtrapolated(List<GPSFixDTO> fixesForCompetitor) {
        for (GPSFixDTO fix : fixesForCompetitor) {
            if (!fix.extrapolated) {
                return fix.timepoint;
            }
        }
        return null;
    }

    private Date getTimepointOfLastNonExtrapolated(List<GPSFixDTO> fixesForCompetitor) {
        if (!fixesForCompetitor.isEmpty()) {
            for (ListIterator<GPSFixDTO> fixIter = fixesForCompetitor.listIterator(fixesForCompetitor.size() - 1); fixIter
                    .hasPrevious();) {
                GPSFixDTO fix = fixIter.previous();
                if (!fix.extrapolated) {
                    return fix.timepoint;
                }
            }
        }
        return null;
    }
}
