package com.sap.sse.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpURLConnectionHelper {
	private static final int HTTP_MAX_REDIRECTS = 5;
	

    public static HttpURLConnection redirectConnection(URL url) throws MalformedURLException, IOException {
    	HttpURLConnection connection = null;
    	URL nextUrl = url;
    	for(int counterOfRedirects = 0; counterOfRedirects <= HTTP_MAX_REDIRECTS; counterOfRedirects ++){
    		if(connection!=null){
    			connection.disconnect();
    		}
    		connection = (HttpURLConnection) nextUrl.openConnection();
    		connection.setInstanceFollowRedirects(false);
    		connection.setRequestProperty("User-Agent", "Mozilla/5.0...");
    		connection.setDoOutput(true);
    		// Initial timeout needs to be big enough to allow the first parts of the response to reach this server
    		connection.setReadTimeout(10000);
    		if(connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP){
    			String location = connection.getHeaderField("Location");
    			nextUrl = new URL(nextUrl,location);
    		}else{
    			break;
    		}
    	}
    	return connection;
	}

}
