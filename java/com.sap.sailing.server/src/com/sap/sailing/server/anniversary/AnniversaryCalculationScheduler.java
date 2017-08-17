package com.sap.sailing.server.anniversary;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.anniversary.SimpleRaceInfo;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

/**
 * This class is created by the Activator, and periodically collects all known local and remote races, it then calls any
 * registered ChangeListeners with a map of all races in the form of {@link SimpleRaceInfo} keyed with
 * {@link RegattaAndRaceIdentifier}
 */
public class AnniversaryCalculationScheduler implements Runnable {
    private static final Logger logger = Logger.getLogger(AnniversaryCalculationScheduler.class.getName());
    private static final long INITIAL_DELAY = 1000;
    private static final long DELAY = 50000;
    private final RacingEventService racingEventService;

    private final Set<ChangeListener> listeners;

    interface ChangeListener {
        /**
         * If the listener is added to the AnniversaryCalculationScheduler , this is called once a minute, and is given
         * an unsorted list of all races. This list is not modifiable and not threadsafe.
         */
        void uponUpdate(Collection<SimpleRaceInfo> collection);

        /**
         * Called by the AnniversaryCalculationScheduler, when the ChangeListener is added to the
         * AnniversaryCalculationScheduler, this allows all ChangeListeners to use getFullAnniversaryData
         */
        void setAnniversaryCalculator(AnniversaryCalculationScheduler anniversaryCalculator);
    }

    public AnniversaryCalculationScheduler(RacingEventServiceImpl racingEventService,
            ScheduledExecutorService scheduledExecutorService) {
        this.racingEventService = racingEventService;
        listeners = new CopyOnWriteArraySet<>();
        scheduledExecutorService.scheduleWithFixedDelay(this, INITIAL_DELAY, DELAY, TimeUnit.MILLISECONDS);
    }

    public void addListener(ChangeListener listener) {
        listener.setAnniversaryCalculator(this);
        listeners.add(listener);
    }

    public void removeListener(ChangeListener listener) {
        listener.setAnniversaryCalculator(null);
        listeners.remove(listener);
    }

    @Override
    public void run() {
        try {
            if (racingEventService == null || listeners.isEmpty()) {
                return;
            }
            Map<RegattaAndRaceIdentifier, SimpleRaceInfo> store = new HashMap<>();
            store.putAll(racingEventService.getRemoteRaceList());
            store.putAll(racingEventService.getLocalRaceList());
            store = Collections.unmodifiableMap(store);
            for (ChangeListener listener : listeners) {
                listener.uponUpdate(store.values());
            }
        } catch (Exception e) {
            logger.warning("Could not update anniversaries! ");
            e.printStackTrace();
        }
    }

    /**
     * retrieves for a given RegattaAndRaceIdentifier a DetailedRaceInfo
     */
    public DetailedRaceInfo getFullAnniversaryData(RegattaAndRaceIdentifier regattaNameAndRaceName) {
        return racingEventService.getFullDetailsForRaceCascading(regattaNameAndRaceName);
    }
}