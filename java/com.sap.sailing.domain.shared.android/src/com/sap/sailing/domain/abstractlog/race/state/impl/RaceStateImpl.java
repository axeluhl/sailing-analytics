package com.sap.sailing.domain.abstractlog.race.state.impl;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.AdditionalScoringInformationFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceStatusAnalyzer;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureFactory;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedureFactoryImpl;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.EmptyRegattaConfiguration;
import com.sap.sailing.domain.common.abstractlog.NotRevokableException;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sse.common.TimePoint;

/**
 * Write-enabled {@link RaceState}.
 */
public class RaceStateImpl extends ReadonlyRaceStateImpl implements RaceState {
    private static final Logger logger = Logger.getLogger(RaceStateImpl.class.getName());
    
    private final AbstractLogEventAuthor author;
    private final RaceLogEventFactory factory;
    
    /**
     * Creates a {@link RaceState} with the initial racing procedure type set to a fallback value and an empty configuration.
     */
    public static RaceState create(RaceLog raceLog, AbstractLogEventAuthor author) {
        return create(raceLog, author, new EmptyRegattaConfiguration());
    }
    
    /**
     * Creates a {@link RaceState}.
     */
    public static RaceState create(RaceLog raceLog, AbstractLogEventAuthor author, ConfigurationLoader<RegattaConfiguration> configuration) {
        return new RaceStateImpl(raceLog, author, 
                RaceLogEventFactory.INSTANCE,
                new RacingProcedureFactoryImpl(author, RaceLogEventFactory.INSTANCE, configuration));
    }
    
    public RaceStateImpl(RaceLog raceLog, AbstractLogEventAuthor author, RaceLogEventFactory eventFactory,
            RacingProcedureFactory procedureFactory) {
        this(raceLog, author, eventFactory, new RaceStatusAnalyzer.StandardClock(), procedureFactory);
    }

    private RaceStateImpl(RaceLog raceLog, AbstractLogEventAuthor author, RaceLogEventFactory eventFactory, RaceStatusAnalyzer.Clock analyzersClock,
            RacingProcedureFactory procedureFactory) {
        super(raceLog, analyzersClock, procedureFactory, /* update */ true);
        this.author = author;
        this.factory = eventFactory;
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
        raceLog.add(factory.createStartProcedureChangedEvent(timePoint, author, raceLog.getCurrentPassId(), newType));
    }

    @Override
    public void requestNewStartTime(final TimePoint now, final TimePoint startTime, RacingProcedurePrerequisite.Resolver resolver) {
        RacingProcedurePrerequisite.FulfillmentFunction function = new RacingProcedurePrerequisite.FulfillmentFunction() {
            @Override
            public void execute() {
                raceLog.add(factory.createStartTimeEvent(now, author, raceLog.getCurrentPassId(), startTime));
            }
        };
        
        getRacingProcedure().checkPrerequisitesForStart(now, startTime, function).resolve(resolver);
    }
    
    @Override
    public void forceNewStartTime(TimePoint now, TimePoint startTime) {
        raceLog.add(factory.createStartTimeEvent(now, author, raceLog.getCurrentPassId(), startTime));
    }

    @Override
    public void setFinishingTime(TimePoint timePoint) {
        raceLog.add(factory.createRaceStatusEvent(timePoint, author, raceLog.getCurrentPassId(), RaceLogRaceStatus.FINISHING));
    }

    @Override
    public void setFinishedTime(TimePoint timePoint) {
        raceLog.add(factory.createRaceStatusEvent(timePoint, author, raceLog.getCurrentPassId(), RaceLogRaceStatus.FINISHED));
    }

    @Override
    public void setProtestTime(TimePoint now, TimePoint timePoint) {
        raceLog.add(factory.createProtestStartTimeEvent(now, author, raceLog.getCurrentPassId(), timePoint));
    }

    @Override
    public void setAdvancePass(TimePoint timePoint) {
        raceLog.add(factory.createPassChangeEvent(timePoint, author, raceLog.getCurrentPassId() + 1));
    }

    @Override
    public void setAborted(TimePoint timePoint, boolean isPostponed, Flags reasonFlag) {
        Flags markerFlag = isPostponed ? Flags.AP : Flags.NOVEMBER;
        raceLog.add(factory.createFlagEvent(timePoint, author, raceLog.getCurrentPassId(), markerFlag, reasonFlag, true));
    }

    @Override
    public void setGeneralRecall(TimePoint timePoint) {
        raceLog.add(factory.createFlagEvent(timePoint, author, raceLog.getCurrentPassId(), Flags.FIRSTSUBSTITUTE, Flags.NONE, true));
    }

    @Override
    public void setFinishPositioningListChanged(TimePoint timePoint, CompetitorResults positionedCompetitors) {
        raceLog.add(factory.createFinishPositioningListChangedEvent(
                timePoint, author, raceLog.getCurrentPassId(), positionedCompetitors));
    }

    @Override
    public void setFinishPositioningConfirmed(TimePoint timePoint) {
        raceLog.add(factory.createFinishPositioningConfirmedEvent(
                timePoint, author, raceLog.getCurrentPassId(), getFinishPositioningList()));
    }

    @Override
    public void setCourseDesign(TimePoint timePoint, CourseBase courseDesign) {
        raceLog.add(factory.createCourseDesignChangedEvent(timePoint, author, raceLog.getCurrentPassId(), courseDesign));
    }

    @Override
    public void setWindFix(TimePoint timePoint, Wind wind) {
        raceLog.add(factory.createWindFixEvent(timePoint, author, raceLog.getCurrentPassId(), wind));
    }

    @Override
    public void setAdditionalScoringInformationEnabled(TimePoint timePoint, boolean enable, AdditionalScoringInformationType informationType) {
        final RaceLogAdditionalScoringInformationEvent event = new AdditionalScoringInformationFinder(raceLog).analyze(/*filterBy*/informationType);
        if (enable) {
            if (event == null) {
                raceLog.add(factory.createAdditionalScoringInformationEvent(timePoint, UUID.randomUUID(), author, raceLog.getCurrentPassId(), informationType));
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

}
