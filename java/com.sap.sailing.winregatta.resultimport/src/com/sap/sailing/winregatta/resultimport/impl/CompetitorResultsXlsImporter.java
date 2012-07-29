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

import com.sap.sailing.winregatta.resultimport.CompetitorResult;
import com.sap.sailing.winregatta.resultimport.RegattaResults;

public class CompetitorResultsXlsImporter {
	private static String jxlsImportTemplatePath = "/exceltemplates/DrachenIDM-2012/resultlistImport.xml";

	public CompetitorResultsXlsImporter() {
	}

    public RegattaResults getRegattaResults(InputStream xlsIs, String sheetName)
    		throws IOException, InvalidFormatException, SAXException {
		InputStream readerXlsConfig = getClass().getResourceAsStream(jxlsImportTemplatePath);

		if (readerXlsConfig == null)
			throw new IllegalStateException(String.format("Can not read classpath resource '%s'",
					jxlsImportTemplatePath));

		final List<CompetitorResult> results = new ArrayList<CompetitorResult>();
		HashMap<String, Object> beans = new HashMap<String, Object>();
		beans.put("competitors", results);

		readXlsSheetGeneric(xlsIs, sheetName, new BufferedInputStream(
				readerXlsConfig), "Erg_Drachen", beans, CompetitorResultImpl.class);

        return new RegattaResults() {
            @Override
            public List<String> getMetadata() {
                return new ArrayList<String>();
            }
            @Override
            public List<CompetitorResult> getCompetitorResults() {
                return results;
            }
        };
	}

	private void readXlsSheetGeneric(InputStream xlsIs, String sheetName,
			InputStream readerXlsConfig, String readerName,
			Map<String, Object> beans, Class<?> sheetMappingRootClass)
			throws IOException, SAXException, InvalidFormatException {
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
