package com.sap.sailing.monitoring;

import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message.RecipientType;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;

/**
 * This monitor tries to restart registered services
 * on failure. It also sends out email to fixed recipients.
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Nov 26, 2012
 */
public class OSGiRestartingPortMonitor extends AbstractPortMonitor {
    Logger log = Logger.getLogger(OSGiRestartingPortMonitor.class.getName());
    
    public OSGiRestartingPortMonitor(Properties properties) {
        super(properties);
        
        String[] props_bundles = properties.getProperty("monitor.bundles").split(",");
        
        for (int i=0; i<endpoints.length;i++) {
            endpoints[i].setBundleName(props_bundles[i].trim());
        }
    }

    @Override
    public void handleFailure(Endpoint endpoint) {
        Bundle bundle = getBundleByName(endpoint.getBundleName());
        if (bundle == null) {
            log.severe("Couldn't find bundle "+endpoint.getBundleName()+". Cannot restart. Please check monitoring.properties file");
        } else {
            if (bundle.getState() == BundleEvent.STARTED || bundle.getState() == BundleEvent.STOPPED) {
                try {
                    bundle.stop();
                } catch (BundleException e) {
                    log.severe("Could not stop " + endpoint.getBundleName() + " trying to start anyway.");
                }
                try {
                    bundle.start();
                } catch (BundleException e) {
                    log.severe("Could not start " + endpoint.getBundleName() + "! Handler will try again next time");
                }
            }

            log.info("Bundle " + endpoint.getBundleName() + " restarted");

            /* only send mail if service has not failed before */
            if (!endpoint.hasFailed() /* before */) {
                log.info("Sending mail to " + this.properties.getProperty("mail.to") + " saying that bundle "
                        + endpoint.getBundleName() + " was restarted");
                try {
                    Session session = Session.getDefaultInstance(this.properties, new SMTPAuthenticator());
                    MimeMessage msg = new MimeMessage(session);

                    msg.setFrom(new InternetAddress("root@sapsailing.com"));
                    msg.setSubject("Bundle " + endpoint.getBundleName() + " restarted");
                    msg.setContent("The Bundle " + endpoint.getBundleName() + " has been restarted - check on "
                            + endpoint + " didn't respond!\n"
                            + "This Mail won't be sent again if service continues to fail.", "text/plain");
                    for (String mailAddress : this.properties.getProperty("mail.to").split(",")) {
                        msg.addRecipient(RecipientType.TO, new InternetAddress(mailAddress.trim()));
                    }
                    Transport ts = session.getTransport();
                    ts.connect();
                    ts.sendMessage(msg, msg.getRecipients(RecipientType.TO));
                    ts.close();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    log.throwing(getClass().getName(), "handleFailure", ex);
                }
            }
        }
    }
    
    @Override
    public void handleConnection(Endpoint endpoint) {
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
