package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.orc.ORCPublicCertificateDatabase.CertificateHandle;
import com.sap.sailing.domain.orc.impl.ORCPublicCertificateDatabaseImpl;
import com.sap.sse.common.Util;

public class TestORCPublicCertificateDatabase {
    private ORCPublicCertificateDatabase db;
    Map<String, GregorianCalendar> dateComparisonMap = new LinkedHashMap<String, GregorianCalendar>();
    List<String> dateFailureCases =null;
    
    @Before
    public void setUp() {
        db = new ORCPublicCertificateDatabaseImpl();
        /**
         * By default GregorianCalendar month starts from 0 as January and so on. In below cases 1 specifies the date
         * month as February.
         */
        dateComparisonMap.put("2019-02-21T10:38:59Z", new GregorianCalendar(2019, 1, 21, 15, 38, 59));
        dateComparisonMap.put("2019-02-21T12:00:00.000GMT+2", new GregorianCalendar(2019, 1, 21, 15, 00, 00));
        dateComparisonMap.put("2019-02-21T15:00:00.000 GMT-2", new GregorianCalendar(2019, 1, 21, 22, 00, 00));
        dateComparisonMap.put("2019-02-21T10:38:55.000-0800", new GregorianCalendar(2019, 1, 21, 23, 38, 55));
        dateComparisonMap.put("2019-02-21T10:38:32.000+08:00", new GregorianCalendar(2019, 1, 21, 07, 38, 32));
        dateComparisonMap.put("2019-02-21T10:38:09.000-06", new GregorianCalendar(2019, 1, 21, 21, 38, 9));
        dateComparisonMap.put("2019-02-21T10:38:22.000Z", new GregorianCalendar(2019, 1, 21, 15, 38, 22));
        dateComparisonMap.put("2019-02-21T10:38:00.000z", new GregorianCalendar(2019, 1, 21, 15, 38, 00));
        dateComparisonMap.put("2019-02-21T10:38:46z", new GregorianCalendar(2019, 1, 21, 15, 38, 46));
        dateComparisonMap.put("2019-02-21T10:38:17", new GregorianCalendar(2019, 1, 21, 15, 38, 17));
        dateComparisonMap.put("2019-02-21T10:38:33.000", new GregorianCalendar(2019, 1, 21, 15, 38, 33));
        dateFailureCases = Arrays.asList("2019-02-21T10:44GMT+2","2019-02-21T10:38+0800","2019-02-21T10:38+08:00",
                "2019-02-21T10:38-08","2019-02-21T10:38Z","2019-02-21T10z","2019-02-21T10:38z");

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
    
    @Ignore("Certificate used for testing no longer valid after 2019")
    @Test
    public void testParallelFuzzySearch() throws InterruptedException, ExecutionException {
        final Future<Set<ORCCertificate>> soulmateCertificatesFuture = db.search("Soulmate", "DEN13", new BoatClassImpl("ORC", BoatClassMasterdata.ORC));
        final Future<Set<ORCCertificate>> amarettoCertificatesFuture = db.search("Amaretto", "NED 6101", new BoatClassImpl("Beneteau First 40.7", /* starts upwind */ true));
        final Set<ORCCertificate> soulmateCertificates = soulmateCertificatesFuture.get();
        final Set<ORCCertificate> amarettoCertificates = amarettoCertificatesFuture.get();
        assertFoundYear(soulmateCertificates, 2019);
        assertFoundYear(amarettoCertificates, 2019);
    }

    private void assertFoundYear(final Set<ORCCertificate> certificates, int year) {
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
        assertTrue(foundYear);
    }
    
    @Test
    public void testParseDateSuccessCases() throws ParseException {
        for (String date : dateComparisonMap.keySet()) {
            Date convertedDate = db.parseDate(date);
            assertEquals(convertedDate.getTime(), dateComparisonMap.get(date).getTimeInMillis());
        }
    }
    
    @Test
    public void testParseDateFailureCases() throws ParseException {
        for (String dateString : dateFailureCases) {
            try {
                db.parseDate(dateString);
                Assert.fail(dateString + " is parsable"); // as date must not be parsable
            } catch (ParseException e) {
                // ignoring exception as date must fail.
            }
        }
        Assert.assertTrue(true);

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
