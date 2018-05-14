package com.sap.sailing.server.operationaltransformation;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Duration;
import com.sap.sse.util.ThreadPoolUtil;

public abstract class AbstractRaceOperation<ResultType> extends AbstractRacingEventServiceOperation<ResultType> {
    private static final long serialVersionUID = -1162468486451355784L;
    private static final Logger logger = Logger.getLogger(AbstractRaceOperation.class.getName());
    private RegattaAndRaceIdentifier raceIdentifier;
    
    /**
     * Used as a race listener on the {@link TrackedRegatta} in case the {@link TrackedRace} identified by the
     * {@link AbstractRaceOperation#getRaceIdentifier() race identifier} isn't found when the operation is to be
     * applied. Such listeners will be removed by a scheduled task after some timeout that can be configured through the
     * constructor. The listener is also removed after having received the tracked race.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    private class DelayedOperationRunnerWaitingForTrackedRace implements RaceListener {
        private final Consumer<DynamicTrackedRace> taskWithTrackedRace;
        private final TrackedRegatta whereToRegisterAndUnregisterListener;
        private final ScheduledFuture<?> timeoutTask;

        public DelayedOperationRunnerWaitingForTrackedRace(TrackedRegatta whereToRegisterAndUnregisterListener,
                Consumer<DynamicTrackedRace> taskWithTrackedRace, Duration timeoutForReceivingTrackedRace) {
            super();
            this.whereToRegisterAndUnregisterListener = whereToRegisterAndUnregisterListener;
            this.taskWithTrackedRace = taskWithTrackedRace;
            whereToRegisterAndUnregisterListener.addRaceListener(this, /* thread local transporter */ Optional.empty());
            timeoutTask = ThreadPoolUtil.INSTANCE.getDefaultForegroundTaskThreadPoolExecutor().schedule(
                    ()->{
                        whereToRegisterAndUnregisterListener.removeRaceListener(DelayedOperationRunnerWaitingForTrackedRace.this);
                        logger.warning("Waiting for tracked race for race identifier "+getRaceIdentifier()+
                                " timed out. Not waiting any longer. Discarding operation "+AbstractRaceOperation.this);
                    },
                    timeoutForReceivingTrackedRace.asMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public void raceAdded(TrackedRace trackedRace) {
            logger.info("Tracked race "+trackedRace+" showed up. Now we can apply "+AbstractRaceOperation.this);
            timeoutTask.cancel(/* mayInterruptIfRunning */ false);
            // the following cast is valid because the tracked regatta is always a DynamicTrackedRegatta which
            // holds DynamicTrackedRace objects
            taskWithTrackedRace.accept((DynamicTrackedRace) trackedRace);
            whereToRegisterAndUnregisterListener.removeRaceListener(this);
        }

        @Override
        public void raceRemoved(TrackedRace trackedRace) {
        }
    }

    public AbstractRaceOperation(RegattaAndRaceIdentifier raceIdentifier) {
        super();
        this.raceIdentifier = raceIdentifier;
    }
    
    /**
     * The default for race operations is that their {@link #internalApplyTo(RacingEventService)} method already
     * replicates the operation's effects.
     */
    @Override
    public boolean isRequiresExplicitTransitiveReplication() {
        return false;
    }
    
    protected RegattaAndRaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }
    
    protected DynamicTrackedRace getTrackedRace(RacingEventService racingEventService) {
        // it's fair to not wait for the tracked race to arrive here because we're receiving a replication operation
        // and the synchronous race-creating operation must have been processed synchronously before this operation
        // could even have been received
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) racingEventService.getExistingTrackedRace(getRaceIdentifier());
        return trackedRace;
    }

    protected void doWithTrackedRace(RacingEventService racingEventService, Consumer<DynamicTrackedRace> taskWithTrackedRace) {
        final Duration timeout = Duration.ONE_MINUTE;
        final DynamicTrackedRace trackedRace = getTrackedRace(racingEventService);
        if (trackedRace != null) {
            taskWithTrackedRace.accept(trackedRace);
        } else {
            logger.info("Tracked race for race identifier "+getRaceIdentifier()+" not found. Waiting for it for "+timeout);
            final TrackedRegatta trackedRegatta = racingEventService.getTrackedRegatta(racingEventService.getRegatta(getRaceIdentifier()));
            if (trackedRegatta != null) {
                new DelayedOperationRunnerWaitingForTrackedRace(/* whereToUnregisterListener */ trackedRegatta,
                        taskWithTrackedRace, /* timeout for waiting for tracked race */ timeout);
            } else {
                logger.warning("Couldn't find tracked regatta for race identifier "+getRaceIdentifier()+
                        ". Ignoring operation "+this);
            }
        }
    }

    @Override
    public String toString() {
        return super.toString()+" [raceIdentifier=" + raceIdentifier + "]";
    }
}
