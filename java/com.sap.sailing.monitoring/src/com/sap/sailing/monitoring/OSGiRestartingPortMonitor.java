package com.sap.sailing.monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;

import com.sap.sailing.monitoring.sysinfo.SystemInformation;
import com.sap.sailing.monitoring.sysinfo.SystemInformationImpl;

/**
 * This monitor tries to restart registered services
 * on failure. It also sends out email to fixed recipients including system information.
 *
 * @author Simon Pamies (info@pamies.de)
 * @since Nov 26, 2012
 */
public class OSGiRestartingPortMonitor extends AbstractPortMonitor {
    final static private Logger log = Logger.getLogger(OSGiRestartingPortMonitor.class.getName());
    
    private final int DEFAULT_RETRY_COUNT_BEFORE_RESTART = 5;
    
    final int retryCountBeforeRestart;
    
    final Map<Endpoint, Integer> failureCount;

    SystemInformation info = null;

    public OSGiRestartingPortMonitor(Properties properties) {
        super(properties);
        failureCount = new HashMap<>();
        String[] props_bundles = properties.getProperty("monitor.bundles").split(",");
        retryCountBeforeRestart = Integer.valueOf(properties.getProperty("monitor.retrycount", ""+DEFAULT_RETRY_COUNT_BEFORE_RESTART).trim());
        for (int i=0; i<endpoints.length;i++) {
            endpoints[i].setBundleName(props_bundles[i].trim());
        }
        this.info = SystemInformationImpl.getInstance();
        log.info("Started and initialized OSGi monitor. State of SIGAR sysstats library: " + (info == null ? "INACTIVE" : "ACTIVE") + " java.library.path: " + System.getProperty("java.library.path"));
    }

    @Override
    public void handleFailure(Endpoint endpoint) {
        int myFailureCount = (failureCount.get(endpoint) == null ? 0 : failureCount.get(endpoint))+1;
        if (myFailureCount > retryCountBeforeRestart) {
            failureCount.remove(endpoint);
            boolean sysinfo_available = true;
            if (this.info == null) {
                sysinfo_available = false;
            }
    
            Bundle bundle = getBundleByName(endpoint.getBundleName());
            if (bundle == null) {
                log.severe("Couldn't find bundle "+endpoint.getBundleName()+". Cannot restart. Please check monitoring.properties file");
                try {
                    sendMail(endpoint, "Couldn't find non-responsive bundle "+endpoint.getBundleName(),
                            "Couldn't find bundle "+endpoint.getBundleName()+". Cannot restart. Please check monitoring.properties file");
                } catch (Exception e) {
                    e.printStackTrace();
                    log.log(Level.SEVERE, "handleFailure", e);
                }
            } else {
                String info_before_restart = "";
                if (sysinfo_available) {
                    info_before_restart = info.toString();
                }
                if (bundle.getState() == BundleEvent.STARTED || bundle.getState() == BundleEvent.STOPPED || bundle.getState() == 32l) {
                    try {
                        bundle.stop();
                    } catch (BundleException e) {
                        log.severe("Could not stop " + endpoint.getBundleName() + " trying to start anyway.");
                    }
                    try {
                        bundle.start();
                    } catch (BundleException e) {
                        e.printStackTrace();
                        log.severe("Could not start " + endpoint.getBundleName() + "! Handler will try again next time");
                    }
                } else {
                    log.severe("Bundle " + endpoint.getBundleName() + " not in state STARTED or ACTIVE (State: +"+bundle.getState()+")! Restart not performed.");
                }
                final String subject = "Bundle " + endpoint.getBundleName() + " restarted";
                log.info(subject);
    
                /* only send mail if service has not failed before and mailing is enabled */
                if (!endpoint.hasFailed() /* before */ && this.properties.getProperty("mail.enabled", "true").equalsIgnoreCase("true")) {
                    log.info("Sending mail to " + this.properties.getProperty("mail.to") + " saying that bundle "
                            + endpoint.getBundleName() + " was restarted");
                    try {
                        String content = "The Bundle " + endpoint.getBundleName() + " has been restarted - check on "
                                + endpoint + " didn't respond!\n"
                                + "This Mail won't be sent again if service continues to fail.";
    
                        if (sysinfo_available) {
                            content += "\n\nSystem Information BEFORE restart:\n" + info_before_restart;
                            content += "\n\nSystem information AFTER restart:\n" + info.toString();
                        } else {
                            content += "\n\nSystem information NOT available due to an error in the library (in most cases caused by native libs not being available)";
                        }
                        sendMail(endpoint, subject, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.throwing(getClass().getName(), "handleFailure", e);
                    }
                }
            }
        } else {
            log.info("Endpoint "+endpoint+" failed the "+myFailureCount+"th time since the last success. Trying again.");
            failureCount.put(endpoint, myFailureCount);
        }
    }

    private void sendMail(Endpoint endpoint, final String subject, String content) throws MessagingException, AddressException,
            NoSuchProviderException {
        Session session = Session.getInstance(this.properties, new SMTPAuthenticator());
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("root@sapsailing.com"));
        msg.setSubject(subject);
        msg.setContent(content, "text/plain");
        for (String mailAddress : this.properties.getProperty("mail.to").split(",")) {
            msg.addRecipient(RecipientType.TO, new InternetAddress(mailAddress.trim()));
        }
        Transport ts = session.getTransport();
        ts.connect();
        ts.sendMessage(msg, msg.getRecipients(RecipientType.TO));
        ts.close();
    }

    @Override
    public void handleConnection(Endpoint endpoint) {
        final Integer previousFailureCount;
        if ((previousFailureCount = failureCount.remove(endpoint)) != null) { // endpoint succeeded
            log.info("endpoint "+endpoint+" recovered after "+previousFailureCount+" failures");
        }
    }

    protected Bundle getBundleByName(String name) {
        for (Bundle bundle : Activator.getContext().getBundles()) {
            if (bundle.getSymbolicName().equalsIgnoreCase(name))
                return bundle;
        }

        return null;
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
           String username = properties.getProperty("mail.smtp.user");
           String password = properties.getProperty("mail.smtp.password");
           return new PasswordAuthentication(username, password);
        }
    }
}
