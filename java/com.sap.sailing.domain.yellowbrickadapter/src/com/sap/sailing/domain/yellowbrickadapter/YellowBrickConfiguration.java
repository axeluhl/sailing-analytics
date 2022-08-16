package com.sap.sailing.domain.yellowbrickadapter;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.Named;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

/**
 * Configuration parameters that can be used to connect to a YellowBrick event / race.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface YellowBrickConfiguration extends Named, WithQualifiedObjectIdentifier {
    String getRaceUrl();
    String getUsername();
    String getPassword();
    String getCreatorName();
    @Override
    default QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    @Override
    default HasPermissions getPermissionType() {
        return SecuredDomainType.YELLOWBRICK_ACCOUNT;
    }
    
    default TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return getTypeRelativeObjectIdentifier(getRaceUrl(), getCreatorName());
    }
    
    static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(String raceUrl, String creatorName) {
        return creatorName == null ? new TypeRelativeObjectIdentifier(raceUrl)
                : new TypeRelativeObjectIdentifier(raceUrl, creatorName);
    }
}
