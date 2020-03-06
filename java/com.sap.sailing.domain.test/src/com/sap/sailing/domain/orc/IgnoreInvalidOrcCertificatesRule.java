package com.sap.sailing.domain.orc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.orc.ORCPublicCertificateDatabase.CertificateHandle;
import com.sap.sailing.domain.orc.impl.ORCPublicCertificateDatabaseImpl;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.CountryCodeFactory;

/***
 * A IgnoreInvalidOrcCerticatesRule is an implementation of TestRule. This class execution depends on
 * {@link IgnoreInvalidOrcCertificates} annotation on any method in a test class containing
 * {@link org.junit.rules.TestRule} annotation with current class implementation. When any test class added
 * {@link IgnoreInvalidOrcCertificatesRule} rule then before executing all of it's test method, Junit will execute the
 * evaluate() method of {@link IgnoreInvalidOrcCertificatesRule} class. This method first check whether the current test
 * method contains the {@link IgnoreInvalidOrcCertificates} annotation, if yes then it will check for any certificate
 * available to parse on ORC Website. If certificate available then this method continue execution of the current test
 * method otherwise it will ignore it.
 * 
 * @author Usman Ali
 *
 */

public class IgnoreInvalidOrcCertificatesRule implements TestRule {
    private ORCPublicCertificateDatabase db = new ORCPublicCertificateDatabaseImpl();
    
    private List<ORCCertificate> availableCerts;

    public Collection<ORCCertificate> getAvailableCerts() {
        return Collections.unmodifiableCollection(availableCerts);
    }

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
            availableCerts = new ArrayList<>();
        }

        /***
         * This method executes for every test case having {@link TestRule} annotation of
         * {@link IgnoreInvalidOrcCertificatesRule} class. Assume statement at the end of this method evaluates whether
         * the test method will execute or ignored.
         */
        @Override
        public void evaluate() throws Throwable {
            boolean certificateExists = true;
            IgnoreInvalidOrcCertificates annotation = description.getAnnotation(IgnoreInvalidOrcCertificates.class);
            if (annotation != null) {
                int year = LocalDate.now().getYear();
                certificateExists = false;
                for (CountryCode cc : CountryCodeFactory.INSTANCE.getAll()) {
                    try {
                        Iterable<CertificateHandle> certificateHandles = db.search(cc, year, null, null, null, null);
                        Iterable<ORCCertificate> orcCertificates = db.getCertificates(certificateHandles);
                        orcCertificates.forEach(availableCerts::add);
                        if (orcCertificates.iterator().hasNext()) {
                            certificateExists = true;
                            break;
                        }
                    } catch (Exception ex) {
                        // Exceptions are ignored because we are searching for any countries orc certificate's
                        // availability.
                    }
                }
            }
            Assert.assertTrue(certificateExists);
            base.evaluate();
        }
        
        
    }
}