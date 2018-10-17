package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

/**
 * Configuration parameters that can be used to connect to a SwissTiming event.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface SwissTimingConfiguration extends WithQualifiedObjectIdentifier {
    String getName();
    
    String getJsonURL();
    
    String getHostname();
    
    Integer getPort();

    String getUpdateURL();

    String getUpdateUsername();

    String getUpdatePassword();

    @Override
    default QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getJsonURL());
    }

    @Override
    default HasPermissions getType() {
        return SecuredDomainType.SWISS_TIMING_ACCOUNT;
    }
}
