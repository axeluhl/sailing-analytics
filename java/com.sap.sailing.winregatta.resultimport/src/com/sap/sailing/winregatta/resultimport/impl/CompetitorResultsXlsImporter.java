package com.sap.sailing.winregatta.resultimport.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.xml.sax.SAXException;

import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.ReaderConfig;
import net.sf.jxls.reader.XLSReadStatus;
import net.sf.jxls.reader.XLSReader;

import com.sap.sailing.resultimport.CompetitorRow;
import com.sap.sailing.resultimport.RegattaResults;

public class CompetitorResultsXlsImporter {
    public static final String IMPORT_TEMPLATE_WITH_RANKS_DRACHEN = "ImportTemplateWithRanks_Drachen.xml";
    public static final String IMPORT_TEMPLATE_WITHOUT_RANKS_DRACHEN = "ImportTemplateWithoutRanks_Drachen.xml";
    public static final String IMPORT_TEMPLATE_505 = "ImportTemplate_505er.xml";

    private static final String RESOURCES_PATH = "/exceltemplates";

    public CompetitorResultsXlsImporter() {
    }

    public RegattaResults getRegattaResults(InputStream excelFileInputStream, String importTemplateName,
            String excelSheetName) throws IOException, InvalidFormatException, SAXException {
        InputStream templateFileInputStream = getClass().getResourceAsStream(RESOURCES_PATH + "/" + importTemplateName);

        final List<CompetitorRow> results = new ArrayList<CompetitorRow>();
        final RegattaInfo regattaInfo = new RegattaInfo();
        HashMap<String, Object> beans = new HashMap<String, Object>();
        beans.put("competitors", results);
        beans.put("regattaInfo", regattaInfo);

        readXlsSheetGeneric(excelFileInputStream, excelSheetName, new BufferedInputStream(templateFileInputStream),
                excelSheetName, beans, CompetitorRowImpl.class);

        return new RegattaResults() {
            @Override
            public Map<String, String> getMetadata() {
                return regattaInfo.toMap();
            }

            @Override
            public List<CompetitorRow> getCompetitorResults() {
                return results;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private void readXlsSheetGeneric(InputStream xlsIs, String sheetName, InputStream readerXlsConfig,
            String readerName, Map<String, Object> beans, Class<?> sheetMappingRootClass) throws IOException,
            SAXException, InvalidFormatException {
        ReaderConfig.getInstance().setSkipErrors(true);
        XLSReader mainReader = ReaderBuilder.buildFromXML(readerXlsConfig);
        Object sheetReader = mainReader.getSheetReaders().get(readerName);
        mainReader.getSheetReaders().put(sheetName, sheetReader);
        InputStream inputXLS = new BufferedInputStream(xlsIs);
        XLSReadStatus readStatus = mainReader.read(inputXLS, beans);
        xlsIs.close();
        if (!readStatus.isStatusOK()) {
            throw new IllegalStateException("Reading xls was not successful according to readStatus: " + readStatus);
        }
    }
}
