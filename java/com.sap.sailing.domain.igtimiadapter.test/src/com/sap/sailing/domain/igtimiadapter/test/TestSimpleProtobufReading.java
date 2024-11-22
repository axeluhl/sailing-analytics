package com.sap.sailing.domain.igtimiadapter.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.junit.Test;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistry;
import com.igtimi.IgtimiStream.Msg;

public class TestSimpleProtobufReading {
    private static final Logger logger = Logger.getLogger(TestSimpleProtobufReading.class.getName());

    @Test
    public void testReadWindbotStartup() throws IOException {
        final InputStream is = getClass().getResourceAsStream("/windbot_startup.protobuf");
        final ExtensionRegistry er = ExtensionRegistry.newInstance();
        final CodedInputStream cis = CodedInputStream.newInstance(is);
        for (int i=0; i<4; i++) { // we know there are four full messages in the stream:
            final int lengthOfMessage = cis.readRawVarint32();
            assertNotEquals(0, lengthOfMessage);
            final int oldLimit = cis.pushLimit(lengthOfMessage);
            final Msg msg = Msg.parseFrom(cis, er);
            assertNotNull(msg);
            logger.info(String.format("Parsed the following message:\n %s", msg.toString()));
            cis.popLimit(oldLimit);
        }
        is.close();
    }

    @Test
    public void testReadJansWindbotStartup() throws IOException {
        final InputStream is = getClass().getResourceAsStream("/windbot_startup_jan.protobuf");
        final ExtensionRegistry er = ExtensionRegistry.newInstance();
        final CodedInputStream cis = CodedInputStream.newInstance(is);
        for (int i=0; i<20; i++) { // we know there are four full messages in the stream:
            final int lengthOfMessage = cis.readRawVarint32();
            assertNotEquals(0, lengthOfMessage);
            final int oldLimit = cis.pushLimit(lengthOfMessage);
            final Msg msg = Msg.parseFrom(cis, er);
            assertNotNull(msg);
            logger.info(String.format("Parsed the following message #%d:\n %s", i, msg.toString()));
            cis.popLimit(oldLimit);
        }
        is.close();
    }
}
