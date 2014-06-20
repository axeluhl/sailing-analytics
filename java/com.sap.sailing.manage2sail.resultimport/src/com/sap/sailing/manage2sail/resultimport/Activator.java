package com.sap.sailing.manage2sail.resultimport;

import java.net.URL;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.xrr.resultimport.ParserFactory;

public class Activator implements BundleActivator {
    private static String MANAGE2SAIL_KW_2014_URL = "http://manage2sail.com/api/public/links/event/d30883d3-2876-4d7e-af49-891af6cbae1b?accesstoken=bDAv8CwsTM94ujZ&mediaType=json";

    public void start(BundleContext bundleContext) throws Exception {
        final ScoreCorrectionProviderImpl service = new ScoreCorrectionProviderImpl(ParserFactory.INSTANCE, ResultUrlRegistry.INSTANCE);
        bundleContext.registerService(ScoreCorrectionProvider.class, service, /* properties */null);
        ResultUrlRegistry.INSTANCE.registerResultUrl(ScoreCorrectionProviderImpl.NAME, new URL(MANAGE2SAIL_KW_2014_URL));
        createAnAllCertificatesTrustingManagerforSSL();
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }  

    private void createAnAllCertificatesTrustingManagerforSSL() throws Exception {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                    throws CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                    throws CertificateException {
            }
        } };
        
        // Install the all-trusting trust manager
        final SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }     
}
