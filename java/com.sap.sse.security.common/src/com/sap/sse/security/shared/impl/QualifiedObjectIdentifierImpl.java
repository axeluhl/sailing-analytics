package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.QualifiedObjectIdentifier;

public class QualifiedObjectIdentifierImpl implements QualifiedObjectIdentifier {
    private final String typeIdentifier;
    private final String typeRelativeObjectIdentifier;
    
    /**
     * Parses the output of {@link QualifiedObjectIdentifier#toString()} into an instance of this class.
     * 
     * @param qualifiedObjectIdentifierAsString
     *            expected to contain {@link QualifiedObjectIdentifier#QUALIFIER_SEPARATOR}; an
     *            {@link IllegalArgumentException will result otherwise.
     */
    public QualifiedObjectIdentifierImpl(String qualifiedObjectIdentifierAsString) {
        final int indexOfSeparator = qualifiedObjectIdentifierAsString.indexOf(QUALIFIER_SEPARATOR);
        if (indexOfSeparator < 0) {
            throw new IllegalArgumentException("Qualified object identifier must contain separator character "+QUALIFIER_SEPARATOR);
        }
        typeIdentifier = qualifiedObjectIdentifierAsString.substring(0, indexOfSeparator);
        typeRelativeObjectIdentifier = qualifiedObjectIdentifierAsString.substring(indexOfSeparator+1);
    }
    
    public QualifiedObjectIdentifierImpl(String typeIdentifier, String typeRelativeObjectIdentifier) {
        super();
        this.typeIdentifier = typeIdentifier;
        this.typeRelativeObjectIdentifier = typeRelativeObjectIdentifier;
    }

    @Override
    public String getTypeIdentifier() {
        return typeIdentifier;
    }

    @Override
    public String getTypeRelativeObjectIdentifier() {
        return typeRelativeObjectIdentifier;
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
