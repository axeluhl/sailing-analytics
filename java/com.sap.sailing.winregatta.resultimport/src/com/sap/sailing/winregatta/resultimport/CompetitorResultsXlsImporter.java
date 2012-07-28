package com.sap.sailing.winregatta.resultimport;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.ReaderConfig;
import net.sf.jxls.reader.XLSReadStatus;
import net.sf.jxls.reader.XLSReader;

public class CompetitorResultsXlsImporter {
	private static String jxlsImportTemplatePath = "/exceltemplates/DrachenIDM-2012/resultlistImport.xml";

	public CompetitorResultsXlsImporter() {
	}

	public List<CompetitorResultBean> readResultlist(String fileName,
			String sheetName) throws Exception {
		InputStream is = getClass().getResourceAsStream(fileName);
		List<CompetitorResultBean> resultlist = readResultlist(is, sheetName);

		return resultlist;
	}

	public List<CompetitorResultBean> readResultlist(InputStream xlsIs,
			String sheetName) throws Exception {
		InputStream readerXlsConfig = getClass().getResourceAsStream(
				jxlsImportTemplatePath);

		if (readerXlsConfig == null)
			throw new IllegalStateException(String.format(
					"Can not read classpath resource '%s'",
					jxlsImportTemplatePath));

		List<CompetitorResultBean> results = new ArrayList<CompetitorResultBean>();
		HashMap<String, Object> beans = new HashMap<String, Object>();
		beans.put("competitors", results);

		readXlsSheetGeneric(xlsIs, sheetName, new BufferedInputStream(
				readerXlsConfig), "Erg_Drachen", beans, CompetitorResultBean.class);

		return results;
	}

	private void readXlsSheetGeneric(InputStream xlsIs, String sheetName,
			InputStream readerXlsConfig, String readerName,
			Map<String, Object> beans, Class<?> sheetMappingRootClass)
			throws IOException, Exception {
		ReaderConfig.getInstance().setSkipErrors(true);
		XLSReader mainReader = ReaderBuilder.buildFromXML(readerXlsConfig);
		Object sheetReader = mainReader.getSheetReaders().get(readerName);
		mainReader.getSheetReaders().put(sheetName, sheetReader);
		InputStream inputXLS = new BufferedInputStream(xlsIs);
		XLSReadStatus readStatus = mainReader.read(inputXLS, beans);
		xlsIs.close();
		if (!readStatus.isStatusOK()) {
			throw new IllegalStateException(
					"Reading xls was not successful according to readStatus: "
							+ readStatus);
		}
	}
}
