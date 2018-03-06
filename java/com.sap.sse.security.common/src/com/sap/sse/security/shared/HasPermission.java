package com.sap.sse.security.shared;

import com.sap.sse.common.WithID;
import com.sap.sse.security.shared.Permission.DefaultModes;
import com.sap.sse.security.shared.Permission.Mode;

/**
 * Types implementing this interface can have their operations protected by permissions
 * that subjects invoking the operations need to have been granted. A {@link Permission}
 * object models a type category, such as, e.g., "LEADERBOARD" or "REGATTA" or "EVENT".
 * These categories will usually not distinguish different implementation (sub-)classes
 * but will rather remain at the highest category level.<p>
 * 
 * The next part of a permission then usually is the {@link Mode} such as one of the
 * {@link DefaultModes}, describing the type of operation to be applied on the object,
 * usually abstracted and simplified to the level of "CRUD" operations.<p>
 * 
 * The last part of a permission object then identifies this object instance. The identification
 * needs to be unique within the permission type (e.g., "LEADERBOARD") and therefore does not
 * have to be universally unique. This implies that this last identification part alone will not
 * be sufficient to construct a lookup key for, e.g., ownership or ACL information. The
 * permission type part (e.g., "LEADERBOARD") needs to be joined with the identification part
 * to make for a unique key.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface HasPermission extends WithID {
    /**
     * Returns the bare {@link Permission} type without any operation mode or object identification
     */
    Permission getPermissionType();
    
    /**
     * An object-identifying part that can go in the third part of a wildcard permission to identify
     * this object, relative to the scope defined by the {@link #getPermissionType() permission type}
     * or "category", such as "LEADERBOARD" or similar.
     */
    String getRelativeObjectId();
    
    /**
     * Combines what is necessary to obtain an ID that is unique beyond the scope of the {@link #getPermissionType()
     * permission type}. For example, if this is a leaderboard object, the ID may contain the permission type name
     * (e.g., "LEADERBOARD") as a prefix preceding the leaderboard name to ensure uniqueness and discriminate against,
     * e.g., an equal-named regatta object.
     */
    @Override
    String getId();
    
    /**
     * Constructs a wildcard permission for this object for the operation mode(s) provided. The permission
     * has the {@link #getPermissionType() permission type} as its first element, the mode(s) in the second
     * place, and this object's ID in the third place of the wildcard permission returned.
     */
    WildcardPermission getPermission(Mode... operationModes);

    /**
     * Same as {@link #getPermission(Mode...)}, only that the result is returned as a wildcard permission in
     * {@link String} format.
     */
    String getStringPermission(Mode... operationModes);
}
