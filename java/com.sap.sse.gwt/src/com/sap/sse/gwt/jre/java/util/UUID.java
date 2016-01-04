package java.util;

import java.io.Serializable;
import java.util.Random;

/**
 * <p><b>Do not use!</b></p>
 * <p>Emulated version of {@link java.util.UUID}. Use {@link java.util.UUID} instead!</p>
 * Information source: http://concentricsky.com/blog/2011/mar/emulating-jre-classes-gwt 
 */
public class UUID implements Serializable, Comparable<UUID> {
    private static final long serialVersionUID = -3096287568577694784L;
    
    private static final Random numberGenerator = new Random();

    public static UUID fromString(String value) {
        return new UUID(value);
    }
    
    public static UUID randomUUID() {
        //Creating a random byte array, analog to java.util.UUID.randomUUID()
        byte[] randomBytes = new byte[16];
        numberGenerator.nextBytes(randomBytes);
        randomBytes[6]  &= 0x0f;  /* clear version        */
        randomBytes[6]  |= 0x40;  /* set to version 4     */
        randomBytes[8]  &= 0x3f;  /* clear variant        */
        randomBytes[8]  |= 0x80;  /* set to IETF variant  */

        //Converting the byte array to two longs, analog to the constructor
        //UUID(byte[] data) of java.util.UUID
        long mostSigBits = 0;
        long leastSigBits = 0;
        for (int i=0; i<8; i++)
            mostSigBits = (mostSigBits << 8) | (randomBytes[i] & 0xff);
        for (int i=8; i<16; i++)
            leastSigBits = (leastSigBits << 8) | (randomBytes[i] & 0xff);
        
        //Converting the two longs to a String, analog to java.util.UUID.toString()
        return new UUID((digits(mostSigBits >> 32, 8) + "-" +
                         digits(mostSigBits >> 16, 4) + "-" +
                         digits(mostSigBits, 4) + "-" +
                         digits(leastSigBits >> 48, 4) + "-" +
                         digits(leastSigBits, 12)));
    }

    /** Returns val represented by the specified number of hex digits. */
    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
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
