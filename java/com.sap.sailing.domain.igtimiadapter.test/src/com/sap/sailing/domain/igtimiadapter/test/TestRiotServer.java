package com.sap.sailing.domain.igtimiadapter.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.igtimi.IgtimiStream.Msg;
import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.riot.RiotServer;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.shared.util.Wait;

public class TestRiotServer {
    private static final Logger logger = Logger.getLogger(TestRiotServer.class.getName());

    private static class TestListener implements BulkFixReceiver {
        private final List<Fix> fixes = new ArrayList<>();
        
        @Override
        public void received(Iterable<Fix> fixes) {
            Util.addAll(fixes, this.fixes);
        }
        
        public Iterable<Fix> getAllFixesReceived() {
            return fixes;
        }
    }
    
    @Test
    public void testSendingAFewMessages() throws Exception {
        final RiotServer riot = RiotServer.create();
        final TestListener listener = new TestListener();
        riot.addListener(listener);
        try (final Socket socket = new Socket("localhost", riot.getPort())) {
            final OutputStream socketOutputStream = socket.getOutputStream();
            final InputStream is = getClass().getResourceAsStream("/windbot_startup_jan.protobuf");
            int b;
            while ((b=is.read()) != -1) {
                socketOutputStream.write(b);
            }
            socketOutputStream.flush();
            final boolean[] finished = new boolean[1];
            new Thread(()->{
                final InputStream socketInputStream;
                try {
                    socketInputStream = socket.getInputStream();
                    while (!finished[0]) {
                        final Msg message = Msg.parseDelimitedFrom(socketInputStream);
                        logger.info("Received message from Riot server:\n"+message);
                    }
                } catch (SocketException e) {
                    // we expect a SocketException when the socket is closed
                } catch (IOException ioe) {
                    logger.log(Level.SEVERE, "Exception trying to read from test Riot server", ioe);
                }
            }).start();
            Wait.wait(()->Util.size(listener.getAllFixesReceived()) >= 218, /* timeout */ Optional.of(Duration.ONE_MINUTE), /* sleepBetweenAttempts */ Duration.ONE_SECOND);
            finished[0] = true;
            riot.stop();
            is.close();
            socketOutputStream.close();
            assertEquals(218, Util.size(listener.getAllFixesReceived()));
        } finally {
            riot.stop(); // may be redundant to the stop() call above, but it's idempotent and important to call at least once in case of exceptions
        }
    }
}
