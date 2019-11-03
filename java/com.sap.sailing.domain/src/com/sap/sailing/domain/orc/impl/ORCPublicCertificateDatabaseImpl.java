package com.sap.sailing.domain.orc.impl;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
    private static final DateFormat isoTimestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
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
        private final Boolean isProvisional;

        public CertificateHandleImpl(CountryCode issuingCountry, Double gph, String sssid, UUID datInGID,
                String referenceNumber, String yachtName, String sailNumber, String boatClassName, String designer,
                String builder, Integer yearBuilt, TimePoint issueDate, Boolean isProvisional) {
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
        public Boolean isProvisional() {
            return isProvisional;
        }

        @Override
        public String toString() {
            return "CertificateHandleImpl [issuingCountry=" + issuingCountry + ", sssid=" + sssid + ", gph=" + gph
                    + ", datInGID=" + datInGID + ", referenceNumber=" + referenceNumber + ", yachtName=" + yachtName
                    + ", sailNumber=" + sailNumber + ", boatClassName=" + boatClassName + ", designer=" + designer
                    + ", builder=" + builder + ", yearBuilt=" + yearBuilt + ", issueDate=" + issueDate
                    + ", isProvisional=" + isProvisional + "]";
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
        TimePoint issueDate = null;
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
                issueDate = new MillisecondsTimePoint(isoTimestampFormat.parse(child.getTextContent()+"+0000")); // assume UTC
                break;
            case "Age":
                yearBuilt = child.getTextContent().trim().isEmpty() ? null : Integer.valueOf(child.getTextContent().trim());
                break;
            case "Provisional":
                isProvisional = Boolean.valueOf(child.getTextContent());
                break;
            }
        }
        return new CertificateHandleImpl(issuingCountry, gph, sssid, datInGID, referenceNumber, yachtName,
                sailNumber, boatClassName, designer, builder, yearBuilt, issueDate, isProvisional);
    }

    @Override
    public ORCCertificate getCertificate(String referenceNumber) throws Exception {
        final HttpClient client = new SystemDefaultHttpClient();
        final HttpGet getRequest = new HttpGet("http://data.orc.org/public/WPub.dll?action=DownBoatRMS&RefNo="+referenceNumber);
        addAuthorizationHeader(getRequest);
        logger.fine("Obtaining certificate for reference number "+referenceNumber);
        final Iterable<ORCCertificate> certificates = new ORCCertificatesRmsImporter().read(client.execute(getRequest).getEntity().getContent())
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
        final Set<Future<ORCCertificate>> futures = new HashSet<>();
        for (final CertificateHandle handle : handles) {
            final FutureTask<ORCCertificate> task = new FutureTask<ORCCertificate>(()->getCertificate(handle.getReferenceNumber()));
            final Thread backgroundExecutor = new Thread(task, "ORC certificate background download thread for "+handle.getReferenceNumber());
            backgroundExecutor.setDaemon(true);
            backgroundExecutor.start();
            futures.add(task);
        }
        final Set<ORCCertificate> result = new HashSet<>();
        for (final Future<ORCCertificate> future : futures) {
            final ORCCertificate certificate = future.get();
            if (certificate != null) {
                result.add(certificate);
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
            if (Util.isEmpty(certificateHandles)) {
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
        Iterable<CertificateHandle> certificateHandles = fuzzySearchVaryingBoatClassName(yachtName, sailNumber, boatClass);
        if (Util.isEmpty(certificateHandles)) {
            logger.fine(()->"Nothing found; trying without restricting sail number to "+sailNumber);
            // try without sail number constraint; if that doesn't find anything either, we can stop
            certificateHandles = fuzzySearchVaryingBoatClassName(yachtName, /* sailNumber */ null, boatClass);
            if (Util.size(certificateHandles) > 1 || !containsHandleForCurrentYear(certificateHandles)) {
                logger.fine("Found "+Util.size(certificateHandles)+" results without restricting sail number to "+
                        sailNumber+"; checking if we find a smaller result set with a specific sail number variation");
                // try all sail number variants and see if/where we get something; if not, return the full set, unconstrained by sail number
                for (final String sailNumberVariant : getSailNumberVariants(sailNumber)) {
                    logger.fine(()->"Trying sail number variation "+sailNumberVariant);
                    final Iterable<CertificateHandle> restrictedHandles = fuzzySearchVaryingBoatClassName(yachtName, sailNumberVariant, boatClass);
                    if (!Util.isEmpty(restrictedHandles)) {
                        certificateHandles = restrictedHandles;
                        break;
                    }
                }
            }
        }
        return certificateHandles;
    }

    private boolean containsHandleForCurrentYear(Iterable<CertificateHandle> certificateHandles) {
        final Calendar cal = new GregorianCalendar();
        final int currentYear = cal.get(Calendar.YEAR);
        for (final CertificateHandle handle : certificateHandles) {
            cal.setTime(handle.getIssueDate().asDate());
            if (cal.get(Calendar.YEAR) ==  currentYear) {
                return true;
            }
        }
        return false;
    }

    /**
     * Varies the boat class name by first using the true boat class name, then, if nothing is found, stepping through
     * the alternative names, and finally removing the boat class name constraint altogether.
     * 
     * @return the boat class name that ultimately led to the matches returned, and the matches in the form of a
     *         sequence of handles
     */
    private Iterable<CertificateHandle> fuzzySearchVaryingBoatClassName(final String yachtName, final String sailNumber, final BoatClass boatClass) throws Exception {
        String successfulBoatClassName = boatClass.getName();
        Iterable<CertificateHandle> certificateHandles = search(/* issuingCountry */ null, /* yearOfIssuance */ null, /* referenceNumber */ null, yachtName, sailNumber, successfulBoatClassName);
        if (Util.isEmpty(certificateHandles)) {
            logger.fine(()->"Nothing found; removing boat class name restriction "+boatClass.getName());
            // try without boat class restriction
            successfulBoatClassName = null;
            certificateHandles = search(/* issuingCountry */ null, /* yearOfIssuance */ null, /* referenceNumber */ null, yachtName, sailNumber, successfulBoatClassName);
        }
        for (final CertificateHandle handle : certificateHandles) {
            final Set<CertificateHandle> restrictedResults = new HashSet<>();
            final BoatClassMasterdata boatClassMasterData = BoatClassMasterdata.resolveBoatClass(boatClass.getName());
            if (boatClassMasterData != null && boatClassMasterData.getDisplayName().equals(boatClass.getDisplayName())) {
                restrictedResults.add(handle);
            }
            if (!restrictedResults.isEmpty()) {
                certificateHandles = restrictedResults;
                break;
            }
        }
        return certificateHandles;
    }

    private Iterable<String> getSailNumberVariants(String sailNumber) {
        final List<String> result = new LinkedList<>();
        result.add(sailNumber);
        if (sailNumber != null) {
            final Matcher matcher = Pattern.compile("[^A-Za-z]*([A-Za-z]*).*([0-9]*).*").matcher(sailNumber);
            final boolean findResult = matcher.find();
            if (findResult) {
                final String country = matcher.group(1);
                final String number = matcher.group(2);
                for (final String paddingToTry : new String[] { " ", "  ", "   ", "    ", " - ", "-", "- ", "-  ", "-   ", "-    " }) {
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
