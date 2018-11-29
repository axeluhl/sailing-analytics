package com.sap.sse.security.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

import com.sap.sse.common.mail.MailException;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.User;

public class SimpleSecurityReplicationTest extends AbstractSecurityReplicationTest {
    @Test
    public void testSimpleReplicationOfUserCreation() throws InterruptedException, UserManagementException, MailException, IllegalAccessException, UserGroupManagementException {
        final String username = "Ernie";
        final String email = "ernie@sesame-street.com";
        final String password = "BertMyFriend";
        final String fullName = "Ernie's Full Name";
        final String company = "Ernie's Company";
        final String validationBaseURL = "http://me.to.back.com";
        assertNull(master.getUserByName(username));
        User user = master.createSimpleUser(username, email, password, fullName, company, Locale.ENGLISH,
                validationBaseURL);
        assertNotNull(user);
        assertSame(user, master.getUserByName(username));
        assertTrue(master.checkPassword(username, password));
        final String emailValidationSecret = user.getValidationSecret();
        
        replicaReplicator.waitUntilQueueIsEmpty();
        Thread.sleep(3000);
        
        User replicatedErnie = replica.getUserByName(username);
        assertNotNull(replicatedErnie);
        assertEquals(username, replicatedErnie.getName());
        assertEquals(email, replicatedErnie.getEmail());
        assertTrue(replica.checkPassword(username, password));
        assertEquals(emailValidationSecret, replicatedErnie.getValidationSecret());
        assertEquals(fullName, replicatedErnie.getFullName());
        assertEquals(company, replicatedErnie.getCompany());
        
        // check that incremental replication of access token handling works
        final String accessToken = master.createAccessToken(username);
        replicaReplicator.waitUntilQueueIsEmpty();
        Thread.sleep(3000);
        assertEquals(username, replica.getUserByAccessToken(accessToken).getName());
    }

    @Test
    public void testSimpleReplicationOfUserEmailChange() throws InterruptedException, UserManagementException, MailException, IllegalAccessException, UserGroupManagementException {
        final String username = "Ernie";
        final String email = "ernie@sesame-street.com";
        final String newEmail = "ernie2@sesame-street.com";
        final String password = "BertMyFriend";
        final String validationBaseURL = "http://me.to.back.com";
        final String fullName = "Ernie's Full Name";
        final String company = "Ernie's Company";
        User user = master.createSimpleUser(username, email, password, fullName, company, Locale.ENGLISH,
                validationBaseURL);
        user.setFullName(fullName);
        user.setCompany(company);
        final String emailValidationSecretAfterCreation = user.getValidationSecret();
        master.updateSimpleUserEmail(username, newEmail, validationBaseURL);
        final String emailValidationSecretAfterChangingEmail = user.getValidationSecret();
        assertFalse(emailValidationSecretAfterChangingEmail.equals(emailValidationSecretAfterCreation));
        assertEquals(newEmail, user.getEmail());
        
        replicaReplicator.waitUntilQueueIsEmpty();
        Thread.sleep(3000);
        
        User replicatedErnie = replica.getUserByName(username);
        assertNotNull(replicatedErnie);
        assertEquals(username, replicatedErnie.getName());
        assertEquals(newEmail, replicatedErnie.getEmail());
        assertEquals(emailValidationSecretAfterChangingEmail, replicatedErnie.getValidationSecret());
        assertEquals(fullName, replicatedErnie.getFullName());
        assertEquals(company, replicatedErnie.getCompany());
    }

    @Test
    public void testSimpleReplicationOfUserPasswordChange() throws InterruptedException, UserManagementException, MailException, IllegalAccessException, UserGroupManagementException {
        final String username = "Ernie";
        final String email = "ernie@sesame-street.com";
        final String password = "BertMyFriend";
        final String newPassword = "ErnieAndBert";
        final String validationBaseURL = "http://me.to.back.com";
        master.createSimpleUser(username, email, password,
                /* fullName */ null, /* company */ null, Locale.ENGLISH, validationBaseURL);
        master.updateSimpleUserPassword(username, newPassword);
        assertTrue(master.checkPassword(username, newPassword));
        
        replicaReplicator.waitUntilQueueIsEmpty();
        Thread.sleep(3000);
        
        User replicatedErnie = replica.getUserByName(username);
        assertNotNull(replicatedErnie);
        assertEquals(username, replicatedErnie.getName());
        assertFalse(replica.checkPassword(username, password));
        assertTrue(replica.checkPassword(username, newPassword));
    }

    @Test
    public void testReplicationOfPasswordReset() throws InterruptedException, UserManagementException, MailException, IllegalAccessException, UserGroupManagementException {
        final String username = "Ernie";
        final String email = "ernie@sesame-street.com";
        final String password = "BertMyFriend";
        final String validationBaseURL = "http://me.to.back.com/validateemail";
        final String passwordResetBaseURL = "http://me.to.back.com/passwordreset";
        User user = master.createSimpleUser(username, email, password,
                /* fullName */ null, /* company */ null, Locale.ENGLISH, validationBaseURL);
        master.validateEmail(username, user.getValidationSecret());
        assertTrue(user.isEmailValidated());
        master.resetPassword(username, passwordResetBaseURL);
        String passwordResetSecret = user.getPasswordResetSecret();
        
        replicaReplicator.waitUntilQueueIsEmpty();
        Thread.sleep(3000);
        
        User replicatedErnie = replica.getUserByName(username);
        assertNotNull(replicatedErnie);
        assertTrue(replicatedErnie.isEmailValidated());
        assertEquals(passwordResetSecret, replicatedErnie.getPasswordResetSecret());
    }
}
