package com.sap.sse.mail.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sap.sse.common.mail.MailException;
import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.impl.OperationWithResultWithIdWrapper;
import com.sap.sse.util.ClearStateTestSupport;

public class MailServiceImpl implements ReplicableMailService, ClearStateTestSupport {
    private static final Logger logger = Logger.getLogger(MailServiceImpl.class.getName());

    private Properties mailProperties;

    /**
     * The master from which this replicable is currently replicating, or <code>null</code> if this replicable is not
     * currently replicated from any master.
     */
    private ReplicationMasterDescriptor replicatingFromMaster;
    private final ConcurrentHashMap<OperationExecutionListener<ReplicableMailService>, OperationExecutionListener<ReplicableMailService>> operationExecutionListeners;
    private Set<OperationWithResultWithIdWrapper<?, ?>> operationsSentToMasterForReplication;
    private ThreadLocal<Boolean> currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster = ThreadLocal
            .withInitial(() -> false);

    public MailServiceImpl(Properties mailProperties) {
        this.mailProperties = mailProperties;
        this.operationExecutionListeners = new ConcurrentHashMap<>();
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            String username = mailProperties.getProperty("mail.smtp.user");
            String password = mailProperties.getProperty("mail.smtp.password");
            return new PasswordAuthentication(username, password);
        }
    }

    private static interface ContentSetter {
        void setContent(MimeMessage msg) throws MessagingException;
    }

    private void internalSendMail(String toAddress, String subject, ContentSetter contentSetter) throws MailException {
        if (mailProperties != null && mailProperties.containsKey("mail.transport.protocol")) {
            if (toAddress != null) {
                Session session = Session.getInstance(mailProperties, new SMTPAuthenticator());
                MimeMessage msg = new MimeMessage(session);
                try {
                    msg.setFrom(new InternetAddress(mailProperties.getProperty("mail.from", "root@sapsailing.com")));
                    msg.setSubject(subject);
                    contentSetter.setContent(msg);
                    msg.addRecipient(RecipientType.TO, new InternetAddress(toAddress.trim()));
                    Transport ts = session.getTransport();
                    ts.connect();
                    ts.sendMessage(msg, msg.getRecipients(RecipientType.TO));
                    ts.close();
                    logger.info("mail sent to " + toAddress + " with subject " + subject);
                } catch (MessagingException e) {
                    logger.log(Level.SEVERE, "Error trying to send mail to " + toAddress, e);
                    throw new MailException(e.getMessage());
                }
            }
        } else {
            logger.warning("No mail properties provided. Cannot send e-mail about "
                    + subject
                    + " to "
                    + toAddress
                    + ". This could also mean that this is running on a replica server in which case this is perfectly fine.");
        }
    }

    @Override
    public Void internalSendMail(String toAddress, String subject, String body) throws MailException {
        internalSendMail(toAddress, subject, new ContentSetter() {
            @Override
            public void setContent(MimeMessage msg) throws MessagingException {
                msg.setContent(body, "text/plain");
            }
        });
        return null;
    }

    @Override
    public void sendMail(String toAddress, String subject, String body) throws MailException {
        apply(s -> s.internalSendMail(toAddress, subject, body));
    }

    @Override
    public Void internalSendMail(String toAddress, String subject, Multipart multipartContent) throws MailException {
        internalSendMail(toAddress, subject, new ContentSetter() {
            @Override
            public void setContent(MimeMessage msg) throws MessagingException {
                msg.setContent(multipartContent);
            }
        });
        return null;
    }

    @Override
    public void sendMail(String toAddress, String subject, Multipart multipartContent) throws MailException {
        apply(s -> s.internalSendMail(toAddress, subject, multipartContent));
    }

    // ----------------- Replication -------------
    @Override
    public void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        // do nothing
    }

    @Override
    public Serializable getId() {
        return getClass().getName();
    }

    @Override
    public ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException {
        return new ObjectInputStream(is);
    }

    @Override
    public void initiallyFillFromInternal(ObjectInputStream is) throws IOException, ClassNotFoundException,
            InterruptedException {
        // send mails only from master
        mailProperties = null;
    }

    @Override
    public void serializeForInitialReplicationInternal(ObjectOutputStream objectOutputStream) throws IOException {
        // do nothing
    }

    @Override
    public Iterable<OperationExecutionListener<ReplicableMailService>> getOperationExecutionListeners() {
        return operationExecutionListeners.keySet();
    }

    @Override
    public void addOperationExecutionListener(OperationExecutionListener<ReplicableMailService> listener) {
        operationExecutionListeners.put(listener, listener);
    }

    @Override
    public void removeOperationExecutionListener(OperationExecutionListener<ReplicableMailService> listener) {
        operationExecutionListeners.remove(listener);
    }

    @Override
    public ReplicationMasterDescriptor getMasterDescriptor() {
        return replicatingFromMaster;
    }

    @Override
    public void startedReplicatingFrom(ReplicationMasterDescriptor master) {
        this.replicatingFromMaster = master;
    }

    @Override
    public void stoppedReplicatingFrom(ReplicationMasterDescriptor master) {
        this.replicatingFromMaster = null;
    }

    @Override
    public void addOperationSentToMasterForReplication(
            OperationWithResultWithIdWrapper<ReplicableMailService, ?> operationWithResultWithIdWrapper) {
        this.operationsSentToMasterForReplication.add(operationWithResultWithIdWrapper);
    }

    @Override
    public boolean hasSentOperationToMaster(OperationWithResult<ReplicableMailService, ?> operation) {
        return this.operationsSentToMasterForReplication.remove(operation);
    }

    @Override
    public boolean isCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster() {
        return currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster.get();
    }

    @Override
    public void setCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster(
            boolean currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster) {
        this.currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster
                .set(currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster);
    }

    @Override
    public void clearState() throws Exception {
        mailProperties.clear();
    }
}
