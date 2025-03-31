package com.sap.sailing.domain.regattalike;

import java.io.Serializable;

import com.sap.sse.common.Named;

/**
 * Identifies a {@link RegattaLike regatta-like} object.
 * @author Fredrik Teschke
 *
 */
public interface RegattaLikeIdentifier extends Serializable, Named {
    /**
     * Resolves this identifier for replication with the help of the given resolver.
     */
    void resolve(RegattaLikeIdentifierResolver resolver);
    
    /**
     * A unique type name (for serialization purposes).
     */
    String getIdentifierType();
}
