package com.sap.sailing.gwt.home.communication.user.profile.domain;

import java.io.Serializable;
import java.util.UUID;

/** This object represents a badge in a sailor profile {@link SailorProfileDTO} */
public class BadgeDTO implements Serializable {
    private static final long serialVersionUID = 2374312724838738559L;

    private UUID key;
    private String name;

    protected BadgeDTO() {

    }

    public BadgeDTO(UUID key, String name) {
        super();
        this.key = key;
        this.name = name;
    }

    public UUID getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
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
        BadgeDTO other = (BadgeDTO) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

}
