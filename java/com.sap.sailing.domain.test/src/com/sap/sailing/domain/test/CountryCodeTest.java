package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;

import com.sap.sse.common.CountryCode;
import com.sap.sse.common.CountryCodeFactory;
import com.sap.sse.common.Util;

public class CountryCodeTest {
    private static final int NUMBER_OF_COUNTRY_CODES = 259;

    @Ignore
    @Test
    public void convertHTML() throws IOException {
        InputStream is = Util.class.getResourceAsStream("countrycodes.csv");
        assertNotNull(is);
        BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String line;
        List<String[]> matrix = new LinkedList<String[]>();
        String[] row = null;
        int column = 8;
        Pattern p = Pattern.compile("<TD[^>]*> *(<[pP]>)?([^<]*)(</[pP]>)? *</TD>");
        while ((line=br.readLine()) != null) {
            if (line.contains("<TR>")) {
                assertEquals(8, column);
                column = 0;
                row = new String[8];
                matrix.add(row);
            } else {
                if (line.contains("<TD")) {
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        String entry = m.group(2);
                        if (entry != null) {
                            entry = URLDecoder.decode(entry, "UTF-8").trim();
                            if (entry.startsWith(".")) {
                                // IANA domain name will be stripped of leading "."
                                entry = entry.substring(1);
                            }
                        }
                        row[column++] = entry == null || entry.length() == 0 ? null : entry;
                    }
                }
            }
        }
        assertEquals(NUMBER_OF_COUNTRY_CODES, matrix.size());
        for (String[] r : matrix) {
            StringBuilder sb = new StringBuilder("        add(new CountryCodeImpl(");
            boolean first = true;
            for (String s : r) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                if (s == null) {
                    sb.append("null");
                } else {
                    sb.append('\"');
                    sb.append(s.replaceAll("\\\"", "\\\\\""));
                    sb.append('\"');
                }
            }
            sb.append("));");
            System.out.println(sb);
        }
    }
    
    @Test
    public void testAFewCountryCodes() {
        CountryCodeFactory ccf = CountryCodeFactory.INSTANCE;
        CountryCode cc;
        assertEquals(NUMBER_OF_COUNTRY_CODES, Util.size(ccf.getAll()));
        cc = ccf.getFromThreeLetterIOCName("GER");
        assertEquals("DE", cc.getTwoLetterISOCode().toUpperCase());
        assertEquals("DEU", cc.getThreeLetterISOCode().toUpperCase());
        cc = ccf.getFromThreeLetterISOName(Locale.GERMANY.getISO3Country().toUpperCase());
        assertEquals("DE", cc.getTwoLetterISOCode().toUpperCase());
        assertEquals("DEU", cc.getThreeLetterISOCode().toUpperCase());
        cc = ccf.getFromIANAInternet("sd");
        assertEquals("SUD", cc.getUNVehicle().toUpperCase());
        assertEquals("SDN", cc.getThreeLetterISOCode().toUpperCase());
    }
    
    @Test
    public void testGermanyCountryCode() {
        for (Locale l : Locale.getAvailableLocales()) {
            try {
                l.getISO3Country();
            } catch (MissingResourceException e) {
                // The "Serbia and Montenegro" locale has no ISO3 code due to the split-up
                assertEquals("sr_CS", l.toString());
            }
        }
        assertEquals("DEU", Locale.GERMANY.getISO3Country());
        assertEquals("DE", Locale.GERMANY.getCountry());
    }
}
