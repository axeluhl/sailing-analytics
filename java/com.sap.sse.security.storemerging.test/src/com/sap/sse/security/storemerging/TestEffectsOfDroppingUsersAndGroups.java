package com.sap.sse.security.storemerging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.interfaces.AccessControlStore;
import com.sap.sse.security.interfaces.UserStore;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.UsernamePasswordAccount;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;

public class TestEffectsOfDroppingUsersAndGroups extends AbstractStoreMergeTest {
    @Before
    public void setUp() throws IOException, UserGroupManagementException, UserManagementException {
        setUp("source_TestEffectsOfDroppingUsersAndGroups", "target_TestEffectsOfDroppingUsersAndGroups");
    }
    
    @Test
    public void testImportFromSource1ToTarget1() throws UserGroupManagementException, UserManagementException {
        final String PREFERENCE_NAME = "preference";
        final String PREFERENCE_VALUE = "value";
        // *********** assertions against unmodified target ***********
        final User targetAaa = targetUserStore.getUserByName("aaa");
        assertNotNull(targetAaa);
        assertNull(targetUserStore.getPreference(targetAaa.getName(), PREFERENCE_NAME));
        final UserGroup targetAaaTenant = targetUserStore.getUserGroupByName("aaa-tenant");
        assertNotNull(targetAaaTenant);
        final User targetSameEmail = targetUserStore.getUserByName("same-email");
        assertNotNull(targetSameEmail);
        assertNull(targetSameEmail.getFullName());
        assertNull(targetSameEmail.getCompany());
        assertNull(targetSameEmail.getLocale());
        assertFalse(targetSameEmail.isEmailValidated());
        final UsernamePasswordAccount targetSameEmailAccount = (UsernamePasswordAccount) targetSameEmail.getAccount(AccountType.USERNAME_PASSWORD);
        final byte[] targetSameEmailAccountSalt = targetSameEmailAccount.getSalt();
        final String targetSameEmailAccountSaltedPassword = targetSameEmailAccount.getSaltedPassword();
        assertNull(targetUserStore.getPreference(targetSameEmail.getName(), PREFERENCE_NAME));
        assertTrue(Util.isEmpty(targetSameEmail.getPermissions()));
        final UserGroup targetGroup1 = targetUserStore.getUserGroupByName("Group1");
        final UUID targetGroup1Id = targetGroup1.getId();
        assertNotNull(targetGroup1);
        // *********** reading source stores ***********
        final Pair<UserStore, AccessControlStore> sourceStores = readSourceStores();
        final UserStore sourceUserStore = sourceStores.getA();
        final AccessControlStore sourceAccessControlStore = sourceStores.getB();
        // *********** assertions against unmodified source ***********
        final User sourceSameEmail = sourceUserStore.getUserByName("same-email");
        assertNotNull(sourceSameEmail);
        assertNotSame(sourceSameEmail, targetSameEmail);
        assertNotNull(sourceSameEmail.getFullName());
        assertNotNull(sourceSameEmail.getCompany());
        assertNotNull(sourceSameEmail.getLocale());
        assertNotSame(sourceSameEmail, targetSameEmail);
        final UsernamePasswordAccount sourceSameEmailAccount = (UsernamePasswordAccount) sourceSameEmail.getAccount(AccountType.USERNAME_PASSWORD);
        final byte[] sourceSameEmailAccountSalt = sourceSameEmailAccount.getSalt();
        assertNotEquals(targetSameEmailAccountSalt, sourceSameEmailAccountSalt);
        final String sourceSameEmailAccountSaltedPassword = sourceSameEmailAccount.getSaltedPassword();
        assertNotEquals(targetSameEmailAccountSaltedPassword, sourceSameEmailAccountSaltedPassword);
        assertTrue(sourceSameEmail.isEmailValidated());
        // we expect the following permissions on sourceSameEmail:
        // LEADERBOARD:UPDATE, SERVER:DATA_MINING and SERVER:DATA_MINING:unknown server name-server
        assertFalse(Util.isEmpty(Util.filter(sourceSameEmail.getPermissions(), p->p.getParts().size() == 2 &&
                p.getParts().get(0).contains("LEADERBOARD") &&
                p.getParts().get(1).contains("UPDATE"))));
        assertFalse(Util.isEmpty(Util.filter(sourceSameEmail.getPermissions(), p->p.getParts().size() == 2 &&
                p.getParts().get(0).contains("SERVER") &&
                p.getParts().get(1).contains("DATA_MINING"))));
        assertFalse(Util.isEmpty(Util.filter(sourceSameEmail.getPermissions(), p->p.getParts().size() == 3 &&
                p.getParts().get(0).contains("SERVER") &&
                p.getParts().get(1).contains("DATA_MINING") && p.getParts().get(2).contains("unknown server name-server"))));
        // the sailing_viewer role qualified for the admin user (which is merged with the target's admin user) is expected to be kept
        assertTrue(StreamSupport.stream(sourceSameEmail.getRoles().spliterator(), /* parallel */ false).
            filter(rd->rd.getName().equals("sailing_viewer") && rd.getQualifiedForUser()!=null&&rd.getQualifiedForUser().getName().equals("admin")).findAny().isPresent());
        // the sailing_viewer role qualified for the admin-tenant group (which is merged with the target's admin-tenant group) is expected to be kept
        assertTrue(StreamSupport.stream(sourceSameEmail.getRoles().spliterator(), /* parallel */ false).
            filter(rd->rd.getName().equals("sailing_viewer") && rd.getQualifiedForTenant()!=null&&rd.getQualifiedForTenant().getName().equals("admin-tenant")).findAny().isPresent());
        // the sailing_viewer role qualified for the aaa user (which is dropped) is expected to be dropped
        assertTrue(StreamSupport.stream(sourceSameEmail.getRoles().spliterator(), /* parallel */ false).
            filter(rd->rd.getName().equals("sailing_viewer") && rd.getQualifiedForUser()!=null&&rd.getQualifiedForUser().getName().equals("aaa")).findAny().isPresent());
        // the sailing_viewer role qualified for the aaa-tenant user (which is expected to be dropped because its user is dropped) is expected to be dropped
        assertTrue(StreamSupport.stream(sourceSameEmail.getRoles().spliterator(), /* parallel */ false).
            filter(rd->rd.getName().equals("sailing_viewer") && rd.getQualifiedForTenant()!=null&&rd.getQualifiedForTenant().getName().equals("aaa-tenant")).findAny().isPresent());
        // the unqualified admin role is expected to be dropped
        assertTrue(StreamSupport.stream(sourceSameEmail.getRoles().spliterator(), /* parallel */ false).
            filter(rd->rd.getName().equals("admin") && rd.getQualifiedForTenant()==null && rd.getQualifiedForUser()==null).findAny().isPresent());
        final UserGroup sourceGroup1 = sourceUserStore.getUserGroupByName("Group1");
        assertNotSame(sourceGroup1, targetUserStore.getUserGroupByName("Group1"));
        final UUID sourceGroup1Id = sourceGroup1.getId();
        assertNotNull(sourceGroup1);
        final User sourceAaa = sourceUserStore.getUserByName("aaa");
        assertNotNull(sourceAaa);
        final AccessControlListAnnotation sourceSameEmailACL = sourceAccessControlStore.getAccessControlList(sourceSameEmail.getIdentifier());
        assertNotNull(sourceSameEmailACL);
        // we expect the source user "same-email" to have an ACL on it that allows READ for Group1 which is dropped:
        assertTrue(sourceSameEmailACL.getAnnotation().getActionsByUserGroup().get(sourceGroup1).contains("READ"));
        // we expect the source user "same-email" to have an ACL on it that allows READ_PUBLIC for all users (null group):
        assertTrue(sourceSameEmailACL.getAnnotation().getActionsByUserGroup().get(null).contains("READ_PUBLIC"));
        // we expect the source user "same-email" to have an ACL on it that allows * for admin-tenant:
        assertTrue(sourceSameEmailACL.getAnnotation().getActionsByUserGroup().get(
                sourceUserStore.getUserGroupByName("admin-tenant")).contains("*"));
        final UserGroup sourceAaaTenant = sourceUserStore.getUserGroupByName("aaa-tenant");
        assertNotNull(sourceAaaTenant);
        assertNotSame(sourceAaaTenant, targetAaaTenant);
        assertTrue(sourceAaaTenant.contains(sourceAaa));
        assertTrue(sourceAaaTenant.contains(sourceSameEmail)); // to see if that gets merged into targetAaaTenant
        // setting a preference on a source user that will be merged:
        sourceUserStore.setPreference(sourceSameEmail.getName(), PREFERENCE_NAME, PREFERENCE_VALUE);
        sourceUserStore.setPreference(sourceAaa.getName(), PREFERENCE_NAME, PREFERENCE_VALUE);
        // *********** merge ***********
        mergeSourceIntoTarget(sourceUserStore, sourceAccessControlStore);
        // *********** assertions for merge result ***********
        assertEquals(targetGroup1Id, targetUserStore.getUserGroupByName("Group1").getId());
        // the source group with same name but different ID is expected to have been dropped
        assertNull(targetUserStore.getUserGroup(sourceGroup1Id));
        // expect user properties of user "same-email" to have been merged:
        final User updatedTargetSameEmail = targetUserStore.getUserByName("same-email");
        assertEquals(sourceSameEmail.getFullName(), updatedTargetSameEmail.getFullName());
        assertEquals(sourceSameEmail.getCompany(), updatedTargetSameEmail.getCompany());
        assertEquals(sourceSameEmail.getLocale(), updatedTargetSameEmail.getLocale());
        // expect email to be validated because it was validated in the source
        assertTrue(updatedTargetSameEmail.isEmailValidated());
        // expect original account/password information to be unchanged:
        final UsernamePasswordAccount newTargetSameEmailAccount = (UsernamePasswordAccount) targetUserStore.getUserByName("same-email").getAccount(AccountType.USERNAME_PASSWORD);
        assertEquals(targetSameEmailAccountSalt, newTargetSameEmailAccount.getSalt());
        final String newTargetSameEmailAccountSaltedPassword = newTargetSameEmailAccount.getSaltedPassword();
        assertEquals(targetSameEmailAccountSaltedPassword, newTargetSameEmailAccountSaltedPassword);
        // the aaa-tenant group is a tricky case: its only user is expected to have been dropped,
        // but its name equals that of a group in target, and originally it had an "aaa" user in it;
        // drop or merge?
        assertFalse(targetAaaTenant.contains(targetSameEmail)); // expecting the group to have been dropped
        // If the sourceAaaTenant group was dropped then so have to be roles qualified by it
        // the sailing_viewer role qualified for the admin user (which is merged with the target's admin user) is expected to be kept
        assertTrue(StreamSupport.stream(targetSameEmail.getRoles().spliterator(), /* parallel */ false).
                filter(rd->rd.getName().equals("sailing_viewer") && rd.getQualifiedForUser()!=null&&rd.getQualifiedForUser().getName().equals("admin")).findAny().isPresent());
        // the sailing_viewer role qualified for the admin-tenant group (which is merged with the target's admin-tenant group) is expected to be kept
        assertTrue(StreamSupport.stream(targetSameEmail.getRoles().spliterator(), /* parallel */ false).
            filter(rd->rd.getName().equals("sailing_viewer") && rd.getQualifiedForTenant()!=null&&rd.getQualifiedForTenant().getName().equals("admin-tenant")).findAny().isPresent());
        // the sailing_viewer role qualified for the aaa user (which is dropped) is expected to be dropped
        assertFalse(StreamSupport.stream(targetSameEmail.getRoles().spliterator(), /* parallel */ false).
            filter(rd->rd.getName().equals("sailing_viewer") && rd.getQualifiedForUser()!=null&&rd.getQualifiedForUser().getName().equals("aaa")).findAny().isPresent());
        // the sailing_viewer role qualified for the aaa-tenant user (which is expected to be dropped because its user is dropped) is expected to be dropped
        assertFalse(StreamSupport.stream(targetSameEmail.getRoles().spliterator(), /* parallel */ false).
            filter(rd->rd.getName().equals("sailing_viewer") && rd.getQualifiedForTenant()!=null&&rd.getQualifiedForTenant().getName().equals("aaa-tenant")).findAny().isPresent());
        // the unqualified admin role is expected to be dropped
        assertFalse(StreamSupport.stream(sourceSameEmail.getRoles().spliterator(), /* parallel */ false).
            filter(rd->rd.getName().equals("admin") && rd.getQualifiedForTenant()==null && rd.getQualifiedForUser()==null).findAny().isPresent());
        // validate preference handling:
        // the aaa user got dropped; so should its preference from the source
        assertNull(targetUserStore.getPreference(targetAaa.getName(), PREFERENCE_NAME));
        // the same-email user got merged; so should its preference
        assertEquals(PREFERENCE_VALUE, targetUserStore.getPreference(targetSameEmail.getName(), PREFERENCE_NAME));
        // we expect the following permissions on targetSameEmail:
        // SERVER:DATA_MINING:unknown server name-server
        // (the LEADERBOARD:UPDATE and SERVER:DATA_MINING permissions are expected to have been dropped
        // for lack of object qualification)
        assertTrue(Util.isEmpty(Util.filter(targetSameEmail.getPermissions(), p->p.getParts().size() == 2 &&
                p.getParts().get(0).contains("LEADERBOARD") &&
                p.getParts().get(1).contains("UPDATE"))));
        assertTrue(Util.isEmpty(Util.filter(targetSameEmail.getPermissions(), p->p.getParts().size() == 2 &&
                p.getParts().get(0).contains("SERVER") &&
                p.getParts().get(1).contains("DATA_MINING"))));
        assertFalse(Util.isEmpty(Util.filter(targetSameEmail.getPermissions(), p->p.getParts().size() == 3 &&
                p.getParts().get(0).contains("SERVER") &&
                p.getParts().get(1).contains("DATA_MINING") && p.getParts().get(2).contains("unknown server name-server"))));
        // we expect the ACL on same-email for Group1 that granted READ to have been dropped because Group1 was dropped
        final AccessControlListAnnotation targetSameEmailACL = targetAccessControlStore.getAccessControlList(targetSameEmail.getIdentifier());
        // we expect the target user "same-email" to have no ACL for Group1 which is dropped:
        assertTrue(targetSameEmailACL == null || targetSameEmailACL.getAnnotation().getActionsByUserGroup().get(targetGroup1) == null);
        // we expect the target user "same-email" to have an ACL for the null group (all users) granting READ_PUBLIC
        assertTrue(targetSameEmailACL.getAnnotation().getActionsByUserGroup().get(null).contains("READ_PUBLIC"));
        // we expect the target user "same-email" to have an ACL on it that allows * for admin-tenant because that group was merged
        assertTrue(targetSameEmailACL.getAnnotation().getActionsByUserGroup().get(
                targetUserStore.getUserGroupByName("admin-tenant")).contains("*"));
    }
}
