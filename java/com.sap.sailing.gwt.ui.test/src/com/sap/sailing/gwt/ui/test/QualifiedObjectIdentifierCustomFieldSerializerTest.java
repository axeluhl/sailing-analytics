package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.assertEquals;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.core.com.sap.sse.security.shared.impl.QualifiedObjectIdentifierImpl_CustomFieldSerializer;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.impl.QualifiedObjectIdentifierImpl;

/**
 * See bug 4927 (https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=4927)
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class QualifiedObjectIdentifierCustomFieldSerializerTest {
    private QualifiedObjectIdentifierImpl_CustomFieldSerializer s;
    private SerializationStreamReader reader;
    private SerializationStreamWriter writer;
    private PipedInputStream pis;
    private PipedOutputStream pos;
    private DataOutputStream dos;
    
    @Before
    public void setUp() throws SerializationException, IOException {
        s = new QualifiedObjectIdentifierImpl_CustomFieldSerializer();
        pos = new PipedOutputStream();
        pis = new PipedInputStream(pos);
        reader = Mockito.mock(SerializationStreamReader.class);
        writer = Mockito.mock(SerializationStreamWriter.class);
        final DataInputStream dis = new DataInputStream(pis);
        dos = new DataOutputStream(pos);
        Mockito.when(reader.readString()).thenAnswer(invocation->dis.readUTF());
        Mockito.doAnswer(invocation->{
            dos.writeUTF(invocation.getArgumentAt(0, String.class));
            return null;
        }).when(writer).writeString(Mockito.anyString());
    }
    
    @Test
    public void testSerializationDeserialization() throws SerializationException, IOException {
        final QualifiedObjectIdentifierImpl original = new QualifiedObjectIdentifierImpl(SecuredDomainType.TRACKED_RACE.getName(),
                new TypeRelativeObjectIdentifier("2017Dec15_2019-02-27T10:01:08.886", "R2"));
        s.serializeInstance(writer, original);
        dos.flush();
        final QualifiedObjectIdentifierImpl read = s.instantiateInstance(reader);
        assertEquals(original, read);
    }
}
