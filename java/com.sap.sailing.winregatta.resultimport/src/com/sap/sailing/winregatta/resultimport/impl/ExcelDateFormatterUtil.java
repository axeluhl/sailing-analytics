package com.sap.sailing.winregatta.resultimport.impl;

import java.util.Date;


public class ExcelDateFormatterUtil {
	String valueAsString;
	Date valueAsDate;

	public ExcelDateFormatterUtil(String importedDate) {
		if (importedDate != null && !importedDate.isEmpty()) {
			// the format is in excel time format (number between 0 and 1)
			try {
				Double value = Double.parseDouble(importedDate) * 86400;
				Long valueInSeconds = value.longValue();

				this.valueAsDate = new Date(valueInSeconds * 1000);
				this.valueAsString = String
						.format("%02d:%02d:%02d", valueInSeconds / 3600,
								(valueInSeconds % 3600) / 60,
								(valueInSeconds % 60));
			} catch (NumberFormatException e) {
				this.valueAsString = "Invalid format: " + importedDate;
			}
		} else {
			valueAsString = importedDate;
			valueAsDate = null;
		}
	}
}