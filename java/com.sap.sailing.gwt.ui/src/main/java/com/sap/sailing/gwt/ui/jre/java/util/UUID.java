package java.util;

import java.io.Serializable;

/**
 * <p><b>Do not use!</b></p>
 * 
 * <p>Emulated version of {@link java.util.UUID}. Use {@link java.util.UUID} instead!</p>
 */
public class UUID implements Serializable, Comparable<UUID> {

    private static final long serialVersionUID = -3096287568577694784L;
    
    public static UUID fromString(String value) {
        return new UUID(value);
    }
    
    public static UUID randomUUID() {
        throw new UnsupportedOperationException("Not supported for emulation");
    }
    
    private String uuidAsString;
    
    @SuppressWarnings("unused")
    private UUID() { /* gwt */
        this.uuidAsString = null;
    }
    
    protected UUID(String value) {
        this.uuidAsString = value;
    }
    
    /**
     * @throws UnsupportedOperationException
     */
    public UUID(long mostSigBits, long leastSigBits) {
        throw new UnsupportedOperationException("Not supported for emulation");
    }

    @Override
    public int compareTo(UUID o) {
        return uuidAsString.compareTo(o.uuidAsString);
    }
    
    @Override
    public int hashCode() {
        return uuidAsString.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        UUID other = (UUID) obj;
        if (uuidAsString == null) {
            if (other.uuidAsString != null)
                return false;
        } else if (!uuidAsString.equals(other.uuidAsString))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return uuidAsString;
    }

}
