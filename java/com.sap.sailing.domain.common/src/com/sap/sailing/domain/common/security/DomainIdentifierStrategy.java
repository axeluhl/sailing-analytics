package com.sap.sailing.domain.common.security;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sse.security.shared.IdentifierStrategy;
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

public interface DomainIdentifierStrategy extends IdentifierStrategy {

    /**
     * Identifier strategy that is used for tracked races. A tracked race
     * is identified by a {@link RegattaAndRaceIdentifier} that has to be used
     * for building a permission.
     */
    static IdentifierStrategy TRACKED_RACE = new IdentifierStrategy() {

        @Override
        public <T> String getIdentifierAsString(T object) {
            RegattaAndRaceIdentifier regattaAndRaceIdentifer = (RegattaAndRaceIdentifier) object;
            return WildcardPermissionEncoder.encode(regattaAndRaceIdentifer.getRegattaName(),
                    regattaAndRaceIdentifer.getRaceName());
        }

    };

    /**
     * Identifier strategy that is used for {@link MediaTrack}s.
     */
    static IdentifierStrategy MEDIA_TRACK = new IdentifierStrategy() {

        @Override
        public <T> String getIdentifierAsString(T object) {
            MediaTrack mediaTrack = (MediaTrack) object;
            return mediaTrack.dbId;
        }

    };

}
