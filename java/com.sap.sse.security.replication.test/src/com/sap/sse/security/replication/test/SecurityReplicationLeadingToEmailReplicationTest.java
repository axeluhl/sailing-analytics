package com.sap.sse.security.replication.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.common.mail.MailException;
import com.sap.sse.mail.MailService;
import com.sap.sse.mail.impl.MailServiceImpl;
import com.sap.sse.mail.replication.testsupport.AbstractMailReplicationTest;
import com.sap.sse.replication.testsupport.AbstractServerWithMultipleServicesReplicationTest;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.impl.SecurityServiceImpl;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

/**
 * See if transitively triggered email operation ultimately behaves well and results in only one outbound email.
 * 
 * @author Fredrik Teschke
 */
public class SecurityReplicationLeadingToEmailReplicationTest extends AbstractServerWithMultipleServicesReplicationTest {
    private static final SecurityServerReplicationTestSetUp securitySetUp = new SecurityServerReplicationTestSetUp();
    private static final MailServerReplicationTestSetUp mailSetUp = new MailServerReplicationTestSetUp();

    private static MailServiceImpl masterMailService;
    private static MailServiceImpl replicaMailService;

    @Before
    @Override
    public void setUp() throws Exception {
        masterMailService = AbstractMailReplicationTest.createMailCountingService(true);
        replicaMailService = AbstractMailReplicationTest.createMailCountingService(false);
        super.setUp();
    }

    private static class SecurityServerReplicationTestSetUp extends
            AbstractSecurityReplicationTest.SecurityServerReplicationTestSetUp {
        @Override
        protected SecurityServiceImpl createNewMaster() throws MalformedURLException, IOException, InterruptedException {
            @SuppressWarnings("unchecked")
            ServiceTracker<MailService, MailService> trackerMock = mock(ServiceTracker.class);
            doReturn(masterMailService).when(trackerMock).getService();
            SecurityServiceImpl result = new SecurityServiceImpl(trackerMock, new UserStoreImpl());
            result.clearReplicaState();
            return result;
        }

        @Override
        protected SecurityServiceImpl createNewReplica() {
            @SuppressWarnings("unchecked")
            ServiceTracker<MailService, MailService> trackerMock = mock(ServiceTracker.class);
            doReturn(replicaMailService).when(trackerMock).getService();
            SecurityServiceImpl result = new SecurityServiceImpl(trackerMock, new UserStoreImpl());
            return result;
        }
    }

    private static class MailServerReplicationTestSetUp extends AbstractMailReplicationTest.MailServerReplicationTestSetUp {
        private static final int SERVLET_PORT = 9991;
        
        public MailServerReplicationTestSetUp() {
            super(SERVLET_PORT);
        }
        
        @Override
        protected MailServiceImpl createNewMaster() throws MailException {
            return masterMailService;
        }

        @Override
        protected MailServiceImpl createNewReplica() throws MailException {
            return replicaMailService;
        }
    }

    public SecurityReplicationLeadingToEmailReplicationTest() {
        testSetUps.add(securitySetUp);
        testSetUps.add(mailSetUp);
    }

    /**
     * See if sending an email (triggered by creating the user on the master) results in only one mail being sent
     * (on the master).
     */
    @Test
    public void triggerEmailSendByAddingUserOnMaster()
            throws UserManagementException, MailException, IllegalAccessException, InterruptedException {
        SecurityService masterSecurityService = securitySetUp.getMaster();

        final String username = "Ernie";
        final String email = "ernie@sesame-street.com";
        final String password = "BertMyFriend";
        final String validationBaseURL = null; //so that validation email is not sent        
        
        masterSecurityService.createSimpleUser(username, email, password, validationBaseURL);

        masterSecurityService.sendMail(username, "subject", "body");
        
        securitySetUp.getReplicaReplicator().waitUntilQueueIsEmpty();
        mailSetUp.getReplicaReplicator().waitUntilQueueIsEmpty();
        Thread.sleep(3000);

        assertThat("mail was not sent on replica",
                AbstractMailReplicationTest.numberOfMailsSent.get(replicaMailService), equalTo(null));
        assertThat("mail was sent on master",
                AbstractMailReplicationTest.numberOfMailsSent.get(masterMailService), equalTo(1));
    }


    /**
     * Try the other way round - have to ignore for now, no replication test seems to try applying
     * operations on the replica.
     */
    @Test
    @Ignore
    public void triggerEmailSendByAddingUserOnReplica()
            throws UserManagementException, MailException, IllegalAccessException, InterruptedException {
        SecurityService replicaSecurityService = securitySetUp.getReplica();

        final String username = "Ernie";
        final String email = "ernie@sesame-street.com";
        final String password = "BertMyFriend";
        final String validationBaseURL = null; //so that validation email is not sent        
        
        replicaSecurityService.createSimpleUser(username, email, password, validationBaseURL);

        replicaSecurityService.sendMail(username, "subject", "body");
        
        securitySetUp.getReplicaReplicator().waitUntilQueueIsEmpty();
        mailSetUp.getReplicaReplicator().waitUntilQueueIsEmpty();
        Thread.sleep(3000);

        assertThat("mail was not sent on replica",
                AbstractMailReplicationTest.numberOfMailsSent.get(replicaMailService), equalTo(null));
        assertThat("mail was sent on master",
                AbstractMailReplicationTest.numberOfMailsSent.get(masterMailService), equalTo(1));
    }
}
