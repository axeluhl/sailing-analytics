package com.sap.sailing.racecommittee.app.domain.startprocedure.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import android.content.Context;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.analyzing.impl.IndividualRecallDisplayedFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.IndividualRecallRemovedFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceStatusAnalyzer;
import com.sap.sailing.domain.racelog.analyzing.impl.StartTimeFinder;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.startprocedure.RunningRaceEventListener;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartPhaseEventListener;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartProcedure;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartProcedureListener;
import com.sap.sailing.racecommittee.app.domain.startprocedure.UserRequiredActionPerformedListener;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.ESSCourseDesignDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.EssFinishedRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.EssFinishingRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.EssRunningRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase.EssStartPhaseFragment;

public class ExtremeSailingSeriesStartProcedure implements StartProcedure {
    
    private final static long startPhaseAPDownInterval = 4 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseESSThreeUpInterval = 3 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseESSTwoUpInterval = 2 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseESSOneUpInterval = 1 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseESSOneDownInterval = 0;
    
    private final static long individualRecallRemovalInterval = 4 * 60 * 1000; // minutes * seconds * milliseconds
    
    private final static double essAutomaticRaceFinishMultiplyer = 0.75;
    
    //list of start procedure specific event id's
    private static final Integer INDIVIDUAL_RECALL_REMOVAL_EVENT_ID = 1;
    
    private List<Long> startProcedureEventIntervals;
    private RaceLog raceLog;
    private StartProcedureListener raceStateChangedListener;
    private EssStartPhaseEventListener startPhaseEventListener;
    private EssRunningRaceEventListener runningRaceEventListener;
    
    private IndividualRecallDisplayedFinder individualRecallDisplayedFinder;
    private IndividualRecallRemovedFinder individualRecallRemovedFinder;
    
    public ExtremeSailingSeriesStartProcedure(RaceLog raceLog) {
        this.raceLog = raceLog;
        startProcedureEventIntervals = new ArrayList<Long>();
        raceStateChangedListener = null;
        startPhaseEventListener = null;
        runningRaceEventListener = null;
        
        startProcedureEventIntervals.add(startPhaseAPDownInterval);
        startProcedureEventIntervals.add(startPhaseESSThreeUpInterval);
        startProcedureEventIntervals.add(startPhaseESSTwoUpInterval);
        startProcedureEventIntervals.add(startPhaseESSOneUpInterval);
        startProcedureEventIntervals.add(startPhaseESSOneDownInterval);

        individualRecallDisplayedFinder = new IndividualRecallDisplayedFinder(raceLog);
        individualRecallRemovedFinder = new IndividualRecallRemovedFinder(raceLog);
    }

    @Override
    public TimePoint getStartPhaseStartTime(TimePoint startTime) {
        return startTime.minus(startPhaseAPDownInterval);
    }

    @Override
    public List<TimePoint> getAutomaticEventFireTimePoints(TimePoint startTime) {
        List<TimePoint> triggerTimePoints = new ArrayList<TimePoint>();
        
        for (Long interval : startProcedureEventIntervals) {
            triggerTimePoints.add(startTime.minus(interval));
        }
        return triggerTimePoints;
    }

    @Override
    public void dispatchFiredEventTimePoint(TimePoint startTime, TimePoint eventTime) {
        long interval = startTime.asMillis() - eventTime.asMillis();
        
        if (interval == startPhaseAPDownInterval) {
            handleAPDown(eventTime);
        } else if (interval == startPhaseESSThreeUpInterval) {
            handleEssThreeUp(eventTime);
        } else if (interval == startPhaseESSTwoUpInterval) {
            handleEssTwoUpAndEssThreeDown(eventTime);
        } else if (interval == startPhaseESSOneUpInterval) {
            handleEssOneUpAndEssTwoDown(eventTime);
        } else if (interval == startPhaseESSOneDownInterval) {
            handleEssOneDown(eventTime);
        }
    }

    private void handleAPDown(TimePoint eventTime) {
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceStartphaseEntered(eventTime);
        }

        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.AP, Flags.NONE, /*isDisplayed*/false);
        raceLog.add(event);
        
        if (startPhaseEventListener != null) {
            startPhaseEventListener.onAPDown();
        }
    }

    private void handleEssThreeUp(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.ESSTHREE, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(event);
        
        if (startPhaseEventListener != null) {
            startPhaseEventListener.onEssThreeUp();
        }
    }

    private void handleEssTwoUpAndEssThreeDown(TimePoint eventTime) {
        
        RaceLogEvent essThreeDownEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.ESSTHREE, Flags.NONE, /*isDisplayed*/false);
        raceLog.add(essThreeDownEvent);
        
        RaceLogEvent essTwoUpEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.ESSTWO, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(essTwoUpEvent);
        
        if (startPhaseEventListener != null) {
            startPhaseEventListener.onEssTwoUp();
        }
    }

    private void handleEssOneUpAndEssTwoDown(TimePoint eventTime) {
        
        RaceLogEvent essTwoDownEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.ESSTWO, Flags.NONE, /*isDisplayed*/false);
        raceLog.add(essTwoDownEvent);
        
        RaceLogEvent essOneUpEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.ESSONE, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(essOneUpEvent);
        
        if (startPhaseEventListener != null) {
            startPhaseEventListener.onEssOneUp();
        }
    }

    private void handleEssOneDown(TimePoint eventTime) {
        RaceLogEvent essOneDownEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.ESSONE, Flags.NONE, /*isDisplayed*/false);
        raceLog.add(essOneDownEvent);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceStarted(eventTime);
        }
    }
    
    @Override
    public void setStartProcedureListener(StartProcedureListener raceStateChangedListener) {
        this.raceStateChangedListener = raceStateChangedListener;
    }

    @Override
    public TimePoint getLogicalStartTimeEventTime(TimePoint newEnteredStartTime) {
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint startPhaseStartTime = getStartPhaseStartTime(newEnteredStartTime);
        TimePoint resultTime;
        if (now.after(startPhaseStartTime)) {
            // We do not want RaceStatusEvents with the same timestamp to ensure a reasonable ordering
            // Therefore we guarantee that SCHEDULDED happens before STARTPHASE
            resultTime = startPhaseStartTime.minus(1);
        } else {
            resultTime = now;
        }
        
        return resultTime;
    }

    @Override
    public void setFinishing(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.BLUE, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(event);
        
        StartTimeFinder startTimeFinder = new StartTimeFinder(raceLog);
        long raceDuration = eventTime.asMillis()-startTimeFinder.analyze().asMillis();
        
        TimePoint automaticRaceEnd = eventTime.plus((long) (raceDuration*essAutomaticRaceFinishMultiplyer));
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceFinishing(eventTime, automaticRaceEnd);
        }
    }
    
    @Override
    public void dispatchAutomaticRaceEndEvent(TimePoint automaticRaceEnd) {
        RaceStatusAnalyzer analyzer = new RaceStatusAnalyzer(raceLog);
        if(analyzer.analyze().equals(RaceLogRaceStatus.FINISHING)) {
            //setFinished(automaticRaceEnd);
        }
    }

    @Override
    public void setFinished(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.BLUE, Flags.NONE, /*isDisplayed*/false);
        raceLog.add(event);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceFinished(eventTime);
        }
    }

    @Override
    public void setPostponed(TimePoint eventTime, Flags lowerFlag) {
        switch (lowerFlag) {
        case NONE:
        case ALPHA:
        case HOTEL:
            handleAPUp(eventTime, lowerFlag);
            break;
        default:
            break;
        }
    }

    private void handleAPUp(TimePoint eventTime, Flags lowerFlag) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.AP, lowerFlag, /*isDisplayed*/true);
        raceLog.add(event);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceAborted(eventTime);
        }
    }

    @Override
    public void setAbandoned(TimePoint eventTime, Flags lowerFlag) {
        switch (lowerFlag) {
        case NONE:
        case ALPHA:
        case HOTEL:
            handleNovemberUp(eventTime, lowerFlag);
            break;
        default:
            break;
        }
    }

    private void handleNovemberUp(TimePoint eventTime, Flags lowerFlag) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.NOVEMBER, lowerFlag, /*isDisplayed*/true);
        raceLog.add(event);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceAborted(eventTime);
        }
    }

    @Override
    public void setGeneralRecall(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.FIRSTSUBSTITUTE, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(event);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceAborted(eventTime);
        }
    }

    public void setIndividualRecall(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.XRAY, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(event);
        
        TimePoint individualRecallRemovalFireTimePoint = eventTime.plus(individualRecallRemovalInterval);
        
        
        if (runningRaceEventListener != null) {
            runningRaceEventListener.onIndividualRecall();
        }
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onStartProcedureSpecificEvent(individualRecallRemovalFireTimePoint, INDIVIDUAL_RECALL_REMOVAL_EVENT_ID);
        }
    }

    public void setIndividualRecallRemoval(TimePoint eventTime) {
        if (this.isIndividualRecallDisplayed()) {
            RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(),
                    Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), Flags.XRAY, Flags.NONE, /* isDisplayed */
                    false);
            raceLog.add(event);

            if (runningRaceEventListener != null) {
                runningRaceEventListener.onIndividualRecallRemoval();
            }
        }
    }
    
    public boolean isIndividualRecallDisplayed() {
        if(this.individualRecallDisplayedFinder.analyze() != null){
            if(this.individualRecallRemovedFinder.analyze() != null){
                if(this.individualRecallRemovedFinder.analyze().after(this.individualRecallDisplayedFinder.analyze())){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    public boolean isIndividualRecallRemoved() {
        if(this.individualRecallDisplayedFinder.analyze()!=null){
            if(this.individualRecallRemovedFinder.analyze()!=null){
                if(this.individualRecallRemovedFinder.analyze().after(this.individualRecallDisplayedFinder.analyze())){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Class<? extends RaceFragment> getStartphaseFragment() {
        return EssStartPhaseFragment.class;
    }
    
    @Override
    public Class<? extends RaceFragment> getRunningRaceFragment() {
        return EssRunningRaceFragment.class;
    }

    @Override
    public void setStartPhaseEventListener(StartPhaseEventListener listener) {
        startPhaseEventListener = (EssStartPhaseEventListener) listener;
    }

    @Override
    public Pair<String, List<Object>> getNextFlagCountdownUiLabel(Context context, long millisecondsTillStart) {
        Pair<String, List<Object>> result;
        List<Object> milisecondsList = new ArrayList<Object>();
        if (millisecondsTillStart < startPhaseESSOneUpInterval) {
            milisecondsList.add(millisecondsTillStart);
            result = new Pair<String, List<Object>>(context.getResources().getString(R.string.race_startphase_ess_countdown_one_flag_remove), milisecondsList );
        } else if (millisecondsTillStart < startPhaseESSTwoUpInterval) {
            milisecondsList.add(millisecondsTillStart - startPhaseESSOneUpInterval);
            result = new Pair<String, List<Object>>(context.getResources().getString(R.string.race_startphase_ess_countdown_one_flag_display), milisecondsList);
        } else if (millisecondsTillStart < startPhaseESSThreeUpInterval) {
            milisecondsList.add(millisecondsTillStart - startPhaseESSTwoUpInterval);
            result = new Pair<String, List<Object>>(context.getResources().getString(R.string.race_startphase_ess_countdown_two_flag_display), milisecondsList);
        } else if (millisecondsTillStart < startPhaseAPDownInterval) {
            milisecondsList.add(millisecondsTillStart - startPhaseESSThreeUpInterval);
            result = new Pair<String, List<Object>>(context.getResources().getString(R.string.race_startphase_ess_countdown_three_flag_display), milisecondsList);
        } else {
            milisecondsList.add(millisecondsTillStart - startPhaseAPDownInterval);
            result = new Pair<String, List<Object>>(context.getResources().getString(R.string.race_startphase_ess_countdown_ap_flag_removed), milisecondsList);
        }
        return result;
    }

    @Override
    public void setRunningRaceEventListener(RunningRaceEventListener listener) {
        this.runningRaceEventListener = (EssRunningRaceEventListener) listener;
        
    }

    @Override
    public void handleStartProcedureSpecificEvent(TimePoint eventTime, Integer eventId) {
        if(eventId.equals(INDIVIDUAL_RECALL_REMOVAL_EVENT_ID)){
            setIndividualRecallRemoval(eventTime);
        }
        
    }

    @Override
    public Class<? extends RaceFragment> getFinishingRaceFragment() {
        return EssFinishingRaceFragment.class;
    }

    @Override
    public Class<? extends RaceFragment> getFinishedRaceFragment() {
        return EssFinishedRaceFragment.class;
    }

    @Override
    public Class<? extends RaceDialogFragment> checkForUserActionRequiredActions(MillisecondsTimePoint newStartTime, UserRequiredActionPerformedListener listener) {
        return null;
    }

    @Override
    public Class<? extends RaceDialogFragment> getCourseDesignDialog() {
        return ESSCourseDesignDialogFragment.class;
    }
}
