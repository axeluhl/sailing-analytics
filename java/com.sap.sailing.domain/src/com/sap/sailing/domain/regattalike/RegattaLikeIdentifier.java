package com.sap.sailing.domain.regattalike;

import java.io.Serializable;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sse.common.Named;

/**
 * Interface for identifying objects that are Regatta-like ("similar to a Regatta"). This is normally a
 * {@link Regatta}. In some special cases however a {@link FlexibleLeaderboard} is used to semantically represent
 * a Regatta - e.g. if a special series uses individual races from other events/regattas.
 */
public interface RegattaLikeIdentifier extends Serializable, Named {
    /**
     * Resolves this identifier for replication with the help of the given resolver.
     */
    void resolve(RegattaLikeIdentifierResolver resolver);
}
