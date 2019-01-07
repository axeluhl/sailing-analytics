package com.sap.sse.mail.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import com.sap.sse.common.IsManagedByCache;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.mail.MailServiceResolver;
import com.sap.sse.mail.SerializableMultipartSupplier;
import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.OperationWithResultWithIdWrapper;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;

public class MailServiceImpl implements ReplicableMailService {
    private static final Logger logger = Logger.getLogger(MailServiceImpl.class.getName());

    private Properties mailProperties;

    /**
     * The master from which this replicable is currently replicating, or <code>null</code> if this replicable is not
     * currently replicated from any master.
     */
    private ReplicationMasterDescriptor replicatingFromMaster;
    private final ConcurrentMap<OperationExecutionListener<ReplicableMailService>, OperationExecutionListener<ReplicableMailService>> operationExecutionListeners;
    private final Set<OperationWithResultWithIdWrapper<?, ?>> operationsSentToMasterForReplication = new HashSet<>();
    private ThreadLocal<Boolean> currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster = ThreadLocal
            .withInitial(() -> false);

    private final MailServiceResolver mailServiceResolver;

    public MailServiceImpl(Properties mailProperties, MailServiceResolver mailServiceResolver) {
        this.mailProperties = mailProperties;
        this.operationExecutionListeners = new ConcurrentHashMap<>();
        this.mailServiceResolver = mailServiceResolver;
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            String username = mailProperties.getProperty("mail.smtp.user");
            String password = mailProperties.getProperty("mail.smtp.password");
            return new PasswordAuthentication(username, password);
        }
    }

    protected static interface ContentSetter {
        void setContent(MimeMessage msg) throws MessagingException;
    }
    
    private boolean canSendMail() {
        return mailProperties != null && mailProperties.containsKey("mail.transport.protocol");
    }

    // protected for testing purposes
    protected void internalSendMail(String toAddress, String subject, ContentSetter contentSetter) throws MailException {
        if (canSendMail()) {
            if (toAddress != null) {
                Session session = Session.getInstance(mailProperties, new SMTPAuthenticator());
                MimeMessage msg = new MimeMessage(session);
                ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
                try {
                    msg.setFrom(new InternetAddress(mailProperties.getProperty("mail.from", "root@sapsailing.com")));
                    try {
                        msg.setSubject(MimeUtility.encodeText(subject, "utf-8", "B"));
                    } catch (UnsupportedEncodingException e) {
                        msg.setSubject(subject);
                    }
                    msg.addRecipient(RecipientType.TO, new InternetAddress(toAddress.trim()));
                    // this fixes the DCH MIME type error 
                    // see http://tanyamadurapperuma.blogspot.de/2014/01/struggling-with-nosuchproviderexception.html
                    Thread.currentThread().setContextClassLoader(javax.mail.Session.class.getClassLoader());
                    contentSetter.setContent(msg);
                    // for testing with gmail
                    //Transport ts = session.getTransport("smtps");
                    Transport ts = session.getTransport();
                    ts.connect();
                    ts.sendMessage(msg, msg.getRecipients(RecipientType.TO));
                    ts.close();
                    logger.info("mail sent to " + toAddress + " with subject " + subject);
                } catch (MessagingException e) {
                    logger.log(Level.SEVERE, "Error trying to send mail to " + toAddress, e);
                    throw new MailException(e.getMessage(), e);
                } finally {
                    Thread.currentThread().setContextClassLoader(oldClassLoader);
                }
            }
        } else {
            logger.warning("No mail properties provided. Cannot send e-mail about " + subject + " to " + toAddress
                    + ". This could also mean that this is running on a replica server in which case this is perfectly fine. "
                    + "The master "+getMasterDescriptor()+" will handle the mail sending operation in this case.");
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
    public Void internalSendMail(String toAddress, String subject, SerializableMultipartSupplier multipartSupplier) throws MailException {
        internalSendMail(toAddress, subject, new ContentSetter() {
            @Override
            public void setContent(MimeMessage msg) throws MessagingException {
                msg.setContent(multipartSupplier.get());
            }
        });
        return null;
    }

    @Override
    public void sendMail(String toAddress, String subject, SerializableMultipartSupplier multipartSupplier) throws MailException {
        apply(s -> s.internalSendMail(toAddress, subject, multipartSupplier));
    }

    @Override
    public void clearState() throws Exception {
        // nothing to clear for test support
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
        return new ObjectInputStreamResolvingAgainstCache<MailServiceResolver>(is, mailServiceResolver) {};
    }

    @Override
    public IsManagedByCache<MailServiceResolver> resolve(MailServiceResolver cache) {
        return cache.getMailService();
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
}
