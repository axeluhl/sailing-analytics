package com.sap.sailing.manage2sail.resultimport;

import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.competitorimport.CompetitorProvider;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.resultimport.impl.ResultUrlRegistryServiceTrackerCustomizer;
import com.sap.sailing.xrr.resultimport.ParserFactory;

public class Activator implements BundleActivator {

    private ServiceTracker<ResultUrlRegistry, ResultUrlRegistry> resultUrlRegistryServiceTracker;

    public void start(BundleContext bundleContext) throws Exception {
        resultUrlRegistryServiceTracker = new ServiceTracker<>(bundleContext, ResultUrlRegistry.class,
                new ResultUrlRegistryServiceTrackerCustomizer(bundleContext) {

                    @Override
                    protected ScoreCorrectionProvider configureScoreCorrectionProvider(
                            ResultUrlRegistry resultUrlRegistry) {
                        final ScoreCorrectionProviderImpl service = new ScoreCorrectionProviderImpl(
                                ParserFactory.INSTANCE, resultUrlRegistry);
                        return service;
                    }

                    @Override
                    protected CompetitorProvider configureCompetitorProvider(ResultUrlRegistry resultUrlRegistry) {
                        final CompetitorProvider service = new CompetitorImporter(ParserFactory.INSTANCE,
                                resultUrlRegistry);
                        return service;
                    }
                });

        resultUrlRegistryServiceTracker.open();
        createAnAllCertificatesTrustingManagerforSSL();
    }

    public void stop(BundleContext bundleContext) throws Exception {
        resultUrlRegistryServiceTracker.close();
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
