package com.sap.sailing.domain.racelog.tracking;



/**
 * If the proper identifier cannot be resolved (with an approriate {@link DeviceIdentifierSerializationHandler}),
 * for example because the OSGi context is not available
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
    public String getIdentifierType() {
        return type;
    }

    @Override
    public String getStringRepresentation() {
        return stringRepresentation;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((stringRepresentation == null) ? 0 : stringRepresentation.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlaceHolderDeviceIdentifier other = (PlaceHolderDeviceIdentifier) obj;
        if (stringRepresentation == null) {
            if (other.stringRepresentation != null)
                return false;
        } else if (!stringRepresentation.equals(other.stringRepresentation))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
