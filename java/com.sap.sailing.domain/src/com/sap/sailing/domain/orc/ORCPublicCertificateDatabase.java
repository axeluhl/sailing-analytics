package com.sap.sailing.domain.orc;

import java.util.UUID;

import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.TimePoint;

/**
 * An interface to {@code data.orc.org}, mostly the search functionality at
 * {@code https://data.orc.org/public/WPub.dll?action=SrchCert&xslp=scert.php}. It can search for certificates by a
 * number of criteria, among them the issuing country, sail number, ORC reference number, or boat name.
 * &lt;p&gt;
 * 
 * As the web site emits XML documents as search results, those can be parsed and turned into handles to certificates.
 * An example output (excerpt):
 * 
 * <pre>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;?xml-stylesheet type="text/xsl" href="public.php?content=ListCert.php" ?&gt;
&lt;ROOT&gt;
  &lt;DATA&gt;
    &lt;ROW RowNum="1"&gt;
      &lt;CountryId&gt;GRE&lt;/CountryId&gt;
      &lt;SSSID&gt;61576&lt;/SSSID&gt;
      &lt;DatInGID&gt;{8C8F3E75-4926-4A30-B0C8-08D36774B1F4}&lt;/DatInGID&gt;
      &lt;dxtID&gt;4793&lt;/dxtID&gt;
      &lt;RefNo&gt;GRE00000701&lt;/RefNo&gt;
      &lt;dxtName&gt;GR1329.dxt&lt;/dxtName&gt;
      &lt;YachtName&gt;*AXION ESTI&lt;/YachtName&gt;
      &lt;SailNo&gt;GRE-1329&lt;/SailNo&gt;
      &lt;Class&gt;SUN ODYSSEY 32&lt;/Class&gt;
      &lt;Designer&gt;&lt;/Designer&gt;
      &lt;Builder&gt;JEANNEAU&lt;/Builder&gt;
      &lt;dxtDate&gt;2009-05-12T23:09:58.000&lt;/dxtDate&gt;
      &lt;CertType&gt;3&lt;/CertType&gt;
      &lt;VPPVer&gt;&lt;/VPPVer&gt;
      &lt;VPPYear&gt;2009&lt;/VPPYear&gt;
      &lt;IsOd&gt;False&lt;/IsOd&gt;
      &lt;GPH&gt;786.1&lt;/GPH&gt;
      &lt;Age&gt;1993&lt;/Age&gt;
      &lt;Override&gt;&lt;/Override&gt;
      &lt;Provisional&gt;False&lt;/Provisional&gt;
      &lt;Selected&gt;False&lt;/Selected&gt;
      &lt;CanSelect&gt;False&lt;/CanSelect&gt;
    &lt;/ROW&gt;
    &lt;ROW RowNum="2"&gt;
      &lt;CountryId&gt;GRE&lt;/CountryId&gt;
      &lt;SSSID&gt;15709&lt;/SSSID&gt;
      &lt;DatInGID&gt;{C790AAFA-9861-435B-BEBE-0606BAFE2721}&lt;/DatInGID&gt;
      &lt;dxtID&gt;&lt;/dxtID&gt;
      &lt;RefNo&gt;&lt;/RefNo&gt;
      &lt;dxtName&gt;GR1329.dat&lt;/dxtName&gt;
      &lt;YachtName&gt;*AXION ESTI&lt;/YachtName&gt;
      &lt;SailNo&gt;GRE-1329&lt;/SailNo&gt;
      &lt;Class&gt;SUN ODYSSEY 32&lt;/Class&gt;
      &lt;Designer&gt;&lt;/Designer&gt;
      &lt;Builder&gt;JEANNEAU&lt;/Builder&gt;
      &lt;dxtDate&gt;2008-09-18T15:29:36.000&lt;/dxtDate&gt;
      &lt;CertType&gt;0&lt;/CertType&gt;
      &lt;VPPVer&gt;&lt;/VPPVer&gt;
      &lt;VPPYear&gt;2008&lt;/VPPYear&gt;
      &lt;IsOd&gt;False&lt;/IsOd&gt;
      &lt;GPH&gt;&lt;/GPH&gt;
      &lt;Age&gt;1998&lt;/Age&gt;
      &lt;Override&gt;&lt;/Override&gt;
      &lt;Provisional&gt;False&lt;/Provisional&gt;
      &lt;Selected&gt;False&lt;/Selected&gt;
      &lt;CanSelect&gt;False&lt;/CanSelect&gt;
    &lt;/ROW&gt;
  &lt;/ROOT&gt;
&lt;/DATA&gt;
 * </pre>
 * 
 * This also shows that some query results do not provide a valid link to a downloadable copy of the certificate. Those are
 * automatically removed from the results here.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ORCPublicCertificateDatabase {
    public interface CertificateHandle {
        CountryCode getIssuingCountry();
        Double getGPH();
        String getSSSID();
        UUID getDatInGID();
        String getReferenceNumber();
        String getYachtName();
        String getSailNumber();
        String getBoatClassName();
        String getDesigner();
        String getBuilder();
        Integer getYearBuilt();
        TimePoint getIssueDate();
        Boolean isProvisional();
    }
    
    /**
     * Searches for certificates based on various criteria. Pass {@code null} for a criterion to not restrict search
     * results based on that criterion.
     */
    Iterable<CertificateHandle> search(CountryCode country, Integer yearOfIssuance, String referenceNumber,
            String yachtName, String sailNumber, String boatClassName) throws Exception;
    
    default ORCCertificate getCertificate(CertificateHandle handle) throws Exception {
        return getCertificate(handle.getReferenceNumber());
    }
    
    default ORCCertificate getCertificate(String referenceNumber) throws Exception {
        return getCertificate(search(null, null, referenceNumber, null, null, null).iterator().next());
    }
}
