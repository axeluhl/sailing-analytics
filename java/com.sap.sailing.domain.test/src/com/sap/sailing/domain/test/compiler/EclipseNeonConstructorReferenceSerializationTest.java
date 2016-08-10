package com.sap.sailing.domain.test.compiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

public class EclipseNeonConstructorReferenceSerializationTest {
    
    @Test
    public void testConstructorReferenceSerializationThatFailsOnEclipseNeon() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        
        SerializableSupplier factory = TypeImplementation::new;
        oos.writeObject(factory);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ois.readObject();
        
    }

}
