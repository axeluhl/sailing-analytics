package com.sap.sse.security.shared;

import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

public class TypeRelativeObjectIdentifier {

    private String typeRelativeIdentifer;
    
    public TypeRelativeObjectIdentifier(String... identifers) {
        typeRelativeIdentifer = WildcardPermissionEncoder.encode(identifers);
    }
    
    @Override
    public String toString() {
        return typeRelativeIdentifer;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((typeRelativeIdentifer == null) ? 0 : typeRelativeIdentifer.hashCode());
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
        TypeRelativeObjectIdentifier other = (TypeRelativeObjectIdentifier) obj;
        if (typeRelativeIdentifer == null) {
            if (other.typeRelativeIdentifer != null)
                return false;
        } else if (!typeRelativeIdentifer.equals(other.typeRelativeIdentifer))
            return false;
        return true;
    }

}
