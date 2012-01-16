package com.sap.sailing.server.test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.sap.sailing.server.api.EventNameAndRaceName;

public class IdentifierSerializationTest {

    @Test
    public void identifierSerializationTest() throws IOException, ClassNotFoundException {
        EventNameAndRaceName original = new EventNameAndRaceName("TestEvent", "Test Race");
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream(pis);
        
        //Don't change order of stream creation, to prevent deadlock
        ObjectOutputStream oos = new ObjectOutputStream(pos);
        ObjectInputStream ois = new ObjectInputStream(pis);
        
        oos.writeObject(original);
        oos.flush();
        
        EventNameAndRaceName streamed = (EventNameAndRaceName) ois.readObject();
        
        ois.close();
        oos.close();
        
        Assert.assertEquals(original, streamed);
    }
    
}
