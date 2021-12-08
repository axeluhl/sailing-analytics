package com.sap.sse.security.shared;

import java.io.Serializable;

public class StringMessagesKey implements Serializable {
    private static final long serialVersionUID = -8849263412380501977L;
    private String key;

    public StringMessagesKey(String messageKey) {
        assert messageKey.matches("^[a-zA-Z0-9_]*$");
        this.key = messageKey;
    }
    
    /**
     * For GWT Serialization only
     */
    @Deprecated
    public StringMessagesKey() {}

    public String getKey() {
        return key;
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
        StringMessagesKey other = (StringMessagesKey) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }
}
