package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.orc.ORCPublicCertificateDatabase.CertificateHandle;
import com.sap.sailing.domain.orc.impl.ORCPublicCertificateDatabaseImpl;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.CountryCodeFactory;
import com.sap.sse.common.Util;

public class TestORCPublicCertificateDatabase {
    private ORCPublicCertificateDatabase db;
    
    @Rule
    public IgnoreInvalidOrcCertificatesRule customIgnoreRule = new IgnoreInvalidOrcCertificatesRule();
    
    @Before
    public void setUp() {
        db = new ORCPublicCertificateDatabaseImpl();
    }
    
    @Test
    public void testSimpleSearchByRefNo() throws Exception {
        final String referenceNumber = "FRA00013881";
        final Iterable<CertificateHandle> result = db.search(/* country */ null, /* yearOfIssuance */ null,
                /* referenceNumber */ referenceNumber, /* yachtName */ null, /* sailNumber */ null,
                /* boatClassName */ null);
        assertEquals(1, Util.size(result));
        assertSoulmate(referenceNumber, result.iterator().next());
    }

    @Test
    public void testSearchByYachtNameAndSailNumberAndYear() throws Exception {
        final String referenceNumber = "FRA00013881";
        final Iterable<CertificateHandle> result = db.search(/* country */ null, /* yearOfIssuance */ 2019,
                /* referenceNumber */ null, /* yachtName */ "Soulmate", /* sailNumber */ "DEN-   13",
                /* boatClassName */ null);
        assertEquals(1, Util.size(result));
        assertSoulmate(referenceNumber, result.iterator().next());
    }

    @Test
    public void testSearchByYachtNameNotUnique() throws Exception {
        final String referenceNumber = "FRA00013881";
        final Iterable<CertificateHandle> result = db.search(/* country */ null, /* yearOfIssuance */ null,
                /* referenceNumber */ null, /* yachtName */ "Soulmate", /* sailNumber */ null,
                /* boatClassName */ null);
        assertTrue(Util.size(result)>1);
        boolean found = false;
        for (final CertificateHandle handle : result) {
            if (handle.getReferenceNumber().equals(referenceNumber)) {
                assertSoulmate(referenceNumber, handle);
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testCertificateHandleAPI() throws Exception {
        final String referenceNumber = "FRA00013881";
        final CertificateHandle result = db.getCertificateHandle(referenceNumber);
        assertSoulmate(referenceNumber, result);
    }

    // TODO this test will probably break 2020 when 2019 certificates will no longer be returned as valid...
    @Test
    public void testGetCertificate() throws Exception {
        final String referenceNumber = "FRA00013881";
        final CertificateHandle handle = db.getCertificateHandle(referenceNumber);
        final ORCCertificate result = db.getCertificate(referenceNumber);
        assertEquals(handle.getGPH(), result.getGPH().asSeconds(), 0.00001);
        assertEquals(handle.getIssueDate(), result.getIssueDate());
        assertEquals(handle.getSailNumber(), result.getSailNumber());
    }
    
    @IgnoreInvalidOrcCertificates
    @Test
    public void testParallelFuzzySearch() throws InterruptedException, ExecutionException {
        Iterator<CountryCode> iterator = CountryCodeFactory.INSTANCE.getAll().iterator();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        List<ORCCertificate> certificates = new ArrayList<ORCCertificate>();
        while (iterator.hasNext()) {
            CountryCode cc = iterator.next();
            try {
                Iterable<CertificateHandle> certificateHandles = db.search(cc, year, null, null, null, null);
                if (certificateHandles.iterator().hasNext()) {
                    Iterable<ORCCertificate> orcCertificates = db.getCertificates(certificateHandles);
                    if (orcCertificates.iterator().hasNext()) {
                        if (certificates.size() == 2) {// certificates size set to two as we are trying to test the
                                                       // parallel search functionality
                            break;
                        }
                        ORCCertificate orcCertificate = orcCertificates.iterator().next();
                        certificates.add(orcCertificate);
                    }
                }
            } catch (Exception ex) {
                // Exceptions are ignored because we are searching for any countries orc certificate's availability.
            }
        }
        List<Future<Set<ORCCertificate>>> futures = new ArrayList<Future<Set<ORCCertificate>>>();
        for (ORCCertificate orcCertificate : certificates) {
            futures.add(db.search(orcCertificate.getBoatName(), orcCertificate.getSailNumber(),
                    new BoatClassImpl(orcCertificate.getBoatClassName(), true)));
        }
        boolean isYearFound = false;
        for (Future<Set<ORCCertificate>> futureResult : futures) {
            isYearFound = isYearFound || assertFoundYear(futureResult.get(), year);
        }
        assertTrue(isYearFound);
    }

    private boolean assertFoundYear(final Set<ORCCertificate> certificates, int year) {
        boolean foundYear = false;
        for (final ORCCertificate certificate : certificates) {
            if (certificate.getIssueDate() != null) {
                final GregorianCalendar cal = new GregorianCalendar();
                cal.setTimeZone(TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(certificate.getIssueDate().asMillis());
                if (cal.get(Calendar.YEAR) == year) {
                    foundYear = true;
                    break;
                }
            }
        }
        return foundYear;
    }

    private void assertSoulmate(final String referenceNumber, CertificateHandle handle) {
        assertEquals("SOULMATE", handle.getYachtName());
        assertEquals("MOSQUITO 85", handle.getBoatClassName());
        assertEquals(662.4, handle.getGPH(), 0.000001);
        assertEquals(referenceNumber, handle.getReferenceNumber());
        assertEquals(1985, (int) handle.getYearBuilt());
        assertFalse(handle.isProvisional());
        assertEquals(UUID.fromString("1890A35C-C71E-4F15-A13D-2A0AC464B699"), handle.getDatInGID());
    }
}
