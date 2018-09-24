package com.sap.sse.security.shared;

import java.io.Serializable;

import com.sap.sse.security.shared.HasPermissions.Action;

/**
 * In permission strings, object identifiers used in the third part of the permission need to be unique only within the
 * scope defined by the type to which the object belongs. The type is assumed to be encoded as the first part of a
 * permission. For example, the permission string "LEADERBORAD:READ:My Leaderboard" has "My Leaderboard" as an object
 * identifier that needs to be unique only within the "LEADERBOARD" type's scope. In order to make the identifier unique
 * across types, the type name needs to be added to the identifier. Objects whose class implements this interface
 * combine a type-relative object identifier with a type identifier into an identifier that is assumed to be unique
 * across types.
 * <p>
 * 
 * {@link Object#hashCode() Hash code} and {@link Object#equals(Object) equality} are defined based on hash codes and
 * equality definitions of the two strings returned by {@link #getTypeIdentifier()} and
 * {@link #getTypeRelativeObjectIdentifier()}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface QualifiedObjectIdentifier extends Serializable {
    /**
     * The separator character used to separate the permission type {@link #name()} from the object identifier
     * when providing a {@link #getQualifiedObjectIdentifier(String) qualified object identifier}.
     */
    char QUALIFIER_SEPARATOR = '/';
    
    /**
     * A type identifier that must not contain the {@link #QUALIFIER_SEPARATOR}.
     */
    String getTypeIdentifier();
    
    /**
     * An object identifier that has to be unique only within the scope of the type identified by {@link #getTypeIdentifier()}
     */
    String getTypeRelativeObjectIdentifier();
    
    /**
     * Constructs a permission in {@link String} form that can be parsed into a {@link WildcardPermission} and which
     * describes the {@code action} on the object identified by this object.
     */
    String getStringPermission(Action action);
    
    /**
     * Constructs a permission which describes the {@code action} on the object identified by this object.
     */
    WildcardPermission getPermission(Action action);
    
    /**
     * @return the concatenation of {@link #getTypeIdentifier()}, {@link #QUALIFIER_SEPARATOR} and
     *         {@link #getTypeRelativeObjectIdentifier()}
     */
    @Override
    String toString();
}
