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
public class PeriodicRaceListCalculationScheduler implements Runnable {
    private static final Logger logger = Logger.getLogger(PeriodicRaceListCalculationScheduler.class.getName());
    private static final long INITIAL_DELAY = 1000;
    private static final long DELAY = 50000;
    private final RacingEventService racingEventService;

    private final Set<PeriodicRaceListCalculator> listeners;

    interface PeriodicRaceListCalculator {
        /**
         * If the listener is added to the {@link PeriodicRaceListCalculationScheduler} , this is called every
         * {@link DELAY}, and is given an unsorted list of all races. This list is not modifiable and not threadsafe.
         */
        void uponUpdate(Collection<SimpleRaceInfo> collection);

        /**
         * Called by the AnniversaryCalculationScheduler, when the ChangeListener is added to the
         * PeriodicRaceListCalculationScheduler, this allows to use getFullAnniversaryData
         */
        void setCalculator(PeriodicRaceListCalculationScheduler anniversaryCalculator);
    }

    public PeriodicRaceListCalculationScheduler(RacingEventServiceImpl racingEventService,
            ScheduledExecutorService scheduledExecutorService) {
        this.racingEventService = racingEventService;
        listeners = new CopyOnWriteArraySet<>();
        scheduledExecutorService.scheduleWithFixedDelay(this, INITIAL_DELAY, DELAY, TimeUnit.MILLISECONDS);
    }

    public void addListener(PeriodicRaceListCalculator listener) {
        listener.setCalculator(this);
        listeners.add(listener);
    }

    public void removeListener(PeriodicRaceListCalculator listener) {
        listener.setCalculator(null);
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
            for (PeriodicRaceListCalculator listener : listeners) {
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