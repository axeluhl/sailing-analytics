package com.sap.sailing.domain.orc;

import java.net.URL;
import java.util.Iterator;

import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.sap.sailing.domain.common.orc.ORCCertificate;

public class IgnoreInvalidOrcCerticatesRule implements TestRule {
    
    @Override
    public Statement apply(Statement base, Description description) {
        return new IgnorableStatement(base, description);
    }
    private class IgnorableStatement extends Statement {
        private final Statement base;
        private final Description description;
        public IgnorableStatement(Statement base, Description description) {
            this.base = base;
            this.description = description;
        }
        @Override
        public void evaluate() throws Throwable {
            boolean certificateExists = true;
            IgnoreInvalidOrcCerticates annotation = description.getAnnotation(IgnoreInvalidOrcCerticates.class);
            if(annotation != null) {
                ORCCertificatesCollection importer = ORCCertificatesImporter.INSTANCE.read(new URL("https://data.orc.org/public/WPub.dll?action=DownRMS&CountryId=GER&ext=json").openStream());
                certificateExists= importer.getCertificates()!=null && importer.getCertificates().iterator().hasNext();
                if(certificateExists) {
                    Iterator<ORCCertificate> iterator = importer.getCertificates().iterator();
                    while(iterator.hasNext()) {
                        ORCCertificate certificate = iterator.next();
                        if(certificate.getCDL()<1 || certificate.getGPHInSecondsToTheMile() <1) {
                            certificateExists = false;
                            break;
                        }
                    }
                    
                }
            }
            Assume.assumeTrue("Test is ignored!", certificateExists);
            base.evaluate();
        }
    }
}