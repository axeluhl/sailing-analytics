package com.sap.sailing.domain.windestimation;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Factory for wind estimator instances. Each wind estimator instance is meant to be assigned to a tracked race. This is
 * necessary because the wind estimation introduces state for its corresponding tracked race due to its incremental
 * computation. A new wind estimator instance can be obtained with
 * {@link #createIncrementalWindEstimationTrack(TrackedRace)}. However, before obtaining a new wind estimator instance,
 * make sure that the wind estimation is ready to estimate the wind by means of {@link #isReady()}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface WindEstimationFactoryService {

    /**
     * Creates a wind estimator instance which can be then attached to tracked race and enable wind estimation for that
     * race. The provided tracked race instance is used internally to add and remove wind fixes via
     * {@link TrackedRace#recordWind(com.sap.sailing.domain.common.Wind, com.sap.sailing.domain.common.WindSource, boolean)}
     * and {@link TrackedRace#removeWind(com.sap.sailing.domain.common.Wind, com.sap.sailing.domain.common.WindSource)},
     * as well as to retrieve additional maneuver features by competitor track analysis and access to
     * {@link PolarDataService}. This method must not be called if {@link #isReady()} returns {@code false}. Consider to
     * use {@link #addWindEstimationModelsChangedListenerAndReceiveUpdate(WindEstimationModelsChangedListener)} to
     * retrieve the ready state of the wind estimation.
     * 
     * @param trackedRace
     *            The tracked race instance for which the wind will be estimated
     * @return Stateful wind estimator which can be also seen as a wind track
     * @see #isReady()
     * @see #addWindEstimationModelsChangedListenerAndReceiveUpdate(WindEstimationModelsChangedListener)
     */
    IncrementalWindEstimation createIncrementalWindEstimationTrack(TrackedRace trackedRace);

    /**
     * Checks whether the wind estimation is ready for its use. The wind estimation maintains multiple internal machine
     * learning models including maneuver classifiers and various regressors. All these internal models must be provided
     * to the com.sap.sailing.windestimation bundle so that the wind estimation can make use of them. If the models are
     * not available, no wind can be estimated.
     * 
     * @return {@code true} if the internal machine learning models have been loaded into the wind estimation bundle,
     *         otherwise {@code false}
     */
    boolean isReady();

    /**
     * Adds a listener which receives notifications about the changes of {@link #isReady()}. After call of this method,
     * the provided listener will be notified immediately, within the execution of this method. The listener will be
     * also called if the wind estimation bundle gets detached and shutdown normally.
     * 
     * @param listener
     *            The listener which will be notified about the changes of the ready state of wind estimation
     */
    void addWindEstimationModelsChangedListenerAndReceiveUpdate(WindEstimationModelsChangedListener listener);

    /**
     * Removes already added listener so that it will not be notified about the changes of ready state of wind
     * estimation in the future.
     * 
     * @param listener
     *            The already added listener to remove
     */
    void removeWindEstimationModelsChangedListener(WindEstimationModelsChangedListener listener);

    /**
     * Removes all added listeners.
     */
    void removeAllWindEstimationModelsChangedListeners();

    /**
     * Listener for receiving notifications when the wind estimation ready state changes.
     * 
     * @author Vladislav Chumak (D069712)
     *
     */
    public interface WindEstimationModelsChangedListener {

        /**
         * Is called whenever the ready state of wind estimation changes.
         * 
         * @param modelsReady
         *            Current result of {@link WindEstimationFactoryService#isReady()}
         */
        void modelsChangedEvent(boolean modelsReady);
    }
}
