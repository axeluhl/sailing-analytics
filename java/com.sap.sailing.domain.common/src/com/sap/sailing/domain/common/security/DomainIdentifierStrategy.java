package com.sap.sailing.domain.common.security;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sse.security.shared.IdentifierStrategy;

public interface DomainIdentifierStrategy extends IdentifierStrategy {

    static IdentifierStrategy MEDIA_TRACK = new IdentifierStrategy() {

        @Override
        public <T> String getIdentifierAsString(T object) {
            MediaTrack mediaTrack = (MediaTrack) object;
            return mediaTrack.dbId;
        }

    };

}
