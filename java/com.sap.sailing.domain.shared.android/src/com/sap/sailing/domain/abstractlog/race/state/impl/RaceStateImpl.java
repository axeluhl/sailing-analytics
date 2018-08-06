package com.sap.sailing.domain.abstractlog.race.state.impl;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.AdditionalScoringInformationFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.DependentStartTimeResolver;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceStatusAnalyzer;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogCourseDesignChangedEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogDependentStartTimeEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFinishPositioningConfirmedEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFinishPositioningListChangedEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFlagEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogPassChangeEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogProtestStartTimeEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogRaceStatusEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartProcedureChangedEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartTimeEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogWindFixEventImpl;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.impl.RaceLogAdditionalScoringInformationEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureFactory;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedureFactoryImpl;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.EmptyRegattaConfiguration;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.abstractlog.NotRevokableException;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;

/**
 * Write-enabled {@link RaceState}.
 */
public class RaceStateImpl extends ReadonlyRaceStateImpl implements RaceState {
    private static final Logger logger = Logger.getLogger(RaceStateImpl.class.getName());
    
    private final AbstractLogEventAuthor author;
    
    /**
     * Creates a {@link RaceState} with the initial racing procedure type set to a fallback value and an empty configuration.
     */
    public static RaceState create(RaceLogResolver raceLogResolver, RaceLog raceLog, AbstractLogEventAuthor author) {
        return create(raceLogResolver, raceLog, author, new EmptyRegattaConfiguration());
    }
    
    /**
     * Creates a {@link RaceState}.
     */
    public static RaceState create(RaceLogResolver raceLogResolver, RaceLog raceLog, AbstractLogEventAuthor author, ConfigurationLoader<RegattaConfiguration> configuration) {
        return new RaceStateImpl(raceLogResolver, raceLog, author, 
                new RacingProcedureFactoryImpl(author, configuration));
    }
    
    public RaceStateImpl(RaceLogResolver raceLogResolver, RaceLog raceLog, AbstractLogEventAuthor author,
            RacingProcedureFactory procedureFactory) {
        this(raceLogResolver, raceLog, author, new RaceStatusAnalyzer.StandardClock(), procedureFactory);
    }

    private RaceStateImpl(RaceLogResolver raceLogResolver, RaceLog raceLog, AbstractLogEventAuthor author, RaceStatusAnalyzer.Clock analyzersClock,
            RacingProcedureFactory procedureFactory) {
        super(raceLogResolver, raceLog, /* forRaceLogIdentifier */ null, analyzersClock, procedureFactory,
                Collections.<SimpleRaceLogIdentifier, ReadonlyRaceState>emptyMap(), /* update */ true);
        this.author = author;
    }
    
    @Override
    public RacingProcedure getRacingProcedure() {
        return (RacingProcedure) super.getRacingProcedure();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RacingProcedure> T getTypedRacingProcedure() {
        return (T) getRacingProcedure();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RacingProcedure> T getTypedRacingProcedure(Class<T> clazz) {
        RacingProcedure procedure = getRacingProcedure();
        if (clazz.isAssignableFrom(procedure.getClass())) {
            return (T) procedure;
        } else {
            return null;
        }
    }

    @Override
    public AbstractLogEventAuthor getAuthor() {
        return author;
    }

    @Override
    public void setRacingProcedure(TimePoint timePoint, RacingProcedureType newType) {
        raceLog.add(new RaceLogStartProcedureChangedEventImpl(timePoint, author, raceLog.getCurrentPassId(), newType));
    }

    @Override
    public void requestNewStartTime(final TimePoint now, final TimePoint startTime, RacingProcedurePrerequisite.Resolver resolver) {
        RacingProcedurePrerequisite.FulfillmentFunction function = new RacingProcedurePrerequisite.FulfillmentFunction() {
            @Override
            public void execute() {
                raceLog.add(new RaceLogStartTimeEventImpl(now, author, raceLog.getCurrentPassId(), startTime));
            }
        };
        
        getRacingProcedure().checkPrerequisitesForStart(now, startTime, function).resolve(resolver);
    }
    
    @Override
    public void forceNewStartTime(TimePoint now, TimePoint startTime) {
        raceLog.add(new RaceLogStartTimeEventImpl(now, author, raceLog.getCurrentPassId(), startTime));
    }
    
    @Override
    public void requestNewDependentStartTime(final TimePoint now, final Duration startTimeDifference, final SimpleRaceLogIdentifier dependentRace, RaceLogResolver raceLogResolver, RacingProcedurePrerequisite.Resolver resolver) {
        final RaceLogDependentStartTimeEvent dependentStartTimeEvent = new RaceLogDependentStartTimeEventImpl(now, author, raceLog.getCurrentPassId(), dependentRace, startTimeDifference);
        
        RacingProcedurePrerequisite.FulfillmentFunction function = new RacingProcedurePrerequisite.FulfillmentFunction() {
            @Override
            public void execute() {
                raceLog.add(dependentStartTimeEvent);
            }
        };
        
        TimePoint startTime = null;
            
        DependentStartTimeResolver dependentStartTimeResolver = new DependentStartTimeResolver(raceLogResolver);
        startTime = dependentStartTimeResolver.resolve(dependentStartTimeEvent).getStartTime();
        
        getRacingProcedure().checkPrerequisitesForStart(now, startTime, function).resolve(resolver);
    }
    
    @Override
    public void forceNewDependentStartTime(TimePoint now, final Duration startTimeDifference, final SimpleRaceLogIdentifier dependentOnRace) {
        raceLog.add(new RaceLogDependentStartTimeEventImpl(now, author, raceLog.getCurrentPassId(), dependentOnRace, startTimeDifference));
    }
    

    @Override
    public void setFinishingTime(TimePoint timePoint) {
        raceLog.add(new RaceLogRaceStatusEventImpl(timePoint, author, raceLog.getCurrentPassId(), RaceLogRaceStatus.FINISHING));
    }

    @Override
    public void setFinishedTime(TimePoint timePoint) {
        raceLog.add(new RaceLogRaceStatusEventImpl(timePoint, author, raceLog.getCurrentPassId(),
                RaceLogRaceStatus.FINISHED));
        // ensure caches are synched
        forceUpdate();
    }

    @Override
    public void setProtestTime(TimePoint now, TimeRange protestTime) {
        assert protestTime != null && protestTime.from() != null && protestTime.to() != null;
        raceLog.add(new RaceLogProtestStartTimeEventImpl(now, author, raceLog.getCurrentPassId(), protestTime));
    }

    @Override
    public void setAdvancePass(TimePoint timePoint) {
        raceLog.add(new RaceLogPassChangeEventImpl(timePoint, author, raceLog.getCurrentPassId() + 1));
    }

    @Override
    public void setAborted(TimePoint timePoint, boolean isPostponed, Flags reasonFlag) {
        Flags markerFlag = isPostponed ? Flags.AP : Flags.NOVEMBER;
        raceLog.add(new RaceLogFlagEventImpl(timePoint, author, raceLog.getCurrentPassId(), markerFlag, reasonFlag, true));
    }

    @Override
    public void setGeneralRecall(TimePoint timePoint) {
        raceLog.add(new RaceLogFlagEventImpl(timePoint, author, raceLog.getCurrentPassId(), Flags.FIRSTSUBSTITUTE, Flags.NONE, true));
    }

    @Override
    public void setFinishPositioningListChanged(TimePoint timePoint, CompetitorResults positionedCompetitors) {
        raceLog.add(new RaceLogFinishPositioningListChangedEventImpl(
                timePoint, author, raceLog.getCurrentPassId(), positionedCompetitors));
    }

    @Override
    public void setFinishPositioningConfirmed(TimePoint timePoint, CompetitorResults positionedCompetitors) {
        raceLog.add(new RaceLogFinishPositioningConfirmedEventImpl(
                timePoint, author, raceLog.getCurrentPassId(), positionedCompetitors));
    }

    @Override
    public void setCourseDesign(TimePoint timePoint, CourseBase courseDesign, CourseDesignerMode courseDesignerMode) {
        raceLog.add(new RaceLogCourseDesignChangedEventImpl(timePoint, author, raceLog.getCurrentPassId(), courseDesign, courseDesignerMode));
    }

    @Override
    public void setWindFix(TimePoint timePoint, Wind wind, boolean isMagnetic) {
        raceLog.add(new RaceLogWindFixEventImpl(timePoint, author, raceLog.getCurrentPassId(), wind, isMagnetic));
    }

    @Override
    public void setAdditionalScoringInformationEnabled(TimePoint timePoint, boolean enable, AdditionalScoringInformationType informationType) {
        final RaceLogAdditionalScoringInformationEvent event = new AdditionalScoringInformationFinder(raceLog).analyze(/*filterBy*/informationType);
        if (enable) {
            if (event == null) {
                raceLog.add(new RaceLogAdditionalScoringInformationEventImpl(timePoint, author, raceLog.getCurrentPassId(), informationType));
            }
        } else {
            if (event != null) {
                // revoke the newest one
                try {
                    raceLog.revokeEvent(author, event, "disable additional scoring information");
                } catch (NotRevokableException e) {
                    logger.log(Level.WARNING, "Could not disable scoring information by adding RevokeEvent", e);
                }
            }
        }
    }
    
    @Override
    public boolean isAdditionalScoringInformationEnabled(AdditionalScoringInformationType informationType) {
        final RaceLogAdditionalScoringInformationEvent event = new AdditionalScoringInformationFinder(raceLog).analyze(/*filterBy*/informationType);
        return event != null;
    }

    @Override
    public void forceUpdate() {
        super.update();
    }

}
