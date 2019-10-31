package com.sap.sailing.domain.orc.impl;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.sailing.domain.orc.ORCPublicCertificateDatabase;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.CountryCodeFactory;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ORCPublicCertificateDatabaseImpl implements ORCPublicCertificateDatabase {
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
    private static final String SEARCH_URL = "https://data.orc.org/public/WPub.dll";
    private static final String ROOT_ELEMENT = "ROOT";
    private static final String DATA_ELEMENT = "DATA";
    private static final String ROW_ELEMENT = "ROW";
    private static final DateFormat isoTimestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
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
    }
    
    @Override
    public Iterable<CertificateHandle> search(CountryCode country, Integer yearOfIssuance, String referenceNumber,
            String yachtName, String sailNumber, String boatClassName) throws Exception {
        final List<ORCPublicCertificateDatabase.CertificateHandle> result = new LinkedList<>(); 
        final HttpClient client = new SystemDefaultHttpClient();
        final List<NameValuePair> params = new ArrayList<>();
        params.add(ACTION_PARAM);
        params.add(XSLP_PARAM);
        params.add(new BasicNameValuePair(COUNTRY_PARAM_NAME, country==null?"*":country.getThreeLetterIOCCode()));
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
        postRequest.addHeader("Authorization", "Basic "+new String(Base64.getEncoder().encode((USER_NAME+":"+getDecodedCredentials()).getBytes())));
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
        return result;
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
                gph = Double.valueOf(child.getTextContent().trim());
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
                yearBuilt = Integer.valueOf(child.getTextContent());
                break;
            case "Provisional":
                isProvisional = Boolean.valueOf(child.getTextContent());
                break;
            }
        }
        return new CertificateHandleImpl(issuingCountry, gph, sssid, datInGID, referenceNumber, yachtName,
                sailNumber, boatClassName, designer, builder, yearBuilt, issueDate, isProvisional);
    }

    private static String getDecodedCredentials() {
        return new String(Base64.getDecoder().decode(CREDENTIALS));
    }
}
