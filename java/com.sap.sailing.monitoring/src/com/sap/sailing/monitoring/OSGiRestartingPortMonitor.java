package com.sap.sailing.monitoring;

import java.util.HashMap;
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
 * on failure. It also send out email to fixed recipients.
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Nov 26, 2012
 */
public class OSGiRestartingPortMonitor extends AbstractPortMonitor {
    Logger log = Logger.getLogger(OSGiRestartingPortMonitor.class.getName());
    
    HashMap<String, Bundle> bundles = new HashMap<String, Bundle>();
    
    public OSGiRestartingPortMonitor(Properties properties) {
        super(properties);
        
        /* store bundles so that we can reference them later without iterating every time */
        for (Bundle bundle : Activator.getContext().getBundles()) {
            bundles.put(bundle.getSymbolicName(), bundle);
        }
        
        String[] prop_services = properties.getProperty("monitor.services").split(",");
        
        for (int i=0; i<endpoints.length;i++) {
            endpoints[i].setName(prop_services[i].trim());
        }
    }

    @Override
    public void handleFailure(Endpoint endpoint) {
        Bundle bundle = bundles.get(endpoint.getName());
        
        if (bundle.getState() == BundleEvent.STARTED || bundle.getState() == BundleEvent.STOPPED) {
            try {
                bundle.stop();
            } catch (BundleException e) {
                log.severe("Could not stop " + endpoint.getName() + " trying to start anyway.");
            }
            
            try {
                bundle.start();
            } catch (BundleException e) {
                log.severe("Could not start " + endpoint.getName() + "! Handler will try again next time");
            }
        }
        
        log.info("Bundle " + endpoint.getName() + " restarted");
        
        /* only send mail if service has not failed before */
        if (!endpoint.hasFailed()) {
            try {
                Session session = Session.getDefaultInstance(this.properties, new SMTPAuthenticator());
                MimeMessage msg = new MimeMessage(session);
                
                msg.setSubject("Bundle " + endpoint.getName() + " restarted");
                msg.setContent("The Bundle " + endpoint.getName() + " has been restarted - port check on " + endpoint.getAddress().getPort() + " didn't respond!\n" +
                		"This Mail won't be send again if service continues to fail.", "text/plain");
                msg.addRecipient(RecipientType.TO, new InternetAddress(this.properties.getProperty("mail.to")));
                
                Transport ts = session.getTransport();
                ts.connect();
                ts.sendMessage(msg, msg.getRecipients(RecipientType.TO));
                ts.close();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
           String username = properties.getProperty("mail.smtp.user");
           String password = properties.getProperty("mail.smtp.password");
           return new PasswordAuthentication(username, password);
        }
    }

    @Override
    public void handleConnection(Endpoint endpoint) {
    }

}
