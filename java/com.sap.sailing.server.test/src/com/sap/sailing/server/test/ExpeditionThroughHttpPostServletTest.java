package com.sap.sailing.server.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;

public class ExpeditionThroughHttpPostServletTest {
    @Ignore // To run, the OSGi-based Jetty needs to run and listen on port 8888"
    @Test
    public void testConnectDisconnect() throws IOException, InterruptedException {
        int jettyPort = 8888;
        URL url = new URL("http://localhost:"+jettyPort+"/sailingserver/expedition");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setChunkedStreamingMode(/* chunklen */ 8192);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        PrintWriter requestWriter = new PrintWriter(connection.getOutputStream());
        requestWriter.println("<ping>");
        requestWriter.flush();
        Thread reader = new Thread(new Runnable() {
            public void run() {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = br.readLine();
                    while (line != null) {
                        System.out.println(line);
                        line = br.readLine();
                    }
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "ExpeditionThroughHttpPostServletTest reader");
        reader.start();
        reader.join();
        requestWriter.close();
    }
}
