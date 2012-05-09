package com.sap.sailing.server.replication;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.jms.JMSException;
import javax.jms.TopicSubscriber;

/**
 * Identifies a master server instance from which a replica can obtain an initial load and continuous updates.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 *
 */
public interface ReplicationMasterDescriptor {

    URL getReplicationRegistrationRequestURL() throws MalformedURLException;

    URL getInitialLoadURL() throws MalformedURLException;

    TopicSubscriber getTopicSubscriber(String clientID) throws JMSException, UnknownHostException;
    
    int getJMSPort();
    
    int getServletPort();

    String getHostname();
}
