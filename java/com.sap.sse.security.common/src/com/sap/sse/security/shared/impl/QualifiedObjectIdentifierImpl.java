package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WildcardPermission;

public class QualifiedObjectIdentifierImpl implements QualifiedObjectIdentifier {
    private static final long serialVersionUID = -1749648443005614962L;

    private final String typeIdentifier;
    private final TypeRelativeObjectIdentifier typeRelativeObjectIdentifier;
    
    private QualifiedObjectIdentifierImpl(String qualifiedObjectIdentifierAsString) {
        final int indexOfSeparator = qualifiedObjectIdentifierAsString.indexOf(QUALIFIER_SEPARATOR);
        if (indexOfSeparator < 0) {
            throw new IllegalArgumentException("Qualified object identifier must contain separator character "+QUALIFIER_SEPARATOR);
        }
        typeIdentifier = qualifiedObjectIdentifierAsString.substring(0, indexOfSeparator);
        String typeRelativeIdentifier = qualifiedObjectIdentifierAsString.substring(indexOfSeparator + 1);
        typeRelativeObjectIdentifier = new TypeRelativeObjectIdentifier(typeRelativeIdentifier);
    }

    public QualifiedObjectIdentifierImpl(String typeIdentifier, TypeRelativeObjectIdentifier typeRelativeObjectIdentifier) {
        super();
        this.typeIdentifier = typeIdentifier;
        this.typeRelativeObjectIdentifier = typeRelativeObjectIdentifier;
    }

    /**
     * This should only be used from the db, as the TypeRelativeIdentifier is not escaped
     */
    public static QualifiedObjectIdentifier fromDBWithoutEscaping(String escapedId) {
        return new QualifiedObjectIdentifierImpl(escapedId);
    }

    @Override
    public String getTypeIdentifier() {
        return typeIdentifier;
    }

    @Override
    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return typeRelativeObjectIdentifier;
    }

    @Override
    public String getStringPermission(Action action) {
        return getPermission(action).toString();
    }

    @Override
    public WildcardPermission getPermission(Action action) {
        return new WildcardPermission(getTypeIdentifier()+WildcardPermission.PART_DIVIDER_TOKEN+action.name()+WildcardPermission.PART_DIVIDER_TOKEN+
                getTypeRelativeObjectIdentifier());
    }

    @Override
    public String toString() {
        return getTypeIdentifier() + QUALIFIER_SEPARATOR + getTypeRelativeObjectIdentifier();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((typeIdentifier == null) ? 0 : typeIdentifier.hashCode());
        result = prime * result
                + ((typeRelativeObjectIdentifier == null) ? 0 : typeRelativeObjectIdentifier.hashCode());
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
        QualifiedObjectIdentifierImpl other = (QualifiedObjectIdentifierImpl) obj;
        if (typeIdentifier == null) {
            if (other.typeIdentifier != null)
                return false;
        } else if (!typeIdentifier.equals(other.typeIdentifier))
            return false;
        if (typeRelativeObjectIdentifier == null) {
            if (other.typeRelativeObjectIdentifier != null)
                return false;
        } else if (!typeRelativeObjectIdentifier.equals(other.typeRelativeObjectIdentifier))
            return false;
        return true;
    }
}
