package com.sap.sse.security.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ObjectInputStream;

import org.junit.Test;

import com.sap.sse.security.User;
import com.sap.sse.security.operations.CreateUser;
import com.sap.sse.security.shared.UserManagementException;

public class SimpleSecurityReplicationTest extends AbstractSecurityReplicationTest {
    @Test
    public void testSimpleReplication() throws InterruptedException, UserManagementException {
        final String username = "Ernie";
        final String email = "ernie@sesame-street.com";
        final String password = "BertMyFriend";
        final String validationURL = "http://me.to.back.com";
        final CreateUser createUser = new CreateUser(username, email, password, validationURL);
        assertNull(master.getUserByName(username));
        User user = master.apply(createUser);
        assertNotNull(user);
        assertSame(user, master.getUserByName(username));
        assertTrue(master.checkPassword(username, password));
        
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
     */
    @Test
    public void testSimpleReplicationWithLambda() throws InterruptedException, UserManagementException {
        final String username = "Ernie";
        final String email = "ernie@sesame-street.com";
        final String password = "BertMyFriend";
        final String validationBaseURL = "http://me.to.back.com";
        assertNull(master.getUserByName(username));
        User user = master.apply(master -> master.createSimpleUser(username, email, password, validationBaseURL));
        assertNotNull(user);
        assertSame(user, master.getUserByName(username));
        assertTrue(master.checkPassword(username, password));
        
        Thread.sleep(3000);
        
        User replicatedErnie = replica.getUserByName(username);
        assertNotNull(replicatedErnie);
        assertEquals(username, replicatedErnie.getName());
        assertEquals(email, replicatedErnie.getEmail());
        assertTrue(replica.checkPassword(username, password));
    }

    private void readObject(ObjectInputStream ois) {
        fail("This test case is not serializable; how did we get here in the first place...?");
    }
}
