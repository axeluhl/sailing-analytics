package com.sap.sse.security.shared;

/**
 * Objects of classes implementing this type have an {@link AccessControlList} and {@link Ownership} information.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface SecuredObject {
    AccessControlList getAccessControlList();

    Ownership getOwnership();

    void setAccessControlList(AccessControlList createAccessControlListDTO);

    void setOwnership(Ownership createOwnershipDTO);
}
