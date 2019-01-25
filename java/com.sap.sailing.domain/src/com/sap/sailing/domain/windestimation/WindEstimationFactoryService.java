package com.sap.sailing.domain.windestimation;

import com.sap.sailing.domain.tracking.TrackedRace;

public interface WindEstimationFactoryService {

    IncrementalWindEstimationTrack createIncrementalWindEstimationTrack(TrackedRace trackedRace);

    boolean isReady();

    void addWindEstimationModelsChangedListenerAndReceiveUpdate(WindEstimationModelsChangedListener listener);

    void removeWindEstimationModelsChangedListener(WindEstimationModelsChangedListener listener);

    void removeAllWindEstimationModelsChangedListeners();

    public interface WindEstimationModelsChangedListener {
        void modelsChangedEvent(boolean modelsReady);
    }
}
