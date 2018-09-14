package com.sap.sse.security.test;

import org.junit.Before;

import com.sap.sse.security.AbstractCompositeAuthorizingRealm;
import com.sap.sse.security.UsernamePasswordRealm;
import com.sap.sse.security.shared.Permission;

/**
 * {@link Permission} objects may specify an object ID in their third part. When checking a permission,
 * ownership information needs to be obtained for the object(s) in question because it affects the
 * permission check. For example, a user may have a role that applies its permissions only to objects
 * that the user owns or where the user belongs to the group that owns the object. Therefore, it must be
 * possible to look up the ownership information based on the object ID provided in the third part of the
 * {@link Permission} object. This test sets up objects of different kinds, specifies ownerships for them,
 * sets up users and roles with qualifications and then validates that the correct permissions emerge based
 * on a successful ownership lookup with the object ID provided by the permission.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ObjectIdFromPermissionForAccessControlStoreTest {
    private AbstractCompositeAuthorizingRealm realm;
    
    @Before
    public void setUp() {
        realm = new UsernamePasswordRealm();
    }
}
