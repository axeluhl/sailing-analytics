package com.sap.sse.security.storemerging;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.interfaces.AccessControlStore;
import com.sap.sse.security.interfaces.UserStore;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;

public class TestRoleHandlingInUserGroupMerge extends AbstractStoreMergeTest {
    @Before
    public void setUp() throws IOException, UserGroupManagementException, UserManagementException {
        setUp("source_TestRoleHandlingInUserGroupMerge", "target_TestRoleHandlingInUserGroupMerge");
    }
    
    @Test
    public void testImportFromSource1ToTarget1() throws UserGroupManagementException, UserManagementException {
        // assertions against unmodified target
        final Pair<UserStore, AccessControlStore> sourceStores = readSourceStores();
        final UserStore sourceUserStore = sourceStores.getA();
        final AccessControlStore sourceAccessControlStore = sourceStores.getB();
        // assertions against unmodified source
        mergeSourceIntoTarget(sourceUserStore, sourceAccessControlStore);
        // assertions for merge result
    }
}
