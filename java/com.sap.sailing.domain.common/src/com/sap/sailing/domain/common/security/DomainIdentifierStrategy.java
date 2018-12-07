package com.sap.sailing.domain.common.security;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sse.security.shared.IdentifierStrategy;
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

public interface DomainIdentifierStrategy extends IdentifierStrategy {

    static IdentifierStrategy TRACKED_RACE = new IdentifierStrategy() {

        @Override
        public <T> String getIdentifierAsString(T object) {
            RegattaAndRaceIdentifier regattaAndRaceIdentifer = (RegattaAndRaceIdentifier) object;
            return WildcardPermissionEncoder.encode(regattaAndRaceIdentifer.getRegattaName(),
                    regattaAndRaceIdentifer.getRaceName());
        }

    };

    static IdentifierStrategy MEDIA_TRACK = new IdentifierStrategy() {

        @Override
        public <T> String getIdentifierAsString(T object) {
            MediaTrack mediaTrack = (MediaTrack) object;
            return mediaTrack.dbId;
        }

    };

}
