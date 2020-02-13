package com.sap.sailing.domain.orc;

import java.util.Calendar;
import java.util.Iterator;

import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.orc.ORCPublicCertificateDatabase.CertificateHandle;
import com.sap.sailing.domain.orc.impl.ORCPublicCertificateDatabaseImpl;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.CountryCodeFactory;

public class IgnoreInvalidOrcCerticatesRule implements TestRule {
    private ORCPublicCertificateDatabase db = new ORCPublicCertificateDatabaseImpl();
    
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
            if (annotation != null) {
                Iterator<CountryCode> iterator = CountryCodeFactory.INSTANCE.getAll().iterator();
                int year = Calendar.getInstance().get(Calendar.YEAR);
                certificateExists = false;
                while (iterator.hasNext()) {
                    CountryCode cc = iterator.next();
                    try {
                        Iterable<CertificateHandle> certificateHandles = db.search(cc, year, null, null, null, null);
                        if (certificateHandles.iterator().hasNext()) {
                            Iterable<ORCCertificate> orcCertificates = db.getCertificates(certificateHandles);
                            if (orcCertificates.iterator().hasNext()) {
                                certificateExists = true;
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        // Exceptions are ignored because we are searching for any countries orc certificate's
                        // availability.
                    }
                }
            }
            Assume.assumeTrue("Test is ignored!", certificateExists);
            base.evaluate();
        }
    }
}