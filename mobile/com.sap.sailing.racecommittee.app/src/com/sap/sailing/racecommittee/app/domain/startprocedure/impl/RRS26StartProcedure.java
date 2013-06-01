package com.sap.sailing.racecommittee.app.domain.startprocedure.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import android.content.Context;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.analyzing.impl.IndividualRecallFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceStatusAnalyzer;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.startprocedure.RunningRaceEventListener;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartModeChoosableStartProcedure;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartPhaseEventListener;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartProcedure;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartProcedureListener;
import com.sap.sailing.racecommittee.app.domain.startprocedure.UserRequiredActionPerformedListener;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.ClassicCourseDesignDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceChooseStartModeDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RRS26FinishedRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RRS26FinishingRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RRS26RunningRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase.RRS26StartPhaseFragment;

public class RRS26StartProcedure implements StartProcedure, StartModeChoosableStartProcedure {

    private final static long startPhaseClassUpInterval = 5 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseStartModeUpInterval = 4 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseStartModeDownInterval = 1 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseClassDownInterval = 0;

    private final static long individualRecallRemovalInterval = 4 * 60 * 1000; // minutes * seconds * milliseconds

    // list of start procedure specific event id's
    private static final Integer INDIVIDUAL_RECALL_REMOVAL_EVENT_ID = 1;

    private List<Long> startProcedureEventIntervals;
    private RaceLog raceLog;
    private StartProcedureListener raceStateChangedListener;
    private RRS26StartPhaseEventListener startPhaseEventListener;
    private RRS26RunningRaceEventListener runningRaceEventListener;
    private UserRequiredActionPerformedListener userRequiredActionPerformedListener;

    private IndividualRecallFinder individualRecallFinder;
    private Flags startModeFlag = Flags.PAPA;
    private String boatClassName = "";
    private boolean startModeFlagChosen = false;

    public RRS26StartProcedure(RaceLog raceLog) {
        this.raceLog = raceLog;
        startProcedureEventIntervals = new ArrayList<Long>();
        raceStateChangedListener = null;
        startPhaseEventListener = null;
        runningRaceEventListener = null;

        startProcedureEventIntervals.add(startPhaseClassUpInterval);
        startProcedureEventIntervals.add(startPhaseStartModeUpInterval);
        startProcedureEventIntervals.add(startPhaseStartModeDownInterval);
        startProcedureEventIntervals.add(startPhaseClassDownInterval);

        individualRecallFinder = new IndividualRecallFinder(raceLog);
    }

    @Override
    public TimePoint getStartPhaseStartTime(TimePoint startTime) {
        return startTime.minus(startPhaseClassUpInterval);
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

        if (interval == startPhaseClassUpInterval) {
            handleClassUp(eventTime);
        } else if (interval == startPhaseStartModeUpInterval) {
            handleStartRuleUp(eventTime);
        } else if (interval == startPhaseStartModeDownInterval) {
            handleStartRuleDown(eventTime);
        } else if (interval == startPhaseClassDownInterval) {
            handleClassDown(eventTime);
        }
    }

    private void handleClassUp(TimePoint eventTime) {

        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceStartphaseEntered(eventTime);
        }

        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), Flags.CLASS, Flags.NONE, /* isDisplayed */
                true);
        raceLog.add(event);

        if (startPhaseEventListener != null) {
            startPhaseEventListener.onClassUp();
        }
    }

    private void handleStartRuleUp(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), this.startModeFlag, Flags.NONE, /* isDisplayed */
                true);
        raceLog.add(event);

        if (startPhaseEventListener != null) {
            startPhaseEventListener.onStartModeUp(this.startModeFlag);
        }
    }

    private void handleStartRuleDown(TimePoint eventTime) {

        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), this.startModeFlag, Flags.NONE, /* isDisplayed */
                false);
        raceLog.add(event);

        if (startPhaseEventListener != null) {
            startPhaseEventListener.onStartModeUp(this.startModeFlag);
        }
    }

    private void handleClassDown(TimePoint eventTime) {

        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), Flags.CLASS, Flags.NONE, /* isDisplayed */
                false);
        raceLog.add(event);

        if (startPhaseEventListener != null) {
            startPhaseEventListener.onClassDown();
        }
        
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
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), Flags.BLUE, Flags.NONE, /* isDisplayed */
                true);
        raceLog.add(event);

        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceFinishing(eventTime);
        }
    }

    @Override
    public void dispatchAutomaticRaceEndEvent(TimePoint automaticRaceEnd) {
        RaceStatusAnalyzer analyzer = new RaceStatusAnalyzer(raceLog);
        if (analyzer.getStatus().equals(RaceLogRaceStatus.FINISHING)) {
            // setFinished(automaticRaceEnd);
        }
    }

    @Override
    public void setFinished(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), Flags.BLUE, Flags.NONE, /* isDisplayed */
                false);
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
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), Flags.AP, lowerFlag, /* isDisplayed */
                true);
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
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), Flags.NOVEMBER, lowerFlag, /* isDisplayed */
                true);
        raceLog.add(event);

        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceAborted(eventTime);
        }
    }

    @Override
    public void setGeneralRecall(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), Flags.FIRSTSUBSTITUTE, Flags.NONE, /* isDisplayed */
                true);
        raceLog.add(event);

        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceAborted(eventTime);
        }
    }

    public void setIndividualRecall(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), Flags.XRAY, Flags.NONE, /* isDisplayed */
                true);
        raceLog.add(event);

        TimePoint individualRecallRemovalFireTimePoint = eventTime.plus(individualRecallRemovalInterval);

        if (runningRaceEventListener != null) {
            runningRaceEventListener.onIndividualRecall();
        }

        if (raceStateChangedListener != null) {
            raceStateChangedListener.onStartProcedureSpecificEvent(individualRecallRemovalFireTimePoint,
                    INDIVIDUAL_RECALL_REMOVAL_EVENT_ID);
        }
    }

    public void setIndividualRecallRemoval(TimePoint eventTime) {
        if (this.getIndividualRecallDisplayed()) {
            RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(),
                    Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), Flags.XRAY, Flags.NONE, /* isDisplayed */
                    false);
            raceLog.add(event);

            if (runningRaceEventListener != null) {
                runningRaceEventListener.onIndividualRecallRemoval();
            }
        }
    }

    public boolean getIndividualRecallDisplayed() {
        if (this.individualRecallFinder.getIndividualRecallDisplayedTime() != null) {
            if (this.individualRecallFinder.getIndividualRecallDisplayedRemovalTime() != null) {
                if (this.individualRecallFinder.getIndividualRecallDisplayedRemovalTime().after(
                        this.individualRecallFinder.getIndividualRecallDisplayedTime())) {
                    return false;
                }
            }
            return true;
        } else
            return false;
    }

    @Override
    public Class<? extends RaceFragment> getStartphaseFragment() {
        return RRS26StartPhaseFragment.class;
    }

    @Override
    public Class<? extends RaceFragment> getRunningRaceFragment() {
        return RRS26RunningRaceFragment.class;
    }

    @Override
    public void setStartPhaseEventListener(StartPhaseEventListener listener) {
        startPhaseEventListener = (RRS26StartPhaseEventListener) listener;
    }

    @Override
    public Pair<String, List<Object>> getNextFlagCountdownUiLabel(Context context, long millisecondsTillStart) {
        Pair<String, List<Object>> result;
        List<Object> milisecondsList = new ArrayList<Object>();
        if (millisecondsTillStart < startPhaseStartModeDownInterval) {
            milisecondsList.add(millisecondsTillStart);
            milisecondsList.add(this.boatClassName);
            result = new Pair<String, List<Object>>(context.getResources().getString(
                    R.string.race_startphase_countdown_class_remove), milisecondsList);
        } else if (millisecondsTillStart < startPhaseStartModeUpInterval) {
            milisecondsList.add(millisecondsTillStart - startPhaseStartModeDownInterval);
            milisecondsList.add(this.startModeFlag.toString());
            result = new Pair<String, List<Object>>(context.getResources().getString(
                    R.string.race_startphase_countdown_mode_remove), milisecondsList);
        } else if (millisecondsTillStart < startPhaseClassUpInterval) {
            milisecondsList.add(millisecondsTillStart - startPhaseStartModeUpInterval);
            milisecondsList.add(this.startModeFlag.toString());
            result = new Pair<String, List<Object>>(context.getResources().getString(
                    R.string.race_startphase_countdown_mode_display), milisecondsList);
        } else {
            milisecondsList.add(millisecondsTillStart - startPhaseClassUpInterval);
            milisecondsList.add(this.boatClassName);
            result = new Pair<String, List<Object>>(context.getResources().getString(
                    R.string.race_startphase_countdown_class_display), milisecondsList);
        }
        return result;
    }

    @Override
    public void setRunningRaceEventListener(RunningRaceEventListener listener) {
        this.runningRaceEventListener = (RRS26RunningRaceEventListener) listener;

    }

    @Override
    public void handleStartProcedureSpecificEvent(TimePoint eventTime, Integer eventId) {
        if (eventId.equals(INDIVIDUAL_RECALL_REMOVAL_EVENT_ID)) {
            setIndividualRecallRemoval(eventTime);
        }

    }

    @Override
    public void setStartModeFlag(Flags startModeFlag) {
        this.startModeFlag = startModeFlag;
        this.startModeFlagChosen = true;
        if(startPhaseEventListener!=null){
            startPhaseEventListener.onStartModeFlagChosen(startModeFlag);
        }
        if(userRequiredActionPerformedListener != null){
            userRequiredActionPerformedListener.onUserRequiredActionPerformed();
        }
    }

    @Override
    public Flags getCurrentStartModeFlag() {
        return this.startModeFlag;
    }

    public boolean isIndividualRecallDisplayed() {
        if(this.individualRecallFinder.getIndividualRecallDisplayedTime() != null){
            if(this.individualRecallFinder.getIndividualRecallDisplayedRemovalTime() != null){
                if(this.individualRecallFinder.getIndividualRecallDisplayedRemovalTime().after(this.individualRecallFinder.getIndividualRecallDisplayedTime())){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    @Override
    public Class<? extends RaceFragment> getFinishingRaceFragment() {
        return RRS26FinishingRaceFragment.class;
    }

    @Override
    public Class<? extends RaceFragment> getFinishedRaceFragment() {
        return RRS26FinishedRaceFragment.class;
    }
    
    @Override
    public List<Class<? extends RaceDialogFragment>> checkForUserActionRequiredActions(MillisecondsTimePoint newStartTime, UserRequiredActionPerformedListener listener) {
        List<Class<? extends RaceDialogFragment>>  actionList = new ArrayList<Class<? extends RaceDialogFragment>>();
        if(MillisecondsTimePoint.now().after(newStartTime.minus(startPhaseStartModeUpInterval)) && !startModeFlagChosen){
            actionList.add(RaceChooseStartModeDialog.class);
            this.userRequiredActionPerformedListener = listener;
        }
        return actionList;
    }
    
    @Override
    public Class<? extends RaceDialogFragment> getCourseDesignDialog() {
        return ClassicCourseDesignDialogFragment.class;
    }
}
