package com.sap.sailing.domain.tractracadapter;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

/**
 * Configuration parameters that can be used to connect to a TracTrac event / race.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface TracTracConfiguration extends WithQualifiedObjectIdentifier {
    String getName();
    
    String getJSONURL();
    
    String getLiveDataURI();
    
    String getStoredDataURI();
    
    /**
     * holds the path of Trac Trac to receive course updates triggered by the race committee
     * @return the TracTrac server path for course updates
     */
    String getCourseDesignUpdateURI();

    /**
     * holds the Trac Trac username used to send course updates to TracTrac
     * @return the TracTrac username
     */
    String getTracTracUsername();

    /**
     * holds the Trac Trac password used to send course updates to TracTrac
     * @return the TracTrac password
     */
    String getTracTracPassword();

    @Override
    default QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    @Override
    default HasPermissions getType() {
        return SecuredDomainType.TRACTRAC_ACCOUNT;
    }

    @Override
    default TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return getTypeRelativeObjectIdentifier(getJSONURL());
    }

    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(TracTracConfiguration config) {
        return new TypeRelativeObjectIdentifier(config.getJSONURL());
    }

    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(String jsonUrl) {
        return new TypeRelativeObjectIdentifier(jsonUrl);
    }
}
