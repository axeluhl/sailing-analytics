package com.sap.sse.security.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

import org.junit.Test;

import com.sap.sse.security.User;
import com.sap.sse.security.operations.CreateUser;
import com.sap.sse.security.operations.SecurityOperation;
import com.sap.sse.security.shared.UserManagementException;

public class SimpleSecurityReplicationTest extends AbstractSecurityReplicationTest {
    private static final Logger logger = Logger.getLogger(SimpleSecurityReplicationTest.class.getName());
    
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
    
    @Test
    public void testLambdaDoesNotSerializeEnclosingInstance() throws InterruptedException, UserManagementException, IOException {
        final String username = "Ernie";
        final String email = "ernie@sesame-street.com";
        final String password = "BertMyFriend";
        final String validationBaseURL = "http://me.to.back.com";
        assertNull(master.getUserByName(username));
        SecurityOperation<User> op = (SecurityOperation<User>) (master -> master.createSimpleUser(username, email, password, validationBaseURL));
        new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(op); // the test case is not serializable; if it were, its writeObject() would thrown an exception
        SecurityOperation<String> opWithRefToEnclosingInstance = (SecurityOperation<String>) (master -> this.toString());
        try {
            new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(opWithRefToEnclosingInstance);
            fail("Expected the lambda not to be serializable because it references the non-serializable enclosing instance");
        } catch (NotSerializableException nse) {
            // this is expected
            logger.info("Caught expected exception "+nse);
        }
    }
}
