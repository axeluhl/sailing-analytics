package com.sap.sse.security.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.sse.security.User;
import com.sap.sse.security.shared.MailException;
import com.sap.sse.security.shared.UserManagementException;

public class SimpleSecurityReplicationTest extends AbstractSecurityReplicationTest {
    /**
     * It's too bad that we can't use this inside a GWT bundle which is currently constrained to Java7... If it was only possible
     * to extract the *.server package such that it could be compiled with Java8.
     * @throws IllegalAccessException 
     */
    @Test
    public void testSimpleReplicationOfUserCreation() throws InterruptedException, UserManagementException, MailException, IllegalAccessException {
        final String username = "Ernie";
        final String email = "ernie@sesame-street.com";
        final String password = "BertMyFriend";
        final String validationBaseURL = "http://me.to.back.com";
        assertNull(master.getUserByName(username));
        User user = master.createSimpleUser(username, email, password, validationBaseURL);
        assertNotNull(user);
        assertSame(user, master.getUserByName(username));
        assertTrue(master.checkPassword(username, password));
        
        replicaReplicator.waitUntilQueueIsEmpty();
        Thread.sleep(3000);
        
        User replicatedErnie = replica.getUserByName(username);
        assertNotNull(replicatedErnie);
        assertEquals(username, replicatedErnie.getName());
        assertEquals(email, replicatedErnie.getEmail());
        assertTrue(replica.checkPassword(username, password));
    }

    /**
     * It's too bad that we can't use this inside a GWT bundle which is currently constrained to Java7... If it was only possible
     * to extract the *.server package such that it could be compiled with Java8.
     * @throws IllegalAccessException 
     */
    @Test
    public void testSimpleReplicationOfUserEmailChange() throws InterruptedException, UserManagementException, MailException, IllegalAccessException {
        final String username = "Ernie";
        final String email = "ernie@sesame-street.com";
        final String newEmail = "ernie2@sesame-street.com";
        final String password = "BertMyFriend";
        final String validationBaseURL = "http://me.to.back.com";
        User user = master.createSimpleUser(username, email, password, validationBaseURL);
        master.updateSimpleUserEmail(username, newEmail, validationBaseURL);
        assertEquals(newEmail, user.getEmail());
        
        replicaReplicator.waitUntilQueueIsEmpty();
        Thread.sleep(3000);
        
        User replicatedErnie = replica.getUserByName(username);
        assertNotNull(replicatedErnie);
        assertEquals(username, replicatedErnie.getName());
        assertEquals(newEmail, replicatedErnie.getEmail());
    }
}
