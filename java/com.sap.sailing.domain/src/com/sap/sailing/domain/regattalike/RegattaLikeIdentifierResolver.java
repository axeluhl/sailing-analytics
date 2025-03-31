package com.sap.sailing.domain.regattalike;


/**
 * This interface is used for replication purposes. It resolves the {@link RegattaLikeIdentifier}, creating
 * a different type of replication operation depending on the type of the Regatta-like object.
 */
public interface RegattaLikeIdentifierResolver {
    
    /**
     * Resolves a identifier template for regattas
     */
    void resolveOnRegattaIdentifier(RegattaAsRegattaLikeIdentifier identifier);
    
    /**
     * Resolves a identifier template for flexible leaderboards
     */
    void resolveOnFlexibleLeaderboardIdentifier(FlexibleLeaderboardAsRegattaLikeIdentifier identifier);
}
