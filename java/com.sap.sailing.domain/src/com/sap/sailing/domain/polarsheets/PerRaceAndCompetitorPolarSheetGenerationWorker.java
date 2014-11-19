package com.sap.sailing.domain.polarsheets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * Iterates through the fixes of one competitor in one tracked race and fills the {@link PolarSheetGenerationWorker}
 * with datapoints that are found for the speed of the boat and its angle to the wind
 * 
 * @author D054528 Frederik Petersen
 * 
 */
public class PerRaceAndCompetitorPolarSheetGenerationWorker implements Runnable {
    
    // private static final Logger logger = Logger.getLogger(PerRaceAndCompetitorPolarSheetGenerationWorker.class.getName());

    private final TrackedRace race;

    private final PolarSheetGenerationWorker polarSheetGenerationWorker;

    private TimePoint startTime;

    private TimePoint endTime;

    private final Competitor competitor;
    
    private boolean noConfidence = false;

    private boolean done = false;
    
    private int finishedEarlyAtWaypoint = -1;

    private PolarSheetGenerationSettings settings;

    public PerRaceAndCompetitorPolarSheetGenerationWorker(TrackedRace race,
            PolarSheetGenerationWorker polarSheetGenerationWorker, TimePoint startTime, TimePoint endTime,
            Competitor competitor, PolarSheetGenerationSettings settings) {
        super();
        this.race = race;
        this.polarSheetGenerationWorker = polarSheetGenerationWorker;
        this.startTime = startTime;
        this.endTime = endTime;
        this.competitor = competitor;
        this.settings = settings;
        optimizeStartTime();
        optimizeEndTime();
        checkIfRaceAborted();
    }

    private void checkIfRaceAborted() {
        NavigableSet<MarkPassing> markPassings = race.getMarkPassings(competitor);
        if (markPassings == null || markPassings.size() < 1) {
            return;
        }
        if (markPassings.size() < race.getRace().getCourse().getLegs().size() + 1) {
            finishedEarlyAtWaypoint = markPassings.size() - 1;
        }
    }

    private void optimizeEndTime() {
        NavigableSet<MarkPassing> markPassings = race.getMarkPassings(competitor);
        if (markPassings == null || markPassings.size() < 1) {
            return;
        }
        MarkPassing passedFinish = markPassings.last();
        TimePoint passedFinishTimePoint = passedFinish.getTimePoint();
        if (passedFinishTimePoint.before(endTime)) {
            endTime = passedFinishTimePoint;
        }
    }

    private void optimizeStartTime() {
        NavigableSet<MarkPassing> markPassings = race.getMarkPassings(competitor);
        if (markPassings == null || markPassings.size() < 1) {
            noConfidence = true;
            return;
        }
        MarkPassing passedStart = markPassings.first();
        TimePoint passedStartTimePoint = passedStart.getTimePoint();
        if (passedStartTimePoint.after(startTime)) {
            startTime = passedStartTimePoint;
        }
    }

    @Override
    public void run() {
        if (noConfidence || (finishedEarlyAtWaypoint != -1 && finishedEarlyAtWaypoint < 3)) {
            done = true;
        } else {
            GPSFixTrack<Competitor, GPSFixMoving> track = race.getTrack(competitor);
            track.lockForRead();
            try {
                Iterator<GPSFixMoving> fixesIterator = track.getFixesIterator(startTime, true);
                TimePoint lastConsideredTimePoint = null;
                if (finishedEarlyAtWaypoint != -1) {
                    NavigableSet<MarkPassing> markPassings = race.getMarkPassings(competitor);
                    MarkPassing lastConsideredPassing = markPassings.last();
                    lastConsideredPassing = markPassings.lower(lastConsideredPassing);
                    lastConsideredTimePoint = lastConsideredPassing.getTimePoint();
                }
                boolean reachedEnd = false;
                while (fixesIterator.hasNext() && reachedEnd == false) {
                    GPSFixMoving fix = fixesIterator.next();
                    if (fix.getTimePoint().after(endTime)
                            || (lastConsideredTimePoint != null && fix.getTimePoint().after(lastConsideredTimePoint))) {
                        reachedEnd = true;
                    } else {
                        addFixIfValid(track, fix);
                    }
                }
            } finally {
                track.unlockAfterRead();
                done = true;
            }
        }
    }

    private void addFixIfValid(GPSFixTrack<Competitor, GPSFixMoving> track, GPSFixMoving fix) {
        if (!track.hasDirectionChange(fix.getTimePoint(), race.getRace().getBoatClass()
                .getManeuverDegreeAngleThreshold())) {
            List<Util.Pair<String, WindWithConfidence<Util.Pair<Position, TimePoint>>>> windWithConfidenceList = new ArrayList<Util.Pair<String, WindWithConfidence<Util.Pair<Position, TimePoint>>>>();
            if (settings.useOnlyWindGaugesForWindSpeed()) {
                if (settings.splitByWindgauges()) {
                    windWithConfidenceList.addAll(addAllWindsOfWindGaugesSplitOneByOne(fix));
                } else {
                    windWithConfidenceList
                            .add(new Util.Pair<String, WindWithConfidence<Util.Pair<Position, TimePoint>>>(
                                    createWindGaugesString(race), race.getWindWithConfidence(
                                            fix.getPosition(), fix.getTimePoint(),
                                            collectWindSourcesToIgnoreForSpeed())));
                }
            } else {
                windWithConfidenceList.add(new Util.Pair<String, WindWithConfidence<Util.Pair<Position, TimePoint>>>(
                        "Combined", race.getWindWithConfidence(fix.getPosition(), fix.getTimePoint())));
            }

            for (Util.Pair<String, WindWithConfidence<Util.Pair<Position, TimePoint>>> windWithSourceIdStringPair : windWithConfidenceList) {
                WindWithConfidence<Util.Pair<Position, TimePoint>> windWithConfidence = windWithSourceIdStringPair
                        .getB();
                if (windWithConfidence != null && windWithConfidence.useSpeed()
                        && windWithConfidence.getConfidence() >= settings.getMinimumWindConfidence()) {
                    PolarFix polarFix = new PolarFix(fix, race, track, windWithConfidence.getObject(),
                            settings, windWithSourceIdStringPair.getA());
                    polarSheetGenerationWorker.addPolarData(polarFix);
                }
            }
        }
    }
    
    private Collection<? extends Util.Pair<String, WindWithConfidence<Util.Pair<Position, TimePoint>>>> addAllWindsOfWindGaugesSplitOneByOne(
            GPSFixMoving fix) {
        Iterable<WindSource> windGaugeSources = race.getWindSources(WindSourceType.EXPEDITION);
        List<Util.Pair<String, WindWithConfidence<Util.Pair<Position, TimePoint>>>> windWithConfidenceList 
                            = new ArrayList<Util.Pair<String, WindWithConfidence<Util.Pair<Position, TimePoint>>>>();
        for (WindSource windGaugeSource : windGaugeSources) {
            Iterable<WindSource> allSources = race.getWindSources();
            Set<WindSource> allSourcesButTheSingleWindGaugeSource = new HashSet<WindSource>();
            for (WindSource windSource : allSources) {
                if (windSource != windGaugeSource) {
                    allSourcesButTheSingleWindGaugeSource.add(windSource);
                }
            }
            windWithConfidenceList.add(new Util.Pair<String, WindWithConfidence<Util.Pair<Position, TimePoint>>>(
                    windGaugeSource.getId().toString(), race.getWindWithConfidence(fix.getPosition(), fix.getTimePoint(),
                            allSourcesButTheSingleWindGaugeSource)));
        }
        return windWithConfidenceList;
    }
    
    private String createWindGaugesString(TrackedRace race) {
        Iterable<WindSource> gaugeWindSources = race.getWindSources(WindSourceType.EXPEDITION);
        String gaugeIdString = "";
        for (WindSource source : gaugeWindSources) {
            if (gaugeIdString.isEmpty()) {
                gaugeIdString = "" + source.getId();
            } else {
                gaugeIdString = gaugeIdString + "+" +  source.getId();
            }
        }
        return gaugeIdString;
    }

    public boolean isDone() {
        return done;
    }
    
    private Set<WindSource> collectWindSourcesToIgnoreForSpeed() {
        Set<WindSource> windSourcesToExclude = new HashSet<WindSource>();
        Iterable<WindSource> combinedSources = race.getWindSources(WindSourceType.COMBINED);
        for (WindSource combinedSource : combinedSources) {
            windSourcesToExclude.add(combinedSource);
        }
        Iterable<WindSource> courseSources = race.getWindSources(WindSourceType.COURSE_BASED);
        for (WindSource courseSource : courseSources) {
            windSourcesToExclude.add(courseSource);
        }
        Iterable<WindSource> trackBasedSources = race.getWindSources(WindSourceType.TRACK_BASED_ESTIMATION);
        for (WindSource trackBasedSource : trackBasedSources) {
            windSourcesToExclude.add(trackBasedSource);
        }
        Iterable<WindSource> rcSources = race.getWindSources(WindSourceType.RACECOMMITTEE);
        for (WindSource rcSource : rcSources) {
            windSourcesToExclude.add(rcSource);
        }
        Iterable<WindSource> webSources = race.getWindSources(WindSourceType.WEB);
        for (WindSource webSource : webSources) {
            windSourcesToExclude.add(webSource);
        }
        return windSourcesToExclude;
    }
}
