package com.sap.sailing.domain.maneuverhash.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.maneuverhash.ManeuverCache;
import com.sap.sailing.domain.maneuverhash.ManeuverRaceFingerprint;
import com.sap.sailing.domain.maneuverhash.ManeuverRaceFingerprintFactory;
import com.sap.sailing.domain.maneuverhash.ManeuverRaceFingerprintRegistry;
import com.sap.sailing.domain.maneuverhash.SerializableManeuverCache;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;
import com.sap.sailing.domain.windestimation.IncrementalWindEstimation;

public class ManeuverCacheDelegate implements SerializableManeuverCache {
    private static final long serialVersionUID = 19872309587435L;
    private final TrackedRaceImpl race;
    private static final Logger logger = Logger.getLogger(ManeuverCacheDelegate.class.getName());
    private transient ManeuverRaceFingerprintRegistry maneuverRaceFingerprintRegistry;
    private volatile transient ManeuverCache cacheToUse;
    
    public ManeuverCacheDelegate(TrackedRaceImpl race,
            ManeuverRaceFingerprintRegistry maneuverRaceFingerprintRegistry) {
        super();
        this.race = race;
        this.maneuverRaceFingerprintRegistry = maneuverRaceFingerprintRegistry;
        this.cacheToUse = createUpdatableManeuverCache();
    }    
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.cacheToUse = (ManeuversFromDatabase) ois.readObject();
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(new ManeuversFromDatabase(getAllKnownManeuvers()));
    }
    
    @Override
    public void ensureFilled() {
        if (cacheToUse.canBeUpdated()) {
            for (final Competitor competitor : race.getShuffledCompetitors()) {
                cacheToUse.triggerUpdate(competitor);
            }
        }
    }

    @Override
    public void setManeuverRaceFingerprintRegistry(ManeuverRaceFingerprintRegistry maneuverRaceFingerprintRegistry) {
        this.maneuverRaceFingerprintRegistry = maneuverRaceFingerprintRegistry;
    }

    private Map<Competitor, List<Maneuver>> getAllKnownManeuvers() {
        final Map<Competitor, List<Maneuver>> result = new HashMap<>();
        for (final Competitor competitor : race.getRace().getCompetitors()) {
            final List<Maneuver> maneuversForCompetitor = get(competitor, /* waitForLatest */ false);
            if (maneuversForCompetitor != null) {
                result.put(competitor, maneuversForCompetitor);
            }
        }
        return result;
    }

    @Override
    public void resume() {
        final ManeuverRaceFingerprint fingerprint;
        if (maneuverRaceFingerprintRegistry != null) {
            logger.info("Compare maneuver fingerprints for race "+race.getRaceIdentifier());
            race.waitForAllRaceLogsAttached();
            fingerprint = maneuverRaceFingerprintRegistry.getManeuverRaceFingerprint(race.getRaceIdentifier());
        } else {
            fingerprint = null;
        }
        // Self-heal check (bug6241): if the fingerprint matches but the loaded maneuvers are all
        // empty for every competitor, the previous run of the server persisted a "computed and
        // empty" verdict -- likely because the maneuver detector completed before the wind
        // estimator had a chance to produce wind fixes, so spots got typed as UNKNOWN and the
        // storage step wrote empty lists. Treat that as a stale cache miss and fall through to
        // the compute path, which now sequences its storage after the wind estimator has settled
        // so it doesn't reproduce the poisoned state.
        final boolean useDbLoad;
        final Map<Competitor, List<Maneuver>> loadedManeuvers;
        if (fingerprint != null && fingerprint.matches(race)) {
            loadedManeuvers = maneuverRaceFingerprintRegistry.loadManeuvers(race, race.getRace().getCourse());
            if (isAllEmpty(loadedManeuvers)) {
                logger.info("Maneuver fingerprints match for race "+race.getRaceIdentifier()
                        +" but stored maneuvers are empty for every competitor; treating as stale cache miss and re-computing (see bug6241)");
                useDbLoad = false;
            } else {
                logger.info("Maneuver fingerprints match for race "+race.getRaceIdentifier()+"; loading from DB instead of computing");
                useDbLoad = true;
            }
        } else {
            loadedManeuvers = null;
            useDbLoad = false;
        }
        if (useDbLoad) {
            cacheToUse = new ManeuversFromDatabase(loadedManeuvers);
        } else {
            new Thread(this::computeAndStore, "Waiting for maneuvers for "+race.getName()+" after having resumed to store the results in registry")
                    .start();
        }
    }

    /**
     * Returns {@code true} iff {@code maneuvers} is {@code null}, contains no entries, or contains
     * only entries whose value is {@code null} or an empty list. Used by {@link #resume()} to
     * self-heal from a previously-persisted "computed and empty" verdict (see bug6241).
     */
    private boolean isAllEmpty(Map<Competitor, List<Maneuver>> maneuvers) {
        boolean allEmpty = true;
        if (maneuvers != null) {
            for (final List<Maneuver> forCompetitor : maneuvers.values()) {
                if (forCompetitor != null && !forCompetitor.isEmpty()) {
                    allEmpty = false;
                    break;
                }
            }
        }
        return allEmpty;
    }

    /**
     * Runs the maneuver detector via the smart-future cache, then -- once a wind estimation has
     * been installed on the tracked race and its inference has produced any wind fixes it can --
     * recalculates each competitor's maneuvers so they get re-typed using the estimator's fixes
     * (which since bug6274 are visible to the typing step via {@code trackedRace.getWind}).
     * Finally snapshots the re-typed maneuvers and persists them via the fingerprint registry.
     * <p>
     *
     * This deferred-store choreography (bug6241) is what prevents the DB from being poisoned with
     * empty / UNKNOWN-typed maneuvers on the first server run for a race that has no other wind
     * source: on subsequent server starts the fingerprint match then loads a properly-typed
     * maneuver list, which fed through {@code feedAlreadyKnownManeuversToWindEstimation} produces
     * wind fixes without needing to redetect.
     */
    private void computeAndStore() {
        logger.info("Maneuver fingerprints do not match for race "+race.getRaceIdentifier()+"; NOT loading from DB");
        if (!cacheToUse.canBeUpdated()) {
            cacheToUse = createUpdatableManeuverCache();
        }
        cacheToUse.resume();
        if (maneuverRaceFingerprintRegistry != null) {
            // First blocking pass: let the detector complete its initial run. This is what emits
            // ManeuverSpots to the wind estimator via newManeuverSpotsDetected. Spots get typed
            // using whatever wind is currently available -- typically nothing on the first pass
            // for races with no non-estimation wind source.
            for (final Competitor competitor : race.getRace().getCompetitors()) {
                cacheToUse.get(competitor, /* waitForLatest */ true);
            }
            // Sequence the persistence step after the wind estimator has been installed and has
            // finished the inference kicked off by our spots. runWhenWindEstimationInstalled
            // fires synchronously here if the estimator was installed while we were computing;
            // otherwise it registers a callback that fires once setWindEstimation is called with
            // a non-null argument, and cancels silently if the race is removed before that
            // happens. See bug6241 for the rationale.
            race.runWhenWindEstimationInstalled(this::retypeAndStoreAfterWindEstimationSettled);
        }
    }

    /**
     * Second phase of {@link #computeAndStore()}: waits for the wind estimator to drain, triggers
     * per-competitor recalculation so maneuvers get re-typed with the estimator's wind fixes,
     * then snapshots and persists. Invoked from
     * {@link com.sap.sailing.domain.tracking.TrackedRace#runWhenWindEstimationInstalled(Runnable)}.
     */
    private void retypeAndStoreAfterWindEstimationSettled() {
        final IncrementalWindEstimation windEstimation = race.getWindEstimation();
        if (windEstimation != null) {
            try {
                windEstimation.waitUntilDone();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.WARNING, "Interrupted while waiting for wind estimation to finish inference for race "
                        +race.getRaceIdentifier()+"; skipping maneuver re-type and store");
            }
        }
        if (!Thread.currentThread().isInterrupted()) {
            final Map<Competitor, List<Maneuver>> maneuvers = new HashMap<>();
            for (final Competitor competitor : race.getRace().getCompetitors()) {
                cacheToUse.recalculate(competitor);
                maneuvers.put(competitor, cacheToUse.get(competitor, /* waitForLatest */ true));
            }
            maneuverRaceFingerprintRegistry.storeManeuvers(race.getRaceIdentifier(),
                    ManeuverRaceFingerprintFactory.INSTANCE.createFingerprint(race),
                    maneuvers, race.getRace().getCourse());
        }
    }

    @Override
    public List<Maneuver> get(Competitor competitor, boolean waitForLatest) {
        return cacheToUse.get(competitor, waitForLatest);
    }

    @Override
    public void suspend() {
        cacheToUse.suspend();
    }

    @Override
    public void recalculate(Competitor competitor) {
        cacheToUse.recalculate(competitor);
    }

    @Override
    public void triggerUpdate(Competitor competitor) {
        if (!cacheToUse.canBeUpdated()) {
            logger.warning("Received a maneuver cache update trigger for competitor "+competitor.getName()+" but current cache cannot be updated; switching to an updatable cache");
            cacheToUse = createUpdatableManeuverCache();
        }
        cacheToUse.triggerUpdate(competitor);
    }

    private ManeuverCache createUpdatableManeuverCache() {
        return new ManeuversFromSmartFutureCache((DynamicTrackedRaceImpl) race);
    }

    @Override
    public boolean canBeUpdated() {
        return true;
    }
}
