package com.sap.sailing.monitoring;

import java.net.InetSocketAddress;
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
    
    HashMap<InetSocketAddress, String> endpointservices = new HashMap<InetSocketAddress, String>();
    HashMap<String, Bundle> bundles = new HashMap<String, Bundle>();
    
    public OSGiRestartingPortMonitor(InetSocketAddress[] endpoints, String[] services, int interval) {
        super(endpoints, interval);
        
        /* store bundles so that we can reference them later */
        for (Bundle bundle : Activator.getContext().getBundles()) {
            bundles.put(bundle.getSymbolicName(), bundle);
        }
        
        for (int i=0; i<endpoints.length;i++) {
            endpointservices.put(endpoints[i], services[i]);
        }
    }

    @Override
    public void handleFailure(InetSocketAddress endpoint) {
        String servicename = endpointservices.get(endpoint);
        Bundle bundle = bundles.get(servicename);
        
        if (bundle.getState() == BundleEvent.STARTED || bundle.getState() == BundleEvent.STOPPED) {
            try {
                bundle.stop();
            } catch (BundleException e) {
                log.severe("Could not stop " + servicename + " trying to start anyway.");
            }
            
            try {
                bundle.start();
            } catch (BundleException e) {
                log.severe("Could not start " + servicename + "! Handler will try again next time");
            }
        }
        
        log.info("Bundle " + servicename + " restarted");
        
        /* Send out mail */
        Properties props = new Properties();
        props.setProperty("mail.from", "s.pamies@banality.de");
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", "mail.banality.de");
        props.setProperty("mail.smtp.port", "25");
        props.setProperty("mail.smtp.auth", "true");
        
        try {
            Session session = Session.getDefaultInstance(props, new SMTPAuthenticator());
            MimeMessage msg = new MimeMessage(session);
            
            msg.setSubject("Bundle " + servicename + " restarted");
            msg.setContent("The Bundle " + servicename + " has been restarted - port check on " + endpoint.getPort() + " didn't respond!", "text/plain");
            msg.addRecipient(RecipientType.TO, new InternetAddress("spamsch@gmail.com"));
            
            Transport ts = session.getTransport();
            ts.connect();
            ts.sendMessage(msg, msg.getRecipients(RecipientType.TO));
            ts.close();
            
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
           String username = "pamiesmail@banality.de";
           String password = "qay?234wsx";
           return new PasswordAuthentication(username, password);
        }
    }

    @Override
    public void handleConnection(InetSocketAddress endpoint) {
    }

}
