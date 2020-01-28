package com.sap.sse.security.storemerging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.interfaces.AccessControlStore;
import com.sap.sse.security.interfaces.UserStore;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;

public class TestEffectsOfDroppingUsersAndGroups extends AbstractStoreMergeTest {
    @Before
    public void setUp() throws IOException, UserGroupManagementException, UserManagementException {
        setUp("source_TestEffectsOfDroppingUsersAndGroups", "target_TestEffectsOfDroppingUsersAndGroups");
    }
    
    @Test
    public void testImportFromSource1ToTarget1() throws UserGroupManagementException, UserManagementException {
        final RoleDefinition sailingViewerRoleInTargetStore = StreamSupport.stream(targetUserStore.getRoleDefinitions().spliterator(), /* parallel */ false).
                filter(rd->rd.getName().equals("sailing_viewer")).findAny().get();
        // assertions against unmodified target
        final User targetAaa = targetUserStore.getUserByName("aaa");
        assertNotNull(targetAaa);
        final User targetSameEmail = targetUserStore.getUserByName("same-email");
        assertNotNull(targetSameEmail);
        assertNull(targetSameEmail.getFullName());
        assertNull(targetSameEmail.getCompany());
        assertNull(targetSameEmail.getLocale());
        final UserGroup targetGroup1 = targetUserStore.getUserGroupByName("Group1");
        final UUID targetGroup1Id = targetGroup1.getId();
        assertNotNull(targetGroup1);
        final Pair<UserStore, AccessControlStore> sourceStores = readSourceStores();
        final UserStore sourceUserStore = sourceStores.getA();
        final AccessControlStore sourceAccessControlStore = sourceStores.getB();
        // assertions against unmodified source
        final User sourceSameEmail = sourceUserStore.getUserByName("same-email");
        assertNotNull(sourceSameEmail);
        assertNotSame(sourceSameEmail, targetSameEmail);
        assertNotNull(sourceSameEmail.getFullName());
        assertNotNull(sourceSameEmail.getCompany());
        assertNotNull(sourceSameEmail.getLocale());
        assertNotSame(sourceSameEmail, targetAaa);
        final UserGroup sourceGroup1 = sourceUserStore.getUserGroupByName("Group1");
        final UUID sourceGroup1Id = sourceGroup1.getId();
        assertNotNull(sourceGroup1);
        final User sourceAaa = sourceUserStore.getUserByName("aaa");
        assertNotNull(sourceAaa);
        assertNotSame(sourceGroup1, targetUserStore.getUserGroupByName("Group1"));
        mergeSourceIntoTarget(sourceUserStore, sourceAccessControlStore);
        // assertions for merge result
        assertEquals(targetGroup1Id, targetUserStore.getUserGroupByName("Group1").getId());
        // the source group with same name but different ID is expected to have been dropped
        assertNull(targetUserStore.getUserGroup(sourceGroup1Id));
        // expect user properties of user "same-email" to have been merged:
        final User updatedTargetSameEmail = targetUserStore.getUserByName("same-email");
        assertEquals(sourceSameEmail.getFullName(), updatedTargetSameEmail.getFullName());
        assertEquals(sourceSameEmail.getCompany(), updatedTargetSameEmail.getCompany());
        assertEquals(sourceSameEmail.getLocale(), updatedTargetSameEmail.getLocale());
    }
}
