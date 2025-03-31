package com.sap.sailing.expeditionconnector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.util.logging.Logger;

import com.sap.sailing.expeditionconnector.impl.HttpServletMessageReceiver;
import com.sap.sailing.expeditionconnector.impl.HttpServletMessageReceiver.Receiver;

/**
 * Uses {@link HttpServletMessageReceiver} to connect to a server-side servlet that streams all expedition messages
 * it receives. The messages received will immediately be forwarded to a UDP port specified in the constructor.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class FromHttpToUdpMirror implements Receiver {
    private static final Logger logger = Logger.getLogger(FromHttpToUdpMirror.class.getName());
    
    private final DatagramSocket udpSocket;
    private final DatagramPacket udpPacket;
    private final byte[] buf;
    private final HttpServletMessageReceiver receiver;

    private final boolean verbose;
    
    public FromHttpToUdpMirror(URL url, int targetPort, boolean verbose) throws SocketException {
        this.verbose = verbose;
        buf = new byte[65536];
        udpSocket = new DatagramSocket();
        udpPacket = new DatagramPacket(buf, buf.length, InetAddress.getLoopbackAddress(), targetPort);
        receiver = new HttpServletMessageReceiver(url, this);
    }
    
    public void start() throws IOException, InterruptedException {
        receiver.connect();
    }
    
    public void stop() throws IOException {
        receiver.stop();
    }

    @Override
    public boolean received(byte[] bytes) {
        if (verbose) {
            logger.info("Forwarding: "+new String(bytes));
        }
        System.arraycopy(bytes, 0, buf, 0, bytes.length);
        udpPacket.setLength(bytes.length);
        try {
            udpSocket.send(udpPacket);
        } catch (IOException e) {
            logger.info("Exception while trying to forward message: "+e.getMessage());
            logger.throwing(FromHttpToUdpMirror.class.getName(), "received", e);
        }
        return false;
    }
    
    /**
     * @param args [-v] &lt;url&gt; &lt;targetUDPPort&gt;<br>where the URL refers to the servlet from which to receive Expedition
     * wind data and the <code>targetUDPPort</code> is the UDP port on the local host to which to forward the wind data. If <code>-v</code>
     * is provided, messages received will be logged with level <code>INFO</code>.
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        int i=0;
        boolean verbose = false;
        URL url = null;
        Integer targetUDPPort = null;
        while (i<args.length && (url == null || targetUDPPort == null)) {
            if (args[i].equals("-v")) {
                verbose = true;
            } else if (url == null) {
                url = new URL(args[i]);
            } else {
                targetUDPPort = Integer.valueOf(args[i]);
            }
            i++;
        }
        if (url == null || targetUDPPort == null) {
            usage();
        } else {
            FromHttpToUdpMirror mirror = new FromHttpToUdpMirror(url, targetUDPPort, verbose);
            mirror.start();
            // wait forever
            while (true) {
                synchronized (url) {
                    url.wait();
                }
                // avoid mirror's garbage collection
                logger.info("Continuing with "+mirror);
            }
        }
    }

    private static void usage() {
        System.err.println("Usage: java "+FromHttpToUdpMirror.class.getName()+" [-v] <url> <targetUDPPort>");
    }
}
