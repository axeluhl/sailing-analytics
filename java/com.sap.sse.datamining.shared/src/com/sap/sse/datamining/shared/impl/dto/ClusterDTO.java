package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;

/**
 * The {@link #signifier} is relevant for the {@link #equals(Object)} and {@link #hashCode()} implementations.
 * Optionally, a {@link #localizedName} can be provided at construction time, e.g., as a localized, human-readable
 * version of the {@link #signifier}. If not, the {@link #localizedName} will be assigned from the {@link #signifier}.
 * <p>
 * 
 * The {@link #signifier} must be set explicitly at construction time. The {@link #localizedName} can optionally
 * be provided as a {@link LocalizedNameProvider} through which it will be evaluated lazily, particularly during
 * serialization with the custom field serializer. 
 * 
 * @author Lennart Hensler
 * @author Axel Uhl (d043530)
 *
 */
public class ClusterDTO implements Serializable {
    private static final long serialVersionUID = -2962035066215989018L;
    
    private final String signifier;
    
    /**
     * If {@code null}, the {@link #signifier} will be returned by {@link #toString} instead.
     */
    private String localizedName;
    
    /**
     * Upon serialization the {@link #signifier} must be obtained from a {@link #localizedNameProvider} and
     * cached in the object serialized.
     */
    private transient LocalizedNameProvider localizedNameProvider;

    public ClusterDTO(String signifier) {
        this(signifier, signifier);
    }
    
    public ClusterDTO(String signifier, String localizedName) {
        this.signifier = signifier;
        this.localizedName = localizedName;
    }
    
    /**
     * Can be used for lazy initialization of objects of this type. As long as the {@link ClusterDTO}
     * object is only passed around and not treated with {@link #hashCode} or {@link #equals} or the
     * {@link #getLocalizedName()} method is called explicitly then the signifier string is not calculated.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    public static interface LocalizedNameProvider {
        String getLocalizedName();
    }
    
    /**
     * A possibility to initialize the {@link #getSignifier() signifier} lazily so that the providing function is
     * called only when the signifier is really asked for.
     */
    public ClusterDTO(String signifier, LocalizedNameProvider localizedNameProvider) {
        this(signifier, /* toStringResult */ (String) null);
        this.localizedNameProvider = localizedNameProvider;
    }
    
    public String getSignifier() {
        return signifier;
    }
    
    @Override
    public String toString() {
        return getLocalizedName();
    }
    
    public String getLocalizedName() {
        if (localizedName == null && localizedNameProvider != null) {
            localizedName = localizedNameProvider.getLocalizedName();
        }
        return localizedName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSignifier() == null) ? 0 : getSignifier().hashCode());
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
        ClusterDTO other = (ClusterDTO) obj;
        if (getSignifier() == null) {
            if (other.getSignifier() != null)
                return false;
        } else if (!getSignifier().equals(other.getSignifier()))
            return false;
        return true;
    }
    
}
