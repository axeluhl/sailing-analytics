package com.sap.sailing.domain.orc.impl;

import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.orc.ORCPublicCertificateDatabase;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.CountryCodeFactory;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ORCPublicCertificateDatabaseImpl implements ORCPublicCertificateDatabase {
    private static final Logger logger = Logger.getLogger(ORCPublicCertificateDatabaseImpl.class.getName());
    private static final String USER_NAME = "Sailing_Analytics@sap.com";
    private static final String CREDENTIALS = "QUdIVUJU";
    
    private static final String ACTION_PARAM_NAME = "action";
    private static final String ACTION_PARAM_VALUE_LIST_CERT = "ListCert";
    private static final String XSLP_PARAM_NAME = "xslp";
    private static final String XSLP_PARAM_VALUE_LIST_CERT = "ListCert.php";
    private static final String COUNTRY_PARAM_NAME = "CountryId";
    private static final String VPP_YEAR_PARAM_NAME = "VPPYear";
    private static final String REF_NO_PARAM_NAME = "RefNo";
    private static final String YACHT_NAME_PARAM_NAME = "YachtName";
    private static final String SAIL_NUMBER_PARAM_NAME = "SailNo";
    private static final String BOAT_CLASS_PARAM_NAME = "Class";
    
    private static final NameValuePair ACTION_PARAM = new BasicNameValuePair(ACTION_PARAM_NAME, ACTION_PARAM_VALUE_LIST_CERT);
    private static final NameValuePair XSLP_PARAM = new BasicNameValuePair(XSLP_PARAM_NAME, XSLP_PARAM_VALUE_LIST_CERT);
    private static final String SEARCH_URL = "http://data.orc.org/public/WPub.dll";
    private static final String ROOT_ELEMENT = "ROOT";
    private static final String DATA_ELEMENT = "DATA";
    private static final String ROW_ELEMENT = "ROW";

    private static final DateTimeFormatter[] DATE_FORMATTERS = 
            new DateTimeFormatter[] {
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
    };
    private static final DateTimeFormatter[] DATE_FORMATTERS_WITH_ZONE = 
            new DateTimeFormatter[] {
                    DateTimeFormatter.ISO_INSTANT,
                    DateTimeFormatter.ISO_DATE_TIME,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
    };
    
    /**
     * Hash code and equality as based on {@link #getReferenceNumber() the reference number field} only.
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private static class CertificateHandleImpl implements CertificateHandle {
        private final CountryCode issuingCountry;
        private final String sssid;
        private final Double gph;
        private final UUID datInGID;
        private final String referenceNumber;
        private final String yachtName;
        private final String sailNumber;
        private final String boatClassName;
        private final String designer;
        private final String builder;
        private final Integer yearBuilt;
        private final TimePoint issueDate;
        private final Integer certType;
        private final Boolean isOneDesign;
        private final Boolean isProvisional;

        public CertificateHandleImpl(CountryCode issuingCountry, Double gph, String sssid, UUID datInGID,
                String referenceNumber, String yachtName, String sailNumber, String boatClassName, String designer,
                String builder, Integer yearBuilt, TimePoint issueDate, Integer certType, Boolean oneDesign, Boolean isProvisional) {
            super();
            this.issuingCountry = issuingCountry;
            this.gph = gph;
            this.sssid = sssid;
            this.datInGID = datInGID;
            this.referenceNumber = referenceNumber;
            this.yachtName = yachtName;
            this.sailNumber = sailNumber;
            this.boatClassName = boatClassName;
            this.designer = designer;
            this.builder = builder;
            this.yearBuilt = yearBuilt;
            this.issueDate = issueDate;
            this.certType = certType;
            this.isOneDesign = oneDesign;
            this.isProvisional = isProvisional;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((referenceNumber == null) ? 0 : referenceNumber.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CertificateHandleImpl other = (CertificateHandleImpl) obj;
            if (referenceNumber == null) {
                if (other.referenceNumber != null)
                    return false;
            } else if (!referenceNumber.equals(other.referenceNumber))
                return false;
            return true;
        }

        @Override
        public CountryCode getIssuingCountry() {
            return issuingCountry;
        }

        @Override
        public Double getGPH() {
            return gph;
        }

        @Override
        public String getSSSID() {
            return sssid;
        }

        @Override
        public UUID getDatInGID() {
            return datInGID;
        }

        @Override
        public String getReferenceNumber() {
            return referenceNumber;
        }

        @Override
        public String getYachtName() {
            return yachtName;
        }

        @Override
        public String getSailNumber() {
            return sailNumber;
        }

        @Override
        public String getBoatClassName() {
            return boatClassName;
        }

        @Override
        public String getDesigner() {
            return designer;
        }

        @Override
        public String getBuilder() {
            return builder;
        }

        @Override
        public Integer getYearBuilt() {
            return yearBuilt;
        }

        @Override
        public TimePoint getIssueDate() {
            return issueDate;
        }
        
        @Override
        public Integer getCertType() {
            return certType;
        }

        @Override
        public Boolean isProvisional() {
            return isProvisional;
        }
        
        @Override
        public Boolean isOd() {
            return isOneDesign;
        }

        @Override
        public String toString() {
            return "CertificateHandleImpl [issuingCountry=" + issuingCountry + ", sssid=" + sssid + ", gph=" + gph
                    + ", datInGID=" + datInGID + ", referenceNumber=" + referenceNumber + ", yachtName=" + yachtName
                    + ", sailNumber=" + sailNumber + ", boatClassName=" + boatClassName + ", designer=" + designer
                    + ", builder=" + builder + ", yearBuilt=" + yearBuilt + ", issueDate=" + issueDate + ", certType="
                    + certType + ", isOneDesign=" + isOneDesign + ", isProvisional=" + isProvisional + "]";
        }
    }
    
    @Override
    public Iterable<CertificateHandle> search(CountryCode issuingCountry, Integer yearOfIssuance, String referenceNumber,
            String yachtName, String sailNumber, String boatClassName) throws Exception {
        final Set<ORCPublicCertificateDatabase.CertificateHandle> result = new HashSet<>(); 
        final HttpClient client = new SystemDefaultHttpClient();
        final List<NameValuePair> params = new ArrayList<>();
        params.add(ACTION_PARAM);
        params.add(XSLP_PARAM);
        params.add(new BasicNameValuePair(COUNTRY_PARAM_NAME, issuingCountry==null?"*":issuingCountry.getThreeLetterIOCCode()));
        params.add(new BasicNameValuePair(VPP_YEAR_PARAM_NAME, yearOfIssuance==null?"0":yearOfIssuance.toString()));
        if (referenceNumber != null) {
            params.add(new BasicNameValuePair(REF_NO_PARAM_NAME, referenceNumber));
        }
        if (yachtName != null) {
            params.add(new BasicNameValuePair(YACHT_NAME_PARAM_NAME, yachtName));
        }
        if (sailNumber != null) {
            params.add(new BasicNameValuePair(SAIL_NUMBER_PARAM_NAME, sailNumber));
        }
        if (boatClassName != null) {
            params.add(new BasicNameValuePair(BOAT_CLASS_PARAM_NAME, boatClassName));
        }
        final UrlEncodedFormEntity parametersEntity = new UrlEncodedFormEntity(params);
        final HttpPost postRequest = new HttpPost(SEARCH_URL);
        postRequest.setEntity(parametersEntity);
        addAuthorizationHeader(postRequest);
        logger.fine(()->"Searching for "+params+"...");
        final HttpResponse processorResponse = client.execute(postRequest);
        final InputStream content = processorResponse.getEntity().getContent();
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document document = builder.parse(content);
        final NodeList dataNodes = document.getElementsByTagName(ROOT_ELEMENT).item(0).getChildNodes();
        for (int dataNodeIndex=0; dataNodeIndex<dataNodes.getLength(); dataNodeIndex++) {
            final Node dataNode = dataNodes.item(dataNodeIndex);
            if (dataNode.getNodeName().equals(DATA_ELEMENT)) {
                final NodeList rowNodes = dataNode.getChildNodes();
                for (int rowNodeIndex=0; rowNodeIndex<rowNodes.getLength(); rowNodeIndex++) {
                    final Node rowNode = rowNodes.item(rowNodeIndex);
                    if (rowNode.getNodeName().equals(ROW_ELEMENT)) {
                        result.add(parseHandle(rowNode));
                    }
                }
            }
        }
        logger.fine(()->"Search for "+params+" returned "+result.size()+" results");
        return result;
    }

    private void addAuthorizationHeader(final HttpMessage httpMessage) {
        httpMessage.addHeader("Authorization", "Basic "+new String(Base64.getEncoder().encode((USER_NAME+":"+getDecodedCredentials()).getBytes())));
    }

    private CertificateHandle parseHandle(Node rowNode) throws DOMException, ParseException {
        assert rowNode.getNodeName().equals(ROW_ELEMENT);
        CountryCode issuingCountry = null;
        String sssid = null;
        Double gph = null;
        UUID datInGID = null;
        String referenceNumber = null;
        String yachtName = null;
        String sailNumber = null;
        String boatClassName = null;
        String designer = null;
        String builder = null;
        Integer yearBuilt = null;
        Integer certType = null;
        TimePoint issueDate = null;
        Boolean isOneDesign = null;
        Boolean isProvisional = null;
        for (int i=0; i<rowNode.getChildNodes().getLength(); i++) {
            final Node child = rowNode.getChildNodes().item(i);
            switch (child.getNodeName()) {
            case "CountryId":
                issuingCountry = CountryCodeFactory.INSTANCE.getFromThreeLetterIOCName(child.getTextContent().trim());
                break;
            case "SSSID":
                sssid = child.getTextContent();
                break;
            case "GPH":
                gph = child.getTextContent().trim().isEmpty() ? null : Double.valueOf(child.getTextContent().trim());
                break;
            case "DatInGID":
                datInGID = UUID.fromString(child.getTextContent().trim().replaceAll("[{}]", ""));
                break;
            case "RefNo":
                referenceNumber = child.getTextContent().trim();
                break;
            case "YachtName":
                yachtName = child.getTextContent();
                break;
            case "SailNo":
                sailNumber = child.getTextContent();
                break;
            case "Class":
                boatClassName = child.getTextContent();
                break;
            case "Designer":
                designer = child.getTextContent();
                break;
            case "Builder":
                builder = child.getTextContent();
                break;
            case "dxtDate":
                issueDate = new MillisecondsTimePoint(parseDate(child.getTextContent())); // assume UTC
                break;
            case "CertType":
                certType = Integer.valueOf(child.getTextContent().trim());
                break;
            case "Age":
                yearBuilt = child.getTextContent().trim().isEmpty() ? null : Integer.valueOf(child.getTextContent().trim());
                break;
            case "IsOd":
                isOneDesign = Boolean.valueOf(child.getTextContent());
                break;
            case "Provisional":
                isProvisional = Boolean.valueOf(child.getTextContent());
                break;
            }
        }
        return new CertificateHandleImpl(issuingCountry, gph, sssid, datInGID, referenceNumber, yachtName,
                sailNumber, boatClassName, designer, builder, yearBuilt, issueDate, certType, isOneDesign, isProvisional);
    }
    
    @Override
    public Date parseDate(final String dateString) throws DateTimeParseException {
        Optional<LocalDateTime> parsedDateWithoutZone = Arrays.asList(DATE_FORMATTERS).stream()
                .map(f -> {
                    try {
                        return LocalDateTime.parse(dateString, f);
                    } catch (DateTimeParseException e) {
                        return null;
                    }
                })
                .filter(out -> out != null)
                .findAny();
        Optional<ZonedDateTime> parsedDateWithZone = Arrays.asList(DATE_FORMATTERS_WITH_ZONE).stream()
                .map(f -> {
                    try {
                        return ZonedDateTime.parse(dateString, f);
                    } catch (DateTimeParseException e) {
                        return null;
                    }
                })
                .filter(out -> out != null)
                .findAny();
        if (parsedDateWithoutZone.isPresent()) {
            LocalDateTime localDateTime = parsedDateWithoutZone.get();
            return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
        }
        if (parsedDateWithZone.isPresent()) {
            ZonedDateTime zonedDateTime = parsedDateWithZone.get();
            return Date.from(zonedDateTime.toInstant());
        }
        logger.fine("Date is not parsable by any of the format :" + dateString);
        throw new DateTimeParseException("Date is not parsable by any of the format", dateString, 0);
    }

    @Override
    public ORCCertificate getCertificate(String referenceNumber) throws Exception {
        final HttpClient client = new SystemDefaultHttpClient();
        final HttpGet getRequest = new HttpGet("http://data.orc.org/public/WPub.dll?action=DownBoatRMS&ext=json&RefNo="+referenceNumber);
        addAuthorizationHeader(getRequest);
        logger.fine("Obtaining certificate for reference number "+referenceNumber);
        final Iterable<ORCCertificate> certificates = new ORCCertificatesJsonImporter().read(client.execute(getRequest).getEntity().getContent())
                .getCertificates();
        final ORCCertificate result;
        if (certificates.iterator().hasNext()) {
            result = certificates.iterator().next();
        } else {
            result = null;
            logger.info("Couldn't find ORC certificate with reference number "+referenceNumber);
        }
        return result;
    }

    public Iterable<ORCCertificate> getCertificates(Iterable<CertificateHandle> handles) throws Exception {
        final Map<CertificateHandle, Future<ORCCertificate>> futures = new HashMap<>();
        for (final CertificateHandle handle : handles) {
            if (!handle.getReferenceNumber().trim().isEmpty()) {
                final FutureTask<ORCCertificate> task = new FutureTask<ORCCertificate>(()->getCertificate(handle.getReferenceNumber()));
                final Thread backgroundExecutor = new Thread(task, "ORC certificate background download thread for "+handle.getReferenceNumber());
                backgroundExecutor.setDaemon(true);
                backgroundExecutor.start();
                futures.put(handle, task);
            } else {
                logger.fine("Ignoring handle "+handle+" because it has an empty reference number");
            }
        }
        final Set<ORCCertificate> result = new HashSet<>();
        for (final Entry<CertificateHandle, Future<ORCCertificate>> e : futures.entrySet()) {
            final ORCCertificate certificate = e.getValue().get();
            if (certificate != null) {
                logger.fine("Found certificate for handle "+e.getKey());
                result.add(certificate);
            } else {
                logger.fine("Did not find certificate for handle "+e.getKey());
            }
        }
        return result;
    }

    @Override
    public Future<Set<ORCCertificate>> search(final String yachtName, final String sailNumber, final BoatClass boatClass) {
        logger.fine(()->"Looking for ORC certificate for "+yachtName+"/"+sailNumber+"/"+boatClass);
        final FutureTask<Set<ORCCertificate>> result = new FutureTask<Set<ORCCertificate>>(()->{
            final Set<ORCCertificate> certificates = new HashSet<>();
            Iterable<CertificateHandle> certificateHandles = fuzzySearchVaryingSailNumberPadding(yachtName, sailNumber, boatClass);
            if (!containsHandleForCurrentYear(certificateHandles)) {
                // try swapping yacht name and sail number and go again:
                logger.fine(()->"Nothing found for "+yachtName+"/"+sailNumber+"/"+boatClass+
                        "; trying by swapping sail number and yacht name");
                certificateHandles = fuzzySearchVaryingSailNumberPadding(sailNumber, yachtName, boatClass);
            }
            for (final CertificateHandle handle : certificateHandles) {
                final Iterable<ORCCertificate> certificatesFromHandle = getCertificates(handle);
                if (certificatesFromHandle.iterator().hasNext()) {
                    final ORCCertificate certificate = certificatesFromHandle.iterator().next();
                    if (certificate != null) {
                        certificates.add(certificate);
                    }
                }
            }
            return certificates;
        });
        final Thread backgroundExecutor = new Thread(result, "ORC certificate background lookup thread for "+yachtName+"/"+sailNumber+"/"+boatClass);
        backgroundExecutor.setDaemon(true);
        backgroundExecutor.start();
        return result;
    }

    private Iterable<CertificateHandle> fuzzySearchVaryingSailNumberPadding(final String yachtName, final String sailNumber, final BoatClass boatClass) throws Exception {
        final Set<CertificateHandle> result = new HashSet<>();
        for (final String sailNumberVariant : getSailNumberVariants(sailNumber)) {
            logger.fine(()->"Trying sail number variation "+sailNumberVariant);
            final Iterable<CertificateHandle> certificateHandles = fuzzySearchVaryingBoatClassName(yachtName, sailNumberVariant, boatClass);
            Util.addAll(filterHandlesForCurrentYear(certificateHandles), result);
        }
        if (sailNumber != null && result.isEmpty() && (yachtName != null || boatClass != null)) {
            logger.fine(()->"Nothing found; trying without restricting sail number to "+sailNumber);
            // try without sail number constraint; if that doesn't find anything either, we can stop
            Util.addAll(fuzzySearchVaryingBoatClassName(yachtName, /* sailNumber */ null, boatClass), result);
        }
        return result;
    }

    private boolean containsHandleForCurrentYear(Iterable<CertificateHandle> certificateHandles) {
        return filterHandlesForCurrentYear(certificateHandles).iterator().hasNext();
    }

    private Iterable<CertificateHandle> filterHandlesForCurrentYear(Iterable<CertificateHandle> certificateHandles) {
        final List<CertificateHandle> result = new LinkedList<>();
        final Calendar cal = new GregorianCalendar();
        final int currentYear = cal.get(Calendar.YEAR);
        for (final CertificateHandle handle : certificateHandles) {
            cal.setTime(handle.getIssueDate().asDate());
            if (cal.get(Calendar.YEAR) ==  currentYear) {
                result.add(handle);
            }
        }
        return result;
    }

    /**
     * Varies the boat class name by first using the true boat class name, then, if nothing is found, stepping through
     * the alternative names, and finally removing the boat class name constraint altogether.
     * 
     * @param yachtName
     *            may be {@code null}
     * @param sailNumber
     *            may be {@code null}
     * @param boatClass
     *            may be {@code null}
     * 
     * @return the boat class name that ultimately led to the matches returned, and the matches in the form of a
     *         sequence of handles; may be {@code null}
     */
    private Iterable<CertificateHandle> fuzzySearchVaryingBoatClassName(final String yachtName, final String sailNumber, final BoatClass boatClass) throws Exception {
        String successfulBoatClassName = boatClass==null?null:boatClass.getName();
        Iterable<CertificateHandle> certificateHandles = search(/* issuingCountry */ null, /* yearOfIssuance */ null, /* referenceNumber */ null, yachtName, sailNumber, successfulBoatClassName);
        if (!containsHandleForCurrentYear(certificateHandles) && successfulBoatClassName != null) {
            if (yachtName != null || sailNumber != null) {
                logger.fine(()->"Nothing found; removing boat class name restriction "+boatClass.getName());
                // try without boat class restriction
                successfulBoatClassName = null;
                certificateHandles = search(/* issuingCountry */ null, /* yearOfIssuance */ null, /* referenceNumber */ null, yachtName, sailNumber, successfulBoatClassName);
            } else {
                logger.fine(()->"No current certificates found for boat class "+boatClass.getName()+
                        " but yacht name and sail number are not specified either; giving up.");
            }
        }
        // if a valid boatClass was specified, try to filter; if none match the filter, return unfiltered
        if (boatClass != null) {
            final Set<CertificateHandle> restrictedResults = new HashSet<>();
            for (final CertificateHandle handle : filterHandlesForCurrentYear(certificateHandles)) {
                final BoatClassMasterdata boatClassMasterData = BoatClassMasterdata.resolveBoatClass(handle.getBoatClassName());
                if (boatClassMasterData != null && boatClassMasterData.getDisplayName().equals(boatClass.getDisplayName())) {
                    restrictedResults.add(handle);
                }
            }
            if (!restrictedResults.isEmpty()) {
                certificateHandles = restrictedResults;
            }
        }
        return certificateHandles;
    }

    /**
     * Produces a sequence of sail number variations of which {@code sailNumber} is the first.
     * It then adds separations of a recognized three-letter IOC nationality code at the beginning
     * of {@code sailNumber} and a digit sequence at the end and connects them with combinations
     * of space and dash (-) characters surrounded by the wildcard character "%". This can lead
     * to slightly unprecise results: e.g., "DEN%-%13" can also match "DEN-413". To be fair, the
     * original search also is a prefix search, so "DEN 13" would also match "DEN 134"" and all other
     * sail numbers of which "DEN 13" is a prefix.
     */
    private Iterable<String> getSailNumberVariants(String sailNumber) {
        final List<String> result = new LinkedList<>();
        result.add(sailNumber);
        if (sailNumber != null) {
            final Matcher matcher = Pattern.compile("[^A-Za-z]*([A-Za-z]*).*([0-9]*).*").matcher(sailNumber);
            final boolean findResult = matcher.find();
            if (findResult) {
                final String country = matcher.group(1);
                final String number = matcher.group(2);
                for (final String paddingToTry : new String[] { "% %", "%-%" }) {
                    result.add(country+paddingToTry+number);
                }
            }
        }
        return result;
    }

    private static String getDecodedCredentials() {
        return new String(Base64.getDecoder().decode(CREDENTIALS));
    }
}
