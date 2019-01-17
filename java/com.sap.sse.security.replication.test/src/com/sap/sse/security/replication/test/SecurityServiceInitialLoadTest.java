package com.sap.sse.security.replication.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;

import org.junit.Test;

import com.sap.sse.common.mail.MailException;
import com.sap.sse.replication.testsupport.AbstractServerWithSingleServiceReplicationTest;
import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.impl.SecurityServiceImpl;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.PersistenceFactory;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public class SecurityServiceInitialLoadTest extends AbstractServerWithSingleServiceReplicationTest<SecurityService, SecurityServiceImpl> {
    private final static String username = "abc";
    private final static String email = "e@mail.com";
    private final static String password = "password";
    private final static String fullName = "Full Name";
    private final static String company = "Company";
    private static String accessToken = "Company";
    
    public SecurityServiceInitialLoadTest() {
        super(new AbstractSecurityReplicationTest.SecurityServerReplicationTestSetUp() {
            @Override
            protected SecurityServiceImpl createNewMaster()
                    throws MalformedURLException, IOException, InterruptedException, UserManagementException, MailException, UserGroupManagementException {
                final UserStore userStore = new UserStoreImpl(PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(),
                        PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(), "TestDefaultTenant");
                userStore.ensureDefaultRolesExist();
                userStore.loadAndMigrateUsers();
                final AccessControlStore accessControlStore = new AccessControlStoreImpl(PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(),
                        PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(), userStore);
                final SecurityServiceImpl newMaster = new SecurityServiceImpl(userStore, accessControlStore);
                newMaster.createSimpleUser(username, email, password, fullName, company,
                        /* validationBaseURL */ Locale.ENGLISH, null, null);
                accessToken = newMaster.createAccessToken(username);
                return newMaster;
            }
        });
    }

    @Test
    public void simpleMasterTest() {
        assertEquals(username, master.getUserByName(username).getName());
        assertEquals(username, master.getUserByAccessToken(accessToken).getName());
    }
    
    @Test
    public void simpleReplicaTest() {
        assertEquals(username, replica.getUserByName(username).getName());
        assertEquals(username, replica.getUserByAccessToken(accessToken).getName());
    }
}
