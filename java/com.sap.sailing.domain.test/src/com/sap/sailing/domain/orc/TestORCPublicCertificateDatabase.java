package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.orc.ORCPublicCertificateDatabase.CertificateHandle;
import com.sap.sailing.domain.orc.impl.ORCPublicCertificateDatabaseImpl;
import com.sap.sse.common.Util;

public class TestORCPublicCertificateDatabase {
    private ORCPublicCertificateDatabase db;
    private Map<String, Date> dateComparisonMap = new LinkedHashMap<String, Date>();
    private List<String> dateFailureCases = Arrays.asList("2019-02-21T10:44GMT+2","2019-02-21T10:38+0800","2019-02-21T10:38+08:00",
            "2019-02-21T10:38-08","2019-02-21T10:38Z","2019-02-21T10z","2019-02-21T10:38z");
    
    @Rule
    public IgnoreInvalidOrcCertificatesRule customIgnoreRule = new IgnoreInvalidOrcCertificatesRule();
    
    @Before
    public void setUp() {
        db = new ORCPublicCertificateDatabaseImpl();
        /**
         * By default GregorianCalendar month starts from 0 as January and so on. In below cases 1 specifies the date
         * month as February.
         */
        dateComparisonMap.put("2019-02-21T10:38:59Z", Date.from(ZonedDateTime.parse("2019-02-21T10:38:59Z").toInstant()));
        //                dateComparisonMap.put("2019-02-21T12:00:00.000GMT+2", Date.from(ZonedDateTime.of(2019, 2, 21, 10, 38, 59, 0, ZoneId.systemDefault()).toInstant()));
        //        //        dateComparisonMap.put("2019-02-21T15:00:00.000 GMT-2", Date.from(ZonedDateTime.of(2019, 2, 21, 10, 38, 59, 0, ZoneId.systemDefault()).toInstant()));
        dateComparisonMap.put("2019-02-21T10:38:55.000-0800", 
                Date.from(ZonedDateTime.parse("2019-02-21T10:38:55.000-0800", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).toInstant())
                );
        dateComparisonMap.put(
                "2019-02-21T10:38:32.000+08:00", 
                Date.from(ZonedDateTime.parse("2019-02-21T10:38:32.000+08:00").toInstant())
                );
        dateComparisonMap.put(
                "2019-02-21T10:38:09.000-06", 
                Date.from(ZonedDateTime.parse("2019-02-21T10:38:09.000-06", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSx")).toInstant())
                );
        dateComparisonMap.put(
                "2019-02-21T10:38:22.000Z", 
                Date.from(ZonedDateTime.parse("2019-02-21T10:38:22.000Z").toInstant())
                );
        dateComparisonMap.put(
                "2019-02-21T10:38:00.000z", 
                Date.from(ZonedDateTime.parse("2019-02-21T10:38:00.000z").toInstant())
                );
        dateComparisonMap.put(
                "2019-02-21T10:38:46z", 
                Date.from(ZonedDateTime.parse("2019-02-21T10:38:46z").toInstant())
                );
        dateComparisonMap.put(
                "2019-02-21T10:38:17", 
                Date.from(LocalDateTime.parse("2019-02-21T10:38:17").toInstant(ZoneOffset.UTC))
                );
        dateComparisonMap.put(
                "2019-02-21T10:38:33.000", 
                Date.from(LocalDateTime.parse("2019-02-21T10:38:33.000").toInstant(ZoneOffset.UTC))
                );
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
    @IgnoreInvalidOrcCertificates
    @Test
    public void testGetCertificate() throws Exception {        
        Collection<ORCCertificate> certificates = customIgnoreRule.getAvailableCerts();
        final ORCCertificate cert = certificates.stream().findFirst().get();
        Iterable<CertificateHandle> certHandles = db.search(null, LocalDate.now().getYear(), null, cert.getBoatName(), cert.getSailNumber(), cert.getBoatClassName());
        Optional<CertificateHandle> certificateHandle = Optional.ofNullable(certHandles.iterator().hasNext() ? certHandles.iterator().next() : null);
        assertTrue(certificateHandle.isPresent());
        final String referenceNumber = certificateHandle.get().getReferenceNumber();
        final CertificateHandle handle = db.getCertificateHandle(referenceNumber);
        final ORCCertificate result = db.getCertificate(referenceNumber);
        assertEquals(handle.getGPH(), result.getGPH().asSeconds(), 0.00001);
        assertEquals(handle.getIssueDate().asMillis(), result.getIssueDate().asMillis(), 1000.0);
        assertEquals(handle.getSailNumber(), result.getSailNumber());
    }
    
    @IgnoreInvalidOrcCertificates
    @Test
    public void testParallelFuzzySearch() throws InterruptedException, ExecutionException {
        int year = LocalDate.now().getYear();
        ArrayList<Future<Set<ORCCertificate>>> futures = new ArrayList<Future<Set<ORCCertificate>>>();
        boolean isYearFound = false;
        for (ORCCertificate orcCertificate : customIgnoreRule.getAvailableCerts()) {
            futures.add(db.search(orcCertificate.getBoatName(), orcCertificate.getSailNumber(),
                    new BoatClassImpl(orcCertificate.getBoatClassName(), true)));
        }
        for (Future<Set<ORCCertificate>> futureResult : futures) {
            isYearFound = isYearFound || assertFoundYear(futureResult.get(), year);
        }
        assertTrue(isYearFound);
    }
    
    @Test
    public void testParseDateSuccessCases() {
        for (String date : dateComparisonMap.keySet()) {
            Date convertedDate = db.parseDate(date);
            assertEquals(convertedDate, dateComparisonMap.get(date));
        }
    }
    
    @Test(expected = DateTimeParseException.class)
    public void testShould() throws Exception {
        for (String dateString : dateFailureCases) {
            db.parseDate(dateString);
            Assert.fail(dateString + " is parsable");
        }
    }

    private boolean assertFoundYear(final Set<ORCCertificate> certificates, int year) {
        return certificates.stream().map(cert -> {
            LocalDate certDate = Instant.ofEpochMilli(cert.getIssueDate().asMillis()).atOffset(ZoneOffset.UTC)
                    .toLocalDate();
            return certDate.getYear();
        }).anyMatch(cy -> cy == year);
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
