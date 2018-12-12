package com.sap.sailing.domain.common.security;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sse.security.shared.IdentifierStrategy;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

public interface DomainIdentifierStrategy extends IdentifierStrategy {

    /**
     * Identifier strategy that is used for tracked races. A tracked race
     * is identified by a {@link RegattaAndRaceIdentifier} that has to be used
     * for building a permission.
     */
    static IdentifierStrategy TRACKED_RACE = new IdentifierStrategy() {

        @Override
        public String getIdentifierAsString(Object... object) {
            assert object.length == 1;
            if (object[0] instanceof WithQualifiedObjectIdentifier) {
                WithQualifiedObjectIdentifier withQualifiedObjectIdentifier = ((WithQualifiedObjectIdentifier) object[0]);
                return withQualifiedObjectIdentifier.getIdentifier().getTypeRelativeObjectIdentifier();
            } else {
                RegattaAndRaceIdentifier regattaAndRaceIdentifer = (RegattaAndRaceIdentifier) object[0];
                return WildcardPermissionEncoder.encode(regattaAndRaceIdentifer.getRegattaName(),
                        regattaAndRaceIdentifer.getRaceName());
            }
        }
    };

    /**
     * Identifier strategy that is used for {@link MediaTrack}s.
     */
    static IdentifierStrategy MEDIA_TRACK = new IdentifierStrategy() {

        @Override
        public String getIdentifierAsString(Object... object) {
            assert object.length == 1;
            MediaTrack mediaTrack = (MediaTrack) object[0];
            return mediaTrack.dbId;
        }

    };

    /**
     * type-relative identifier is the {@link ScoreCorrectionProvider#getName() name of the score correction provider}
     * and the URL, encoded using the {@link WildcardPermissionEncoder#encodeStringList(String...)} method
     */
    static IdentifierStrategy RESULT_IMPORT_URL = new IdentifierStrategy() {

        @Override
        public String getIdentifierAsString(Object... object) {
            assert object.length == 2;
            ScoreCorrectionProvider scoreCorrectionProvider = (ScoreCorrectionProvider) object[0];
            return WildcardPermissionEncoder.encode(scoreCorrectionProvider.getName(), object[1].toString());
        }

    };

}
