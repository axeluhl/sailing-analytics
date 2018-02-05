package com.sap.sse.security.shared;

import java.io.Serializable;

/**
 * Ownership information that can be used in an {@link OwnershipAnnotation} or can be attached,
 * e.g., to a serializable DTO. It tells about the user and tenant ownerships of an object.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface Ownership extends Serializable {
    SecurityUser getUserOwner();
    Tenant getTenantOwner();
}