package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.IsManagedBySharedDomainFactory;
import com.sap.sailing.domain.base.SharedDomainFactory;


/**
 * If the proper identifier cannot be resolved, for example because the OSGi context is not available
 * on the smartphone receiving the {@code DevicieIdentifier} via its {@code RaceLog}, then this
 * class can be used as a placeholder, that will at least offer the {@code type} and {@code string representation}
 * of the original identifier.
 * 
 * @author Fredrik Teschke
 *
 */
public class PlaceHolderDeviceIdentifier implements DeviceIdentifier {
    private static final long serialVersionUID = 8642469389180406246L;
    public static final String TYPE = "PLACEHOLDER";
    
    private final String type;
    private final String stringRepresentation;
    
    public PlaceHolderDeviceIdentifier(String type, String stringRepresentation) {
        this.type = type;
        this.stringRepresentation = stringRepresentation;
    }

    @Override
    public IsManagedBySharedDomainFactory resolve(SharedDomainFactory domainFactory) {
        return this;
    }

    @Override
    public String getIdentifierType() {
        return type;
    }

    @Override
    public String getStringRepresentation() {
        return stringRepresentation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeviceIdentifier) {
            DeviceIdentifier other = (DeviceIdentifier) obj;
            return type.equals(other.getIdentifierType()) &&
                    stringRepresentation.equals(other.getStringRepresentation());
        }
        return false;
    }
}
